package com.example.recipeapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Recipe_Item#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Recipe_Item extends Fragment {

    TextView mNameTextView;
    TextView mInstructionTextView;
    TextView mIngredientTextView;
    ImageView img;

    private Button CommentButton;

    private Button EditBtn;

    private Button MapBtn;

    private int LOCATION_PERMISSION_CODE = 1001;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference recipeCollectionRef = db.collection("recipes");
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();

    private View root = null;
    private ProgressBar dataLoadingBar;
    private LinearLayout recipeContainer;

    private String recipeId = null;

    public Recipe_Item() {
        // Required empty public constructor
    }

    public static Recipe_Item newInstance() {
        return new Recipe_Item();
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


        root = inflater.inflate(R.layout.fragment_recipe_item, container,false);

        mInstructionTextView = root.findViewById(R.id.Instructions);
        mNameTextView = root.findViewById(R.id.Recipe_Name);
        img = root.findViewById(R.id.Image);
        mIngredientTextView = root.findViewById(R.id.Ingredients);
        dataLoadingBar = root.findViewById(R.id.dataLoadingBar);
        recipeContainer = root.findViewById(R.id.recipeContainer);

        Bundle bundle = this.getArguments();

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        CommentButton = root.findViewById(R.id.CommentsButton);
        // CommentButton.setOnClickListener(this::Go_Comments);

        // Direct user to Google Maps
        MapBtn = root.findViewById(R.id.map);
        MapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if the permission is open
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                    // Create a Uri from an intent string. Use the result to create an Intent.
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=grocery&z=17");

                    // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    // Make the Intent explicit by setting the Google Maps package
                    mapIntent.setPackage("com.google.android.apps.maps");

                    if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        // Attempt to start an activity that can handle the Intent
                        startActivity(mapIntent);
                    }
                }
                // if the permission if off
                else {
                    requestLocationPermission();
                }
            }
        });

        EditBtn = root.findViewById(R.id.Edit_Recipe);
        EditBtn.setEnabled(false);
        EditBtn.setOnClickListener(this::onEdit);

        if (bundle.containsKey("id")) {
            recipeId = bundle.getString("id");
            CommentButton.setOnClickListener(v -> {
                Bundle commentBundle = new Bundle();
                commentBundle.putString("id", recipeId);
                Navigation.findNavController(v).navigate(R.id.comments, commentBundle);
            });
            loadItem(recipeId);
        }

        return root;
    }

    // request permission method
    private void requestLocationPermission(){
        // show a pop up dialog to tell user why we need this permission
        // (in case the user denied before but try to permit again)
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
            // Pop up dialog
            new AlertDialog.Builder(getActivity())
                    .setTitle("Permission needed")
                    .setMessage("Location permission is needed because of using the Google Map service")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                            requestPermissions(permissions, LOCATION_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        }else{
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissions, LOCATION_PERMISSION_CODE);
        }
    }

//    private void Go_Comments(View view){
//        Bundle bundle = new Bundle();
//        Navigation.findNavController(view).navigate(R.id.comments, bundle);
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadItem(String id) {
        // We will not load anything if no `id` has been passed
        if (id.isEmpty() || id == null) return;

        dataLoadingBar.setVisibility(View.VISIBLE);
        recipeContainer.setVisibility(View.GONE);

        recipeCollectionRef.document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();

                            // Populating the fields!
                            mNameTextView.setText(String.valueOf(data.get("Name")));

                            Glide.with(img.getContext())
                                    .load(String.valueOf(data.get("Image")))
                                    .placeholder(R.drawable.recipe_placeholder)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(img);

                            if (data.containsKey("Type")) {
                                String type = (String) data.get("Type");
                                if (!type.equals("any")) {
                                    root.findViewById(R.id.typeContainer).setVisibility(View.VISIBLE);
                                    ((TextView) root.findViewById(R.id.type)).setText(Utils.capitalize(type));
                                }
                            }

                            if (data.containsKey("Cuisine")) {
                                ((TextView) root.findViewById(R.id.cuisine)).setText(String.valueOf(data.get("Cuisine")));
                            }
                            if (data.containsKey("Ingredients")) {
                                List<String> ingredients = new ArrayList<String>();
                                for (String ingredient: (List<String>) data.get("Ingredients")) {
                                    ingredients.add(Utils.capitalize(ingredient));
                                }

                                mIngredientTextView.setText(String.join(", ", ingredients));
                            }
                            mInstructionTextView.setText(String.valueOf(data.get("Instruction")));
                            if (data.containsKey("Tags")) {
                                List<String> tags = new ArrayList<String>();
                                for (String tag: (List<String>) data.get("Tags")) {
                                    tags.add(Utils.capitalize(tag));
                                }

                                ((TextView) root.findViewById(R.id.tags)).setText(String.join(", ", tags));
                            }

                            // Only the author of the recipe can edit it
                            if (String.valueOf(data.get("AuthorEmail")).equals(user.getEmail())) {
                                EditBtn.setEnabled(true);
                            }

                            dataLoadingBar.setVisibility(View.GONE);
                            recipeContainer.setVisibility(View.VISIBLE);

                        } else {
                            dataLoadingBar.setVisibility(View.GONE);

                            Toast.makeText(getActivity(), "No item found!", Toast.LENGTH_LONG).show();
                        }

                    } else {
                        dataLoadingBar.setVisibility(View.GONE);

                        Log.d("Firestore failure", "Error getting document: ", task.getException());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Unavailable to get data from firebase!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void onEdit(View v) {
        Bundle editRecipeBundle = new Bundle();
        editRecipeBundle.putString("id", recipeId);

        Navigation.findNavController(v).navigate(R.id.addRecipe, editRecipeBundle);
    }

}