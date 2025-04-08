package br.com.personadev.arduino_demo.domain.model;


public class SensorReading {
    private String sensor;
    private double value;
    private String unit;
    private String status;

    public SensorReading(String sensor, double value, String unit, String status) {
        this.sensor = sensor;
        this.value = value;
        this.unit = unit;
        this.status = status;
    }

    @Override
    public String toString() {
        return "SensorReading{" +
                "sensor='" + sensor + '\'' +
                ", value=" + value +
                ", unit='" + unit + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public String getSensor() {
        return sensor;
    }
    public double getValue() {
        return value;
    }
    public String getUnit() {
        return unit;
    }
    public String getStatus() {
        return status;
    }
}
