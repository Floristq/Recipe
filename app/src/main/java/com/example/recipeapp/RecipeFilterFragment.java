package com.example.recipeapp;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import com.example.recipeapp.autocompleteadapter.AdapterItem;
import com.example.recipeapp.autocompleteadapter.AutoCompleteAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class RecipeFilterFragment extends Fragment {

    private View root = null;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference utilitiesRef = db.collection("utilities");

    AutoCompleteTextView searchIngredientsInput;
    ChipGroup ingredientsContainer;
    AutoCompleteTextView searchCuisines;
    ChipGroup cuisinesContainer;
    AutoCompleteTextView searchTags;
    ChipGroup tagsContainer;

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

        ingredientsContainer = root.findViewById(R.id.ingredientsContainer);
        searchIngredientsInput = root.findViewById(R.id.searchIngredients);
        cuisinesContainer = root.findViewById(R.id.cuisinesContainer);
        searchCuisines = root.findViewById(R.id.searchCuisines);
        tagsContainer = root.findViewById(R.id.tagsContainer);
        searchTags = root.findViewById(R.id.searchTags);

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton confirmBtn = root.findViewById(R.id.confirmBtn);
        Activity activity = getActivity();

        populateFilter(
                "ingredients",
                AutoCompleteAdapter.getInstance(activity, searchIngredientsInput, ingredientsContainer)
        );
        populateFilter(
                "cuisines",
                AutoCompleteAdapter.getInstance(activity, searchCuisines, cuisinesContainer)
        );
        populateFilter(
                "tags",
                AutoCompleteAdapter.getInstance(activity, searchTags, tagsContainer)
        );

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SavedStateHandle previousState = Navigation.findNavController(v).getPreviousBackStackEntry().getSavedStateHandle();

                previousState.set("ingredients", getChipValues(ingredientsContainer));
                previousState.set("cuisines", getChipValues(cuisinesContainer));
                previousState.set("tags", getChipValues(tagsContainer));

                Navigation.findNavController(v).navigateUp();
            }
        });
    }

    private List<String> getChipValues(ChipGroup chipGroup) {
        List<Integer> chipIds = chipGroup.getCheckedChipIds();
        List<String> values = new ArrayList<String>();
        for (Integer id: chipIds){
            com.google.android.material.chip.Chip chip = chipGroup.findViewById(id);
            values.add(chip.getText().toString());
        }

        return values;
    }

    private void populateFilter(String key, AutoCompleteAdapter adapter) {
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
                                List<AdapterItem> list = new ArrayList<>();
                                for (String item: (List<String>) document.get("values")) {
                                    list.add(new AdapterItem(item, 0));
                                }

                                adapter.setData(list, false);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        root = null;
    }
}