## Redirect Change Request Plugin

### Context

The projects `jgit/jgit` and `egit/egit` are going to be migrated from `Eclipse foundation (git.eclise.org)` infrastructure
to `Gerrihub.io (eclipse.gerrithub.io)` infrastructure as `GerritCodeReview/jgit` and `GerritCodeReview/egit`.

To allow a smooth transition, all the HTTP requests to get `Change Details page`, i.e https://git.eclipse.org/r/c/jgit/jgit/+/201852
under `git.eclise.org` domain will be redirected to `eclipse.gerrithub.io` domain i.e https://eclipse.gerrithub.io/201852

### Purpose of the plugin

The purpose of the plugin is to translate the HTTP requests i.e https://eclipse.gerrithub.io/201852 to correct form i.e
https://eclipse.gerrithub.io/c/GerritCodeReview/jgit/+/201852.

### Use cases

| Description                                                                                    | HTTP request VERB | HTTP request URL                              | HTTP Response status code | HTTP Response headers                                                         | 
|------------------------------------------------------------------------------------------------|-------------------|-----------------------------------------------|---------------------------|-------------------------------------------------------------------------------|
| Request change details of `201852` that belongs to project `jgit/jgit`                         | GET               | https://eclipse.gerrithub.io/201852           | Status code 302           | Location header https://eclipse.gerrithub.io/c/GerritCodeReview/jgit/+/201852 |
| Request change details of `234572` that belongs to project `egit/egit`                         | GET               | https://eclipse.gerrithub.io/234572           | Status code 302           | Location header https://eclipse.gerrithub.io/c/GerritCodeReview/egit/+/234572 |
| Request change details of `532556` that does not belong to neither `jgit/jgit` nor `egit/egit` | GET               | https://eclipse.gerrithub.io/532556           | Status code 404           |                                                                               |
| Change details `234572/42423432` is not a numeric value                                        | GET               | https://eclipse.gerrithub.io/234572/42423432  | Status code 400           |                                                                               |






