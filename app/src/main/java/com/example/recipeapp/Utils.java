package com.example.recipeapp;

import android.util.Log;

import java.util.concurrent.Callable;

public class Utils {
    public final static String[] allTypes = {
            "Breakfast", "Lunch", "Dinner", "Snack", "Any"
    };

    public final static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
