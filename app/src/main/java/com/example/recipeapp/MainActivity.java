package com.example.recipeapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity  {

    TextView name, email;
    //Button signOut;
    public static final String ANONYMOUS = "anonymous";
    private GoogleSignInClient mSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private AppBarConfiguration mAppBarConfiguration;
//     private static final String TAG = "MainActivity";
//     private static final int ERROR_DIALOG_REQUEST = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        // Initialize Firebase Auth and check if the user is signed in
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser() == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        TextView name = (TextView) header.findViewById(R.id.name);
        TextView email = (TextView) header.findViewById(R.id.email);
        ImageView image = header.findViewById(R.id.profileImage);

        navigationView.getMenu().findItem(R.id.sign_out_button).setOnMenuItemClickListener(menuItem -> {
            signOut();
            return true;
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mSignInClient = GoogleSignIn.getClient(this, gso);

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            String personName = user.getDisplayName();
            String personEmail = user.getEmail();

            name.setText(personName);
            email.setText(personEmail);
            Glide.with(MainActivity.this)
                    .load(user.getPhotoUrl())
                    .into(image);
        }

        // Request for the permission ACCESS_FINE_LOCATION
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to LOCATION - requesting it");
                String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

                requestPermissions(permissions, 1);

            }
        }

//         if (isServiceOK()){
//             init();
//         }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    private void signOut() {
        mFirebaseAuth.signOut();
        mSignInClient.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();

    }
    
//     private void init(){
//         Button btnMap = (Button) findViewById(R.id.btnMap);
//         btnMap.setOnClickListener(new View.OnClickListener() {
//             @Override
//             public void onClick(View view) {
//                 Intent intent = new Intent(MainActivity.this, MapActivity.class);
//                 startActivity(intent);
//             }
//         });
//     }
    
//     public boolean isServiceOK(){
//         Log.d(TAG, "isServiceOK: checking google services version");
//         int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
//         if (available== ConnectionResult.SUCCESS){
//             //everything is fine and the user can make map requests
//             Log.d(TAG, "isServicesOK: Google Play Services is working");
//             return true;
//         }
//         else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
//             //an error occured but we can fix
//             Log.d(TAG, "isServiceOK: an error occured but we can fix");
//             Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available,ERROR_DIALOG_REQUEST);
//             dialog.show();
//         }else {
//             Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
//         }
//         return false;
//     }
}
