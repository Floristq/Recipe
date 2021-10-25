package com.example.recipeapp;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewRecipe#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewRecipe extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference recipeCollectionRef = db.collection("recipes");
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();

    private List<String> filteredIngredients = null;
    private List<String> filteredTags = null;
    private List<String> filteredCuisines = null;

    private View root = null;
    private ChipGroup typeContainer;
    private Button searchRecipeBtn;
    private ProgressBar dataLoadingBar;
    private RelativeLayout rvListing;

    private boolean ownRecipeOnly = false;

    public ViewRecipe() {
        // Required empty public constructor
    }

    public static ViewRecipe newInstance() {
        return new ViewRecipe();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_view_recipe, container, false);

        searchRecipeBtn = root.findViewById(R.id.searchRecipeBtn);
        typeContainer = root.findViewById(R.id.typeContainer);
        dataLoadingBar = root.findViewById(R.id.dataLoadingBar);
        rvListing = root.findViewById(R.id.rvListing);
        Activity activity = getActivity();

        for (String type: Utils.allTypes) {
            Chip chip = new Chip(activity);
            chip.setText(type);
            chip.setId(root.generateViewId());
            chip.setCheckable(true);

            typeContainer.addView(chip);
        }
        typeContainer.setOnCheckedChangeListener((group, checkedId) -> {
            onSearch(group);
        });

        searchRecipeBtn.setOnClickListener(this::onSearch);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            if (bundle.containsKey("ownRecipeOnly")) {
                ownRecipeOnly = bundle.getBoolean("ownRecipeOnly");
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("My Recipes");
            }
        }

        root.findViewById(R.id.advancedFilterBtn).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.recipeFilter);
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavBackStackEntry navBackStackEntry = Navigation.findNavController(view).getBackStackEntry(R.id.viewRecipe);

        // Create our observer and add it to the NavBackStackEntry's lifecycle
        final LifecycleEventObserver observer = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event.equals(Lifecycle.Event.ON_RESUME)) {
                    SavedStateHandle savedState = navBackStackEntry.getSavedStateHandle();

                    if (savedState.contains("ingredients")) {
                        filteredIngredients = savedState.get("ingredients");
                    }

                    if (savedState.contains("tags")) {
                        filteredTags = savedState.get("tags");
                    }

                    if (savedState.contains("cuisines")) {
                        filteredCuisines = savedState.get("cuisines");
                    }
                }
            }
        };

        navBackStackEntry.getLifecycle().addObserver(observer);

        loadRecipeList();
    }

    private void loadRecipeList() {


        // Starting with emptying the recyclerView
        ViewRecipeListFragment.recyclerView.setAdapter(new ViewRecipeListRecyclerViewAdapter(new ArrayList<RecipeItem>()));
        dataLoadingBar.setVisibility(View.VISIBLE);

        // Getting selected type
        Integer typeChipId = typeContainer.getCheckedChipId();
        String type = (typeChipId != -1 ?
                ((Chip) typeContainer.findViewById(typeChipId)).getText().toString() :
                "Any").toLowerCase();

        Task<QuerySnapshot> snap;

        // Building query and subsequent query snapshot
        Query query = recipeCollectionRef;

        if (ownRecipeOnly) {
            query = query.whereEqualTo("AuthorEmail", user.getEmail());
        }

        if (!type.equals("any")) {
            query = query.whereEqualTo("Type", type);
        }

        boolean hasFilteredIngredients = false, hasFilteredTags = false,
                filterTagsManually = false, filterCuisinesManually = false;
        if (filteredIngredients != null && filteredIngredients.size() > 0) {
            hasFilteredIngredients = true;
            query = query.whereArrayContainsAny("Ingredients", filteredIngredients);
        }

        if (filteredTags != null && filteredTags.size() > 0) {
            hasFilteredTags = true;
            if (hasFilteredIngredients) {
                filterTagsManually = true;
            } else {
                query = query.whereArrayContainsAny("Tags", filteredTags);
            }
        }

        if (filteredCuisines != null && filteredCuisines.size() > 0) {
            if (hasFilteredIngredients || hasFilteredTags) {
                filterCuisinesManually = true;
            } else {
                query = query.whereIn("Cuisine", filteredCuisines);
            }
        }

        final boolean finalFilterTagsManually = filterTagsManually;
        final boolean finalFilterCuisinesManually = filterCuisinesManually;

        snap = query.get();

        snap.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<RecipeItem> data = new ArrayList<RecipeItem>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    Map<String, Object> item = document.getData();

                    if (finalFilterTagsManually) {
                        boolean shouldProceed = false;
                        // TODO
                        // Do the tag filtration manually here
                        // Since firebase doesn't support multiple `whereArrayContainsAny`
                        // we had to resort to this!
                        List<String> itemTags = (List<String>) item.get("Tags");

                        for (String tag: filteredTags) {
                            if (itemTags.contains(tag)) {
                                shouldProceed = true;
                                break;
                            }
                        }

                        if (!shouldProceed) {
                            continue;
                        }
                    }

                    if (finalFilterCuisinesManually) {
                        // TODO
                        // Do the tag filtration manually here
                        // Since firebase doesn't support multiple `whereIn|whereArrayContainsAny`
                        // we had to resort to this!
                        if (!filteredCuisines.contains(String.valueOf(item.get("Cuisine")))) {
                            continue;
                        }
                    }

                    data.add(new RecipeItem(
                            document.getId(),
                            String.valueOf(item.get("Name")),
                            String.valueOf(item.get("Image"))
                    ));
                }

                // TODO
                // Remove the following workaround and implement a better approach
                if (data.size() > 0) {
                    rvListing.setVisibility(View.VISIBLE);
                    ViewRecipeListFragment.recyclerView.setAdapter(new ViewRecipeListRecyclerViewAdapter(data));
                } else {
                    rvListing.setVisibility(View.GONE);
                }
            } else {
                Log.d("Firestore failure", "Error getting documents: ", task.getException());
            }

            dataLoadingBar.setVisibility(View.GONE);
        });

    }

    private void onSearch(View view) {
        // TODO
        // Either preprocess or check if we can remove this
        // function altogether!
        loadRecipeList();
    }

    public static class RecipeItem {
        public String id;
        public String name;
        public String imageUrl;

        public RecipeItem(String id, String name, String imageUrl) {
            this.id = id;
            this.name = name;
            this.imageUrl = imageUrl;
        }

    }
}