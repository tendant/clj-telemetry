(ns telemetry.middleware
  (:require [telemetry.tracing :as tracing]
            [clojure.string :as string]))

(defn event-fn-skip-probe
  [req]
  (let [user-agent (get-in req [:headers "user-agent"])]
    (if (and user-agent
             (string/includes? user-agent "probe"))
      false
      true)))

(defn wrap-telemetry-tracing
  ([handler]
   (wrap-telemetry-tracing handler {:event-fn event-fn-skip-probe}))
  ([handler options]
   (fn [req]
     (let [event-fn (:event-fn options)]
       (if (and event-fn
                (event-fn req))
         (let [span (tracing/create-span)
               method (:request-method req)
               uri (:uri req)
               msg (format "HTTP %s %s Begin" (string/upper-case (name method)) uri)]
           (try
             (tracing/add-event span msg)
             (handler (assoc req :span span))
             (finally
               (tracing/end-span span))))
         (handler req))))))