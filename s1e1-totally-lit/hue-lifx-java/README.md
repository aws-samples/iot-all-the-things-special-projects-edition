## hue-lifx-java

This directory contains code that communicates with Philips Hue bulbs and LiFX bulbs. Every function provided by the
code is available through shell scripts. The shell scripts build the Java code with either a local installation of Java
or Docker. You'll need either Java or Docker to run this code.

The scripts currently available are:

- [`hue-dance-party.sh`](hue-dance-party.sh) - randomly cycles through colors on Hue bulbs
- [`hue-off.sh`](hue-off.sh) - turns off Hue bulbs
- [`hue-on.sh`](hue-on.sh) - turns on Hue bulbs
- [`hue-rainbow.sh`](hue-rainbow.sh) - slowly cycles Hue bulbs through the colors of the rainbow
- [`list-hue-lights.sh`](list-hue-lights.sh) - lists all of the Hue bulbs connected to the local bridge
- [`list-hue-rooms.sh`](list-hue-rooms.sh) - lists all of the rooms the local bridge knows about
- [`list-lifx-lights.sh`](list-lifx-lights.sh) - lists all of the LiFX bulbs it can detect on the local network

Each script will provide usage instructions if no arguments are provided. Each script also rebuilds the code, if
necessary, on each run so you can make changes and test them quickly.

## FAQ

- Q: I'm seeing errors from the LiFX code that look like the snippet below, is that a problem?

```
Sep 18, 2020 12:20:24 PM com.github.besherman.lifx.impl.network.LFXMessageRouter sendWithPath
SEVERE: No address for gateway, this should not happen
```

- A: This error is not a problem. It appears to be a bug in the library.