package com.example.recipeapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.recipeapp.autocompleteadapter.AdapterItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.content.Context.INPUT_METHOD_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class Comments extends Fragment {

    ImageButton button;
    EditText editText;
    ListView listView;
    String userName;
    String userEmail;
    LinearLayout emptyConversationView;
    ProgressBar dataLoadingBar;

    String recipeId;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth mFirebaseAuth;

    private View root = null;

    private ListenerRegistration registration;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Comments() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        root = inflater.inflate(R.layout.fragment_comments, container,false);

        button = root.findViewById(R.id.imageButton);
        editText = root.findViewById(R.id.commentText);
        listView = root.findViewById(R.id.listview);
        emptyConversationView = root.findViewById(R.id.emptyConversationView);
        dataLoadingBar = root.findViewById(R.id.dataLoadingBar);

        // Determines that the first request to fetch the comments is complete
        final boolean[] commentInitialized = {false};
        final boolean[] commentFound = {false};

        Bundle bundle = this.getArguments();
        if (bundle != null && bundle.containsKey("id")) {
            recipeId = bundle.getString("id");
        }

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            userName = user.getDisplayName();
            userEmail = user.getEmail();
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd LLL, uu");

        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                String newMessage = editText.getText().toString();

                if (!newMessage.isEmpty()){
                    Map<String, Object> map = new HashMap<>();

                    map.put("AuthorName", userName);
                    map.put("AuthorEmail", userEmail);
                    map.put("Message", newMessage);
                    map.put("Time", System.currentTimeMillis());

                    WriteBatch batch = db.batch();

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("Comments", FieldValue.increment(1));

                    batch.set(
                        db.collection("users").document(user.getUid()),
                        userMap,
                        SetOptions.merge()
                    );

                    batch.set(
                        db.collection("recipes/" + recipeId + "/comments").document(),
                        map
                    );

                    batch.commit()
                        .addOnFailureListener(ex -> {
                            Toast.makeText(
                                    getActivity(),
                                    "Server is not responding at this moment, please try again later!",
                                    Toast.LENGTH_LONG
                            ).show();
                        });

                } else {
                    Toast.makeText(getActivity(), "Your comment can't be empty!", Toast.LENGTH_LONG).show();
                }

                try {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    editText.setText("");
                } catch (Exception e) {
                    // Toast.makeText(getActivity(), "Error: " + e, Toast.LENGTH_LONG).show();
                }
            }

        });

        registration = db.collection("recipes/" + recipeId + "/comments")
            .whereNotEqualTo("Time", 0)
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "listen:error", e);
                        return;
                    }

                    if (!commentInitialized[0]) {
                        commentInitialized[0] = true;
                        dataLoadingBar.setVisibility(View.GONE);
                    }

                    if (!commentFound[0]) {
                        if (documentSnapshots.size() == 0) {
                            emptyConversationView.setVisibility(View.VISIBLE);
                        } else {
                            emptyConversationView.setVisibility(View.GONE);
                            commentFound[0] = true;
                        }
                    }


                    List<HashMap<String, String>> comments = new ArrayList<>();

                    for (DocumentSnapshot snapshot : documentSnapshots) {
                        HashMap<String, String> comment = new HashMap<>();

                        long time = (Long) snapshot.get("Time");
                        LocalDateTime messageDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
                        comment.put("timeMS", String.valueOf(time));
                        comment.put("Time", timeFormatter.format(messageDate));

                        comment.put("AuthorName", snapshot.getString("AuthorName"));
                        comment.put("Message", snapshot.getString("Message"));

                        comments.add(comment);
                    }

                    Comparator<Map<String, String>> mapComparator = new Comparator<Map<String, String>>() {
                        public int compare(Map<String, String> comment1, Map<String, String> comment2) {
                            return comment1.get("timeMS").compareTo(comment2.get("timeMS"));
                        }
                    };

                    Collections.sort(comments, mapComparator);
                    List<HashMap<String, String>> adapterComments = new ArrayList<>();

                    String lastDate = "",
                        lastName = "";

                    for (HashMap<String, String> comment: comments) {
                        LocalDateTime messageDate = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(Long.parseLong(comment.get("timeMS"))),
                                ZoneId.systemDefault()
                        );

                        String dateStr = dateFormatter.format(messageDate);
                        if (!dateStr.equals(lastDate)) {
                            HashMap<String, String> item = new HashMap<>();
                            item.put("dateOnly", "true");
                            item.put("Date", dateStr);

                            adapterComments.add(item);

                            lastDate = dateStr;
                        }

                        String name = comment.get("AuthorName");
                        if (lastName.equals(name)) {
                            comment.put("AuthorName", "");
                        } else {
                            lastName = name;
                        }

                        adapterComments.add(comment);
                    }

                    CommentsAdapter adapter = new CommentsAdapter(getContext(), adapterComments);

                    listView.setAdapter(adapter);
                }
            });

        return root;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (registration!= null) {
            registration.remove();
            registration = null;
        }
    }

    private class CommentsAdapter extends ArrayAdapter<HashMap<String, String>> {

        public final int[] colors = {
                Color.RED,
                Color.GREEN,
                Color.YELLOW,
                Color.CYAN,
                Color.GRAY,
                Color.MAGENTA
        };

        public CommentsAdapter(@NonNull Context context, List<HashMap<String, String>> data) {
            super(context, 0, data);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public View getView(int position, View view, ViewGroup parent) {

            HashMap<String, String> item = (HashMap<String, String>) getItem(position);

            if (item.getOrDefault("dateOnly", "false").equals("true")) {
                view = LayoutInflater.from(getContext()).inflate(
                    R.layout.date_view, parent, false
                );

                ((TextView) view.findViewById(R.id.date)).setText(item.get("Date"));

            } else {
                view = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false
                );

                String name = item.get("AuthorName");
                if (!name.isEmpty()) {
                    char letter = Character.toUpperCase(name.charAt(0));
                    int code = (int) letter;
                    TextDrawable drawable = TextDrawable.builder()
                            .buildRound(String.valueOf(letter), colors[(code - 65) % colors.length]);
                    ((ImageView) view.findViewById(R.id.profileImage)).setImageDrawable(drawable);
                } else {
                    // This will indicate the same author is commenting!
                    name = "~";
                }

                ((TextView) view.findViewById(R.id.name)).setText(name);
                ((TextView) view.findViewById(R.id.time)).setText(item.get("Time"));
                ((TextView) view.findViewById(R.id.message)).setText(item.get("Message"));
            }

            return view;
        }

    }



}