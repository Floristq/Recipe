package com.example.recipeapp;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class RecipeFilterFragment extends Fragment {

    private View root = null;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference ingredientsRef = db.collection("utilities").document("ingredients");;

    ChipsInput ingredientsChipInput;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecipeFilterFragment() {
    }

    public static RecipeFilterFragment newInstance(int columnCount) {
        return new RecipeFilterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_recipe_filter, container, false);

        FloatingActionButton confirmBtn = root.findViewById(R.id.confirmBtn);
        ingredientsChipInput = root.findViewById(R.id.ingredientsChip);

        ingredientsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<ChipItem> ingredients = new ArrayList<>();
                        for (String item: (List<String>) document.get("values")) {
                            ingredients.add(new ChipItem(item));
                        }

                        ingredientsChipInput.setFilterableList(ingredients);
                    } else {
                        // TODO
                        // Handle empty result
                    }
                } else {
                    // TODO
                    // Handle error
                }
            }
        });

        root.findViewById(R.id.confirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> selectedIngredients = new ArrayList<>();
                for (ChipInterface item: ingredientsChipInput.getSelectedChipList()) {
                    selectedIngredients.add(item.getLabel());
                }

                Navigation.findNavController(v).getPreviousBackStackEntry().getSavedStateHandle().set("ingredients", selectedIngredients);
                Navigation.findNavController(v).navigateUp();

            }
        });

        return root;
    }

    public static class ChipItem implements ChipInterface {
        private String item;

        public ChipItem(String item) {
            this.item = item;
        }

        @Override
        public Object getId() {
            return null;
        }

        @Override
        public Uri getAvatarUri() {
            return null;
        }

        @Override
        public Drawable getAvatarDrawable() {
            return null;
        }

        @Override
        public String getLabel() {
            return item;
        }

        @Override
        public String getInfo() {
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        root = null;
    }
}