package com.example.recipe;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddRecipe extends AppCompatActivity {

    private FloatingActionButton cameraBtn;
    private FloatingActionButton galleryBtn;
    private LinearLayout uploadImgBtnContainer;
    private ImageView recipeImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        cameraBtn = findViewById(R.id.cameraBtn);
        galleryBtn = findViewById(R.id.galleryBtn);
        recipeImg = findViewById((R.id.recipeImg));
        uploadImgBtnContainer = findViewById(R.id.uploadImgBtnContainer);

        getSupportActionBar().setTitle("Add Recipe");

        cameraBtn.setOnClickListener(this::onCameraBtnClick);
        galleryBtn.setOnClickListener(this::onGalleryBtnClick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            recipeImg.setImageURI(data.getData());
            LayoutParams params = (LayoutParams) uploadImgBtnContainer.getLayoutParams();
            params.topMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show();
        }
    }

    private void onCameraBtnClick(View view) {
        invokeImagePicker(view, true);
    }

    private void onGalleryBtnClick(View view) {
        invokeImagePicker(view, false);
    }

    private void invokeImagePicker(View view, Boolean cameraOnly) {
        ImagePicker.Builder picker = ImagePicker.with(this);

        if (cameraOnly) {
            picker.cameraOnly();
        } else {
            picker.galleryOnly();
        }

        picker.crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start();
    }
}