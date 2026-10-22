Thanks for submitting your Pull Request!

Please refer to our [guidelines for making contributions](https://github.com/jbosstm/narayana/blob/main/CONTRIBUTING.md) when creating your pull request. In particular, it helps the reviewer if you ensure that the:
- [ ] Pull Request title is properly formatted: [#XXXX] Subject
- [ ] Pull Request contains a link to the [GitHub issue(s)](https://github.com/jbosstm/narayana/issues) and that they contain sufficient information for the reviewer to be able to gauge whether the proposed changes correctly address the issue

## CI builds to run

Edit the line below — delete or `!`-prefix anything you don't want to run. Everything from the marker downwards is reference only and is not read by CI.

CORE AS_TESTS RTS JACOCO XTS QA_JTA QA_JTS_OPENJDKORB PERFORMANCE QUICKSTART EXPERIMENTAL DB_TESTS mysql db2 postgres oracle

<!-- ci-ignore-below -->

---

### Reference (not parsed by CI)

Build axes: CORE, AS_TESTS, RTS, JACOCO, XTS, QA_JTA, QA_JTS_OPENJDKORB, PERFORMANCE, QUICKSTART, EXPERIMENTAL, DB_TESTS. Prefix any axis with `!` to exclude it, or replace the whole selection line with NO_TESTS to skip all build axes.

> [!IMPORTANT]
> If the DB_TESTS axis is enabled but no database is selected, all databases will be tested by default. Multiple databases may be selected: mysql, db2, postgres, oracle.

Pull requests build and run with one of the following JDKs: JDK17, JDK21, and JDK25. If the JDK version is omitted, the minimum supported JDK will be used.

You may trigger a performance repo Pull Request by including the PERFORMANCE axis and PERF_PR_NUMBER (the performance PR number) in the selection line. You can define which benchmarks to run by setting BENCHMARKS to a comma or blank separated list of benchmarks. The performance benchmarks use JMH which can be tuned for the run using the variables JMH_MEASUREMENT_TIME, JMH_FORKS, JMH_WARMUP_ITERATIONS and JMH_MEASUREMENT_ITERATIONS - the defaults are 120, 5, 10 and 10, respectively.

Similarly you may trigger a quickstart repo Pull Request by specifying the QUICKSTART axis and QUICKSTART_PR_NUMBER in the selection line. For example: QUICKSTART QUICKSTART_PR_NUMBER=775

You may also test against specific upstream branches by setting AS_BRANCH (the branch/commit of jboss-as to check out) and/or SPI_BRANCH (the branch/commit of jboss-transaction-spi to check out) in the selection line. For example: AS_BRANCH=main SPI_BRANCH=main
