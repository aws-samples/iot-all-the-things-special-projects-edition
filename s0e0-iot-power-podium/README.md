## S0 E0 - IoT Power Podium with Werner Vogels

This episode was actually Wale and Rudy's IoT All the Things season 2 finale

## What did we show off?

- An ESP32 in Arduino mode connected to AWS IoT Core
- I2S audio triggered from MQTT messages
- Configuration and file storage on an ESP32 in Arduino mode using SPIFFS
- A simple serverless soundboard to control the ESP32

## What code is available?

- TODO [Arduino code](power-podium-esp32-arduino/power-podium.ino)
- TODO [Soundboard code]()
- Greengrass Lambda functions for Philips Play light bar working-from-home status indicator (Coming soon!)

## What hardware was used?

- [Philips Hue E26 bulb](https://www.philips-hue.com/en-us/p/hue-white-and-color-ambiance-1-pack-e26/046677548483)
- [Philips Hue Play light bar](https://www.philips-hue.com/en-us/p/hue-white-and-color-ambiance-play-light-bar-single-pack/7820130U7)
- [LiFX A19 bulb](https://www.lifx.com/collections/gaming/products/lifx)

## FAQ

- Q: How do I get started with my Hue bridge?
- A: You'll need the IP address of your bridge and the username. You can [follow Hue's developer documentation getting started page](https://developers.meethue.com/develop/get-started-2/) to find them.