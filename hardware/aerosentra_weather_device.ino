#include <DHT.h>
#include <TinyGPS++.h>
#include <WiFi.h>
#include <WebServer.h>
#include <ESPmDNS.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

#define LED_PIN 2        // GPIO2 (D2)
#define TOUCH_PIN 4      // GPIO4 (D4)
#define DHT_PIN 15       // GPIO15 (D15)
#define MQ135_PIN 34     // GPIO34 (D34)
#define RAIN_PIN 35      // GPIO35 (D35)
#define GPS_RX 16        // GPIO16 (RX2)
#define GPS_TX 17        // GPIO17 (TX2)
#define LED_GOOD 25      // GPIO25 (D25)
#define LED_MODERATE 26  // GPIO26 (D26)
#define LED_POOR 27      // GPIO27 (D27)
#define BUZZER_PIN 33    // GPIO33 (D33)
#define DHTTYPE DHT11
#define WIFI_SSID "<your-wifi-SSID>"
#define WIFI_PASS "<your-wifi-password>"
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64



enum SystemState {
  BOOTING,
  SETTING_ENV,
  CALIBRATING,
  WAIT_FOR_TOUCH,
  READING_SENSORS,
  SHOW_RESULTS,
  ALERT_OUTPUT
};
SystemState currentState;
enum LocationSource {
  LOC_GPS,
  LOC_WIFI,
  LOC_NONE
};
LocationSource locSrc = LOC_NONE;
enum AlertLevel {
  SAFE,
  MODERATE,
  DANGER
};
AlertLevel currentAlert = SAFE;

struct EarthData {
  float temperature;
  float humidity;
  int aqi;
  float uvi;
  float wind;
  float pressure;
  bool isRaining;
  int rainIntensityVal;
  float coords[2];
  String aqiLvl;
};
EarthData earthData;

struct ApiResponse {
  int statusCode;
  String payload;
};

DHT dht(DHT_PIN, DHTTYPE);    // dht11 object
TinyGPSPlus gps;              // gps object
HardwareSerial gpsSerial(2);  // UART2
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);  // display screen
WebServer server(80);  // server instance


// server handlers and re-runners
ApiResponse callServer();
void setupRoutes();

