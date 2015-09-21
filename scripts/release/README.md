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
