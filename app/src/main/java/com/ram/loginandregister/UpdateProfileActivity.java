package com.ram.loginandregister;



import static java.util.Objects.requireNonNull;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText etUpdateName, etUpdateDOb, etUpdateMobile;
    private RadioGroup rgUpdateGender;
    private RadioButton rbGenderSelected;
    private String textFullName, textDOB, textGender, textMobile;

    private FirebaseAuth authProfile;
    private ProgressBar progressBar;

    private SwipeRefreshLayout swipeContainer;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        Toolbar toolbar = findViewById(R.id.update_profile_toolbar);
        toolbar.setTitle("Update Profile Details");
        setSupportActionBar(toolbar);
        requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Swipe to refresh
        swipeToRefresh();

        progressBar = findViewById(R.id.update_profile_progressbar);
        etUpdateName = findViewById(R.id.et_update_profile_name);
        etUpdateDOb = findViewById(R.id.et_update_profile_dob);
        etUpdateMobile = findViewById(R.id.et_update_profile_mobile);

        rgUpdateGender = findViewById(R.id.rg_update_gender);
        ImageView imgDatePicker = findViewById(R.id.imageView_date_picker);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        //show Profile Data
        showProfile(requireNonNull(firebaseUser));

        //upload profile pic
        TextView tvUploadProfilePic = findViewById(R.id.tv_update_upload_profile_pic);
        tvUploadProfilePic.setOnClickListener(v -> {
            Intent intent = new Intent(UpdateProfileActivity.this, UploadProfilePicActivity.class);
            startActivity(intent);
            finish();
        });

        //Update Email
        TextView tvUpdateEmail = findViewById(R.id.tv_profile_update_email);
        tvUpdateEmail.setOnClickListener(v -> {
            Intent intent = new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        });

        //Date picker DOB
        //Setting up DatePicker on EditText
        //etUpdateDOb
        imgDatePicker.setOnClickListener(v -> {
            //Extracting saved dd, mm, yyyy into different variables by creating an array delimeter by "/
            String[] textSADoB = textDOB.split("/");





            int day = Integer.parseInt(textSADoB[0]);
            int month =  Integer.parseInt(textSADoB[1]) - 1; //to take care of month index startingfrom 0
            int year =  Integer.parseInt(textSADoB[2]);

            DatePickerDialog picker;

            // DatePicker Dialog
            picker = new DatePickerDialog(UpdateProfileActivity.this, (view, year1, month1, dayOfMonth) -> etUpdateDOb.setText(dayOfMonth + "/" + (month1 + 1) + "/"+ year1),year, month, day);
            picker.show();
        });

        //Update Profile Button
        Button btnUpdateProfile = findViewById(R.id.btn_update_profile);
        btnUpdateProfile.setOnClickListener(v -> updateProfile(firebaseUser));
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

    private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGenderId = rgUpdateGender.getCheckedRadioButtonId();
        rbGenderSelected = findViewById(selectedGenderId);

        //Validate the Mobile Number using matcher and pattern (Regular Expression)
        String mobileRegex = "[6-9][0-9]{9}"; //First no. can be {6,8,9} and rest 9 nos can be any no.
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        mobileMatcher = mobilePattern.matcher(textMobile);

        if(TextUtils.isEmpty(textFullName)){
            Toast.makeText(UpdateProfileActivity.this, "Plaese enter your full name", Toast.LENGTH_LONG).show();
            etUpdateName.setError("Full Name is Required");
            etUpdateName.requestFocus();
        }  else if (TextUtils.isEmpty(textDOB)) {
            Toast.makeText(UpdateProfileActivity.this, "Plase enter your DOB", Toast.LENGTH_SHORT).show();
            etUpdateDOb.setError("Dob is required");
            etUpdateDOb.requestFocus();
        } else if (TextUtils.isEmpty(rbGenderSelected.getText())) {
            Toast.makeText(UpdateProfileActivity.this, "Please select your gender", Toast.LENGTH_SHORT).show();
            rbGenderSelected.setError("Gender is Required!!");
            rbGenderSelected.requestFocus();
        } else if (TextUtils.isEmpty(textMobile)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your Mobile", Toast.LENGTH_SHORT).show();
            etUpdateMobile.setError("Mobile Number is required!!");
            etUpdateMobile.requestFocus();
        } else if (textMobile.length() != 10) {
            Toast.makeText(UpdateProfileActivity.this, "Please Re-enter your Mobile", Toast.LENGTH_SHORT).show();
            etUpdateMobile.setError("Mobile number should be 10 digits");
            etUpdateMobile.requestFocus();
        } else if (! mobileMatcher.find() ) {
            Toast.makeText(UpdateProfileActivity.this, "Please Re-enter your Mobile", Toast.LENGTH_SHORT).show();
            etUpdateMobile.setError("Mobile number is not valid");
            etUpdateMobile.requestFocus();
        }  else {
            //Obtain the data entered by user
            textGender = rbGenderSelected.getText().toString().trim();
            textFullName = etUpdateName.getText().toString().trim();
            textDOB = etUpdateDOb.getText().toString().trim();
            textMobile = etUpdateMobile.getText().toString().trim();

            //Enter user data into the Firebase Realtime Database.Set up dependencies
            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textDOB, textGender, textMobile);

            //Extract user reference from Database for "Registerd Users"
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

            String userId = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);

            referenceProfile.child(userId).setValue(writeUserDetails).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    //setting new display name
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(textFullName).build();
                    firebaseUser.updateProfile(profileUpdates);

                    Toast.makeText(UpdateProfileActivity.this, "Update Successfull!!", Toast.LENGTH_SHORT).show();

                    //stop user returning to UpdateProfileActivity on pressing back button and close the activity
                    Intent intent = new Intent(UpdateProfileActivity.this, UserProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    try{
                        throw requireNonNull(task.getException());
                    } catch (Exception e){
                        Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            });
        }
    }

    //fetch data from firebase and display
    private void showProfile(FirebaseUser firebaseUser) {
        String userIdofRegistered = firebaseUser.getUid();

        //Extract user reference from database for "registered users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

        progressBar.setVisibility(View.VISIBLE);

        referenceProfile.child(userIdofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null ){
                    textFullName = firebaseUser.getDisplayName();
                    textDOB = readUserDetails.dob;
                    textMobile = readUserDetails.mobile;
                    textGender = readUserDetails.gender;

                    //updating the details
                    etUpdateName.setText(textFullName);
                    etUpdateDOb.setText(textDOB);
                    etUpdateMobile.setText(textMobile);

                    //show gender through radio button
                    if(textGender.equals("Male")){
                        rbGenderSelected = findViewById(R.id.rb_update_male);

                    } else {
                        rbGenderSelected = findViewById(R.id.rb_update_female);
                    }
                    rbGenderSelected.setChecked(true);
                } else {
                    Toast.makeText(UpdateProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
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
            NavUtils.navigateUpFromSameTask(UpdateProfileActivity.this);
        }else if(id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile) {
            Intent updateProfileIntent  = new Intent(UpdateProfileActivity.this, UpdateProfileActivity.class);
            startActivity(updateProfileIntent);
            finish();
        } else if (id == R.id.menu_update_email) {
            Intent updateEmailIntent  = new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class);
            startActivity(updateEmailIntent);
            finish();
        }else if (id == R.id.menu_settings) {
            Toast.makeText(this, "Menu settings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menu_change_password) {
            Intent changePasswordIntent  = new Intent(UpdateProfileActivity.this, ChangePasswordActivity.class);
            startActivity(changePasswordIntent);
            finish();
        } else if (id == R.id.menu_delete_profile) {
            Intent deleteProfileIntent  = new Intent(UpdateProfileActivity.this, DeleteProfileActivity.class);
            startActivity(deleteProfileIntent);
            finish();
        } else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent mainActivityIntent = new Intent(UpdateProfileActivity.this, MainActivity.class);

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