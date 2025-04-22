pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()

        maven { url = uri("https://artifactory.gz.cvte.cn/artifactory/mvn-releases/")}
        maven { url = uri("https://artifactory.gz.cvte.cn/artifactory/SR_maven_releases_local/")}
        maven { url = uri("https://artifactory.gz.cvte.cn/artifactory/SR_maven_snapshots_local/")}

        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/jcenter") }
        maven { url = uri("https://maven.aliyun.com/nexus/content/groups/public") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven { url = uri("https://artifactory.gz.cvte.cn/artifactory/mvn-releases/")}
        maven { url = uri("https://artifactory.gz.cvte.cn/artifactory/SR_maven_releases_local/")}
        maven { url = uri("https://artifactory.gz.cvte.cn/artifactory/SR_maven_snapshots_local/")}

        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/jcenter") }
        maven { url = uri("https://maven.aliyun.com/nexus/content/groups/public") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }

    }
}
rootProject.name = "IPConnect"
include(":app")
 