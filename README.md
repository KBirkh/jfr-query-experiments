JFR Query Experiments
=====================

Experiments with `jfr` tool code and JFR queries.

This tool privides an extended syntax derived from and still using the [JDK 21](https://github.com/openjdk/jdk21u) code at its core.

The extensions include:
- Comparisons for equality, inequality, greater/less than
- Percentiles for comparable Values (e.g. P95(identifier))
- Correlation to Garbage Collection events using beforeGC(timestamp), nearGC(...), afterGC(...)
- Evaluation of multiple queries in one program
- Assignment of queries to an identifier to be used in other queries

Currently the web UI is not available due to these changes.

<!-- ```shell
java -jar target/query.jar web recording.jfr
-> starts a web server on port 8080
-> open http://localhost:8080
``` -->

Build
-----

```shell
git clone https://github.com/KBirkh/jfr-query-experiments
cd jfr-query-experiments
mvn clean package
```

Run
---
```shell
java -jar target/jfr-query-experiments.jar
-> shows the help
```

Purpose
-------
Play with JFR queries, maybe extend them, without having to build the whole JDK.

License
-------
GPL-2.0, Copyright 2017 - 2025 SAP SE or an SAP affiliate company and contributors.