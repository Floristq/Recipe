package com.example.recipeapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.recipeapp.autocompleteadapter.AdapterItem;
import com.example.recipeapp.autocompleteadapter.AutoCompleteAdapter;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddRecipe#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class AddRecipe extends Fragment {

    private View root = null;

    private FloatingActionButton cameraBtn;
    private FloatingActionButton galleryBtn;
    private LinearLayout uploadImgBtnContainer;
    private ImageView recipeImg;
    private MultiAutoCompleteTextView recipeTagInput;
    private Button submitBtn;
    private Spinner cuisineSpinner;

    private AutoCompleteAdapter recipeTagsAdapter;
    private Uri imageUri = null;

    private final String SELECT_CUISINE_HINT = "Select Cuisine";
    // TODO
    // Remove temporaryTags and use server/firebase retrieved tags
    final private String[] temporaryTags = {
            "frost", "lunch", "breakfast", "dinner", "snacks", "bakery", "healthy",
            "chef", "delicious", "cooking", "dessert", "yummy", "vegan", "glutenfree", "tasty",
            "fitness", "mexican", "american", "chinese", "indian"
    };

    // TODO
    // Remove temporaryCuisines and use server/firebase retrieved tags
    final private String[] temporaryCuisines = {
            "Chinese", "English", "Indian", "French",
            "American", "Japanese", "Mexican"
    };

    public static AddRecipe newInstance() {
        return new AddRecipe();
    }

    public AddRecipe() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_add_recipe, container, false);

        // Initializing components / elements
        cameraBtn = root.findViewById(R.id.cameraBtn);
        galleryBtn = root.findViewById(R.id.galleryBtn);
        recipeImg = root.findViewById((R.id.recipeImg));
        uploadImgBtnContainer = root.findViewById(R.id.uploadImgBtnContainer);
        recipeTagInput = root.findViewById(R.id.recipeTagInput);
        submitBtn = root.findViewById(R.id.submitBtn);
        cuisineSpinner = root.findViewById(R.id.cuisine);

        List<String> cuisines =  new ArrayList<String>();
        cuisines.add(SELECT_CUISINE_HINT);
        for (String cuisine: temporaryCuisines) {
            cuisines.add(cuisine);
        }

        ArrayAdapter<String> cuisineAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                cuisines
        );

        cuisineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuisineSpinner.setAdapter(cuisineAdapter);

        // Configuring recipeTagsInput
        // TODO
        // Temporarily using the temporaryTags to populate tags-input until
        // server / firebase is set up
        List<AdapterItem> tagAdapterList = new ArrayList<>();
        for (String tag: temporaryTags) {
            tagAdapterList.add(new AdapterItem(tag, 0));
        }

        recipeTagsAdapter = new AutoCompleteAdapter(getActivity(), tagAdapterList);
        recipeTagInput.setAdapter(recipeTagsAdapter);
        recipeTagInput.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());


        // Attaching events
        cameraBtn.setOnClickListener(this::onCameraBtnClick);
        galleryBtn.setOnClickListener(this::onGalleryBtnClick);
        submitBtn.setOnClickListener(this::onSubmit);

        return root;
    }

    // Handle submission
    private void onSubmit(View view) {
        String name = ((EditText) root.findViewById(R.id.recipeName)).getText().toString();
        String instructions = ((EditText) root.findViewById(R.id.instructions)).getText().toString();
        // We already have imageUri
        String tags = recipeTagInput.getText().toString();

        if (name.toString() == "" || instructions.toString() == "" || imageUri == null || tags.toString() == "") {
            // TODO
            // Let the user know which fields to fill specifically
            Toast.makeText(getActivity(), "Please fill in all the fields!", Toast.LENGTH_LONG).show();
        } else {
            // TODO
            // Post data to firebase
        }
    }

    // TODO
    // Document functions and their parameters
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // Loading image on the interface
            imageUri = data.getData();
            recipeImg.setImageURI(imageUri);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) uploadImgBtnContainer.getLayoutParams();
            // TODO
            // Make it hidden at the beginning, then visible after image gets uploaded!
            params.topMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            // Informing user about error(s)
            Toast.makeText(getActivity(), ImagePicker.getError(data), Toast.LENGTH_LONG).show();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        root = null;
    }
}