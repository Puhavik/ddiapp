# DDI Checker

Desktop app for looking up **drug–drug interactions**. Enter two drug names; the app lists known interactions sorted by severity and opens an alert window for significant and critical results. Built with JavaFX and FXML.

## Features

- Search by two drug names, in either order.
- Exact name match first; falls back to word-based matching when nothing is found.
- Results table: both drugs, interaction, effect, severity, recommendation.
- Sorted from most to least severe.
- Separate alert window for significant and critical interactions.
- macOS-style theme (`macos-theme.css`).

## Data

Interaction data (`CombinedDatasetConservativeTWOSIDES.csv`) is tracked with Git LFS. Install [git-lfs](https://git-lfs.com) before cloning, or the file arrives as a pointer.

## Requirements

- Java 17+
- Maven (JavaFX 21 + opencsv pulled via `pom.xml`)

## Run

```bash
mvn -q javafx:run
```
