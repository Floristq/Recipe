package com.example.recipeapp.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.example.recipeapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore db;
    private Integer commentSum = 0;
    private Integer recipeSum = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextInputLayout email = root.findViewById(R.id.profile_textfield_email);
        final TextInputLayout user_name = root.findViewById(R.id.profile_textfield_name);
        final ImageView img = root.findViewById(R.id.profile_image);
        final TextView commentCount = root.findViewById(R.id.profile_comment_num);
        final TextView recipeCount = root.findViewById(R.id.profile_recipe_num);

        // Get the user info from firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();

        // Initialize firebase db
        db = FirebaseFirestore.getInstance();

        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String totalRecipes = String.valueOf(document.get("Recipes"));
                                String totalComments = String.valueOf(document.get("Comments"));

                                if (totalRecipes == null) totalRecipes = "0";
                                if (totalComments == null) totalComments = "0";

                                recipeCount.setText(totalRecipes.toString());
                                commentCount.setText(totalComments.toString());

                            } else {
                                recipeCount.setText("0");
                                commentCount.setText("0");
                            }
                        }


                    });

            user_name.getEditText().setText(user.getDisplayName());
            email.getEditText().setText(user.getEmail());
            Glide.with(this).load(user.getPhotoUrl()).into(img);

            ((CardView) recipeCount.getParent().getParent()).setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putBoolean("ownRecipeOnly", true);
                Navigation.findNavController(v).navigate(R.id.viewRecipe, bundle);
            });
        }
        return root;
    }
}