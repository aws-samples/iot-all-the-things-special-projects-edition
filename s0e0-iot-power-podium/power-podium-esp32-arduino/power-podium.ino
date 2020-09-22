// For WiFi, MQTT, HTTP support, and SPIFFS
#include <WiFiClientSecure.h>
#include <MQTTClient.h>
#include <WiFi.h>
#include <SPIFFS.h>
#include <HTTPClient.h>

// For I2S
// NOTE: Only tested on the ESP32! Board must run at 160MHz or 240MHz or audio will be mangled.
// ESP-WROOM-32
//  Real pin # 9 -> LRC  (Yellow) - AKA GPIO26
//  Real pin #10 -> BCLK (Blue)   - AKA GPIO25
//  Real pin #36 -> DIN  (Green)  - AKA GPIO22
#include "AudioFileSourceSPIFFS.h"
#include "AudioFileSourceID3.h"
#include "AudioGeneratorMP3.h"
#include "AudioOutputI2S.h"

// Globals for MP3 file playing
AudioGeneratorMP3 *mp3;
AudioFileSourceSPIFFS *file;
AudioOutputI2S *out;
AudioFileSourceID3 *id3;

#define DEBUG_TOPIC "power-podium/debug/"

#define RESPONSE_TOPIC_PREFIX "power-podium/response/"
#define LIST_RESPONSE_TOPIC RESPONSE_TOPIC_PREFIX"list"

#define REQUEST_TOPIC_PREFIX "power-podium/request/"
#define REQUEST_TOPIC_FILTER REQUEST_TOPIC_PREFIX"#"
#define PLAY_REQUEST_TOPIC REQUEST_TOPIC_PREFIX"play"
#define PLAY_RANDOM_REQUEST_TOPIC REQUEST_TOPIC_PREFIX"play_random"
#define PLAY_NUMBER_REQUEST_TOPIC REQUEST_TOPIC_PREFIX"play_number"
#define LIST_REQUEST_TOPIC REQUEST_TOPIC_PREFIX"list"
#define DOWNLOAD_REQUEST_TOPIC REQUEST_TOPIC_PREFIX"download"
#define DELETE_REQUEST_TOPIC REQUEST_TOPIC_PREFIX"delete"

#define MAX_FILENAME_LENGTH 32
#define MQTT_BUFFER_SIZE 512

// For capacitive touch interface
#include <Wire.h>
#include "Adafruit_MPR121.h"

#ifndef _BV
#define _BV(bit) (1 << (bit))
#endif

// You can have up to 4 on one i2c bus but one is enough for testing!
Adafruit_MPR121 cap = Adafruit_MPR121();
bool cap_enabled = false;

// Keeps track of the last pins touched
// so we know when buttons are 'released'
uint16_t lasttouched = 0;
uint16_t currtouched = 0;

char protected_file_list[][MAX_FILENAME_LENGTH]{
        "wifissid.txt",
        "ca.pem",
        "name.txt",
        "core.pem",
        "wifipass.txt",
        "core.key",
        "endpoint.txt"
};
int protected_file_list_size = sizeof(protected_file_list) / sizeof(protected_file_list[0]);

// How many times we should attempt to connect to AWS
#define AWS_MAX_RECONNECT_TRIES 50

WiFiClientSecure net = WiFiClientSecure();
MQTTClient client = MQTTClient(MQTT_BUFFER_SIZE);

void setup() {
    Serial.begin(9600);
    randomSeed(analogRead(0));

    Serial.println("Mounting FS...");
    if (!SPIFFS.begin()) {
        Serial.println("Failed to mount file system");
        return;
    }

    connectToWiFi();
    connectToAWS();
    publish_start_message();

    // Default address is 0x5A, if tied to 3.3V its 0x5B
    // If tied to SDA its 0x5C and if SCL then 0x5D
    if (!cap.begin(0x5A)) {
        Serial.println("MPR121 not found, check wiring?");
    } else {
        Serial.println("MPR121 found!");
        cap_enabled = true;
    }

    // Log audio info to the serial port
    audioLogger = &Serial;
}

void loop() {
    if (mp3 != NULL) {
        // There is an MP3 handle
        if (mp3->isRunning()) {
            // The MP3 is playing. If we're not looping then
            if (!mp3->loop()) mp3->stop();

            // Go back to the top of the loop so we give all of our resources to playing the MP3
            return;
        } else {
            // This branch is hit when the MP3 is done playing
            // You can add post-sound actions here
        }
    }

    if (cap_enabled) {
        // Get the currently touched pads
        handle_touch();
    }

    // Get any pending MQTT messages
    client.loop();

    // Yield to SPIFFS so it can handle file system requests
    yield();
}

