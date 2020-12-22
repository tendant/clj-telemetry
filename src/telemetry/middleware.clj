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
   (wrap-telemetry-tracing handler nil))
  ([handler options]
   (fn [req]
     (let [event-fn (:event-fn options event-fn-skip-probe)]
       (if (and event-fn
                (event-fn req)
                (:tracer options))
         (let [span (tracing/create-span (:tracer options) (:span-name options "wrap-telemetry-tracing"))
               method (:request-method req)
               uri (:uri req)
               msg (format "HTTP %s %s Begin" (string/upper-case (name method)) uri)]
           (try
             (tracing/add-event span msg)
             (handler (assoc req :span span))
             (finally
               (tracing/end-span span))))
         (handler req))))))