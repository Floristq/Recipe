package com.example.recipeapp;

public class Utils {
    public final static String[] allTypes = {
            "Breakfast", "Lunch", "Dinner", "Snack", "Any"
    };

    public final static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
