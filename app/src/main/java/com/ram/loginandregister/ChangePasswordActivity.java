package com.ram.loginandregister;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    FirebaseAuth authProfile;
    private EditText editTextPwdCurr, editTextPwdNew;
    private TextView textViewAuthenticated;
    private Button buttonChangePwd, buttonReAuthenticate;

    private ProgressBar progressBar;
    private String userPwdCurr;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        //setting the title for toolbar
        Toolbar toolbar = findViewById(R.id.change_pwd_toolbar);
        toolbar.setTitle("Change Password");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Swipe to refresh
        swipeToRefresh();

        //binding the views
        editTextPwdNew = findViewById(R.id.et_change_pwd_new);
        editTextPwdCurr = findViewById(R.id.et_change_pwd_current);
        textViewAuthenticated = findViewById(R.id.tv_chagne_pwd_authenticated);
        buttonReAuthenticate = findViewById(R.id.btn_change_pwd_authenticate);

        progressBar = findViewById(R.id.progressbar);
        buttonChangePwd = findViewById(R.id.btn_change_pwd);

        //show hide password using eye Icon
        ImageView imgShowHidepwd = findViewById(R.id.imageView_show_hide_curr_pwd);
        imgShowHidepwd.setImageResource(R.drawable.ic_show_pwd);

        imgShowHidepwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextPwdCurr.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //if password is visible then hide it
                    editTextPwdCurr.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //change icon
                    imgShowHidepwd.setImageResource(R.drawable.ic_show_pwd);
                }else {
                    editTextPwdCurr.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imgShowHidepwd.setImageResource(R.drawable.ic_hide_pwd);
                }
            }
        });


        //Disable edittext for new password, Confirm new Password and make change pwd button unclickable till user is authenticated
        editTextPwdNew.setEnabled(false);
        buttonChangePwd.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if( firebaseUser.equals("")){
            Toast.makeText(this, "Something went wrong!! User details are not available", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticateUser(firebaseUser);

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

    //Reauthneticate user before changing password
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwdCurr = editTextPwdCurr.getText().toString();

                if(TextUtils.isEmpty(userPwdCurr)){
                    Toast.makeText(ChangePasswordActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
                    editTextPwdCurr.setError("plase enter current Password to authenticate!!");
                    editTextPwdCurr.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    //ReAuthenticate user now
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwdCurr);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                //Disable edit text for current  pwd and enable new password and button
                                editTextPwdCurr.setEnabled(false);
                                buttonReAuthenticate.setEnabled(false);
                                //enable new bu
                                editTextPwdNew.setEnabled(true);
                                buttonChangePwd.setEnabled(true);

                                //show hide password using for new password eye Icon
                                ImageView imgShowHidepwdnew = findViewById(R.id.imageView_show_hide_new_pwd);
                                imgShowHidepwdnew.setImageResource(R.drawable.ic_show_pwd);

                                imgShowHidepwdnew.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(editTextPwdNew.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                                            //if password is visible then hide it
                                            editTextPwdNew.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                            //change icon
                                            imgShowHidepwdnew.setImageResource(R.drawable.ic_show_pwd);
                                        }else {
                                            editTextPwdNew.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                                            imgShowHidepwdnew.setImageResource(R.drawable.ic_hide_pwd);
                                        }
                                    }
                                });

                                //set TextView to show user authenticated/verfied
                                textViewAuthenticated.setText("You are Authenticated/verified" +
                                        "you can change password now!");
                                Toast.makeText(ChangePasswordActivity.this, "Password hasbeen verified" +
                                        "chagne password now", Toast.LENGTH_SHORT).show();
                                //update color of change password button
                                buttonChangePwd.setBackgroundTintList(ContextCompat.getColorStateList(
                                        ChangePasswordActivity.this, R.color.dark_green));

                                buttonChangePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //calling change pwd method
                                        changePwd(firebaseUser);
                                    }
                                });
                            } else{
                                try {
                                    throw task.getException();

                                } catch (Exception e){
                                    Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void changePwd(FirebaseUser firebaseUser) {
        //Toast.makeText(this, "inside the Changepwd", Toast.LENGTH_SHORT).show();
        String userPwdNew = editTextPwdNew.getText().toString();




        if (TextUtils.isEmpty(userPwdNew)){
            Toast.makeText(this, "new Password is required!!", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please enter your new password!!");
            editTextPwdNew.requestFocus();
        } /*else if (TextUtils.isEmpty(userPwdNewConfirm)) {
            Toast.makeText(this, "Please confirm your new password!!", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNew.setError("Please re-enter new password!!");
            editTextPwdConfirmNew.requestFocus();
        } else if (!userPwdNew.matches(userPwdNewConfirm)) {
            Toast.makeText(this, "Password didn't Match!!", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNew.setError("Please re-enter same password!!");
            editTextPwdConfirmNew.requestFocus();
        }*/ else if (userPwdCurr.matches(userPwdNew)){
            Toast.makeText(this, "New password and old password can not be same!!", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please enter a new password!!");
            editTextPwdNew.requestFocus();
        }else {
            progressBar.setVisibility(View.VISIBLE);
            firebaseUser.updatePassword(userPwdNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChangePasswordActivity.this, "Password Has been Changed", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e){
                            Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

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
            NavUtils.navigateUpFromSameTask(ChangePasswordActivity.this);
        }else if(id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile) {
            Intent updateProfileIntent  = new Intent(ChangePasswordActivity.this, UpdateProfileActivity.class);
            startActivity(updateProfileIntent);
            finish();
        } else if (id == R.id.menu_update_email) {
            Intent updateEmailIntent  = new Intent(ChangePasswordActivity.this, UpdateEmailActivity.class);
            startActivity(updateEmailIntent);
            finish();
        } else if (id == R.id.menu_settings) {
            Toast.makeText(ChangePasswordActivity.this, "Menu settings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menu_change_password) {
            Intent changePasswordIntent  = new Intent(ChangePasswordActivity.this, ChangePasswordActivity.class);
            startActivity(changePasswordIntent);
            finish();
        } else if (id == R.id.menu_delete_profile) {
            Intent deleteProfileIntent  = new Intent(ChangePasswordActivity.this, DeleteProfileActivity.class);
            startActivity(deleteProfileIntent);
        } else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent mainActivityIntent = new Intent(ChangePasswordActivity.this, MainActivity.class);

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