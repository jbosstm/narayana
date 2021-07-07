# Contributing guide

**Want to contribute? Great!** 
We try to make it easy, and all contributions, even the smaller ones, are more than welcome.
This includes bug reports, fixes, documentation, examples... 
But first, read this page.

## Legal

All original contributions to Narayana are licensed under the
[GNU LESSER GENERAL PUBLIC LICENSE Version 2.1](https://github.com/jbosstm/narayana/blob/master/LICENSE).

All contributions are subject to the [Developer Certificate of Origin (DCO)](https://developercertificate.org/).
The DCO text is also included verbatim in the [dco.txt](dco.txt) file in the root directory of the repository.

New files may contain a license header in the following format:

```
/*
 *
 * Copyright The Narayana Authors
 * 
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */
```

## Reporting an issue

This project uses the [JBTM issue tracker](https://issues.redhat.com/projects/JBTM)
to manage the issues. Open an issue directly in this issue tracker.

If you believe you found a bug, and it's likely possible, please indicate a way to reproduce it, what you are seeing and what you would expect to see.
Don't forget to indicate your Narayana, Java and Maven version. 

## Making open source more inclusive

Red Hat is committed to eradicating problematic language in all of our interactions, including code, documentation, web properties, etc (https://www.redhat.com/en/blog/making-open-source-more-inclusive-eradicating-problematic-language). Although the Narayana project has no plans to retrospectively update its materials we do ask that all contributors be cognizant of the language they use in all communications related to Narayana.

### Building master

See the [README](README.md) file in the root directory of the repository.

## Before you contribute

To contribute, use GitHub Pull Requests (PRs), from your **own** fork.

When you create a PR, the description field of the PR will include brief instructions on what you need to include.
But the following guidelines provide a more detailed set of requirements that we have found useful:

1. The Pull Request title is properly formatted: `JBTM-XYZ Subject`
2. The Pull Request *should* contain a link to the JIRA issue(s) at the start of the PR description (only minor changes to script/text files are exempt from this rule). If the engineer wishes to address multiple issues and they are closely related then they can be addressed in a single PR. The JIRA must contain sufficient information to enable the reader to understand what the issue is, so at a minimum the description field of the JIRA must be present and legible/clear.
3. Engineers are not allowed to submit PRs which only contain formatting changes. The guidance on formatting code are covered in the [Coding Guidelines](#coding-guidelines) section below.
4. New PRs are tested against multiple Jenkins CI axes. If you know that a change only affects particular axes then you can disable the ones that aren't required. When you first create a PR, the description field will contain a basic instructions about how to disable a CI test axis.

Also, make sure you have set up your Git authorship correctly:

```
git config --global user.name "Your Full Name"
git config --global user.email your.email@example.com
```

If you use different computers to contribute, please make sure the name is the same on all your computers.

### Code reviews

All submissions, including submissions by project members, should be reviewed before being merged.
A possible exception is that if a change only effects build scripts or non-source files and is "small" then self review is permitted. But use this option with care to avoid breaking build scripts or the readabilty of text files.

Before asking for a review it's best to wait for [Continuous Integration tests](#continuous-integration) to finish successfully (unless early feedback is being sought).

Once a review has started both parties should attempt to respond to feedback in a timely manner.
Do not approve the PR until you have either seen a successful CI test of the PR, or you can reasonably explain why a failure is unrelated to the code changes made in the PR (and documented in a PR comment).

### Guidelines

We follow [Open source participation guidelines](https://www.redhat.com/en/resources/open-source-participation-guidelines-overview)

### Coding Guidelines

We use the maven `checkstyle` and `sortpom` plugins which are skipped by default for older modules
and enabled for newer ones.

For older modules where checkstyle is disabled, when editing a file you should either
a) follow the style currently used in the file you have edited or,
b) reformat it according to our checkstyle ruleset (by temporarily enabling checkstyle
on the affected module) but separate functional changes from formatting changes into different commits
in the same PR (this rule facilitates traceability).
In other words a file (in a module with checkstyle disabled) may only be re-formatted if it also
contains a functional change and is made in a separate commit.

Most IDEs allow you to configure a rule file from your IDE settings (for Intelij that would be Settings -> Other Settings -> Checkstyle). The rule file is located the [narayana-checkstyle repo](https://github.com/jbosstm/narayana-checkstyle-config/tree/main/src/main/resources/narayana-checkstyle)

We do not use `@author` tags in the Javadoc in new code: they are hard to maintain, especially in a very active project, and we use the Git history to track authorship, however GitHub provides [this nice page with your contributions](https://github.com/jbosstm/narayana/graphs/contributors).

Commits should be atomic and semantic. Commits should be squashed before submitting the PR unless the commits are necessary to track different parts of a fix or to separate out formatting only changes from functional changes. Fixup commits can be used temporarily during the review process, to help the reviewer navigate your changeset, but these should be squashed before merging the PR (in order to provide the software with a more meaningful commit history).

New files should include the project [copyright statement](#legal)

### Continuous Integration

To ensure Narayana is stable for everyone, all changes should go through Narayana continuous integration: when you raise a pull request one of the members of the team will schedule a CI run to test your PR.
Note that when a CI test axis passes there is *no need* to disable further testing of the axis (the danger of doing this is that if further commits are added to the PR then the axis will not be retested).

### Tests and documentation are not optional

Don't forget to include tests in your pull requests. 
Also don't forget the documentation (reference documentation for features, javadoc...).
