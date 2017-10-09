package ageha.gesturecollector.event;

/**
 * Created by Ageha on 9/10/17.
 */
import ageha.gesturecollector.data.Sensor;

public class NewSensorEvent {
    private Sensor sensor;

    public NewSensorEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
