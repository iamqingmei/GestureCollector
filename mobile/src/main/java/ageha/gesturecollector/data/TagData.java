package ageha.gesturecollector.data;

import java.sql.Timestamp;

public class TagData {
    private String tagName;
    private Timestamp timestamp;
    private String name;
    private int age;
    private int height;
    private String gender;

    public TagData(String pTagName, Timestamp pTimestamp, String name, int age, int height, String gender) {
        tagName = pTagName;
        timestamp = pTimestamp;
        this.name = name;
        this.age = age;
        this.height = height;
        this.gender = gender;
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
}
