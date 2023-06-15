load("//tools/bzl:junit.bzl", "junit_tests")
load("//tools/bzl:plugin.bzl", "PLUGIN_DEPS", "PLUGIN_TEST_DEPS", "gerrit_plugin")

gerrit_plugin(
    name = "redirect-change-request",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Implementation-Title: Redirect Change Number plugin",
        "Gerrit-PluginName: redirect-change-number",
        "Gerrit-HttpModule: com.googlesource.gerrit.plugins.redirect.api.HttpModule",
    ],
    resources = glob(["src/main/resources/**/*"]),
)

junit_tests(
    name = "redirect_change_request_tests",
    srcs = glob([
        "src/test/java/**/*Test.java",
        "src/test/java/**/*IT.java",
    ]),
    tags = ["redirect-change-request"],
    visibility = ["//visibility:public"],
    deps = PLUGIN_TEST_DEPS + PLUGIN_DEPS + [
        ":redirect-change-request__plugin",
    ],
    resources = glob(["src/test/resources/**/*"])
)