dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "JetPref"
include(":example")
include(":datastore-annotations")
include(":datastore-model")
include(":ui-compose")
