(ns telemetry.middleware
  (:require [telemetry.tracing :as tracing]
            [clojure.string :as string]))

(defn wrap-telemetry-tracing
  ([handler]
   (wrap-telemetry-tracing handler {:default-event? true}))
  ([handler options]
   (fn [req]
     (let [span (tracing/create-span)
           method (:request-method req)
           uri (:uri req)
           msg (format "%s %s" (string/upper-case (name method)) uri)]
       (try
         (if (:default-event? options)
           (tracing/add-event span msg))
         (handler (assoc req :span span))
         (finally
           (tracing/end-span span)))))))