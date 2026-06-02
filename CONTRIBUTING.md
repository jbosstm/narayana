# Contributing guide

Want to contribute? Great!

We try to make it easy, and all contributions, even the smaller ones, are more than welcome.
This includes bug reports, fixes, documentation, examples...

If you are looking for an issue to work on and haven't had much previous experience with Narayana then you could choose one that has the label [good-first-issue](https://github.com/jbosstm/narayana/issues?q=state%3Aopen%20label%3Agood-first-issue) or [hacktoberfest](https://github.com/jbosstm/narayana/issues?q=state%3Aopen%20label%3Ahacktoberfest).
For `hacktoberfest` we have created a zulip stream called [hacktoberfest](https://narayana.zulipchat.com/#narrow/stream/406889-hacktoberfest/topic/stream.20events/near/393204842) for discussions.
If you want to take on an issue, simply assign it to yourself. If you don't have the necessary permissions, leave a comment saying, "I'd like to work on this."

On the other hand, if you are set to tackle a big, complicated issue or an issue that will have a high impact on the code base, it is highly recommended to follow the [Request for enhancement (RFE) [workflow](https://github.com/jbosstm/narayana-proposals/blob/main/README.md). Choosing to employ this workflow before starting the development of the selected RFE will help the Narayana core team and yourself to clarify the requirements, the constraints, and the overall design to tackle the issue in advance. In other words, employing the RFE workflow will make sure that the Narayana core team and yourself are on the same page before starting coding.

But first, read this page.

We use a four step process for contributions:

1. Fork the project repository to your own GitHub account.
1. Commit your changes to your fork.
1. Create a GitHub Pull Request for your change, following the instructions in the pull request template.
1. Perform a Code Review with the project maintainers on the pull request.

## Certificate of Origin and License

All files in this repository are subject to the Apache 2.0 license.

By contributing to this project you agree to the Developer Certificate of Origin (DCO).
This document was created by the Linux Kernel community and is a simple statement that you, as a contributor, have the legal right to make the contribution.

There are copies of the [license](LICENSE) and [DCO](dco.txt) in the root directory of this repository.

New files MUST include the project SPDX License Identifier (Apache-2.0), for example Java files would include:

```
/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
```

Please use comment characters appropriate to the file you are creating. We also ask contributors *not* to include `@author` tags.

Although copyright notices on contributions are not strictly necessary we do ask contributors to include the above text; code authors still receive attribution for their contribution via the information contained in the git commit metadata.

## Reporting an issue

If you believe you found a bug (and all software has bugs) we'll need to know how to reproduce it, what you are seeing and what you would expect to see.
To report the issue use the [GitHub issue tracker](https://github.com/jbosstm/narayana/issues). Don't forget to indicate which version of Narayana, Java and Maven you are using.

## Making open source more inclusive

In accordance with the [Commonhaus Foundation Code of Conduct](https://www.commonhaus.org/policies/code-of-conduct/), we are committed to using welcoming and inclusive language in all our interactions, including, but not limited to, code, documentation, and web properties. Although the Narayana project does not plan to retroactively update historical materials, we expect all contributors to uphold these standards in all Narayana-related communications and across all our communication channels.

### Building main

See the [README](README.md) file in the root directory of the repository.

## Before you contribute

### Guidelines

As a proud [Commonhaus Foundation](https://www.commonhaus.org/about/) project, we operate under its community-first principles and strictly adhere to its [Intellectual Property Policy](https://www.commonhaus.org/policies/ip-policy/) for all open source contributions.

We expect all contributors and users to follow our [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) when communicating through project channels. These include, but are not limited to: chat, issues, code.

#### Coding Guidelines

We use the maven `checkstyle` and `sortpom` plugins which are enabled by default and skipped in certain cases for older modules.

For older modules where checkstyle is disabled, when editing a file you should either:

1. follow the style currently used in the file you have edited or,
1. reformat it according to our checkstyle ruleset (by temporarily enabling checkstyle on the affected module) but separate functional changes from formatting changes into different commits in the same PR (this rule facilitates traceability). In other words, a file (in a module with checkstyle disabled) may only be re-formatted if it also contains a functional change and is made in a separate commit.

> [!NOTE]
> To scan a single file using Narayana's checkstyle rules in a maven module where checkstyle is not enabled, run the following command from the root directory of the maven module containing the file:
> ```
> mvn checkstyle:check -Dcheckstyle.includes="path/to/the/file/from/package/root/filename.java" -Dcheckstyle.skip=false
> ```
> For example, to scan the file `ArjunaJTA/jta/classes/com/arjuna/ats/internal/jta/resources/arjunacore/CommitMarkableResourceRecord.java` with Narayana's checkstyle rules, the following command should be executed from `ArjunaJTA/jta`:
> ```
> mvn checkstyle:check -Dcheckstyle.includes=com/arjuna/ats/internal/jta/resources/arjunacore/CommitMarkableResourceRecord.java -Dcheckstyle.skip=false
> ```

In general, most IDEs allow you to configure a rule file from your IDE settings (for Intelij that would be Settings -> Other Settings -> Checkstyle). The rule file is located in the [narayana-checkstyle repo](https://github.com/jbosstm/narayana-checkstyle-config/tree/main/src/main/resources/narayana-checkstyle).

We do not use `@author` tags in the Javadoc in new code: they are hard to maintain, especially in a very active project, and we use the Git history to track authorship, however GitHub provides [this nice page with your contributions](https://github.com/jbosstm/narayana/graphs/contributors).

Commits should be atomic and semantic. Commits should be squashed before submitting the PR unless the commits are necessary to track different parts of a fix or to separate out formatting only changes from functional changes. Fixup commits can be used temporarily during the review process, to help the reviewer navigate your changeset, but these should be squashed before merging the PR (in order to provide the software with a more meaningful commit history).

To contribute, use GitHub Pull Requests (PRs), from your **own** fork.

When you create a PR, the description field of the PR will include brief instructions on what you need to include.
But the following guidelines provide a more detailed set of requirements that we have found useful:

1. The Pull Request title is properly formatted: `[#XXXX] Subject`
2. The Pull Request *should* contain a link to the GitHub issue(s) at the start of the PR description (only minor changes to script/text files are exempt from this rule). If the engineer wishes to address multiple issues and they are closely related then they can be addressed in a single PR. The GitHub issue(s) must contain sufficient information to enable the reader to understand what the issue is, so at a minimum the description field of the issue must be present and legible/clear.
3. If the Pull Request depends on other pull requests, please put the full URL of the pull request(s) that you depend on. Ideally with a prefix "Depends on ".
4. Engineers are not allowed to submit PRs which only contain formatting changes. The guidance on formatting code are covered in the [Coding Guidelines](#coding-guidelines) section below.
5. New PRs are tested against multiple testing axes. If you know that a change only affects particular axes then you can disable the ones that aren't required, ask a team member for clarification if you're not sure (or look at how the `PROFILE` variable is used in the CI script `.github/scripts/narayana.sh`. When you first create a PR, the PR description field contains basic instructions about how to disable a CI test axis.
6. The engineer raising the PR should have tested their changes prior to submitting the request to merge changes. However, there are some circumstances where this may not be possible in which case you must add the label "Hold" and update the PR description indicating why it isn't ready for review just yet. The policy to add the label "Hold" is a signal to reviewers that the changes are not yet ready to be reviewed.

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

#### What can I expect in a code review?

The review procedure is that the reviewer:
* Adds comments to the code if they have any remarks about it - remarks should not be a reason for not providing an approval.
* Requests changes on the pull request if the pull request contravenes a project expectation - in this case the reviewer should look to cite criteria

These criteria may be:
* Not specifically addressing the issue being fixed
* Clean up changes not being in a separate git commit
* Commit message not prefixed with a GitHub issue number
* Too many separate commits in the same pull request related to the same GitHub issue and the same part of the change

The person that raised the PR then responds by either:
* Agreeing - in which case they add the change to their local repo and push to their own GitHub fork of it - this will update the PR
* Disagreeing - Adding a comment on why they think the review comment is invalid.

If the person raising the PR disagrees with the review comment then he/she should respond to the comment saying why they disagree.

Once all disagreements have been resolved and any changes after review have been pushed and retested then the PR may be merged.

### Continuous Integration

To ensure Narayana remains stable for everyone, all changes go through continuous integration (CI) via GitHub Actions. Tests are fully automated thus a new CI run will trigger automatically whenever you push new commits or modify the PR description. We use concurrency control, meaning only one run per test axis will execute per PR at any given time. Note that when a CI test axis passes there is *no need* to disable further testing of the axis (the danger of doing this is that if further commits are added to the PR then the axis will not be retested).

### Tests and documentation are not optional

Don't forget to include tests in your pull requests.
Also don't forget the documentation (reference documentation for features, javadoc...).

## Update the issue with the correct release information:

If the change would result in behaviour in Narayana that is incompatible with the current release stream for Narayana, then the relevant GitHub issue must be included in the GitHub [milestone](https://github.com/jbosstm/narayana/milestones) `<major_version+1>.next`. For example if the root `pom.xml` is currently `<version>7.0.3.Final-SNAPSHOT</version>` then the GitHub issue should be included in the milestone `8.next`. When resolving the issue we encourage the assignee to consider adding a release note describing the impact of the change.

When the GitHub Pull Request has passed all relevant CI checks and has been Approved by a reviewer, the code can be merged. If you don't have permission to do this then ping one of the team who will then merge it.

## Release

The Narayana project applies the [Semantic Versioning Specification](https://semver.org/) (SemVer) 2.0.0 to mark its releases.
