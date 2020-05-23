(ns telemetry.middleware
  (:require [telemetry.tracing :as tracing]))

(defn wrap-telemetry-tracing
  [handler]
  (fn [request]
    (let [span (tracing/create-span)]
      (handler (assoc request :span span)))))