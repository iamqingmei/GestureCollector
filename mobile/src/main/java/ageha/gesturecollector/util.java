package ageha.gesturecollector;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * Created by Ageha on 8/10/17.
 */

public class util {
    private static String file_name = null;

    public static void set_filename(String s){
        file_name = s;
    }

    public static String get_filename(){
        return file_name;
    }
    public static void warning_msg(Context context){
        Log.w("main_activity", "warning_msg function");
        CharSequence text = "Please input the tester information!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public static void warning_msg(Context context, String tex){
        Log.w("main_activity", "warning_msg function");
//        CharSequence text = "Please input the tester information!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, tex, duration);
        toast.show();
    }



    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
}
