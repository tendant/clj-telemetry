(ns telemetry.tracing
  (:require [clojure.walk :as walk])
  (:import [io.opentelemetry.sdk.trace SdkTracerProvider]
           [io.opentelemetry.api OpenTelemetry]
           [io.opentelemetry.sdk OpenTelemetrySdk]
           [io.opentelemetry.context.propagation ContextPropagators]
           [io.opentelemetry.api.trace.propagation W3CTraceContextPropagator]
           [io.opentelemetry.sdk.trace.export BatchSpanProcessor SimpleSpanProcessor]
           [io.grpc ManagedChannelBuilder]
           [io.opentelemetry.exporter.jaeger JaegerGrpcSpanExporter]
           [io.opentelemetry.context Context]
           [io.opentelemetry.api.common Attributes]))

(defn build-exporter-jaeger
  [service-name ip port]
  (let [port (Integer. port)
        channel (-> (ManagedChannelBuilder/forAddress ip port)
                    (.usePlaintext)
                    (.build))
        exporter (-> (JaegerGrpcSpanExporter/builder)
                     (.setServiceName service-name)
                     (.setChannel channel)
                     (.setDeadlineMs 30000)
                     (.build))]
    exporter))

(defn build-batch-span-processor
  [exporter]
  (-> (BatchSpanProcessor/builder exporter)
      .build))

(defn build-simple-span-processor
  [exporter]
  (-> (SimpleSpanProcessor/builder exporter)
      .build))

(defn init-open-telemetry
  [span-processor]
  (let [open-telemetry (.build (OpenTelemetrySdk/builder))]
    (-> open-telemetry
        .getTracerManagement
        (.addSpanProcessor span-processor))
    open-telemetry))

(defn shutdown-open-telemetry
  [open-telemetry]
  (-> (.getTraceManagement open-telemetry)
      (.shutdown)))

(defn get-tracer
  [open-telemetry library-name]
  (if open-telemetry
    (.getTracer open-telemetry (or library-name "telemetry.tracing"))))

(defn create-span
  ([tracer id]
   (create-span tracer id nil))
  ([tracer id parent]
   (if tracer
     (let [span (if parent
                  (.setParent (.spanBuilder tracer id)
                              (.with (Context/current) parent))
                  (.spanBuilder tracer id))]
       (.startSpan span)))))

(defn end-span
  [span]
  (if span
    (.end span)))

(defn span-attributes
  [span attributes]
  (if span
    (doseq [[k v] attributes]
      (.setAttribute span (str k) (str v)))))

(defn attrs [m]
  (let [builder (Attributes/builder)]
    (doseq [[k v] m]
      (.put builder (str k) (str v)))
    (.build builder)))

(defn add-event
  ([span message]
   (add-event span message nil))
  ([span message m]
   (if span
     (if (and m
              (not (empty? m)))
       (.addEvent span message (attrs m))
       (.addEvent span message)))))

(comment
  (def exporter (build-exporter-jaeger "test-service-name" "localhost" "14250"))
  (def span-processor (build-simple-span-processor exporter))
  (def open-telemetry (init-open-telemetry exporter))
  (def tracer (get-tracer open-telemetry "test.tracing"))
  )

(defn test-span
  [tracer]
  (let [span1 (create-span tracer "span-1")]
    (add-event span1 "1. first event")
    (add-event span1 "1. second event")
    (let [span2 (create-span tracer "span-2")]
      (add-event span2 "span 2. first event")
      (add-event span2 "span 2. second event" {"attr1" "attr1 value"
                                               "attr2" "attr2 value2"})
      (.end span2))
    (.end span1)))

(defn test-parent-span
  [tracer]
  (let [root (create-span tracer "test-parent-span")]
    (add-event root "root event")
    (let [child (create-span tracer "test-parent-span child" root)]
      (add-event child "child span")
      (add-event root "root span with child")
      (.end child)
      (add-event child "child span after end child"))
    (add-event root "root span after child")
    (add-event root "root span after end root")
    (.end root)))

(defn test-parallel-spans
  [tracer]
  (let [span-1 (create-span tracer "test-parallel-1")
        span-2 (create-span tracer "test-parallel-2")]
    (add-event span-1 "begin")
    (add-event span-2 "event: a")
    (Thread/sleep 1000)
    (add-event span-1 "event: b")
    (Thread/sleep 1000)
    (add-event span-2 "event: c")
    (Thread/sleep 1000)
    (add-event span-1 "event: d")
    (Thread/sleep 1000)
    (add-event span-2 "event: e")
    (Thread/sleep 1000)
    (add-event span-1 "event: f")
    (add-event span-2 "span: after end span")
    (.end span-1)
    (.end span-2)))