// center content
void drawCenteredText(String text, int y, int size) {
  display.setTextSize(size);

  int16_t x1, y1;
  uint16_t w, h;

  display.getTextBounds(text, 0, 0, &x1, &y1, &w, &h);
  int x = (display.width() - w) / 2;
  display.setCursor(x, y);
  display.print(text);
}
// loader GIF
void drawLoader(int y) {
  static int frame = 0;
  String anim[] = { "[]", "[= ]", "[== ]", "[===]", "[ ==]", "[ =]" };
  drawCenteredText(anim[frame], y, 2);
  frame++;
  if (frame > 5) frame = 0;
}
// show GIF on display
void showState(String line1, String line2) {
  display.clearDisplay();

  display.setTextColor(WHITE);
  drawCenteredText(line1, 33, 1);
  drawCenteredText(line2, 45, 1);

  drawLoader(75);

  display.display();
}
// LED infinte blink
void blinkHeartBeat() {
  static unsigned long lastBlink = 0;
  static uint8_t ledState = 0;
  unsigned long now = millis();

  switch (ledState) {
    case 0:
      digitalWrite(LED_PIN, HIGH);
      lastBlink = now;
      ledState = 1;
      break;

    case 1:
      if (now - lastBlink >= 60) {
        digitalWrite(LED_PIN, LOW);
        lastBlink = now;
        ledState = 2;
      }
      break;

    case 2:
      if (now - lastBlink >= 120) {
        digitalWrite(LED_PIN, HIGH);
        lastBlink = now;
        ledState = 3;
      }
      break;

    case 3:
      if (now - lastBlink >= 60) {
        digitalWrite(LED_PIN, LOW);
        lastBlink = now;
        ledState = 4;
      }
      break;

    case 4:
      if (now - lastBlink >= 4500) ledState = 0;
      break;
  }
}
// sense alert level
AlertLevel evaluateAlert() {
  if (earthData.aqi >= 150 && earthData.temperature >= 40) return DANGER;
  if (earthData.aqi >= 80 || earthData.isRaining) return MODERATE;
  return SAFE;
}
// initializer for gpio pins and instances
void initSensors() {
  pinMode(LED_PIN, OUTPUT);
  pinMode(TOUCH_PIN, INPUT);
  pinMode(MQ135_PIN, INPUT);
  pinMode(RAIN_PIN, INPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(LED_GOOD, OUTPUT);
  pinMode(LED_MODERATE, OUTPUT);
  pinMode(LED_POOR, OUTPUT);

  dht.begin();
  gpsSerial.begin(9600, SERIAL_8N1, GPS_RX, GPS_TX);
}
// WiFi initializer
void initWiFi() {
  Serial.println("Connecting to WiFi 📶...");
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASS);

  unsigned long startAttempt = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - startAttempt < 15000) {
    Serial.print(".");
    delay(500);
  }
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWiFi connected ✅");
    Serial.print("IP: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\nWiFi not available ❌");
  }
}
void initOLED() {
  Wire.begin(21, 22);
  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println("📺OLED failed! ❌");
    return;
  }
  display.setRotation(3);  //portrait mode
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);
}
// alert beep utility
void beep(int duration = 150) {
  digitalWrite(BUZZER_PIN, HIGH);
  delay(duration);
  digitalWrite(BUZZER_PIN, LOW);
}
// alert blink LED utility
void blinkLED(int pin, int duration = 300) {
  digitalWrite(pin, HIGH);
  delay(duration);
  digitalWrite(pin, LOW);
}
// main alert driver
void alert(AlertLevel level) {
  switch (level) {
    case SAFE:
      beep(150);
      blinkLED(LED_GOOD, 300);
      break;

    case MODERATE:
      for (int i = 0; i < 2; i++) {
        beep(150);
        delay(300);
        beep(150);
        blinkLED(LED_MODERATE, 300);
        delay(1000);
      }
      break;

    case DANGER:
      for (int i = 0; i < 4; i++) {
        beep(150);
        delay(300);
        beep(150);
        blinkLED(LED_POOR, 300);
        delay(1000);
      }
      break;
  }
}
// read temp + humidity
void readDHT() {
  float t = dht.readTemperature();
  float h = dht.readHumidity();
  if (isnan(t) || isnan(h)) {
    Serial.println("DHT read failed! ❌");
    return;
  }
  earthData.temperature = t;
  earthData.humidity = h;
}
// read Air-Quality
void readMQ135() {
  int value = analogRead(MQ135_PIN);
  earthData.aqi = value;
}
// read rainfall
void readRain() {
  int rainValue = analogRead(RAIN_PIN);
  if (rainValue < 2500)
    earthData.isRaining = true;
  else
    earthData.isRaining = false;
  earthData.rainIntensityVal = rainValue;
}
// get device coordinates
bool gpsFix = false;
bool fetchGPSCoords() {
  if (gps.location.isValid()) {
    earthData.coords[0] = gps.location.lat();
    earthData.coords[1] = gps.location.lng();
    gpsFix = true;
    locSrc = LOC_GPS;
    return true;
  }
  return false;
}
bool fetchWiFiCoords() {
  if (WiFi.status() != WL_CONNECTED)
    return false;

  HTTPClient http;
  http.begin("http://ip-api.com/json");
  http.addHeader("Content-Type", "application/json");
  int httpResCode = http.GET();
  if (httpResCode <= 0) {
    Serial.println("WiFi geo request failed! ❌");
    http.end();
    return false;
  }
  String payload = http.getString();
  http.end();
  StaticJsonDocument<512> doc;
  DeserializationError error = deserializeJson(doc, payload);
  if (error) {
    Serial.println("JSON parse failed! ❌");
    return false;
  }
  earthData.coords[0] = doc["lat"];
  earthData.coords[1] = doc["lon"];
  gpsFix = false;
  locSrc = LOC_WIFI;
  return true;
}
void readGPS() {
  if (fetchGPSCoords()) {
    Serial.println("📡 GPS fix acquired");
    return;
  }
  if (fetchWiFiCoords()) {
    Serial.println("🌐 WiFi location used.");
    return;
  }
  locSrc = LOC_NONE;
  Serial.println("❌ Location not available!");
}
// main sensor reader caller
void readAllSensors() {
  Serial.println("Getting sensors...⌛");
  showState("Getting", "sensors...");
  delay(1500);
  readDHT();
  readMQ135();
  readRain();
  readGPS();
}
// serial result printer
void printResults() {
  Serial.println("\n===== EARTH DATA FOUND ✅=====\n");
  Serial.print("🌡️ Temp: ");
  Serial.print(earthData.temperature);
  Serial.println("°C");
  Serial.print("💧 Humidity: ");
  Serial.print(earthData.humidity);
  Serial.println("%");
  Serial.printf("🧭 Pressure: %.1fmb\n", earthData.pressure);
  Serial.printf("💨 Wind Speed: %.1fkm/h\n", earthData.wind);
  Serial.printf("☀️ UV Index: %.1f\n", earthData.uvi);
  Serial.print("🍃 AQI: ");
  if (earthData.aqi >= 150 && earthData.temperature >= 40) {
    Serial.print("POOR 🔴");
    earthData.aqiLvl = "Poor";
  } else if (earthData.aqi >= 80 || earthData.isRaining) {
    Serial.print("MOD 🟡");
    earthData.aqiLvl = "Avg";
  } else {
    Serial.print("GOOD 🟢");
    earthData.aqiLvl = "Good";
  }
  Serial.printf(" (%d)\n", earthData.aqi);
  Serial.print("⛈️ Rain analog value: ");
  Serial.println(earthData.rainIntensityVal);
  Serial.print("Rain status: ");
  if (earthData.isRaining)
    Serial.println("🌧️ Raining");
  else
    Serial.println("☀️ Not Raining");
  if (locSrc != LOC_NONE) {
    Serial.printf(" 📍Latitude: %.6f\n", earthData.coords[0]);
    Serial.printf(" 📍Longitude: %.6f\n", earthData.coords[1]);
  }
  Serial.println("\n===============================");
}
// display value printer
void drawDashboard() {
  display.clearDisplay();

  // ===== TEMPERATURE =====
  String temp = String((int)round(earthData.temperature)) + (char)247 + "C";
  drawCenteredText(temp, 25, 2);

  display.setTextSize(1);

  // ===== HUMIDITY =====
  display.setCursor(0, 65);
  display.print("HUM:");
  String hum = String((int)round(earthData.humidity)) + "%";
  display.print(hum);

  // ===== PRESSURE =====
  display.setCursor(0, 79);
  display.print("PRE:");
  String pressure = String((int)round(earthData.pressure)) + "mb";
  display.print(pressure);

  // ===== WIND SPEED =====
  display.setCursor(0, 93);
  display.print("WIND:");
  String windSpeed = String((int)round(earthData.wind)) + "kmh";
  display.print(windSpeed);

  // ===== AQI =====
  display.setCursor(0, 105);
  display.print("AQI: ");
  display.print(earthData.aqiLvl);

  // ===== UV =====
  display.setCursor(0, 119);
  display.print("UV: ");
  display.print(String((int)round(earthData.uvi)));

  display.display();
}
// display error printer
void drawErrorScreen(String message) {
  display.clearDisplay();

  drawCenteredText("ERROR!", 40, 2);
  drawCenteredText(message, 60, 1);
}

