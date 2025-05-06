import java.net.URI

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
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = URI("https://jitpack.io")
        }
        maven {
            url = URI("https://repo.itextsupport.com/android")
        }
        maven { url = URI("https://repository.liferay.com/nexus/content/repositories/public") }
    }
}

rootProject.name = "Supply Sync"
include(":app")
 