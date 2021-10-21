package com.example.recipeapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener{
//    TextView
    EditText email, password;
    Button register;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        email = findViewById(R.id.inputEmailRegister);
        password = findViewById(R.id.inputPasswordRegister);
        register = findViewById(R.id.btnLoginRegister);

        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String input_email = email.getText().toString();
        String input_password = password.getText().toString();
        if (TextUtils.isEmpty(input_email) || TextUtils.isEmpty(input_password)){
            Toast.makeText(SignupActivity.this,"email/password cannot be empty", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(input_password) && TextUtils.isEmpty(input_email)){
            Toast.makeText(SignupActivity.this,"password and email cannot be empty", Toast.LENGTH_LONG).show();
        }
        else if (!(TextUtils.isEmpty(input_password) && TextUtils.isEmpty(input_email))) {
            Toast.makeText(SignupActivity.this, "Register successfully", Toast.LENGTH_LONG).show();
        }
    }
}
