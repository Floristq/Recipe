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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Input_Ingredients#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Input_Ingredients extends Fragment {

    EditText First_Ingredient;
    Button button;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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

        View v = inflater.inflate(R.layout.fragment_input_ingredients, container,false);
        button = v.findViewById(R.id.button);
        First_Ingredient = v.findViewById(R.id.First_Ingredient);

        button.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString("key", First_Ingredient.getText().toString());
            Navigation.findNavController(view).navigate(R.id.view_Recipe, bundle);
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Input Ingredients");
    }
}