pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    mavenLocal() //Load from local ~/.m2
    maven { url = uri("https://jitpack.io") }
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    mavenLocal() //Load from local ~/.m2
    maven { url = uri("https://jitpack.io") }
  }
}

rootProject.name = "ZeroLanguageServerPrompt"

include(":app")