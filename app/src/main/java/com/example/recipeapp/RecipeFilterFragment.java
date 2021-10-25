package com.example.recipeapp;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.tylersuehr.chips.Chip;
import com.tylersuehr.chips.ChipsInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class RecipeFilterFragment extends Fragment {

    private View root = null;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference utilitiesRef = db.collection("utilities");

    ChipsInputLayout ingredientsChipInput;
    ChipsInputLayout cuisinesChipInput;
    ChipsInputLayout tagsChipInput;

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

        ingredientsChipInput = root.findViewById(R.id.ingredientsChipInput);
        cuisinesChipInput = root.findViewById(R.id.cuisinesChipInput);
        tagsChipInput = root.findViewById(R.id.tagsChipInput);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton confirmBtn = root.findViewById(R.id.confirmBtn);

        populateFilter("ingredients", ingredientsChipInput);
        populateFilter("tags", tagsChipInput);
        populateFilter("cuisines", cuisinesChipInput);

        root.findViewById(R.id.confirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> selectedIngredients = new ArrayList<>();
                for (Chip item: ingredientsChipInput.getSelectedChips()) {
                    selectedIngredients.add(item.getTitle());
                }

                List<String> selectedCuisines = new ArrayList<>();
                for (Chip item: cuisinesChipInput.getSelectedChips()) {
                    selectedCuisines.add(item.getTitle());
                }

                List<String> selectedTags = new ArrayList<>();
                for (Chip item: tagsChipInput.getSelectedChips()) {
                    selectedTags.add(item.getTitle());
                }

                SavedStateHandle previousState = Navigation.findNavController(v).getPreviousBackStackEntry().getSavedStateHandle();

                previousState.set("ingredients", selectedIngredients);
                previousState.set("cuisines", selectedCuisines);
                previousState.set("tags", selectedTags);

                Navigation.findNavController(v).navigateUp();
            }
        });
    }

    private void populateFilter(String key, ChipsInputLayout chipsInput) {
        utilitiesRef.document(key)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (root == null) {
                        return;
                    }

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<ChipItem> list = new ArrayList<>();
                            for (String item: (List<String>) document.get("values")) {
                                list.add(new ChipItem(item));
                            }

                            chipsInput.setFilterableChipList(list);
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
    }

    public class ChipItem extends Chip {
        private final String item;

        public ChipItem(String item) {
            super();
            this.item = item;
        }

        @Nullable
        @Override
        public Integer getId() {
            return null;
        }

        @NonNull
        @Override
        public String getTitle() {
            return item;
        }

        @Override
        public Uri getAvatarUri() {
            return null;
        }

        @Nullable
        @Override
        public Drawable getAvatarDrawable() {
            return null;
        }

        @Nullable
        @Override
        public String getSubtitle() {
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        root = null;
    }
}