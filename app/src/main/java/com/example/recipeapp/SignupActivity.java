package com.example.recipeapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity{
//    TextView
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    EditText mEmail,mPassword, mName;
    Button mRegister;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mEmail = findViewById(R.id.inputEmailRegister);
        mName = findViewById(R.id.inputNameRegister);
        mPassword = findViewById(R.id.inputPasswordRegister);
        mPassword.setTransformationMethod(new PasswordTransformationMethod());
        mRegister = findViewById(R.id.btnLoginRegister);
        fAuth = FirebaseAuth.getInstance();

//        if (fAuth.getCurrentUser() != null) {
//            startActivity(new Intent(getApplicationContext(), MainActivity.class));
//            finish();
//        }

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String name = mName.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is required!");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is required!");
                    return;
                }

                if(TextUtils.isEmpty(name)){
                    mPassword.setError("Name is required!");
                    return;
                }

                if(password.length() < 8){
                    mPassword.setError("Password must be longer than 8 characters");
                    return;
                }

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            User user = new User(name,email);

                            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {

                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .setPhotoUri(Uri.parse("https://cdn.icon-icons.com/icons2/2468/PNG/512/user_kids_avatar_user_profile_icon_149314.png"))
                                                .build();
                                        user.updateProfile(profileUpdates);

                                        Toast.makeText(SignupActivity.this, "User Created!", Toast.LENGTH_LONG).show();

                                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                        finish();
                                    }
                                    else {
                                        Toast.makeText(SignupActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_LONG).show();

                                    }
                                }
                            });
                        }
                        else {
                            Toast.makeText(SignupActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
    }

}
