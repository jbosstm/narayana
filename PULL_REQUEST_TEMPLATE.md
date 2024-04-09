Thanks for submitting your Pull Request!

Please refer to our [guidelines for making contributions](https://github.com/jbosstm/narayana/blob/main/CONTRIBUTING.md) when creating your pull request. In particular, it helps the reviewer if you ensure that the:
- [ ] Pull Request title is properly formatted: JBTM-XYZ Subject
- [ ] Pull Request contains a link to the JIRA issue(s) and that they contain sufficient information for the reviewer to be able to gauge whether or not the proposed changes correctly address the issue

The build axis can be controlled by prefixing a ! on the following as appropriate:

CORE TOMCAT AS_TESTS RTS JACOCO XTS QA_JTA QA_JTS_OPENJDKORB PERFORMANCE LRA DB_TESTS mysql db2 postgres oracle

Pull requests build and run with JDK11 and JDK17. Add the prefix `!` to disable these defaults. Include the text `JDK21` to build and run with JDK21

If it is determined that nothing needs to be tested for the pull request then you need to concatenate the two sets of characters: `NO_` `TEST` as a single word and provide the result into the description.

Please be aware that none of this configuration affects which GitHub Actions will run.
