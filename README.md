# AutoStepper

AutoStepper is a Java application that converts audio tracks (`.mp3`/`.wav`) into StepMania `.sm` charts.

## Features
- GUI mode with drag & drop.
- CLI mode for scripted/batch processing.
- BPM estimation from audio analysis.
- Multi-difficulty chart generation (Beginner to Challenge).
- Optional image discovery for banner/background.
- Multi-threaded batch mode (`threads=<n>`).
- JSON config support (`config=path/to/config.json`).

## Requirements
- Java 17+
- Maven 3.9+

## Build
```bash
mvn clean package
```

## Run
### GUI
```bash
java -jar target/autostepper-2.0.0.jar
```

### CLI
```bash
java -jar target/autostepper-2.0.0.jar input=/music output=/out duration=120 hard=true threads=4
```

### CLI with config file
```bash
java -jar target/autostepper-2.0.0.jar input=song.mp3 output=out config=autostepper.json
```

Example `autostepper.json`:
```json
{
  "maxBpm": 175,
  "minBpm": 70,
  "bpmSensitivity": 0.05,
  "startSync": 0.0
}
```

## Troubleshooting
- If no BPM is detected, AutoStepper falls back to 120 BPM.
- If artwork scraping fails, charts are still generated without banner/background.
- Use `duration=0` to analyze full track.

## Contributing
1. Fork and branch.
2. Add tests for behavior changes.
3. Run `mvn test` before opening a PR.

## Screenshots
- GUI main window: `docs/images/gui-main.png` (placeholder)
- Generated song folder: `docs/images/output-folder.png` (placeholder)
