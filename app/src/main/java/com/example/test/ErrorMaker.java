package com.example.test;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ErrorMaker {
    public static boolean if_error (Context context, String newUrl) {
        if (newUrl.equals("500")) {
            make_error(context, Integer.parseInt(newUrl));
            return true;
        }
        else return false;
    }

    static void make_error(Context context, int error) {
        Toast toast = Toast.makeText(context,
                "Извините, трек не найден!", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
