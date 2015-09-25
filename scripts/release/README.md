Pre-release Check Script
========================

This scripts checks three things: Jenkins failures, JIRA blockers, and old deprecated code. Deprecated code is considered old, if the last commit in the file was made before the last major (x.0.0) release.

Usage
-----

```
./pre_release.py
```

Script is configurable with environment variables:

    * SOURCE_PATH - path to the Narayana source code. Default is "../..".
    * JENKINS_HOST - Jenkins host address. Default is "albany.eng.hst.ams2.redhat.com"
    * JENKINS_JOBS - comma separated list of Jenkins jobs. Default is "narayana,narayana-catelyn,narayana-quickstarts,narayana-benchmarks"
    * JIRA_HOST - JIRA host address. Default is "issues.jboss.org".
    * PROJECT_KEY - JIRA project key. Default is "JBTM".
    * CURRENT_VERSION - current project version. Default is "5.next".
    * ASK_USER - whether the user should be asked what to do in case of failures. Default is "True".
    * FAIL_DEPRECATION - whether check should fail in case there id old deprecated code available. Default is "False".
    * FAIL_BLOCKER - whether check should fail in case there are open blocker issues. Default is "True".
    * FAIL_JENKINS - whether check should fail in case there are failures in Jenkins. Default is "True".
    

JIRA Update Script
==================

This script executes all necessary actions to make the release in JIRA. Marks specified version as released, closes resolved issues of the released version, and moves unresolved issues to the newly created temporary version (e.g. 5.next).

Usage
-----

```
./update_jira.py -k <project key> -n <release version>
    \ [-l <log level>] [-j <JIRA host>] [-t <temporary version>]
    \ [-u <username>] [-p <password>]
```
