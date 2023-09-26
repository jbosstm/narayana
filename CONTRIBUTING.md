# Contributing guide

Want to contribute? Great!

We try to make it easy, and all contributions, even the smaller ones, are more than welcome.
This includes bug reports, fixes, documentation, examples...

If you are looking for an issue to work on and haven't had much previous experience with Narayana then you could choose one that has the label [good-first-issue or hacktoberfest](https://issues.redhat.com/browse/JBTM-2493?filter=12421681) or one whose "Estimated Difficulty" field is `Low`. For Hacktoberfest we have created a zulip stream called [hacktoberfest](https://narayana.zulipchat.com/#narrow/stream/406889-hacktoberfest/topic/stream.20events/near/393204842) for discussions. If you want to take an issue then ping a team member (or add a message to the zulip stream) and we will update the assignee field.

On the other hand, if you are set to tackle a big, complicated issue or an issue that will have a high impact on the code base, it is highly recommended to follow the [Request for enhancement (RFE) [workflow](https://github.com/jbosstm/narayana-proposals/blob/main/README.md). Choosing to employ this workflow before starting the development of the selected RFE will help the Narayana core team and yourself to clarify the requirements, the constraints, and the overall design to tackle the issue in advance. In other words, employing the RFE workflow will make sure that the Narayana core team and yourself are on the same page before starting coding.

But first, read this page.

We use a four step process for contributions:

1. Fork the project repository to your own GitHub account.
1. Commit your changes to your fork.
1. Create a GitHub Pull Request for your change, following the instructions in the pull request template.
1. Perform a Code Review with the project maintainers on the pull request.

## Certificate of Origin and License

All files in this repository are subject to the Apache 2.0 license.

By contributing to this project you agree to the [Developer Certificate of Origin (DCO)](https://developercertificate.org/). The DCO was created by the Linux Kernel community and is a simple statement that you, as a contributor, have the legal right to make the contribution (we adopt the "CoreOS method" approach to using the DCO in which there is no per-commit signoff ceremony).

There are copies of the [license](LICENSE) and [DCO](dco.txt) in the root directory of this repository.

New files MUST include the project SPDX License Identifier (Apache-2.0), for example Java files would include:

```
/*
 + Copyright The Narayana Authors
 + SPDX short identifier: Apache-2.0
 */
```

Please use comment characters appropriate to the file you are creating. We also ask contributors *not* to include `@author` tags.

Although copyright notices on contributions are not strictly necessary we do ask contributors to include the above text; code authors still receive attribution for their contribution via the information contained in the git commit.

## Reporting an issue

If you believe you found a bug (and all software has bugs) we'll need to know how to reproduce it, what you are seeing and what you would expect to see.
To report the issue use the [JBTM issue tracker](https://issues.redhat.com/projects/JBTM). Fill in as many fields as are relevant including the following:

`Affects Version/s`: The version where you found the issue.
`Fix Version/s`: Leave this field blank, the engineer who fixes the issue will set the correct version when the PR is merged.
`Component/s`: The components relevant to the problem. Leave the field blank if you're not sure which components are affected.

Don't forget to indicate which version of Narayana, Java and Maven you are using.

## Making open source more inclusive

Red Hat is committed to eradicating problematic language in all of our interactions, including code, documentation, web properties, etc (https://www.redhat.com/en/blog/making-open-source-more-inclusive-eradicating-problematic-language). Although the Narayana project has no plans to retrospectively update its materials we do ask that all contributors be cognizant of the language they use in all communications related to Narayana.

### Building main

See the [README](README.md) file in the root directory of the repository.

## Before you contribute

To contribute, use GitHub Pull Requests (PRs), from your **own** fork.

When you create a PR, the description field of the PR will include brief instructions on what you need to include.
But the following guidelines provide a more detailed set of requirements that we have found useful:

1. The Pull Request title is properly formatted: `JBTM-XYZ Subject`
2. The Pull Request *should* contain a link to the JIRA issue(s) at the start of the PR description (only minor changes to script/text files are exempt from this rule). If the engineer wishes to address multiple issues and they are closely related then they can be addressed in a single PR. The JIRA must contain sufficient information to enable the reader to understand what the issue is, so at a minimum the description field of the JIRA must be present and legible/clear.
3. Engineers are not allowed to submit PRs which only contain formatting changes. The guidance on formatting code are covered in the [Coding Guidelines](#coding-guidelines) section below.
4. New PRs are tested against multiple Jenkins CI axes. If you know that a change only affects particular axes then you can disable the ones that aren't required, ask a team member for clarification if you're not sure (or look at how the `PROFILE` variable is used in the CI script `scripts/hudson/narayana.sh`. When you first create a PR, the PR description field contains basic instructions about how to disable a CI test axis.
5. The engineer raising the PR should have tested their changes prior to submitting the request to merge changes. However, there are some circumstances where this may not be possible in which case you must add the label "Hold" and update the PR description indicating why it isn't ready for review just yet. The policy to add the label "Hold" is a signal to reviewers that the changes are not yet ready to be reviewed.

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

### Continuous Integration

To ensure Narayana is stable for everyone, all changes should go through Narayana continuous integration: when you raise a pull request one of the members of the team will schedule a CI run to test your PR.
Note that when a CI test axis passes there is *no need* to disable further testing of the axis (the danger of doing this is that if further commits are added to the PR then the axis will not be retested).

## Update the issue with the correct release information:

When the github Pull Request has passed all relevant CI checks and has been Approved by a reviewer the code can be merged. If you don't have permission to do this then ping one of the team who will then merge it. Once merged the issue must be updated in the issue tracker (if you don't have permission then a team member will do this):

1. Press the `Pull Request Closed` button (the status will automatically be set to `Done`)
2. Set the `Fix Version/s` field to `6.next` (if the fix is targeted for a different major version, such as 7.next use that instead).
   Note that this step is vital since the release process only includes issues in the `Done` state and with the correct `<version>.next` field, otherwise your fix will not be included in the next release.
   Note that if you merge to a branch other than master please ensure that it is a maintenance release (or, in some cases, a topic branch).
   The release coordinator will update this `Fix Version` field to correspond with the actual version being released.
   The release coordinator will also move the status of the issue to `Closed` once it's included in a release.
4. Sanity check that the other fields have been filled in correctly (although these are normally set when the issue is created):
5. Please provide meaningful release notes so that potential users can see, at a glance, what new things can be expected from the release that contains the fix.

### Tests and documentation are not optional

Don't forget to include tests in your pull requests.
Also don't forget the documentation (reference documentation for features, javadoc...).
