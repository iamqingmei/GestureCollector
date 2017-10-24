package ageha.gesturecollector.data;

import android.hardware.*;
import android.util.SparseArray;

import java.util.ArrayList;

/**
 * Created by Ageha on 8/10/17.
 */

public class SensorNames {
    public ArrayList<String> names;

    public SensorNames() {
        names = new ArrayList<>();
        names.add(0, "light sensor");
        names.add(1, "accelerometer");
        names.add(2, "magnetic field sensor");
        names.add(3, "gyroscope");
        names.add(4, "quaternion");
        names.add(5, "orientation");
        names.add(6, "any motion detector");
        names.add(7, "step counter");
        names.add(8, "step detector");
        names.add(9, "tilt sensor");
        names.add(10, "significant motion detector");
        names.add(11, "Gravity Sensor");
        names.add(12, "Linear Acceleration Sensor");


    }

    public String getName(int sensorId) {
        String name = names.get(sensorId);

        if (name == null) {
            name = "Unknown";
        }

        return name;
    }
}
