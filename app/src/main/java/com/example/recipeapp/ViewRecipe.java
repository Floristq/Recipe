package com.example.recipeapp;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Arrays;
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
    private CollectionReference utilitiesCollectionRef = db.collection("utilities");

    // TODO
    // Remove temporaryCuisines and use server/firebase retrieved tags
    final private String[] temporaryCuisines = {
            "Chinese", "English", "Indian", "French",
            "American", "Japanese", "Mexican"
    };

    private ArrayList<String> result;

    private View root = null;
    private ChipGroup cuisineContainer;
    private RecyclerView recipeListContainer;
    private Button searchRecipeBtn;
    private TextView tvAdvanceFilter;
    private LinearLayout llMain;
    private LinearLayout llAdvanceFilter;

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
        cuisineContainer = root.findViewById(R.id.cuisineContainer);
        tvAdvanceFilter = root.findViewById(R.id.tvAdvanceFilter);
        llMain = root.findViewById(R.id.llMain);
        llAdvanceFilter = root.findViewById(R.id.llAdvanceFilter);
        Activity activity = getActivity();

        Bundle bundle = this.getArguments();
//        String Ingredient1 = bundle.getString("Ingredient1");
//        String Ingredient2 = bundle.getString("Ingredient2");
//
//        if (!bundle.getString("Ingredient3").equals("")){
//            String Ingredient3 = bundle.getString("Ingredient3");
//        }else{
//            String Ingredient3 = "";
//        }
//
//        if (!bundle.getString("Ingredient4").equals("")){
//            String Ingredient4 = bundle.getString("Ingredient4");
//        }else{
//            String Ingredient4 = "";
//        }
//
//        if (!bundle.getString("Ingredient5").equals("")){
//            String Ingredient5 = bundle.getString("Ingredient5");
//        }else{
//            String Ingredient5 = "";
//        }

        for (String cuisine : temporaryCuisines) {
            Chip chip = new Chip(activity);
            chip.setText(cuisine);
            chip.setId(root.generateViewId());
            chip.setCheckable(true);

            cuisineContainer.addView(chip);
        }


        searchRecipeBtn.setOnClickListener(this::onSearch);

        tvAdvanceFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                llAdvanceFilter.setVisibility(View.VISIBLE);
//                llMain.setVisibility(View.GONE);
//                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
//                fragmentTransaction.replace(R.id.fmAdvanced,new AdvanceItemFragment());
//                fragmentTransaction.commit();
//                Navigation.findNavController(view).getCurrentBackStackEntry().getSavedStateHandle().getLiveData("key").observe(new );

                Navigation.findNavController(view).navigate(R.id.advanceFiltered);
            }
        });

        loadRecipeList();

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
                if (event.equals(Lifecycle.Event.ON_RESUME)
                        && navBackStackEntry.getSavedStateHandle().contains("key")) {
                    result = navBackStackEntry.getSavedStateHandle().get("key");
                    Log.e("@@@",result.size()+"");
                }
            }
        };
        navBackStackEntry.getLifecycle().addObserver(observer);
    }

    private void loadRecipeList() {

        // TODO
        // Display a loader

        // Starting with emptying the recyclerView
        ViewRecipeListFragment.recyclerView.setAdapter(new ViewRecipeListRecyclerViewAdapter(new ArrayList<RecipeItem>()));

        // Getting selected cuisines
        List<Integer> ids = cuisineContainer.getCheckedChipIds();
        List<CharSequence> selectedCuisines = new ArrayList<CharSequence>();
        for (Integer id : ids) {
            Chip chip = cuisineContainer.findViewById(id);
            selectedCuisines.add(chip.getText());
        }

        Task<QuerySnapshot> snap;

        // Building query and subsequent query snapshot
        if (selectedCuisines.size() > 0) {
            if(result!=null && result.size()>0) {
                snap = recipeCollectionRef
//                        .whereIn("Cuisine", selectedCuisines)
                        .whereArrayContainsAny("Ingredients", result)
                        .get();
            }else{
                snap = recipeCollectionRef
                        .whereIn("Cuisine", selectedCuisines)
                        .get();
            }
        } else {
            if(result!=null && result.size()>0) {
                snap = recipeCollectionRef
                        .whereArrayContainsAny("Ingredients", result)
                        .get();
            }else {
                snap = recipeCollectionRef
                        // .whereNotEqualTo("Image", "")
                        .get();
            }
        }

        snap.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<RecipeItem> data = new ArrayList<RecipeItem>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    Map<String, Object> item = document.getData();

                    data.add(new RecipeItem(
                            document.getId(),
                            String.valueOf(item.get("Name")),
                            String.valueOf(item.get("Image"))
                    ));
                }

                // TODO
                // Remove the following workaround and implement a better approach
                ViewRecipeListFragment.recyclerView.setAdapter(new ViewRecipeListRecyclerViewAdapter(data));
            } else {
                Log.d("Firestore failure", "Error getting documents: ", task.getException());
            }
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