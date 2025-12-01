Thanks for submitting your Pull Request!

Please refer to our [guidelines for making contributions](https://github.com/jbosstm/narayana/blob/main/CONTRIBUTING.md) when creating your pull request. In particular, it helps the reviewer if you ensure that the:
- [ ] Pull Request title is properly formatted: JBTM-XYZ Subject
- [ ] Pull Request contains a link to the JIRA issue(s) and that they contain sufficient information for the reviewer to be able to gauge whether the proposed changes correctly address the issue

The build axes can be controlled either by prefixing them with a `!` or by adding only the ones needed:

CORE AS_TESTS RTS JACOCO XTS QA_JTA QA_JTS_OPENJDKORB PERFORMANCE DB_TESTS mysql db2 postgres oracle

> [!IMPORTANT]  
> If the DB_TESTS axis is enabled but no database is selected, all databases will be tested by default. Multiple databases may be selected.

Pull requests build and run with one of the following JDKs: JDK17, JDK21, and JDK25. If the JDK version is omitted, the minimum supported JDK will be used.
