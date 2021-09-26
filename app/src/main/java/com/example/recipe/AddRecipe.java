package com.example.recipe;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.example.recipe.autocompleteadapter.*;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AddRecipe extends AppCompatActivity {

    private FloatingActionButton cameraBtn;
    private FloatingActionButton galleryBtn;
    private LinearLayout uploadImgBtnContainer;
    private ImageView recipeImg;
    private MultiAutoCompleteTextView recipeTagInput;

    private AutoCompleteAdapter recipeTagsAdapter;

    // TODO
    // Remove temporaryTags and use server/firebase retrieved tags
    final private String[] temporaryTags = {
            "frost", "lunch", "breakfast", "dinner", "snacks", "bakery", "healthy",
            "chef", "delicious", "cooking", "dessert", "yummy", "vegan", "glutenfree", "tasty",
            "fitness", "mexican", "american", "chinese", "indian"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        // Initializing components / elements
        cameraBtn = findViewById(R.id.cameraBtn);
        galleryBtn = findViewById(R.id.galleryBtn);
        recipeImg = findViewById((R.id.recipeImg));
        uploadImgBtnContainer = findViewById(R.id.uploadImgBtnContainer);
        recipeTagInput = findViewById(R.id.recipeTagInput);

        // Setting page title
        getSupportActionBar().setTitle("Add Recipe");

        // Configuring recipeTagsInput
        // TODO
        // Temporarily using the temporaryTags to populate tags-input until
        // server / firebase is set up
        List<AdapterItem> tagAdapterList = new ArrayList<>();
        for (String tag: temporaryTags) {
            tagAdapterList.add(new AdapterItem(tag, 0));
        }

        recipeTagsAdapter = new AutoCompleteAdapter(this, tagAdapterList);
        recipeTagInput.setAdapter(recipeTagsAdapter);
        recipeTagInput.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());


        // Attaching events
        cameraBtn.setOnClickListener(this::onCameraBtnClick);
        galleryBtn.setOnClickListener(this::onGalleryBtnClick);
    }

    // TODO
    // Document functions and their parameters
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // Loading image on the interface
            recipeImg.setImageURI(data.getData());
            LayoutParams params = (LayoutParams) uploadImgBtnContainer.getLayoutParams();
            params.topMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            // Informing user about error(s)
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