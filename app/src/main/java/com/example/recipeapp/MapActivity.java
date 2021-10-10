package com.example.recipeapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import javax.annotation.Nullable;

public class MapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
    }
}
