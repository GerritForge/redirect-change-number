load("@rules_java//java:defs.bzl", "java_binary", "java_library")
load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
    "gerrit_plugin",
)

gerrit_plugin(
    name = "redirect-change-request",
    srcs = glob(
        ["src/main/java/**/*.java"],
    ),
    manifest_entries = [
        "Gerrit-PluginName: redirect-change-request",
        "Gerrit-Module: com.googlesource.gerrit.plugins.gitrepometrics.Module",
        "Implementation-Title: redirect-change-request plugin",
        "Implementation-URL: https://review.gerrithub.io/admin/repos/GerritForge/redirect-change-request",
        "Implementation-Vendor: GerritForge",
    ],
    resources = glob(
        ["src/main/resources/**/*"],
    ),
)

junit_tests(
    name = "redirect-change-request_tests",
    srcs = glob(
        [
            "src/test/java/**/*Test.java",
            "src/test/java/**/*IT.java",
        ],
    ),
    resources = glob(["src/test/resources/**/*"]),
    tags = [
        "redirect-change-request",
    ],
    deps = [
        ":redirect-change-request__plugin_test_deps",
    ],
)

java_library(
    name = "redirect-change-request__plugin_test_deps",
    testonly = 1,
    visibility = ["//visibility:public"],
    exports = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":redirect-change-request__plugin",
    ],
)

java_library(
    name = "redirect-change-request__plugin_deps",
    visibility = ["//visibility:public"],
    exports = PLUGIN_DEPS,
)
