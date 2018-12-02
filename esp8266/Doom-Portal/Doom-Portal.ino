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
#define BUTTON_PIN        D5
#define DATA_PIR_PIN      D6

#define MIN_PAIRING_TIME  10000

// Published topics
#define P_PAIRING_TOPIC   "doom_portal/await_pairing"
#define P_UNLOCK_TOPIC    "doom_portal/unlock_door"

// Subscribed topics
#define S_UNLOCK_TOPIC    "doom_portal/unlock_response"
#define S_PAIRING_TOPIC   "doom_portal/result_pairing"

WiFiClient espClient;
PubSubClient client(espClient);
CRGB leds[NUM_LEDS];

bool isUnlocking = false;
bool isPairing = false;

void unlockDoorTopicManager(char data[])
{
  Serial.print("UNLOCK_RESPONSE: ");
  Serial.println(data);
  FastLED.clear();
  isUnlocking = false;
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

void pairingTopicManager()
{
  unsigned long start_time = millis();
  while (millis() - start_time <= 1000)
  {
    changeLEDColor(CRGB::White);
    delay(200);
    changeLEDColor(CRGB::Green);
    delay(200);
  }
  isPairing = false;
}

void message_received(char* topic, byte* payload, unsigned int length)
{
  Serial.println("-----------------------");
  Serial.print("Message arrived in topic: ");
  Serial.println(topic);


  char data[length + 1];
  Serial.print("Message:");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
    data[i] = payload[i];
  }
  data[length] = '\0';
  Serial.println();

  if (strcmp(topic, S_UNLOCK_TOPIC) == 0)
    unlockDoorTopicManager(data);
  else if (strcmp(topic, S_PAIRING_TOPIC) == 0)
    pairingTopicManager();
  Serial.println("-----------------------");
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

void do_connect()
{
  // Create a random client ID
  String clientId = "ESP8266Client-";
  clientId += String(random(0xffff), HEX);
  Serial.println("Connecting to MQTT...");
  if (client.connect(clientId.c_str(), MQTT_USER, MQTT_PASSWORD))
  {
    Serial.println("connected");
    client.subscribe(S_UNLOCK_TOPIC);
    client.subscribe(S_PAIRING_TOPIC);
  }
  else
  {
    Serial.print(client.state());
    Serial.print("failed with state ");
    Serial.print(client.state());
    delay(2000);
  }
}

void loop() {
  static unsigned long timeSinceLastPairing = 0;
  static unsigned long timeSinceLastUnlocking = 0;

  if (WiFi.status() != WL_CONNECTED)
    doWifiReconnect();
  if (!client.connected())
  {
    changeLEDColor(CRGB::Red);
    while (!client.connected())
    {
      Serial.println("Not connected");
      do_connect();
    }
    changeLEDColor(CRGB::Green);
  }
  if (isPairing == false && digitalRead(BUTTON_PIN) == LOW)
  {
    isPairing = true;
    client.publish(P_PAIRING_TOPIC, "PAIRING");
    timeSinceLastPairing = millis();
  }
  else if (isPairing == true)
  {
    unsigned long currentTime = millis();
    if (currentTime - timeSinceLastPairing < MIN_PAIRING_TIME)
    {
      static unsigned long timeSinceLastBlink = 0;
      if (currentTime - timeSinceLastBlink > 1500 || timeSinceLastBlink == 0)
      {
        timeSinceLastBlink = currentTime;
        changeLEDColor(CRGB::White);
        delay(500);
      }
    }
    else
    {
      isPairing = false;
      changeLEDColor(CRGB::Red);
      delay(1000);
    }
    changeLEDColor(CRGB::Green);
  }
  if (!isUnlocking && digitalRead(DATA_PIR_PIN) == 1 && millis() - timeSinceLastUnlocking > 5000)
  {
    Serial.println("Unlocking...");
    client.publish(P_UNLOCK_TOPIC, "UNLOCK");
    isUnlocking = true;
    timeSinceLastUnlocking = millis();
  }
  else if (isUnlocking)
  {
    static unsigned long timeSinceLastBlinkUnlocking = 0;
    unsigned long currentTime = millis();
    if (currentTime - timeSinceLastBlinkUnlocking > 1500 || timeSinceLastBlinkUnlocking == 0)
    {
      timeSinceLastBlinkUnlocking = currentTime;
      changeLEDColor(CRGB::Yellow);
      delay(500);
    }
  }
  client.loop();
}
