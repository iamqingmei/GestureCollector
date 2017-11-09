package ageha.gesturecollector;

import android.content.Context;

import java.sql.Timestamp;
import java.util.LinkedList;

import ageha.gesturecollector.data.TagData;
import ageha.gesturecollector.event.*;

public class TagManager {
    private static final String TAG = "TagManager";
    private boolean is_recording = false;
//    private static final int CLIENT_CONNECTION_TIMEOUT = 15000;

    private static TagManager instance;

    private LinkedList<TagData> tags = new LinkedList<>();



    public static synchronized TagManager getInstance(Context context) {
        if (instance == null) {
            instance = new TagManager(context.getApplicationContext());
        }

        return instance;
    }

    //private constructor.
    private TagManager(Context context) {
    }

    public LinkedList<TagData> getTags() {
        return (LinkedList<TagData>) tags.clone();
    }

    public void DeleteTags(){
        this.tags = new LinkedList<>();
    }

    synchronized void addTag(String pTagName, String name, int age, int height, String gender, int weight, char leftOrRight) {
        if (pTagName.equals("wear_start")){
            this.is_recording = true;
        }
        if (pTagName.equals("wear_stop")){
            this.is_recording = false;
        }

        if (!is_recording){
            return;
        }
        TagData tag = new TagData(pTagName, new Timestamp(System.currentTimeMillis()), name, age, height, gender, weight, leftOrRight);
        this.tags.add(tag);


        BusProvider.postOnMainThread(new TagAddedEvent(tag));
    }

    public boolean if_recording(){
        return is_recording;
    }

}

