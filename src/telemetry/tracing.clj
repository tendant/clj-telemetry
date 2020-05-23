(ns telemetry.tracing
  (:require [clojure.walk :as walk])
  (:import [io.opentelemetry OpenTelemetry]
           [io.opentelemetry.sdk OpenTelemetrySdk]
           [io.grpc ManagedChannel ManagedChannelBuilder]
           [io.opentelemetry.exporters.jaeger JaegerGrpcSpanExporter]
           [io.opentelemetry.sdk.trace.export SimpleSpansProcessor]
           [io.opentelemetry.common AttributeValue]))

(defn- get-tracer
  []
  (-> (OpenTelemetry/getTracerProvider)
      (.get "telemetry.tracing" "semver:1.0.0")))

(defn- build-exporter-jaeger
  [service-name ip port]
  (let [port (Integer. port)
        channel (-> (ManagedChannelBuilder/forAddress ip port)
                    (.usePlaintext)
                    (.build))
        exporter (-> (JaegerGrpcSpanExporter/newBuilder)
                     (.setServiceName service-name)
                     (.setChannel channel)
                     (.setDeadlineMs 30000)
                     (.build))]
    exporter))

(defn- build-spans-processor
  [exporter]
  (-> (SimpleSpansProcessor/create exporter)))

(defn create-spans-processor-jaeger
  [service-name ip port]
  (let [exporter (build-exporter-jaeger service-name ip port)]
    (build-spans-processor exporter)))

(defn setup-span-processor
  [span-processor]
  (-> (OpenTelemetrySdk/getTracerProvider)
      (.addSpanProcessor span-processor)))

(defn shutdown-provider
  []
  (-> (OpenTelemetrySdk/getTracerProvider)
      (.shutdown)))

(defn walk-kv
  "Recursively transforms all map keys from keywords to strings and apply fv to value"
  [m fv]
  (let [f (fn [[k v]] (if (keyword? k) [(name k) (fv v)] [k (fv v)]))]
    ;; only apply to maps
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn ->attr
  [v]
  (cond
    (instance? java.lang.String v) (AttributeValue/stringAttributeValue v)))

(defn ->attrs
  [m]
  (if m
    (walk-kv m ->attr)))

(defn get-current-span
  []
  (let [tracer (get-tracer)]
    (.getCurrentSpan tracer)))

(defn end-current-span
  []
  (-> (get-current-span)
      (.end)))

(defn set-current-span
  [span]
  (-> (get-tracer)
      (.withSpan span)))

(defn create-span
  ([]
   (create-span (str (java.util.UUID/randomUUID))))
  ([^java.lang.String id]
   (let [span-id id]
     (-> (get-tracer)
         (.spanBuilder span-id)
         (.startSpan)))))

(defn end-span
  [span]
  (if span
    (.end span)))

(defn add-event
  ([span message]
   (add-event span message nil))
  ([span message m]
   (if span
     (if m
       (.addEvent span message (->attrs m))
       (.addEvent span message)))))

(comment
  ;; Make sure jaeger all in one server is started
  (setup-span-processor (create-spans-processor-jaeger "example" "localhost" 14250))
  )

(defn test-span
  []
  (let [span1 (create-span)]
    (add-event span1 "1. first event")
    (add-event span1 "1. second event")
    (let [span-id (str (java.util.UUID/randomUUID))
          span2 (create-span span-id)]
      ;; (add-event span2 "span 2. first event")
      (add-event span2 "span 2. second event" {"attr1" "attr1 value"
                                               "attr2" "attr2 value2"})
      (println "span2:" span2)
      (.end span2))
    (.end span1)))
