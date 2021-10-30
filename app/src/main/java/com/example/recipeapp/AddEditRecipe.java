package com.example.recipeapp;

import static java.util.Objects.*;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.recipeapp.autocompleteadapter.AutoCompleteAdapter;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddEditRecipe#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class AddEditRecipe extends Fragment {

    private View root = null;

    private String recipeId;

    // TODO
    // Refactor & create common source to use the database
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final CollectionReference recipeCollectionRef = db.collection("recipes");
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();

    private FloatingActionButton cameraBtn;
    private FloatingActionButton galleryBtn;
    private LinearLayout uploadImgBtnContainer;
    private ImageView recipeImg;
    private Button submitBtn;
    private ChipGroup typeContainer;
    private AutoCompleteTextView ingredientsInput;
    private AutoCompleteAdapter ingredientAdapter;
    private AutoCompleteAdapter cuisineAdapter;
    private AutoCompleteAdapter tagAdapter;
    private LinearLayout recipeForm;
    private ProgressBar dataLoadingBar;

    private EditText recipeNameInput;
    private EditText instructionInput;

    Snackbar loadingSnackbar = null;

    private Uri imageUri = null;

    private boolean uploading = false;

    // TODO
    // The basic tags to look from, user can also add their own
    final private String[] basicTags = {
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

    // TODO
    // Get a better way than saving the following one
    // to identify server received image url
    private String serverReceivedImageUrl;

    public static AddEditRecipe newInstance() {
        return new AddEditRecipe();
    }

    public AddEditRecipe() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_add_edit_recipe, container, false);
        FragmentActivity activity = getActivity();

        // Initializing components / elements
        cameraBtn = root.findViewById(R.id.cameraBtn);
        galleryBtn = root.findViewById(R.id.galleryBtn);
        recipeImg = root.findViewById((R.id.recipeImg));
        uploadImgBtnContainer = root.findViewById(R.id.uploadImgBtnContainer);
        submitBtn = root.findViewById(R.id.submitBtn);
        typeContainer = root.findViewById(R.id.typeContainer);
        recipeForm = root.findViewById(R.id.recipeForm);
        dataLoadingBar = root.findViewById(R.id.dataLoadingBar);
        ingredientsInput = root.findViewById(R.id.ingredientsInput);

        recipeNameInput = root.findViewById(R.id.recipeName);
        instructionInput = root.findViewById(R.id.instructions);

        for (String type: Utils.allTypes) {
            Chip chip = new Chip(activity);
            chip.setText(type);
            chip.setId(root.generateViewId());
            chip.setCheckable(true);

            typeContainer.addView(chip);
        }

        ingredientAdapter = new AutoCompleteAdapter(
                activity,
                ingredientsInput,
                root.findViewById(R.id.ingredientsContainer)
        );
        ingredientAdapter.setData(new String[]{});

        cuisineAdapter = new AutoCompleteAdapter(
                activity,
                root.findViewById(R.id.cuisinesInput),
                root.findViewById(R.id.cuisinesContainer)
        );
        cuisineAdapter.setAllowMultipleSelection(false);
        cuisineAdapter.setData(temporaryCuisines);

        tagAdapter = new AutoCompleteAdapter(
                activity,
                root.findViewById(R.id.tagsInput),
                root.findViewById(R.id.tagsContainer)
        );
        tagAdapter.setCustomCreationEnabled(true);
        tagAdapter.setData(basicTags);

        // Attaching events
        cameraBtn.setOnClickListener(this::onCameraBtnClick);
        galleryBtn.setOnClickListener(this::onGalleryBtnClick);
        submitBtn.setOnClickListener(this::onSubmit);
        root.findViewById(R.id.addIngredient).setOnClickListener(this::onAddIngredient);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            if (bundle.containsKey("id")) {
                loadData(bundle.getString("id"));
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Edit Recipe");
            }
        }

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadData(String id) {
        dataLoadingBar.setVisibility(View.VISIBLE);
        recipeForm.setVisibility(View.GONE);

        recipeCollectionRef.document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            recipeId = id; // Confirming the recipe ID

                            Map<String, Object> data = document.getData();

                            // Populating the fields!
                            recipeNameInput.setText(String.valueOf(data.get("Name")));
                            if (data.containsKey("Cuisine")) {
                                cuisineAdapter.addSelectedData(String.valueOf(data.get("Cuisine")));
                            }

                            String type = (String) data.get("Type");
                            int types = typeContainer.getChildCount();
                            while (types > 0) {
                                Chip typeChip = (Chip) typeContainer.getChildAt(types - 1);
                                if (typeChip.getText().toString().equalsIgnoreCase(type)) {
                                    typeChip.setChecked(true);
                                    break;
                                }
                                types -= 1;
                            }

                            serverReceivedImageUrl = String.valueOf(data.get("Image"));
                            recipeImg.setVisibility(View.VISIBLE);
                            Glide.with(recipeImg.getContext())
                                    .load(serverReceivedImageUrl)
                                    .placeholder(R.drawable.recipe_placeholder)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(recipeImg);

                            if (data.containsKey("Ingredients")) {
                                for (Object ingredient: (ArrayList) data.get("Ingredients")) {
                                    ingredientAdapter.addSelectedData(Utils.capitalize(ingredient.toString()));
                                }
                            }

                            instructionInput.setText(String.valueOf(data.get("Instruction")));

                            if (data.containsKey("Tags")) {
                                for (Object tag: (ArrayList) data.get("Tags")) {
                                    tagAdapter.addSelectedData(Utils.capitalize(tag.toString()));
                                }
                            }

                            dataLoadingBar.setVisibility(View.GONE);
                            recipeForm.setVisibility(View.VISIBLE);

                        } else {
                            Toast.makeText(getActivity(), "No item found!", Toast.LENGTH_LONG).show();
                        }

                    } else {
                        // We let the user to create a new recipe in such case
                        dataLoadingBar.setVisibility(View.GONE);
                        recipeForm.setVisibility(View.VISIBLE);
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("New Recipe");

                        Log.d("Firestore failure", "Error getting document: ", task.getException());
                    }
                });
    }

    // Add the typed input as an ingredient
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void onAddIngredient(View view) {
        String ingredient = ingredientsInput.getText().toString().trim();

        if (ingredient.isEmpty()) {
            Toast.makeText(getActivity(), "Please provide some input first!", Toast.LENGTH_LONG).show();
            return;
        } else {
            List<String> ingredients = ingredientAdapter.getSelectedData();
            for (String item: ingredients) {
                if (item.equalsIgnoreCase(ingredient)) {
                    Toast.makeText(getActivity(), "Provided ingredient already added!", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        ingredientAdapter.addSelectedData(ingredient);
        ingredientsInput.setText("");
    }

    // Handle submission
    private void onSubmit(View view) {
        // If `uploading` is enabled, it means data is already in
        // a being uploaded state
        if (uploading) {
            return;
        }

        String name = recipeNameInput.getText().toString();
        int typeChipId = typeContainer.getCheckedChipId();
        String type = (typeChipId != -1 ?
                ((Chip) typeContainer.findViewById(typeChipId)).getText().toString() :
                "Any").toLowerCase();

        String cuisine = cuisineAdapter.getSelectedItem();

        List<String> ingredients = new ArrayList<>();
        for (String ingredient: ingredientAdapter.getSelectedData()) {
            ingredients.add(ingredient.toLowerCase());
        }

        String instructions = instructionInput.getText().toString();
        // We already have imageUri
        List<String> tags = new ArrayList<>();
        for (String tag: tagAdapter.getSelectedData()) {
            tags.add(tag.toLowerCase());
        }

        if (name.isEmpty() ||
                type.isEmpty() ||
                cuisine == null ||
                ingredients.size() == 0 ||
                instructions.isEmpty() ||
                recipeImg.getDrawable() == null ||
                tags.size() == 0) {
            // TODO
            // Let the user know which fields to fill specifically
            Toast.makeText(getActivity(), "Please fill in all the fields!", Toast.LENGTH_LONG).show();
        } else {

            Map<String, Object> data = new HashMap<>();
            data.put("Name", name);
            data.put("Type", type);
            data.put("Cuisine", cuisine);
            data.put("Ingredients", ingredients);
            data.put("Instruction", instructions);
            data.put("Tags", tags);

            uploading = true;
            // Until `uploading` is disabled, we no longer
            // respond to `Submit` button click
            loadingSnackbar = Snackbar.make(
                    root,
                    "Uploading recipe data!",
                    Snackbar.LENGTH_INDEFINITE
            );
            loadingSnackbar.show();

            if (imageUri == null) {
                data.put("Image", serverReceivedImageUrl);
                uploadDocument(data);
                // We no longer need to upload the image first!
                return;
            }

            // Upload the image first
            // Create the file metadata
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();

            StorageReference imageRef = storage.getReference().child("images/" + imageUri.getLastPathSegment());

            UploadTask uploadTask = imageRef.putFile(imageUri, metadata);

            // TODO
            // onProgress, onPause
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(imageUploadtask -> {
                if (imageUploadtask.isSuccessful()) {
                    Uri downloadUri = imageUploadtask.getResult();
                    data.put("Image", downloadUri.toString());

                    uploadDocument(data);
                } else {
                    uploading = false;
                    Toast.makeText(getActivity(), "Image failed to upload!", Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    private void uploadDocument(Map<String, Object> data) {

        CollectionReference utilityRef = db.collection("utilities");
        WriteBatch batch = db.batch();
        DocumentReference newRecipeDoc = null;

        for (String ingredient: (List<String>) data.get("Ingredients")) {
            batch.update(
                utilityRef.document("ingredients"),
                "values",
                FieldValue.arrayUnion(ingredient)
            );
        }

        for (String tag: (List<String>) data.get("Tags")) {
            batch.update(
                    utilityRef.document("tags"),
                    "values",
                    FieldValue.arrayUnion(tag)
            );
        }

        if (recipeId != null) {
            batch.update(recipeCollectionRef.document(recipeId), data);
        } else {
            // Increasing recipe count first!
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("Recipes", FieldValue.increment(1));

            batch.set(
                db.collection("users").document(user.getUid()),
                userMap,
                SetOptions.merge()
            );

            data.put("AuthorEmail", user.getEmail());

            newRecipeDoc = recipeCollectionRef.document();

            batch.set(newRecipeDoc, data);
            recipeId = newRecipeDoc.getId();
        }

        DocumentReference finalNewRecipeDoc = newRecipeDoc;

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(
                        getActivity(),
                        "Recipe `" + data.get("Name") + "` - " +
                        (finalNewRecipeDoc != null ? "added" : "updated") + " successfully!",
                        Toast.LENGTH_LONG
                ).show();
                goToRecipeItem(recipeId);
            } else {
                Toast.makeText(getActivity(), "Upload failed - please try back at a later time!", Toast.LENGTH_LONG).show();
            }

            loadingSnackbar.dismiss();
            loadingSnackbar = null;
            uploading = false;
        });
    }

    private void goToRecipeItem(String id) {
        if (root != null) {
            // Redirect to recipe item page if user hasn't
            // already altered the page
            Bundle bundle = new Bundle();
            bundle.putString("id", id);

            Navigation.findNavController(root).navigate(R.id.recipe_Item, bundle);
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
            recipeImg.setVisibility(View.VISIBLE);
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