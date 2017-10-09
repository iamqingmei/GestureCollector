package ageha.gesturecollector.event;

/**
 * Created by Ageha on 9/10/17.
 */
import ageha.gesturecollector.data.Sensor;
import ageha.gesturecollector.data.SensorDataPoint;

public class SensorUpdatedEvent {
    private Sensor sensor;
    private SensorDataPoint sensorDataPoint;

    public SensorUpdatedEvent(Sensor sensor, SensorDataPoint sensorDataPoint) {
        this.sensor = sensor;
        this.sensorDataPoint = sensorDataPoint;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public SensorDataPoint getDataPoint() {
        return sensorDataPoint;
    }
}
