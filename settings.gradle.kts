pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials {
                username = "mapbox"
                password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").orElse(
                    "sk.eyJ1IjoiYmxhbmNjYSIsImEiOiJjbXFhcXRyZzcwM2RwMnNzOXNnczJjMDR3In0.0kcwEDBavaOR7WXqQ-huVA"
                ).get()
            }
            authentication { create<BasicAuthentication>("basic") }
        }
    }
}
rootProject.name = "Swart"
include(":app")
include(":benchmark")
