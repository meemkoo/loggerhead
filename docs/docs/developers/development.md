# Development

## Prerequisite
* WPILib installation (Build tools, VS Code, etc.)
* Python installation with MkDocs [^1]
<br><br>
[^1]: Python is only necessery for building and editing documentation.

## Notes
By default running `./gradlew build` or `./gradlew publish` will generate a vendordep that points to the local build directory.
using the `-PreleaseMode` will configure the vendordep to point to the actual hosted maven and json URLs.

## Useful commands
Main build command <br>
`./gradlew build`
`./gradlew build -PreleaseMode`

Publish will generate meven repository and vendordep. <br>
`./gradlew publish`
`./gradlew publish -PreleaseMode`

This was useful once for something, but we don't remember. <br>
`./gradlew installRoboRIOToolchain`

Clean command <br>
`./gradlew clean`

Generates gradle wrapper <br>
`./gradlew wrapper`

Spotless is run automatically, but if you want to manually trigger it run this <br>
`./gradlew spotlessApply`

Get the vendordep command to import loggerhead into a robot project. Useful for development. <br>
`./gradlew getDevVendordep`

## Docs
Main documentation (not javadoc) is built using MkDocs. Note there are currently no scripts to automatically setup a venv with mkdocs because
we can't be bothered to fight with gradle for the time being.
### Steps
1. `cd <project-root>/docs`
2. `mkdocs <build|serve>` (You might need to use `python -m mkdocs ...` if you are not using a venv)

### Javadoc
Javadocs are built when you use `./gradlew` build and publish
