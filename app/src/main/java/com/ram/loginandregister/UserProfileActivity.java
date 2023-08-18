package com.ram.loginandregister;



import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;


public class UserProfileActivity extends AppCompatActivity {

    private TextView tvWelcome, tvFullName, tvEmail, tvDOB, tvMobile, tvGender, tvRegisteredDate;
    private ProgressBar progressBar, profileProgressbar;
    private String fullName, email, dob, gender, mobile;
    private Long registeredDate;
    private ImageView imageView;

    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("HOME");*/
        Toolbar toolbar = findViewById(R.id.user_profile_toolbar);

        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        swipeToRefresh();

        //getSupportActionBar().setTitle("HOME");

        tvWelcome = findViewById(R.id.tv_show_welcome);
        tvFullName = findViewById(R.id.tv_show_fullname);
        tvEmail = findViewById(R.id.tv_show_email);
        tvDOB = findViewById(R.id.tv_show_dob);
        tvMobile = findViewById(R.id.tv_show_mobile);
        tvGender = findViewById(R.id.tv_show_gender);
        progressBar = findViewById(R.id.user_progressbar);
        tvRegisteredDate = findViewById(R.id.tv_show_register_date);
        profileProgressbar = findViewById(R.id.progress_bar_profile_pic);

        //set onClickListener for imageview to uploadprofilepicture
        imageView = findViewById(R.id.img_profile_dp);
        imageView.setOnClickListener(v -> {
            Intent uploadpicIntent = new Intent(UserProfileActivity.this, UploadProfilePicActivity.class);
            startActivity(uploadpicIntent);
        });


        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null ){
            Toast.makeText(this, "Something went wrong user details are not available at the moment!!", Toast.LENGTH_SHORT).show();

        }else {
            checkifEmailVerified(firebaseUser);
            profileProgressbar.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
        }
    }

    private void swipeToRefresh() {
        //LOOk for container
        swipeContainer = findViewById(R.id.swipeContainer);

        //setup the refresh listener
        swipeContainer.setOnRefreshListener(() -> {
            //code to refresh goes here. make sure to call swipeContainer.setRefreshing(false) once the refresh is complete
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
            swipeContainer.setRefreshing(false);

        });

        //configure the colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }

    //user coming userprofileactivity after successful Registration
    private void checkifEmailVerified(FirebaseUser firebaseUser) {
        if(! firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        //setup the Alert Builder
        AlertDialog.Builder builder= new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please Verify your email now. You can not login without email verification next time.");

        //open email apps if user clicks/taps Continue
        builder.setPositiveButton("Continue", (dialog, which) -> {
            Intent emailIntent = new Intent(Intent.ACTION_MAIN);
            emailIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // to email app in new window not in out app
            startActivity(emailIntent);
        });
        //create the AlertDialog
        AlertDialog alertDialogEmail = builder.create();

        //show the alertdialog
        alertDialogEmail.show();
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();

        //user registered date
        FirebaseUserMetadata metadata = firebaseUser.getMetadata();
        //Get the register date of the user
        long registerTimeStamp = metadata.getCreationTimestamp();

        //Define a Pattern for the date
        //String datePattern = "E, dd MMMM yyyy hh:mm a z"; //Day, dd MMMM yyyy hh:mm AM/PM Time zoneSimp
        String datePattern = "dd/MM/yyyy"; //Day, dd MMMM yyyy hh:mm AM/PM Time zoneSimp
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        sdf.setTimeZone(TimeZone.getDefault());
        String register = sdf.format(new Date(registerTimeStamp));

        //String registerDate = getResources().getString(register);
        tvRegisteredDate.setText(register);
        //user registered date | end

        //Extract user reference from database for "Registered users"
        DatabaseReference referenceProfie = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfie.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUsersDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if(readUsersDetails != null){
                    fullName = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();
                    dob = readUsersDetails.dob;
                    gender = readUsersDetails.gender;
                    mobile = readUsersDetails.mobile;


                    tvWelcome.setText(getString(R.string.welcome_head_profile, fullName));//setting welcome textview
                    tvFullName.setText(fullName);
                    tvEmail.setText(email);
                    tvDOB.setText(dob);
                    tvMobile.setText(mobile);
                    tvGender.setText(gender);

                    //Set User Dp (After user has uploaded)

                    Uri uri = firebaseUser.getPhotoUrl();


                    //ImageViewer seTimageURI() should not be used with regular URis. so we are using Picasso
                    Picasso.with(UserProfileActivity.this).load(uri).into(imageView);


                    //String registerDate = getResources().getString(register);
                    tvRegisteredDate.setText(register);

                    // Convert the registrationDate to a human-readable format (if needed) and display it in the TextView
                    //String formattedDate = convertTimestampToString(registeredDate);
                    //tvRegisteredDate.setText(formattedDate);
                } else{
                    Toast.makeText(UserProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                profileProgressbar.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Helper method to convert a timestamp to a human-readable date format (if needed)
    private String convertTimestampToString(long timestamp) {
        // Convert the timestamp to a Date object
        Date date = new Date(timestamp);

        // Define the desired date format (e.g., "dd MMM yyyy HH:mm:ss")
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());

        // Format the date as a string
        return sdf.format(date);
    }

    //Creating Actionbar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //when any menu item selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(UserProfileActivity.this);
        } else if(id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile) {
            Intent updateProfileIntent  = new Intent(UserProfileActivity.this, UpdateProfileActivity.class);
            startActivity(updateProfileIntent);
        } else if (id == R.id.menu_update_email) {
            Intent updateEmailIntent  = new Intent(UserProfileActivity.this, UpdateEmailActivity.class);
            startActivity(updateEmailIntent);
        } else if (id == R.id.menu_settings) {
            Toast.makeText(this, "Menu settings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menu_change_password) {
            Intent changePasswordIntent  = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
            startActivity(changePasswordIntent);
        } else if (id == R.id.menu_delete_profile) {
            Intent deleteProfileIntent  = new Intent(UserProfileActivity.this, DeleteProfileActivity.class);
            startActivity(deleteProfileIntent);
        } else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent mainActivityIntent = new Intent(UserProfileActivity.this, MainActivity.class);

            //Clear staack to prevent user comming back to user profile activity when he click back button after logout
            mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity(mainActivityIntent);
            finish();
        } else {
            Toast.makeText(this, "Something went Wrong!!", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}