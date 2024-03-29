package ageha.gesturecollector.data;

import java.sql.Timestamp;
import java.util.Arrays;

/**
 * Created by Ageha on 8/10/17.
 */

public class SensorDataPoint {
    private Timestamp timestamp;
    private float[] values;
    private int accuracy;

    public SensorDataPoint(Timestamp timestamp, int accuracy, float[] values) {
        this.timestamp = timestamp;
        this.accuracy = accuracy;
        this.values = values;
    }

    public float[] getValues() {
        return values;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public String toString(){
        String temp = "";
        for (int i = 0; i < 5; i++) {
            if (i < values.length) {
                temp += ", " +String.valueOf(values[i]);

            } else {
                temp += ", ";
            }
        }
        return (timestamp.toString() + ", " + accuracy + temp);
    }
}