bool touched = false;
bool executeFSM = true;

void setup() {
  Serial.begin(115200);
  initSensors();
  initWiFi();
  initOLED();
  if (MDNS.begin("aerosentra")) Serial.println("mDNS started on-> http://aerosentra.local");
  setupRoutes();
  server.begin();
  currentState = BOOTING;
}
void loop() {
  while (gpsSerial.available())
    gps.encode(gpsSerial.read());
  blinkHeartBeat();

  if (executeFSM) {
    switch (currentState) {
      case BOOTING:
        delay(2500);
        Serial.println("Earth rover running 🚀");
        showState("Rover", "running...");
        delay(1000);
        currentState = SETTING_ENV;
        break;

      case SETTING_ENV:
        Serial.println("Setting environment 🌎");
        showState("Setting", "environment...");
        delay(1000);
        currentState = CALIBRATING;
        break;

      case CALIBRATING:
        Serial.println("Calibrating sensors 🔌");
        showState("Calibrating", "sensors...");
        delay(1500);
        currentState = WAIT_FOR_TOUCH;
        break;

      case WAIT_FOR_TOUCH:
        Serial.println("Touch to get earth data ☝🏻");
        showState("Touch to", "get data");
        executeFSM = false;
        break;

      case READING_SENSORS:
        readAllSensors();  // fills earthData
        currentAlert = evaluateAlert();
        currentState = SHOW_RESULTS;
        break;

      case SHOW_RESULTS:
        printResults();
        drawDashboard();
        currentState = ALERT_OUTPUT;
        break;

      case ALERT_OUTPUT:
        alert(currentAlert);
        executeFSM = false;
        break;
    }
  }

  if (digitalRead(TOUCH_PIN) == HIGH && !touched) {
    Serial.println("Touch detected ✔️");
    showState("Touch", "detected");
    currentState = READING_SENSORS;
    executeFSM = true;
    touched = true;
    delay(300);
  }

  server.handleClient();
}

