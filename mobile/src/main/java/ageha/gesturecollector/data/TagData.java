package ageha.gesturecollector.data;

import java.sql.Timestamp;

public class TagData {
    private String tagName;
    private Timestamp timestamp;
    private String name;
    private int age;
    private int height;
    private String gender;
    private int weight;

    public TagData(String pTagName, Timestamp pTimestamp, String name, int age, int height, String gender, int weight) {
        tagName = pTagName;
        timestamp = pTimestamp;
        this.name = name;
        this.age = age;
        this.height = height;
        this.gender = gender;
        this.weight = weight;
    }

    public String getTagName() {
        return tagName;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public int getAge(){
        return age;
    }

    public int getHeight() {
        return height;
    }

    public String getGender(){
        return gender;
    }

    public int getWeight() { return weight; }
}
