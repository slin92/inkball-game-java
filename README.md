# Inkball – Java Game (Processing)

A Java implementation of an Inkball-inspired puzzle game.  
The player draws lines to guide moving balls into matching holes across multiple levels.

---

## Features

- Multiple levels loaded from text files (`level1.txt`, `level2.txt`, `level3.txt`)
- Config-driven game setup (`config.json`)
- Ball physics and collision handling
- Capture / scoring logic
- Modular OOP class design (`Level`, `Ball`, `Player`, `LevelLoader`)

---

## Technologies

- Java
- Processing (graphics/game loop)
- Gradle

---

## Project Layout
build.gradle
config.json
level1.txt
level2.txt
level3.txt
src/main/java/inkball/ # game source code
src/main/resources/inkball/ # assets (if used)
src/test/java/inkball/ # tests


---

## How to Run

### IntelliJ (Recommended)
1. Open the project as a Gradle project
2. Run `src/main/java/inkball/App.java`

### Terminal (Gradle)

macOS/Linux:
```bash
./gradlew test
./gradlew run

Windows:
gradlew test
gradlew run
If run is not configured, run App.java directly from your IDE.

What This Demonstrates

Object-oriented program design
State management in a game loop
File-based level loading and configuration
Testing using JUnit