void handle_touch() {
    currtouched = cap.touched();

    for (uint8_t i = 0; i < 12; i++) {
        // If a touch is registered now but wasn't registered before, and it is the expected button, then start playing a file
        if ((currtouched & _BV(i)) && !(lasttouched & _BV(i))) {
            // Print that it was released
            Serial.print(i);
            Serial.println(" touched");

            if (i == 11) {
                // Touch sensor 11 plays a random file
                play_random_file();

                // Don't return immediately, we want to update the state at the bottom of this function
                break;
            }
        }

        // If a touch was registered before but isn't now then we print that it was released
        if (!(currtouched & _BV(i)) && (lasttouched & _BV(i))) {
            Serial.print(i);
            Serial.println(" released");
        }
    }

    // Reset the last touched state to the current state
    lasttouched = currtouched;
}

void connectToWiFi() {
    WiFi.mode(WIFI_STA);

    // No more hard coded credentials!

    // Get the WiFi SSID from the wifissid.txt file
    char *ssid = read_file_and_trim("/wifissid.txt");
    // Get the WiFi password from the wifipass.txt file
    char *password = read_file_and_trim("/wifipass.txt");

    WiFi.begin(ssid, password);

    // Only try 15 times to connect to the WiFi
    int retries = 0;
    while (WiFi.status() != WL_CONNECTED && retries < 15) {
        delay(500);
        Serial.print(".");
        retries++;
    }

    Serial.print("ESP32 IP address: ");
    Serial.println(WiFi.localIP());

    // If we still couldn't connect to the WiFi, go to deep sleep for a minute and try again.
    if (WiFi.status() != WL_CONNECTED) {
        esp_sleep_enable_timer_wakeup(1 * 60L * 1000000L);
        esp_deep_sleep_start();
    }
}

void connectToAWS() {
    // No more pasted in certificates!

    // Configure WiFiClientSecure to use the AWS certificates we generated
    char *ca = read_file_and_trim("/ca.pem");
    char *pem = read_file_and_trim("/core.pem");
    char *private_key = read_file_and_trim("/core.key");
    char *endpoint = read_file_and_trim("/endpoint.txt");
    char *device_name = read_file_and_trim("/name.txt");

    net.setCACert(ca);
    net.setCertificate(pem);
    net.setPrivateKey(private_key);

    // Connect to the MQTT broker on the AWS endpoint we defined earlier
    client.begin(endpoint, 8883, net);

    // Try to connect to AWS and count how many times we retried.
    int retries = 0;
    Serial.print("Connecting to AWS IOT [");
    Serial.print(endpoint);
    Serial.println("]");

    while (!client.connect(device_name) && retries < AWS_MAX_RECONNECT_TRIES) {
        Serial.print(".");
        delay(100);
        retries++;
    }

    // Make sure that we did indeed successfully connect to the MQTT broker
    // If not we just end the function and wait for the next loop.
    if (!client.connected()) {
        Serial.println(" Timeout!");
        return;
    }

    // If we land here, we have successfully connected to AWS!
    // And we can subscribe to topics and send messages.

    client.onMessage(messageReceived);
    client.subscribe(REQUEST_TOPIC_FILTER);
    Serial.print("Subscribed to: ");
    Serial.println(REQUEST_TOPIC_FILTER);

    Serial.println("Connected!");
}

void messageReceived(String &topic, String &payload) {
    Serial.print("Topic: ");
    Serial.println(topic);

    if (topic.equals(PLAY_REQUEST_TOPIC)) {
        play_file(payload);

        return;
    }

    if (topic.equals(PLAY_RANDOM_REQUEST_TOPIC)) {
        play_random_file();

        return;
    }

    if (topic.equals(PLAY_NUMBER_REQUEST_TOPIC)) {
        char *temp = to_char_array(payload);
        int temp_number = atoi(temp);
        play_file_by_number(temp_number);
        free(temp);

        return;
    }

    if (topic.equals(LIST_REQUEST_TOPIC)) {
        list_files();
        return;
    }

    if (topic.equals(DELETE_REQUEST_TOPIC)) {
        delete_file(payload);

        return;
    }

    if (topic.equals(DOWNLOAD_REQUEST_TOPIC)) {
        download_file(payload);

        return;
    }
}

void list_files() {
    String output = "";
    String separator = "";

    File root = SPIFFS.open("/");

    File file = root.openNextFile();

    while (file) {
        // Must copy the name and size values into temporary strings otherwise they'll get corrupted as the buffers get reused when looking at the next file
        output += separator;
        separator = ", ";
        output += file.name();
        output += ":";
        output += file.size();
        file = root.openNextFile();
    }

    client.publish(LIST_RESPONSE_TOPIC, output);

    root.close();
}

int get_number_of_mp3_files() {
    File root = SPIFFS.open("/");

    File file = root.openNextFile();
    int counter = 0;

    while (file) {
        Serial.println(file.name());

        if (is_speech_mp3(file.name())) {
            counter++;
        } else {
        }

        file = root.openNextFile();
    }

    root.close();

    return counter;
}

