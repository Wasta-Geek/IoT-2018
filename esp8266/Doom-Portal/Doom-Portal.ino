#include <PubSubClient.h>
#include <ESP8266WiFi.h>
#define FASTLED_ESP8266_RAW_PIN_ORDER
#include <FastLED.h>

/*#define WIFI_SSID "Bbox-7D60413A"
  #define WIFI_PASSWORD "269F673694319C1951A61DA7DCCD95"*/
#define WIFI_SSID         "SFR-24f0"
#define WIFI_PASSWORD     "Q6WZVJSZ2TWH"

#define MQTT_SERVER       IPAddress(192, 168, 0, 14)
#define MQTT_PORT         1883
#define MQTT_USER         "wasta-geek"
#define MQTT_PASSWORD     "root"

#define DATA_LED_PIN      4
#define NUM_LEDS          1
#define DATA_PIR_PIN      D5
#define BUTTON_PIN        A0
#define THRESHOLD         100

#define MIN_PAIRING_TIME  10

#define PAIRING_TOPIC     "doom_portal/await_pairing"
#define UNLOCK_DOOR_TOPIC "doom_portal/unlock_response"

WiFiClient espClient;
PubSubClient client(espClient);
CRGB leds[NUM_LEDS];

bool isMotionDetected = false;

void unlockDoorTopicManager(char data[])
{
  Serial.print("UNLOCK_RESPONSE: ");
  Serial.println(data);
  FastLED.clear();
  static unsigned long minTimeBlinking = 4000;
  // Refuse
  if (strcmp(data, "UNAUTHORIZED") == 0)
  {
    unsigned long start_time = millis();
    while (millis() - start_time <= minTimeBlinking)
    {
      changeLEDColor(CRGB::Red);
      delay(250);
      clearLED();
      delay(250);
    }
  }
  // Accept
  else if (strcmp(data, "AUTHORIZED") == 0)
  {
    unsigned long start_time = millis();
    while (millis() - start_time <= minTimeBlinking)
    {
      changeLEDColor(CRGB::Green);
      delay(250);
      clearLED();
      delay(250);
    }
  }
  // No face detected
  else if (strcmp(data, "NO FACE") == 0)
  {
    unsigned long start_time = millis();
    while (millis() - start_time <= minTimeBlinking)
    {
      changeLEDColor(CRGB::Orange);
      delay(250);
      clearLED();
      delay(250);
    }
  }
  changeLEDColor(CRGB::Green);
}

void motionDetected()
{
  isMotionDetected = true;
  leds[0] = CRGB::Red; FastLED.show(); delay(100);
  leds[0] = CRGB::Green; FastLED.show(); delay(100);
  leds[0] = CRGB::Blue; FastLED.show(); delay(100);
}

void message_received(char* topic, byte* payload, unsigned int length)
{
  Serial.println("-----------------------");
  Serial.print("Message arrived in topic: ");
  Serial.println(topic);


  char data[length];
  Serial.print("Message:");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
    data[i] = payload[i];
  }
  Serial.println();

  if (strcmp(topic, UNLOCK_DOOR_TOPIC) == 0)
    unlockDoorTopicManager(data);
  Serial.println("-----------------------");
}

void do_connect()
{
  // Create a random client ID
  String clientId = "ESP8266Client-";
  clientId += String(random(0xffff), HEX);
  Serial.println("Connecting to MQTT...");
  if (client.connect(clientId.c_str(), MQTT_USER, MQTT_PASSWORD))
  {
    Serial.println("connected");
    client.subscribe(UNLOCK_DOOR_TOPIC);
    client.subscribe(PAIRING_TOPIC);
  }
  else
  {
    Serial.print(client.state());
    Serial.print("failed with state ");
    Serial.print(client.state());
    delay(2000);
  }
}

void clearLED()
{
  FastLED.clear();
  FastLED.show();
}

void changeLEDColor(const unsigned int &color)
{
  leds[0] = color;
  FastLED.show();
}

void setup() {
  // Setup serial debug
  Serial.begin(9600);

  // Setup FastLED (GRB = order GreenRedBlue)
  FastLED.addLeds<WS2812B, DATA_LED_PIN, GRB>(leds, NUM_LEDS);
  changeLEDColor(CRGB::Red);

  // Declare sensor as input
  pinMode(DATA_PIR_PIN, INPUT);

  // Declare button as input
  pinMode(BUTTON_PIN, INPUT);

  // Setup Wifi Connection
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to wifi...");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println();
  Serial.print("connected: ");
  Serial.println(WiFi.localIP());

  // Setup MQQT
  client.setServer(MQTT_SERVER, 1883);
  client.setCallback(message_received);
  delay(500);
  Serial.println("Setup done");
  changeLEDColor(CRGB::Green);
}

void doWifiReconnect()
{
  changeLEDColor(CRGB::Red);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED)
  {
    Serial.print(".");
    delay(500);
  }
  changeLEDColor(CRGB::Green);
}

void loop() {
  static unsigned long timeSinceLastPairing = 0;

  if (WiFi.status() != WL_CONNECTED)
    doWifiReconnect();
  while (!client.connected())
  {
    Serial.println("Not connected");
    do_connect();
  }
  if (analogRead(BUTTON_PIN) > THRESHOLD) && millis() - timeSinceLastPairing > MIN_PAIRING_TIME)
  {
    client.publish(PAIRING_TOPIC, "PAIRING");
    timeSinceLastPairing = millis();
  }
  /*int PIR_value = digitalRead(DATA_PIR_PIN);
    if (PIR_value == HIGH)
    {
    Serial.print("PIR value: ");
    Serial.println(PIR_value);
    }*/
  //delay(250);
  client.loop();
}
