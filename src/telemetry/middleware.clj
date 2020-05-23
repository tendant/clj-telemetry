(ns telemetry.middleware
  (:require [telemetry.tracing :as tracing]))

(defn wrap-telemetry-tracing
  [handler]
  (fn [request]
    (let [span (tracing/create-span)
          resp (handler (assoc request :span span))]
      (tracing/end-span span)
      resp)))