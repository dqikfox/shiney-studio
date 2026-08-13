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
        // LiveKit (a transitive dependency of the ElevenLabs Conversational AI SDK) pulls
        // a couple of its own dependencies (e.g. com.github.davidliu:audioswitch) as
        // GitHub-commit-hash "versions", which only JitPack resolves.
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "ShinyVerse"
include(":app")
