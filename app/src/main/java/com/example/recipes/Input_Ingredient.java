package com.example.recipes;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class Input_Ingredient extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input__ingredient);

        getIntent().getExtras();

        Bundle bundle = getIntent().getExtras();

        if (bundle != null){
            if(bundle.getString("some") != null){
                Toast.makeText(getApplicationContext(),"data:" + bundle.getString("some"),Toast.LENGTH_SHORT).show();
            }
        }

        Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(Input_Ingredient.this,
                android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.names));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(myAdapter);
    }
}