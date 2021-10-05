package com.example.recipeapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;

import com.example.recipeapp.databinding.FragmentHomeBinding;
import com.example.recipeapp.Input_Ingredients;
import com.example.recipeapp.R;



public class HomeFragment extends Fragment {



    FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


//        View root = inflater.inflate(R.layout.fragment_home, container, false);
//        Button button = (Button) root.findViewById(R.id.button);
//        return root;

        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonInputIngredients.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.input_Ingredients);
        });

        binding.buttonAddRecipe.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.addRecipe);
        });

        binding.buttonViewRecipe.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("Ingredient1", "");
            bundle.putString("Ingredient2", "");
            Navigation.findNavController(v).navigate(R.id.viewRecipe, bundle);
        });
    }

    // to avoid memory leakage
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}