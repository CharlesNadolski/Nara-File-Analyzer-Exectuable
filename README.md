# File Analyzer

## PURPOSE

Project Page: <http://georgetown-university-libraries.github.io/File-Analyzer/>

The File Analyzer and Metadata Harvester is a general purpose desktop (and command line) tool designed to automate simple, file-based operations.
The File Analyzer assembles a toolkit of tasks a user can perform.

The tasks that have been written into the File Analyzer code base have been optimized for use by libraries, archives, and other cultural heritage institutions.

File Analyzer Wiki: <https://github.com/Georgetown-University-Libraries/File-Analyzer/wiki>

### Demonstration Videos

[![Demonstration Video](https://i.ytimg.com/vi/kVi_k-HdH_4/1.jpg)](http://www.youtube.com/watch?v=kVi_k-HdH_4)
[![Demonstration Video](https://i.ytimg.com/vi/1I8n60ZrwHo/1.jpg)](http://www.youtube.com/watch?v=1I8n60ZrwHo)
[![Demonstration Video](https://i.ytimg.com/vi/5zYA04P0HPk/default.jpg)](http://www.youtube.com/watch?v=5zYA04P0HPk)

## History

This code has been derived from the NARA File Analyzer and Metadata Harvester which is available at <https://github.com/usnationalarchives/File-Analyzer>.

## Local Development

- Download amd install the most recent LTS release of the [Adoptium Eclipse Temurin JDK](https://adoptium.net/).
Don't forget to select the option to set the `JAVA_HOME` environment variable.
For Linux, follow <https://askubuntu.com/questions/1375383/how-to-properly-install-temurin-jdk-with-update-alternatives>.
- For Windows, [Install Maven using Chocolatey](https://community.chocolatey.org/packages/maven) or [Install Maven Manually](https://maven.apache.org/download.cgi).
- Download and install [VS Code](https://code.visualstudio.com/download).
- Clone this code to your computer
- Run `mvn package`
- Detailed Installation Instructions: <https://github.com/Georgetown-University-Libraries/File-Analyzer/wiki/Installation-instructions>

## Build Artifacts

This code will build 3 flavors of the File Analyzer.
To read more on all possible flavors of File Analyzer, see <https://github.com/Georgetown-University-Libraries/File-Analyzer/wiki/File-analyzer-component-packages>.

### Core File Analyzer

- All code runs from a self-extracting jar file

### DSpace File Analyzer

- This version of the file analyzer is a self-extracting jar file that references the core file analyzer jar file.
- It contains tools for automating the creation of DSpace ingestion folders

### Demo File Analyzer

- This version contains extensions illustrating various capabilities of the File Analyzer.  
- This version of the file analyzer is a self-extracting jar file that references both the core and dspace file analyzer jar files.
- This version of the application uses features of Apache Tika, BagIt, and Marc4j

## Deployment

### Testing Deployment Locally

- We will be packaging the jar build artifacts into installers using [jpackage for Windows](https://www.baeldung.com/jar-windows-executables) and [jpackage for Linux](https://www.baeldung.com/java14-jpackage).
- jpackage has additional dependencies based on your OS.
For Windows, to use jpackage you need to [install Wix using Chocolatey](https://community.chocolatey.org/packages/wixtoolset).
For Linux, to use jpackage you need to install the packages `fakeroot` and `binutils`.

On Windows, to generate installers for each executable jar with a start menu shortcut and local user privileges, run the following.

```bash
jpackage --verbose --app-version 2.0 --input core/target --main-jar CoreFileAnalyzer-2.0.jar --win-menu --win-per-user-install
jpackage --verbose --app-version 2.0  -input dspace/target --main-jar DSpaceFileAnalyzer-2.0.jar --win-menu --win-per-user-install
jpackage --verbose --app-version 2.0 --input demo/target --main-jar DemoFileAnalyzer-2.0.jar --win-menu --win-per-user-install
```

On Linux, to generate installers for each executable jar with an appropriate menu group, run the following.

```bash
jpackage --verbose --app-version 2.0 --input core/target --main-jar CoreFileAnalyzer-2.0.jar --linux-menu-group Utility;FileTools;Java
jpackage --verbose --app-version 2.0  -input dspace/target --main-jar DSpaceFileAnalyzer-2.0.jar --linux-menu-group Utility;FileTools;Java
jpackage --verbose --app-version 2.0 --input demo/target --main-jar DemoFileAnalyzer-2.0.jar --linux-menu-group Utility;FileTools;Java
```

### Deployment using GitHub

- Deployment will be done using GitHub actions using the `maven-installer-upload.yml` workflow.
- Once Maven and jpackage steps are executed, deployment is completed using `actions/upload-artifact` then `actions/upload-release-asset`.

***
[![Georgetown University Library IT Code Repositories](https://raw.githubusercontent.com/Georgetown-University-Libraries/georgetown-university-libraries.github.io/master/LIT-logo-small.png)Georgetown University Library IT Code Repositories](http://georgetown-university-libraries.github.io/)
