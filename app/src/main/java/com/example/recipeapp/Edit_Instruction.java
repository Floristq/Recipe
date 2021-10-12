package com.example.recipeapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Edit_Instruction#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Edit_Instruction extends Fragment {


    public static final String INSTRUCTION_KEY = "Instruction";
    private EditText Instruction;
    private Button button;

    private View root = null;

    private DocumentReference mDocRef = FirebaseFirestore.getInstance().document("recipes/Greek lemon roast potatoes");

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Edit_Instruction() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Edit_Instruction.
     */
    // TODO: Rename and change types and number of parameters
    public static Edit_Instruction newInstance(String param1, String param2) {
        Edit_Instruction fragment = new Edit_Instruction();
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
        root = inflater.inflate(R.layout.fragment_edit_instruction, container,false);

        Instruction = root.findViewById(R.id.Instruction_Edittext);

        button = root.findViewById(R.id.InstructionBtn);
        button.setOnClickListener(this::Submit_edit);

        mDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            String InsturctionText = documentSnapshot.getString(INSTRUCTION_KEY);
                            Instruction.setText(InsturctionText);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Unavailable to get data from firebase!", Toast.LENGTH_LONG).show();
                    }
                });


        return root;
    }


    private void Submit_edit(View view){
        String New_Instruction = Instruction.getText().toString();

        //Map<String,Object> note = new HashMap<>();
        //note.put(INSTRUCTION_KEY, New_Instruction);

        mDocRef.update(INSTRUCTION_KEY,New_Instruction).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(getActivity(), "Instruction updated!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}