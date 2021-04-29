#include "secrets.h"
#include <WiFiClientSecure.h>
#include <MQTTClient.h>
#include <ArduinoJson.h>
#include "WiFi.h"

// The MQTT topics that this device should publish/subscribe
#define AWS_IOT_SUBSCRIBE_TOPIC "cfn_watcher/were_done"

const int in_A1 = 4;
const int in_A2 = 15;
int stack_complete = 0;

WiFiClientSecure net = WiFiClientSecure();
MQTTClient client = MQTTClient(256);

void connectAWS()
{
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  Serial.println("Connecting to Wi-Fi");

  while (WiFi.status() != WL_CONNECTED){
    Serial.println(WiFi.status());
    delay(500);
    Serial.print(".");
  }

  // Configure WiFiClientSecure to use the AWS IoT device credentials
  net.setCACert(AWS_CERT_CA);
  net.setCertificate(AWS_CERT_CRT);
  net.setPrivateKey(AWS_CERT_PRIVATE);

  // Connect to the MQTT broker on the AWS endpoint we defined earlier
  Serial.println("Connecting to MQTT broker on the AWS Endpoint");
  client.begin(AWS_IOT_ENDPOINT, 8883, net);

  // Create a message handler
  Serial.println("Creating a message handler");
  client.onMessage(messageHandler);

  Serial.println("Connecting to AWS IOT");

  while (!client.connect(THINGNAME)) {
    Serial.print(".");
    delay(100);
  }

  if(!client.connected()){
    Serial.println("AWS IoT Timeout!");
    return;
  }

  // Subscribe to a topic
  client.subscribe(AWS_IOT_SUBSCRIBE_TOPIC);
  Serial.print("Subscribing to the topic ");
  Serial.println(AWS_IOT_SUBSCRIBE_TOPIC);

  Serial.println("AWS IoT Connected!");
}

void messageHandler(String &topic, String &payload) {
  Serial.println("incoming: " + topic + " - " + payload);
  stack_complete = 1;
  Serial.print("Stack complete set to: ");
  Serial.println(stack_complete);
  // deserialize json
  StaticJsonDocument<2000> doc;
  deserializeJson(doc, payload);
  Serial.println(payload);
  String message = doc["StackStatus"];


  // Print the message on the thermal printer
  Serial.println(message);
  // Possible status for CFN stack complete: stack_completed_list = ['CREATE_COMPLETE', 'CREATE_FAILED', 'ROLLBACK_COMPLETE', 'ROLLBACK_FAILED', 'UPDATE_COMPLETE', 'UPDATE_ROLLBACK_COMPLETE', 'UPDATE_ROLLBACK_FAILED', 'IMPORT_COMPLETE', 'IMPORT_ROLLBACK_FAILED', 'IMPORT_ROLLBACK_COMPLETE']
  if (stack_complete == 1) {
    if (message == "CREATE_COMPLETE" || message == "UPDATE_COMPLETE" || message == "IMPORT_COMPLETE") {
      Serial.println("We have a successful stack launch!");
      digitalWrite(in_A1, HIGH);
      digitalWrite(in_A2, LOW);
      delay(5000);
      digitalWrite(in_A1, LOW);
      digitalWrite(in_A2, LOW);
      stack_complete = 0;
    }
    else {
      int oh_no = 1;
      while (oh_no < 15){
      digitalWrite(in_A1, LOW);
      digitalWrite(in_A2, HIGH);
      delay(200);
      digitalWrite(in_A1, HIGH);
      digitalWrite(in_A2, LOW);
      delay(200);
      oh_no += 1;
      }
      digitalWrite(in_A1, LOW);
      digitalWrite(in_A2, LOW);
      stack_complete = 0;
     }
   }
}

void setup() {
  Serial.begin(9600);
  Serial.println("Initializing motor");
  pinMode(in_A1, OUTPUT);
  pinMode(in_A2, OUTPUT);
  digitalWrite(in_A1, LOW);
  digitalWrite(in_A2, LOW);
  connectAWS();
}

void loop() {
  client.loop();
  delay(10);
}
