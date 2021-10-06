package com.example.recipeapp.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.recipeapp.R;
import com.google.android.material.textfield.TextInputLayout;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FirebaseAuth mFirebaseAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextView name = root.findViewById(R.id.profile_display_name);
        final TextInputLayout email = root.findViewById(R.id.profile_textfield_email);
        final TextInputLayout user_name = root.findViewById(R.id.profile_textfield_name);
        final ImageView img = root.findViewById(R.id.profile_image);

        // Get the user info from firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            String personName = user.getDisplayName();
            String personEmail = user.getEmail();
            name.setText(personName);
            user_name.getEditText().setText(personName);
            email.getEditText().setText(personEmail);
            Glide.with(this).load(user.getPhotoUrl()).into(img);

            // TODO: extract data recipe num and comment num from firebase
        }
        return root;
    }
}