bool is_speech_mp3(const char *filename) {
    // Check if this is a file we want to count (ignore non-MP3 files, ignore files with single letter names [e.g. "/1.mp3"])
    return (ends_with(".mp3", filename) && strlen(filename) > 6);
}

bool ends_with(const char *ending, const char *input) {
    if (strlen(input) < strlen(ending)) {
        return false;
    }

    return (strcmp(ending, (input + strlen(input) - strlen(ending))) == 0);
}

void delete_file(String filename) {
    for (int loop = 0; loop < protected_file_list_size; loop++) {
        if (filename.equals(protected_file_list[loop])) {
            Serial.print("Cannot delete protected file: ");
            Serial.println(filename);
            return;
        } else {
        }
    }

    filename = "/" + filename;

    if (SPIFFS.remove(filename)) {
        Serial.print("Deleted file: ");
    } else {
        Serial.print("Delete failed, file may not exist: ");
    }
    Serial.println(filename);
}

void download_file(String input) {
    int commaIndex = input.indexOf(",");

    if (commaIndex == -1) {
        Serial.println("Invalid download request");
        return;
    }

    String filename = input.substring(0, commaIndex);
    filename = "/" + filename; // Must have a slash prefix or this SPIFFS.open will fail
    String url = input.substring(commaIndex + 1);

    HTTPClient http;
    http.begin(url);
    File file = SPIFFS.open(filename, "w");
    if (!file) {
        Serial.print("Could not open file: ");
        Serial.println(filename);
        return;
    }
    http.begin(url);
    int httpCode = http.GET();
    if ((httpCode <= 0) || (httpCode != HTTP_CODE_OK)) {
        Serial.printf("[HTTP] GET... failed, error: %s\n", http.errorToString(httpCode).c_str());
    } else {
        http.writeToStream(&file);
    }
    file.close();
    http.end();
}

void publish_start_message() {
    client.publish(DEBUG_TOPIC, "Started!");
}

void remove_newline(char *input) {
    int lastPosition = strlen(input) - 1;

    // Remove \n
    if (input[lastPosition] == 10) {
        input[lastPosition] = 0;
    }

    // Remove \r for Windows users
    if (input[lastPosition - 1] == 13) {
        input[lastPosition - 1] = 0;
    }
}

char *read_file_and_trim(String filename) {
    char *filedata = to_char_array(read_file(filename));
    remove_newline(filedata);

    return filedata;
}

String read_file(String filename) {
    int counter = 0;
    File file = SPIFFS.open(filename);

    if (!file) {
        Serial.println("Failed to open " + filename);
        return "";
    }

    Serial.println("Opened " + filename);

    String output = "";

    while (file.available()) {
        counter++;
        output += char(file.read());
    }

    file.close();

    Serial.println(counter);
    Serial.println("Read " + filename);

    return output;
}

char *to_char_array(String input) {
    int len = input.length() + 1;
    char *output = (char *) malloc(len);
    input.toCharArray(output, len);
    return output;
}

// START MP3 code
// Called when a metadata event occurs (i.e. an ID3 tag, an ICY block, etc.
void md_callback(void *cbData, const char *type, bool isUnicode, const char *string) {
    (void) cbData;
    Serial.printf("ID3 callback for: %s = '", type);

    if (isUnicode) {
        string += 2;
    }

    while (*string) {
        char a = *(string++);
        if (isUnicode) {
            string++;
        }
        Serial.printf("%c", a);
    }
    Serial.printf("'\n");
    Serial.flush();
}

void play_file(String filename) {
    if (filename[0] != '/') {
        filename = "/" + filename;
    }
    char *char_filename = to_char_array(filename);
    file = new AudioFileSourceSPIFFS(char_filename);
    free(char_filename);
    id3 = new AudioFileSourceID3(file);
    id3->RegisterMetadataCB(md_callback, (void *) "ID3TAG");
    out = new AudioOutputI2S();
    mp3 = new AudioGeneratorMP3();
    mp3->begin(id3, out);
}

void play_random_file() {
    int random_file_number = random(get_number_of_mp3_files());

    Serial.print("Random number: ");
    Serial.println(random_file_number);

    play_file_by_number(random_file_number);
}

void play_file_by_number(int number) {
    File root = SPIFFS.open("/");
    File file = root.openNextFile();

    int counter = 0;

    while (file) {
        Serial.print("Counter: ");
        Serial.println(counter);

        if (is_speech_mp3(file.name())) {
            if (counter == number) {
                Serial.print("Playing: ");
                Serial.println(file.name());
                play_file(file.name());
            }

            counter++;
        }

        file = root.openNextFile();
    }

    root.close();
}
