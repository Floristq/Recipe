package com.example.recipeapp;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewRecipe#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewRecipe extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // TODO
    // Remove temporaryCuisines and use server/firebase retrieved tags
    final private String[] temporaryCuisines = {
            "Chinese", "English", "Indian", "French",
             "American", "Japanese", "Mexican"
    };

    private View root = null;
    private RecyclerView recipeListContainer;

    public ViewRecipe() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewRecipe.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewRecipe newInstance(String param1, String param2) {
        ViewRecipe fragment = new ViewRecipe();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_view_recipe, container, false);

        ChipGroup cuisineContainer = root.findViewById(R.id.cuisineContainer);
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

        for (String cuisine: temporaryCuisines) {
            Chip chip = new Chip(activity);
            chip.setText(cuisine);
            chip.setId(root.generateViewId());
            chip.setCheckable(true);

            cuisineContainer.addView(chip);
        }

        loadRecipeList();

        return root;
    }

    private void loadRecipeList() {
        // TODO
        // Integrate with database
        List<RecipeItem> data = new ArrayList<RecipeItem>();
        data.add(new RecipeItem("1", "Custard Pie", null));
        data.add(new RecipeItem("2", "Chocolate Cake", null));
        data.add(new RecipeItem("3", "Pastry", null));
        data.add(new RecipeItem("4", "Chicken Pop Pie", null));
        data.add(new RecipeItem("5", "Ice-Cream Cake", null));

        // TODO
        // Remove the following workaround and implement a better approach
        ViewRecipeListFragment.recyclerView.setAdapter(new ViewRecipeListRecyclerViewAdapter(data));
    }

    public static class RecipeItem {
        public String id;
        public String name;
        public Uri imageUri;

        public RecipeItem(String id, String name, Uri imageUri) {
            this.id = id;
            this.name = name;
            this.imageUri = imageUri;
        }

    }
}