## Redirect Change Number Plugin

### Context

The projects `jgit/jgit` and `egit/egit` are going to be migrated from `Eclipse foundation (git.eclise.org)` infrastructure
to `Gerrithub.io (eclipse.gerrithub.io)` infrastructure as `GerritCodeReview/jgit` and `GerritCodeReview/egit`.

To allow a smooth transition, all the HTTP requests to get `Change Details page`, i.e https://git.eclipse.org/r/c/jgit/jgit/+/201852
under `git.eclise.org` domain will be redirected to `eclipse.gerrithub.io` domain i.e https://eclipse.gerrithub.io/201852

### Purpose of the plugin

The purpose of the plugin is to allow `Eclipse Foundation Gerrit` to redirect all requests to `https://git.eclise.org/r/<change-num>`
to `https://eclipse.gerrithub.io/<change-num>`.

The plugin imports the `jgit` and `egit` legacy change numbers and makes the mapping from `/<change-num>` to `/c/<project>/+/<change-num>`.

An example of how the end to end works would be:
1. Request to `https://git.eclipse.org/r/c/jgit/jgit/+/202303`
2. Redirect to `https://eclipse.gerrithub.io/202303`
3. Redirect to `https://eclipse.gerrithub.io/c/GerritCodeReview/jgit/+202303`

### Use cases

| Description                                                                                    | HTTP request VERB | HTTP request URL                                | HTTP Response status code | HTTP Response headers                                                         |
|------------------------------------------------------------------------------------------------|-------------------|-------------------------------------------------|---------------------------|-------------------------------------------------------------------------------|
| Request change details of `201852` that belongs to project `jgit/jgit`                         | GET               | https://eclipse.gerrithub.io/201852             | Status code 302           | Location header https://eclipse.gerrithub.io/c/GerritCodeReview/jgit/+/201852 |
| Request change details of `234572` that belongs to project `egit/egit`                         | GET               | https://eclipse.gerrithub.io/234572             | Status code 302           | Location header https://eclipse.gerrithub.io/c/GerritCodeReview/egit/+/234572 |

In the use cases where the change number doesn't belong to `jgit` or `egit` legacy change numbers, the request should go through the backend.

### How to build

Clone or link this plugin to the plugins directory of Gerrit‘s source tree, and then run bazel build on the plugin’s directory.

Example:

```
git clone --recursive https://gerrit.googlesource.com/gerrit && \
git clone https://review.gerrithub.io/GerritForge/redirect-change-number && \
cd gerrit/plugins && \
ln -s ../../redirect-change-number . && cd .. \
bazelisk build plugins/redirect-change-number
```

In order to run the test:

Example:
```
bazelisk test plugins/redirect-change-number/...
```




