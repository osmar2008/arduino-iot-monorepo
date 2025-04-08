package br.com.personadev.arduino_demo.domain.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DeviceMessage {
    private String type;
    private String id;
    private String device;
    private Instant timestamp;
    private List<SensorReading> readings;
    private String status;

    private DeviceMessage() {
    }

    public static DeviceMessage from(String type, String id, String device, Instant timestamp,
            List<SensorReading> readings, String status) {
        DeviceMessage message = new DeviceMessage();
        message.type = type;
        message.id = id;
        message.device = device;
        message.timestamp = timestamp;
        message.readings = readings;
        message.status = status;
        return message;
    }

    public static DeviceMessage fromJson(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        String type = jsonObject.get("type").getAsString();
        String id = jsonObject.get("id").getAsString();
        Instant timestamp = Instant.ofEpochSecond(jsonObject.get("timestamp").getAsLong());

        List<SensorReading> readings = new ArrayList<>();
        JsonArray readingsArray = jsonObject.getAsJsonArray("readings");
        for (JsonElement element : readingsArray) {
            JsonObject readingObject = element.getAsJsonObject();
            String sensor = readingObject.get("name").getAsString();
            double value = readingObject.has("value") ? readingObject.get("value").getAsDouble() : Double.NaN;
            String unit = readingObject.has("unit") ? readingObject.get("unit").getAsString() : null;
            String status = readingObject.get("status").getAsString();
            readings.add(new SensorReading(sensor, value, unit, status));
        }

        String status = jsonObject.has("status") ? jsonObject.get("status").getAsString() : "unknown";

        return DeviceMessage.from(type, id, "Arduino", timestamp, readings, status);
    }

    @Override
    public String toString() {
        return "DeviceMessage{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", device='" + device + '\'' +
                ", timestamp=" + timestamp +
                ", readings=" + readings.stream().map(SensorReading::toString).reduce((a, b) -> a + ", " + b).orElse("")
                +
                ", status='" + status + '\'' +
                '}';
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getDevice() {
        return device;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public List<SensorReading> getReadings() {
        return readings;
    }

    public String getStatus() {
        return status;
    }
}
