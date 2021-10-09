package com.example.recipeapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Recipe_Item#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Recipe_Item extends Fragment {

    TextView mNameTextView;
    TextView mInsturctionTextView;

    private Button CommentButton;

    public static final String NAME_KEY = "Name";
    public static final String INSTRUCTION_KEY = "Instruction";

    private DocumentReference mDocRef = FirebaseFirestore.getInstance().document("recipes/Greek lemon roast potatoes");

    private View root = null;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Recipe_Item() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Recipe_Item.
     */
    // TODO: Rename and change types and number of parameters
    public static Recipe_Item newInstance(String param1, String param2) {
        Recipe_Item fragment = new Recipe_Item();
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


        root = inflater.inflate(R.layout.fragment_recipe_item, container,false);

        mInsturctionTextView = root.findViewById(R.id.Instructions);
        mNameTextView = root.findViewById(R.id.Recipe_Name);

        CommentButton = root.findViewById(R.id.CommentsButton);

        CommentButton.setOnClickListener(this::Go_Comments);

        mDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    String NameText = documentSnapshot.getString(NAME_KEY);
                    String InsturctionText = documentSnapshot.getString(INSTRUCTION_KEY);
                    mNameTextView.setText(NameText);
                    mInsturctionTextView.setText(InsturctionText);

                }
            }

        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Unavailable to get data from firebase!", Toast.LENGTH_LONG).show();
                    }
                })


        ;




        return root;

    }

    private void Go_Comments(View view){
        Bundle bundle = new Bundle();

        Navigation.findNavController(view).navigate(R.id.comments, bundle);
    }


}