// mDNS triggerer from frontend
void setupRoutes() {
  server.on("/trigger", HTTP_GET, []() {
    Serial.println("API trigger recieved 🌐");
    showState("API", "Triggered");
    
    readAllSensors();
    
    Serial.println("🌡️ Predicting weather... ❄️");
    showState("Predicting", "weather");
        
    ApiResponse apiRes = callServer();
        
    server.send(apiRes.statusCode, "application/json", apiRes.payload);
    
    if (apiRes.statusCode == 200) {
      printResults();
      drawDashboard();
      currentAlert = evaluateAlert();
      alert(currentAlert);
    } else {
      drawErrorScreen("Try again...");
      currentAlert = DANGER;
      alert(currentAlert);
    }
  });
}
// API data exchange
ApiResponse callServer() {    
  Serial.println("API CALL START");
  // =========== SERVER CALL =============
  ApiResponse res;
  StaticJsonDocument<512> reqDoc; 
  StaticJsonDocument<1024> resDoc; 
  
  reqDoc["temperature"] = earthData.temperature;
  reqDoc["humidity"] = earthData.humidity;
  reqDoc["aqi"] = earthData.aqi;
  reqDoc["lat"] = earthData.coords[0];
  reqDoc["lon"] = earthData.coords[1];

  HTTPClient http;
  http.begin("http://<your-server-IP>/api/predict");
  http.addHeader("Content-Type", "application/json");
  http.setTimeout(15000);
  http.useHTTP10(true);
  
  String body;
  serializeJson(reqDoc, body);
  
  int code = http.POST(body);
  
  String response = "{}";
  
  if (code == 200) {
    response = http.getString();
  
    DeserializationError error = deserializeJson(resDoc, response);

    if (!error) {
      earthData.wind = resDoc["data"]["api_data"]["wind"].as<float>();
      earthData.pressure = resDoc["data"]["api_data"]["pressure"].as<float>();
      earthData.uvi = resDoc["data"]["api_data"]["uv_index"].as<float>();
    } else Serial.println("JSON parse failed ❌");
  }
  else response = "{\"error\":\"SERVER NOT REACHABLE!\"}";
  
  Serial.println("Status code: " + String(code));
  // Serial.println(response);

  res.statusCode = code; 
  res.payload = response;

  http.end();
  Serial.println("API CALL END");

  return res;
}
