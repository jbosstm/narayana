Thanks for submitting your Pull Request!

Please make sure your PR meets the following requirements:
- [ ] Pull Request title is properly formatted: JBTM-XYZ Subject
- [ ] Pull Request contains link to the JIRA issue(s)

Our style rules for submitting code are as follows:

* If you add a new file it MUST adhere to our checkstyle ruleset.
  1. If you change a file (in a non trivial way) you are allowed (MAY) to reformat the code to conform to our checkstyle ruleset (`org.jboss.narayana:checkstyle`).
  2. If you choose not to reformat it then you SHOULD, where possible, try to follow the same style that's already in use for that file.


The build axis can be controlled by prefixing a ! on the following as appropriate.

MAIN CORE TOMCAT AS_TESTS RTS JACOCO XTS QA_JTA QA_JTS_JACORB QA_JTS_JDKORB QA_JTS_OPENJDKORB BLACKTIE PERF LRA !NO_WIN DB_TESTS mysql db2 postgres oracle
