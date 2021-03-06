package com.example.recipeapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Input_Ingredients#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Input_Ingredients extends Fragment {

    EditText First_Ingredient;
    EditText Second_Ingredient;
    EditText Third_Ingredient;
    EditText Fourth_Ingredient;
    EditText Fifth_Ingredient;
    private Button button;

//    private Button itembutton;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View root = null;

    public Input_Ingredients() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Input_Ingredients.
     */
    // TODO: Rename and change types and number of parameters
    public static Input_Ingredients newInstance(String param1, String param2) {
        Input_Ingredients fragment = new Input_Ingredients();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        root = inflater.inflate(R.layout.fragment_input_ingredients, container,false);
        button = root.findViewById(R.id.SubmitButton);
        First_Ingredient = root.findViewById(R.id.First_Ingredient);
        Second_Ingredient = root.findViewById(R.id.Second_Ingredient);
        Third_Ingredient = root.findViewById(R.id.Third_Ingredient);
        Fourth_Ingredient = root.findViewById(R.id.Fourth_Ingredient);
        Fifth_Ingredient = root.findViewById(R.id.Fifth_Ingredient);
//        itembutton = root.findViewById(R.id.Recipe_Item);
//        itembutton.setOnClickListener(this::onItem);
        button.setOnClickListener(this::onSubmit);
        return root;
    }


    private void onSubmit(View view) {
        Bundle bundle = new Bundle();


        String Ingredient_1 = ((EditText) root.findViewById(R.id.First_Ingredient)).getText().toString();
        String Ingredient_2 = ((EditText) root.findViewById(R.id.Second_Ingredient)).getText().toString();
        String Ingredient_3 = ((EditText) root.findViewById(R.id.Third_Ingredient)).getText().toString();
        String Ingredient_4 = ((EditText) root.findViewById(R.id.Fourth_Ingredient)).getText().toString();
        String Ingredient_5 = ((EditText) root.findViewById(R.id.Fifth_Ingredient)).getText().toString();

        bundle.putString("Ingredient1", First_Ingredient.getText().toString());
        bundle.putString("Ingredient2", Second_Ingredient.getText().toString());
        bundle.putString("Ingredient3", Third_Ingredient.getText().toString());
        bundle.putString("Ingredient4", Fourth_Ingredient.getText().toString());
        bundle.putString("Ingredient5", Fifth_Ingredient.getText().toString());

        if (Ingredient_1.toString().equals("") || Ingredient_2.toString().equals("")) {
            Toast.makeText(getActivity(), "Please submit at least 2 ingredients!", Toast.LENGTH_LONG).show();
        } else {
            Navigation.findNavController(view).navigate(R.id.viewRecipe, bundle);
        }
    }

    private void onItem(View view){
        Bundle bundle = new Bundle();

        Navigation.findNavController(view).navigate(R.id.recipe_Item, bundle);
    }
}