package com.example.recipeapp.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.recipeapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore db;
    private Integer commentSum = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextInputLayout email = root.findViewById(R.id.profile_textfield_email);
        final TextInputLayout user_name = root.findViewById(R.id.profile_textfield_name);
        final ImageView img = root.findViewById(R.id.profile_image);
        final TextView commentCount = root.findViewById(R.id.profile_comment_num);

        // Get the user info from firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();

        // Initialize firebase db
        db = FirebaseFirestore.getInstance();

        if (user != null) {
            String personName = user.getDisplayName();
            String personEmail = user.getEmail();
            user_name.getEditText().setText(personName);
            email.getEditText().setText(personEmail);
            Glide.with(this).load(user.getPhotoUrl()).into(img);

            // TODO: extract data recipe num and comment num from firebase

            // Initialize recipe list
            ArrayList<String> recipeList = new ArrayList<String>();

            // get all recipe id
            db.collection("recipes")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                for (QueryDocumentSnapshot document: task.getResult()){
                                    recipeList.add(document.getId());
                                }

                                // get comments from each recipe
                                for (int i=0; i<recipeList.size(); i++){
                                    String path = "recipes/"+recipeList.get(i)+"/comments";

                                    db.collection(path)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()){
                                                        Integer count = 0;
                                                        for (QueryDocumentSnapshot document: task.getResult()){
                                                            String author = document.getString("Author");
                                                            String comment = document.getData().toString();
//                                                            Log.d("debug", author);
                                                            if (author.contains(personName)){
                                                                count++;
                                                            }
                                                        }
                                                        commentSum += count;
//                                                        Log.d("debug", commentSum.toString());
                                                        commentCount.setText(commentSum.toString());
                                                    }
                                                }
                                            });
                                }
                                // count the recipe number of user
                            }else{
                                Log.d("debug", "Error getting documents");
                                Log.d("debug", task.getException().toString());
                            }
                        }
                    });
        }
        return root;
    }
}