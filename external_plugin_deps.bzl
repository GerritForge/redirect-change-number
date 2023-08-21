load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
    maven_jar(
        name = "gerrit-plugin-api",
        artifact = "com.google.gerrit:gerrit-plugin-api:3.8"
    )
