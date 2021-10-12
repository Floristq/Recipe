package com.example.recipeapp;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

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


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Comments#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Comments extends Fragment {

    ImageButton button;
    EditText editText;
    ListView listView;
    String personName;

    private FirebaseAuth mFirebaseAuth;

    private List<String> namesList = new ArrayList<>();

    private List<String> commentsList = new ArrayList<>();

    private View root = null;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Comments() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Comments.
     */
    // TODO: Rename and change types and number of parameters
    public static Comments newInstance(String param1, String param2) {
        Comments fragment = new Comments();
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

        root = inflater.inflate(R.layout.fragment_comments, container,false);

        button = root.findViewById(R.id.imageButton);
        editText = root.findViewById(R.id.commentText);
        listView = root.findViewById(R.id.listview);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            personName = user.getDisplayName();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                Map<String,String> map = new HashMap<>();

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/YY HH:mm:ss");
                LocalDateTime now = LocalDateTime.now(ZoneId.of("GMT+11:00"));

                map.put("Author", dtf.format(now) + " - " + personName + ":");
//                map.put("Author", personName);
                map.put("Content",editText.getText().toString());

                db.collection("recipes/Greek lemon roast potatoes/comments").document().set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getActivity(), "Comment posted!", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

        db.collection("recipes/Greek lemon roast potatoes/comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException error) {
                namesList.clear();
                commentsList.clear();

                for(DocumentSnapshot snapshot : documentSnapshots){
                    namesList.add(snapshot.getString("Author"));
                    commentsList.add(snapshot.getString("Content"));
                }


                HashMap<String,String> namecomments = new HashMap<>();


                for (int i = 0; i < namesList.size(); i++) {
                    namecomments.put(commentsList.get(i), namesList.get(i));
                }


                List<HashMap<String, String>> listItems =new ArrayList<>();

                SimpleAdapter adapter = new SimpleAdapter(getContext(), listItems, R.layout.list_item,
                        new String[]{"First Line","Second Line"},
                        new int[]{R.id.textView1, R.id.textView2});

                Iterator it = namecomments.entrySet().iterator();
                while (it.hasNext())
                {
                    HashMap<String,String> resultsMap = new HashMap<>();
                    Map.Entry pair = (Map.Entry)it.next();
                    resultsMap.put("First Line", pair.getValue().toString());
                    resultsMap.put("Second Line", pair.getKey().toString());
                    listItems.add(resultsMap);
                }

                Comparator<Map<String, String>> mapComparator = new Comparator<Map<String, String>>() {
                    public int compare(Map<String, String> m1, Map<String, String> m2) {
                        return m1.get("First Line").compareTo(m2.get("First Line"));
                    }
                };

                Collections.sort(listItems, mapComparator);
                listView.setAdapter(adapter);


            }
        });

        return root;
    }



}