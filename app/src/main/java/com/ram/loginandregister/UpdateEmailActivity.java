package com.ram.loginandregister;



import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdateEmailActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;

    private TextView textViewAuthenticate;

    private String userOldEmail, userNewEmail, userPassword;

    private Button btnUpdateEmail;

    private EditText editTextNewEmail, editTextPwd;

    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        Toolbar toolbar = findViewById(R.id.update_Email_toolbar);
        toolbar.setTitle("Update Email");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Swipe to refresh
        swipeToRefresh();

        progressBar = findViewById(R.id.progressbar);
        editTextPwd = findViewById(R.id.et_update_email_verify_password);
        editTextNewEmail = findViewById(R.id.et_update_email_new);
        textViewAuthenticate = findViewById(R.id.tv_update_email_authenticated);
        btnUpdateEmail = findViewById(R.id.btn_update_email);

        btnUpdateEmail.setEnabled(false); //make update email button until user verified
        editTextNewEmail.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        //show hide password using eye Icon
        ImageView imgShowHidepwd = findViewById(R.id.imageView_show_hide_pwd);
        imgShowHidepwd.setImageResource(R.drawable.ic_show_pwd);

        imgShowHidepwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextPwd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //if password is visible then hide it
                    editTextPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //change icon
                    imgShowHidepwd.setImageResource(R.drawable.ic_show_pwd);
                }else {
                    editTextPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imgShowHidepwd.setImageResource(R.drawable.ic_hide_pwd);
                }
            }
        });

        //Set old email id on textview
        userOldEmail = firebaseUser.getEmail();
        TextView textViewOldEmial = findViewById(R.id.tv_update_email_old);
        textViewOldEmial.setText(userOldEmail);

        if(firebaseUser.equals("")){
            Toast.makeText(this, "Something went wrong!! user details not avaiable", Toast.LENGTH_SHORT).show();

        } else {
            reAuthenticate(firebaseUser);
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

    //Reauthnticate user before updating email
    private void reAuthenticate(FirebaseUser firebaseUser) {
        Button btnverifyUser = findViewById(R.id.btn_authenticate_user);

        btnverifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //obtain the password for authentication
                userPassword = editTextPwd.getText().toString().trim();

                if (TextUtils.isEmpty(userPassword)){
                    Toast.makeText(UpdateEmailActivity.this, "Password is needed to continue!", Toast.LENGTH_SHORT).show();
                    editTextPwd.setError("Please enter your password for authentication");
                    editTextPwd.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail, userPassword);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(UpdateEmailActivity.this, "Password has been Verified." +
                                        " You can update email now. " , Toast.LENGTH_SHORT).show();
                                //set textView to shwo user is authenticated
                                textViewAuthenticate.setText("You are authenticated. you can update your email now.");

                                //Disable EditText for password and enable Edittext for new email and update email button
                                btnverifyUser.setEnabled(false);
                                editTextPwd.setEnabled(false);
                                editTextNewEmail.setEnabled(true);
                                btnUpdateEmail.setEnabled(true);

                                //Change color of Update emaiil button
                                btnUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmailActivity.this,
                                        R.color.dark_green));

                                btnUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        userNewEmail = editTextNewEmail.getText().toString().trim();
                                        if(TextUtils.isEmpty(userNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this, "New Email is required", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Plese enter new email");
                                            editTextNewEmail.requestFocus();
                                        } else if (!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()){
                                            Toast.makeText(UpdateEmailActivity.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Plese provide valid email");
                                            editTextNewEmail.requestFocus();
                                        } else if (userOldEmail.matches(userNewEmail)) {
                                            Toast.makeText(UpdateEmailActivity.this, "New Email Can't be same as Old email", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Plese provide valid email");
                                            editTextNewEmail.requestFocus();
                                        } else {
                                            progressBar.setVisibility(View.VISIBLE);
                                            updateEmail(firebaseUser);
                                        }
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();

                                }catch (Exception e){
                                    Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()){

                    //verify Email
                    firebaseUser.sendEmailVerification();
                    Toast.makeText(UpdateEmailActivity.this, "Email has benn updated, please verify your new email", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateEmailActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e){
                        Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
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
            NavUtils.navigateUpFromSameTask(UpdateEmailActivity.this);
        }else if(id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile) {
            Intent updateProfileIntent  = new Intent(UpdateEmailActivity.this, UpdateProfileActivity.class);
            startActivity(updateProfileIntent);
            finish();
        } else if (id == R.id.menu_update_email) {
            Intent updateEmailIntent  = new Intent(UpdateEmailActivity.this, UpdateEmailActivity.class);
            startActivity(updateEmailIntent);
            finish();
        } else if (id == R.id.menu_settings) {
            Toast.makeText(UpdateEmailActivity.this, "Menu settings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menu_change_password) {
            Intent changePasswordIntent  = new Intent(UpdateEmailActivity.this, ChangePasswordActivity.class);
            startActivity(changePasswordIntent);
            finish();
        } else if (id == R.id.menu_delete_profile) {
            Intent deleteProfileIntent  = new Intent(UpdateEmailActivity.this, DeleteProfileActivity.class);
            startActivity(deleteProfileIntent);
            finish();
        } else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent mainActivityIntent = new Intent(UpdateEmailActivity.this, MainActivity.class);

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