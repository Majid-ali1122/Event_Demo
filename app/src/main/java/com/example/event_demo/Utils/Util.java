package com.example.event_demo.Utils;

import android.content.Context;
import android.widget.Toast;

import com.airbnb.lottie.animation.content.Content;

import java.text.DateFormatSymbols;

public class Util {
    public static void toastMsg(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    public static String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month - 1];
    }
}
