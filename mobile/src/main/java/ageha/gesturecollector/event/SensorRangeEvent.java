package ageha.gesturecollector.event;

/**
 * Created by Ageha on 8/10/17.
 */
import ageha.gesturecollector.data.Sensor;

public class SensorRangeEvent {
    private Sensor sensor;

    public SensorRangeEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
