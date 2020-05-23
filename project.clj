(defproject clj-telemetry "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [io.opentelemetry/opentelemetry-api "0.4.1"]
                 [io.opentelemetry/opentelemetry-sdk "0.4.1"]
                 [io.opentelemetry/opentelemetry-exporters-jaeger "0.4.1"]
                 [io.grpc/grpc-protobuf "1.28.0"]
                 [io.grpc/grpc-netty-shaded "1.28.0"]]
  :repl-options {:init-ns telemetry.tracing})
