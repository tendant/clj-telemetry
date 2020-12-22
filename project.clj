(defproject clj-telemetry "0.2.2"
  :description "A Clojure library designed to wrap OpenTelemetry Java API"
  :url "https://github.com/tendant/clj-telemetry"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [io.opentelemetry/opentelemetry-api "0.13.1"]
                 [io.opentelemetry/opentelemetry-sdk "0.13.1"]
                 [io.opentelemetry/opentelemetry-exporter-jaeger "0.13.1"]
                 [io.grpc/grpc-protobuf "1.34.1"]
                 [io.grpc/grpc-netty-shaded "1.34.1"]]
  :repl-options {:init-ns telemetry.tracing})
