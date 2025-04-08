// Arduino - Sensor DHT - Umidade e Temperatura
// https://blog.eletrogate.com/
// Adafruit Unified Sensor Library: https://github.com/adafruit/Adafruit_Sensor

#include <Arduino.h>                              // Biblioteca Arduino
#include <Adafruit_Sensor.h>                      // Biblioteca DHT Sensor Adafruit 
#include <DHT.h>
#include <DHT_U.h>
#include <ArduinoJson.h>                          

#define ledPin 10                                 // LED conectado ao pino digital 10
#define AnalogLDR A0                              // LDR conectado ao pino analógico A0

#define DHTTYPE      DHT22                        // Sensor DHT22 ou AM2302
#define DHTPIN 5                                  // Pino do Arduino conectado no Sensor(Data) 

JsonDocument doc;
DHT_Unified dht(DHTPIN, DHTTYPE);                 // configurando o Sensor DHT - pino e tipo
uint32_t delayMS;                                 // variável para o atraso entre as leituras
int ledState = LOW;                               // variável para o estado do LED


void setup()
{
  
  Serial.begin(9600);                             // monitor serial 9600 bps
  pinMode(ledPin, OUTPUT);                        
  Serial.println("DEBUG: Iniciando... \n\n"); 
                  // imprime no monitor serial
  dht.begin();                                    // inicializa a função
  sensor_t sensor;
  dht.temperature().getSensor(&sensor);           // imprime os detalhes do Sensor de Temperatura
  dht.humidity().getSensor(&sensor);              // imprime os detalhes do Sensor de Umidade

  delayMS = sensor.min_delay / 1000;              // define o atraso entre as leituras
  // concatenate debug message
  Serial.print("DEBUG: Atraso entre leituras: ");
  Serial.print(delayMS);
  Serial.println(" milissegundos \n\n");
}

void populateReadings(JsonArray &readings) {
  // Temperature reading
  sensors_event_t tempEvent;
  dht.temperature().getEvent(&tempEvent);
  JsonObject tempReading = readings.add<JsonObject>();
  tempReading["type"] = "sensor";
  tempReading["name"] = "DHT22";
  if (isnan(tempEvent.temperature)) {
    tempReading["status"] = "error";
    tempReading["errorMessage"] = "Erro na leitura da Temperatura!";
  } else {
    tempReading["value"] = tempEvent.temperature;
    tempReading["unit"] = "Celsius";
    tempReading["status"] = "ok";
  }

  // Humidity reading
  sensors_event_t humidityEvent;
  dht.humidity().getEvent(&humidityEvent);
  JsonObject humidityReading = readings.add<JsonObject>();
  humidityReading["type"] = "sensor";
  humidityReading["name"] = "DHT22";
  if (isnan(humidityEvent.relative_humidity)) {
    humidityReading["status"] = "error";
    humidityReading["errorMessage"] = "Erro na leitura da Umidade!";
  } else {
    humidityReading["value"] = humidityEvent.relative_humidity;
    humidityReading["unit"] = "percent";
    humidityReading["status"] = "ok";
  }

  // LDR reading
  int ldrValue = analogRead(AnalogLDR);
  JsonObject ldrReading = readings.add<JsonObject>();
  ldrReading["type"] = "sensor";
  ldrReading["name"] = "LDR";
  if (ldrValue < 0 || ldrValue > 1023) {
    ldrReading["status"] = "error";
    ldrReading["errorMessage"] = "Erro na leitura do LDR!";
  } else {
    ldrReading["value"] = ldrValue;
    ldrReading["unit"] = "lux";
    ldrReading["status"] = "ok";
  }

  JsonObject actuator = readings.add<JsonObject>();
  actuator["type"] = "actuator";
  actuator["name"] = "LED 1";
  actuator["status"] = "ok";
  actuator["unit"] = "boolean";
  actuator["previousValue"] = ledState; // Read LED status


  // Handle LED logic based on LDR value
  if (ldrValue > 600) {
    digitalWrite(ledPin, HIGH); // liga o LED
    actuator["value"] = HIGH;
    ledState = HIGH; // Update LED state
  } else {
    digitalWrite(ledPin, LOW); // desliga o LED
    actuator["value"] = LOW;
    ledState = LOW; // Update LED state
  }
}

void loop() {
  delay(delayMS);

  doc["type"] = "reading";
  doc["id"] = millis();
  doc["timestamp"] = millis() / 1000; // timestamp em segundos

  JsonArray readings = doc["readings"].to<JsonArray>();
  readings.clear(); // Clear previous readings
  populateReadings(readings);

  // Serialize and print the JSON document (optional)
  serializeJson(doc, Serial);
  Serial.println("\n\n");
}