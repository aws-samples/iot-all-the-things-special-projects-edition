#include <WiFi.h>
#include <SPI.h>
#include <CapacitiveSensor.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

int sendPin = X; // In the show, I used pin 4
int sensorPin = Y; // In the show, I used pin 15
CapacitiveSensor Sensor = CapacitiveSensor(sendPin, sensorPin);
long val;
int pos;
bool lightState;
String body;


char ssid[] = "<WIFI SSID>";     //  your network SSID (name)
char pass[] = "<WIFI PASSWORD";  // your network password
int status = WL_IDLE_STATUS;     // the Wifi radio's status
String LOCAL_HUE = "<HUE BRIDGE IP>";
String USER_NAME = "<HUE USER HASH>";
String LIGHT_ID = "<LIGHT ID>";
String getStateUrl = "https://"+LOCAL_HUE+"/api/"+USER_NAME+"/lights/"+LIGHT_ID+"/";
String setStateUrl = "https://"+LOCAL_HUE+"/api/"+USER_NAME+"/lights/"+LIGHT_ID+"/state";

WiFiClientSecure *client = new WiFiClientSecure;
HTTPClient https;


void setup() {
  Serial.begin(115200);
  StaticJsonDocument<1500> doc;
  delay(2000);
  while ( WiFi.status() != WL_CONNECTED) {
    Serial.print("Attempting to connect to WPA SSID: ");
    Serial.println(ssid);
    // Connect to WPA/WPA2 network:
    WiFi.begin(ssid, pass);
    status = WiFi.status();

    // wait 10 seconds for connection:
    delay(5000);

      // you're connected now, so print out the data:
  }
  Serial.print("You're connected to the network");

    https.begin(*client, getStateUrl);
    int httpCode = https.GET();
    String jsonInitGet = https.getString();
    DeserializationError error = deserializeJson(doc, jsonInitGet);

    // Test if parsing succeeds.
    if (error) {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.c_str());
      return;
    }
    
    lightState = doc["state"]["on"];
    Serial.println(lightState);
}

void loop() {
  val = Sensor.capacitiveSensor(30);

  if (val >= 1000) {
    if (lightState == false) {
      body = "{\"on\": true, \"transitiontime\":0,}";
    }
    else if (lightState == true) {
      body = "{\"on\": false, \"transitiontime\":0,}";
    }
      https.begin(*client, setStateUrl);
      https.addHeader("Content-Type", "text/plain");
      int httpResponseCode = https.PUT(body);
  }
  delay(100);
}
