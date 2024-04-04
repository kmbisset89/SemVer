# Semantic Versioning Gradle Plugin 🐘

[![Pre Merge Checks](https://github.com/cortinico/kotlin-gradle-plugin-template/workflows/Pre%20Merge%20Checks/badge.svg)](https://github.com/cortinico/kotlin-gradle-plugin-template/actions?query=workflow%3A%22Pre+Merge+Checks%22)  [![License](https://img.shields.io/github/license/cortinico/kotlin-android-template.svg)](LICENSE) ![Language](https://img.shields.io/github/languages/top/cortinico/kotlin-android-template?color=blue&logo=kotlin)

This repository contains a Gradle plugin designed to automate the management of project versions following [Semantic Versioning (SemVer)](https://semver.org/) principles. The plugin provides tasks to bump major, minor, patch, and release candidate versions based on the project's current version and its Git history.


## How to use 👣

### Installation
To use the plugin, add the following to your project's `build.gradle.kts` file:

```kotlin
plugins {
    id("io.github.kmbisset89.semver") version "1.0.1"
}
```

### Configuration
Configure the plugin via the simVerConfig extension in your Gradle build script:

```
simVerConfig {
    gitDirectory.set("${project.rootDir}")
    baseBranchName.set("main")
    gitEmail.set("example@example.com")
    gitPat.set("yourPersonalAccessToken")
    considerLocalPropertiesFile.set(true)
}
```

#### Extension Properties
- gitDirectory: Directory of the Git repository.
- baseBranchName: The base branch for versioning.
- gitEmail: Email to use for Git operations.
- gitPat: Personal access token for Git operations.
- considerLocalPropertiesFile: Flag to consider a local properties file for configuration.

### Usage

Run the desired version bump task:

```
./gradlew bumpPatch # Bumps the patch version
./gradlew bumpMinor # Bumps the minor version
./gradlew bumpMajor # Bumps the major version
./gradlew bumpReleaseCandidate # Bumps or creates a release candidate version
```

### How It Works
1. Determine Current Version: The plugin identifies the current version based on the latest Git tag following SemVer principles.
2. Version Bumping: Based on the executed task, the plugin calculates the next version number.
3. Tagging and Updating: The new version is both set as the project's version and tagged in the Git repository.

## Features 🎨

- **Automatic Version Detection**: Determines the project's current version based on Git tags.
- **Version Bumping**: Supports incrementing major, minor, patch versions, and creating or incrementing release candidates.
- **Git Tagging**: Automatically tags the repository with the new version after bumping.
- **Customizable Git Configuration**: Allows specifying the Git directory, branch, user, and personal access token (PAT) for operations requiring authentication.


## Publishing 🚀

[![Publish Plugin to Portal](https://github.com/cortinico/kotlin-gradle-plugin-template/workflows/Publish%20Plugin%20to%20Portal/badge.svg?branch=1.0.0)](https://github.com/cortinico/kotlin-gradle-plugin-template/actions?query=workflow%3A%22Publish+Plugin+to+Portal%22)


## Contributing 🤝

Feel free to open a issue or submit a pull request for any bugs/improvements.

## License 📄

This template is licensed under the MIT License - see the [License](License) file for details.
Please note that the generated template is offering to start with a MIT license but you can change it to whatever you wish, as long as you attribute under the MIT terms that you're using the template.
