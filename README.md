# clj-telemetry

A Clojure library designed to wrap [OpenTelemetry Java API](https://github.com/open-telemetry/opentelemetry-java).

## Usage

```clj
[clj-telemetry "0.1.4"]
```

1. Setup exporter

```
(setup-span-processor (create-spans-processor-jaeger "example" "localhost" 14250))
```

2. Create span

```
(import 'telemetry.tracing)

(def span (create-span))

(add-event span1 "1. first event")
(add-event span1 "1. second event")

(end-span span)
```



## References

https://github.com/open-telemetry/opentelemetry-java/blob/master/QUICKSTART.md

Tracing:

https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/api.md#obtaining-a-tracer

API Doc:
https://javadoc.io/doc/io.opentelemetry/opentelemetry-api/latest/index.html

Jaeger example:
https://github.com/open-telemetry/opentelemetry-java/tree/master/examples/jaeger

## License

Copyright © 2020 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
