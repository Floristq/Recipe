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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.recipeapp.autocompleteadapter.AdapterItem;
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
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private MultiAutoCompleteTextView recipeTagInput;
    private Button submitBtn;
    private ChipGroup typeContainer;
    private Spinner cuisineSpinner;
    private ChipGroup ingredientContainer;
    private LinearLayout recipeForm;
    private ProgressBar dataLoadingBar;

    private EditText recipeNameInput;
    private EditText instructionInput;

    Snackbar loadingSnackbar = null;

    private AutoCompleteAdapter recipeTagsAdapter;
    private Uri imageUri = null;

    private boolean uploading = false;

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
        recipeTagInput = root.findViewById(R.id.recipeTagInput);
        submitBtn = root.findViewById(R.id.submitBtn);
        typeContainer = root.findViewById(R.id.typeContainer);
        cuisineSpinner = root.findViewById(R.id.cuisine);
        ingredientContainer = root.findViewById(R.id.ingredientContainer);
        recipeForm = root.findViewById(R.id.recipeForm);
        dataLoadingBar = root.findViewById(R.id.dataLoadingBar);

        recipeNameInput = root.findViewById(R.id.recipeName);
        instructionInput = root.findViewById(R.id.instructions);

        List<String> cuisines =  new ArrayList<String>();
        cuisines.add(SELECT_CUISINE_HINT);
        for (String cuisine: temporaryCuisines) {
            cuisines.add(cuisine);
        }
        setCuisineAdapter(cuisines);

        for (String type: Utils.allTypes) {
            Chip chip = new Chip(activity);
            chip.setText(type);
            chip.setId(root.generateViewId());
            chip.setCheckable(true);

            typeContainer.addView(chip);
        }

        // Configuring recipeTagsInput
        // TODO
        // Temporarily using the temporaryTags to populate tags-input until
        // server / firebase is set up
        List<AdapterItem> tagAdapterList = new ArrayList<>();
        for (String tag: temporaryTags) {
            tagAdapterList.add(new AdapterItem(tag, 0));
        }

        recipeTagsAdapter = new AutoCompleteAdapter(activity, tagAdapterList);
        recipeTagInput.setAdapter(recipeTagsAdapter);
        recipeTagInput.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());


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
                                setCuisine(String.valueOf(data.get("Cuisine")));
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
                                    addIngredientChip(ingredient.toString());
                                }
                            }

                            instructionInput.setText(String.valueOf(data.get("Instruction")));

                            if (data.containsKey("Tags")) {
                                List<String> tags = new ArrayList<String>();
                                for (Object tag: (ArrayList) data.get("Tags")) {
                                    tags.add(Utils.capitalize(tag.toString()));
                                }

                                recipeTagInput.setText(String.join(", ", tags) +
                                        (tags.size() > 0 ? ", " : ""));
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

    private void setCuisine(String cuisine) {
        SpinnerAdapter cuisineAdapter = cuisineSpinner.getAdapter();
        int cuisineAdapterLen = cuisineAdapter.getCount();
        List<String> cuisines = new ArrayList<String>();
        for (int index = 0; index < cuisineAdapterLen; index++) {
            Object cuisineValue = cuisineAdapter.getItem(index);
            String cuisineStr = cuisineValue.toString();
            if (cuisineStr.equalsIgnoreCase(cuisine)) {
                cuisineSpinner.setSelection(index);
                return;
            }
            cuisines.add(cuisineStr);
        }

        cuisines.add(cuisine);
        setCuisineAdapter(cuisines);

        cuisineSpinner.setSelection(cuisineAdapterLen);
    }

    private void setCuisineAdapter(List<String> cuisines) {
        ArrayAdapter<String> cuisineAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                cuisines
        );

        cuisineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuisineSpinner.setAdapter(cuisineAdapter);
    }

    // Add the typed input as an ingredient
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void onAddIngredient(View view) {
        EditText ingredientInput = root.findViewById(R.id.ingredient);
        String ingredient = ingredientInput.getText().toString().trim();

        if (ingredient.isEmpty()) {
            Toast.makeText(getActivity(), "Please provide some input first!", Toast.LENGTH_LONG).show();
            return;
        } else {
            String ingredientLC = ingredient.toLowerCase();
            List<Integer> chipIds = ingredientContainer.getCheckedChipIds();
            for (Integer chipId: chipIds) {
                Chip chip = ingredientContainer.findViewById(chipId);
                if (ingredient.equalsIgnoreCase(chip.getText().toString())) {
                    Toast.makeText(getActivity(), "Provided ingredient already added!", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        addIngredientChip(ingredient);
        ingredientInput.setText("");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void addIngredientChip(String ingredient) {
        Chip chip = new Chip(getActivity());
        chip.setText(Utils.capitalize(ingredient));
        chip.setId(root.generateViewId());
        chip.setCheckable(true);
        chip.setChecked(true);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            ingredientContainer.removeView(v);
        });

        ingredientContainer.addView(chip);
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

        String tempCuisine = cuisineSpinner.getSelectedItem().toString();
        String cuisine = tempCuisine == SELECT_CUISINE_HINT ? "" : tempCuisine; // User can't choose the hint

        List<Integer> ingredientChipIds = ingredientContainer.getCheckedChipIds();
        List<String> ingredients = new ArrayList<String>();
        for (Integer id: ingredientChipIds){
            Chip chip = ingredientContainer.findViewById(id);
            ingredients.add(chip.getText().toString().toLowerCase());
        }

        String instructions = instructionInput.getText().toString();
        // We already have imageUri
        List<String> tempTags = Arrays.asList(recipeTagInput.getText().toString().split(", "));
        List<String> tags = new ArrayList<String>();
        for (String tempTag: tempTags) {
            if (!tempTag.isEmpty()) tags.add(tempTag.toLowerCase()); // Removing the empty strings
        }

        if (name.isEmpty() ||
                type.isEmpty() ||
                cuisine.isEmpty() ||
                ingredients.isEmpty() ||
                instructions.isEmpty() ||
                recipeImg.getDrawable() == null ||
                tags.isEmpty()) {
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
                FieldValue.arrayUnion(ingredient.toLowerCase())
            );
        }

        for (String tag: (List<String>) data.get("Tags")) {
            batch.update(
                    utilityRef.document("tags"),
                    "values",
                    FieldValue.arrayUnion(tag.toLowerCase())
            );
        }

        if (recipeId != null) {
            batch.update(recipeCollectionRef.document(recipeId), data);
        } else {
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