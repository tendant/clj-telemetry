(ns telemetry.middleware
  (:require [telemetry.tracing :as tracing]))

(defn wrap-telemetry-tracing
  ([handler]
   (wrap-telemetry-tracing handler {:default-event? true}))
  ([handler options]
   (fn [req]
     (let [span (tracing/create-span)
           method (:method req)
           uri (:method req)
           msg (format "%s %s" method uri)]
       (try
         (if (:default-event? options)
           (tracing/add-event span msg))
         (handler (assoc req :span span))
         (finally
           (tracing/end-span span)))))))