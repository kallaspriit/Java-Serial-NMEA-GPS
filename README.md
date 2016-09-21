# Java-Serial-NMEA-GPS

## About
**Java Serial GPS**
- Connects to given serial port using RXTX.
- Parses NMEA messages to GPS state (position, altitude, fix, quality etc)
- Uses threads and asyncronous listeners.
- Works on Windows.

## Usage
- `gradle run` - builds and runs the example

## Example output
- 58.383602, 26.71938 (got fix)
- 58.383602, 26.719387 (got fix)
- 58.383602, 26.719387 (got fix)
- ...