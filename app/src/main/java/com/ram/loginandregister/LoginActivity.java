package com.ram.loginandregister;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginEmail, etLoginpwd;
    private ProgressBar progressBar;

    private FirebaseAuth authProfile;

    private ImageView imgShowHidepwd;

    private SwipeRefreshLayout swipeContainer;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.login_toolbar);
        toolbar.setTitle("Login");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Swipe to refresh
        swipeToRefresh();

        //Reset Password
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password_link);
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "You can reset your password", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
        //Register
        TextView tvRegisterUser = findViewById(R.id.tv_register_link);
        tvRegisterUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "You can Register Now", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        //getSupportActionBar().setTitle("Login");

        etLoginEmail = findViewById(R.id.et_login_email);
        etLoginpwd = findViewById(R.id.et_login_pwd);
        progressBar = findViewById(R.id.login_progressbar);

        authProfile = FirebaseAuth.getInstance();

        //show hide password using eye Icon
        imgShowHidepwd = findViewById(R.id.img_show_hide_pwd);
        imgShowHidepwd.setImageResource(R.drawable.ic_show_pwd);

        imgShowHidepwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etLoginpwd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //if password is visible then hide it
                    etLoginpwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //change icon
                    imgShowHidepwd.setImageResource(R.drawable.ic_show_pwd);
                }else {
                    etLoginpwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imgShowHidepwd.setImageResource(R.drawable.ic_hide_pwd);
                }
            }
        });


        //LOgin user
        Button buttonLogin = findViewById(R.id.btn_loginActivity);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textEmail = etLoginEmail.getText().toString().trim();
                String textPwd = etLoginpwd.getText().toString().trim();


                if (TextUtils.isEmpty(textEmail)){
                    Toast.makeText(LoginActivity.this, "Please enter the email id!!", Toast.LENGTH_LONG).show();
                    etLoginEmail.setError("Email is Required!!");
                    etLoginEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(LoginActivity.this, "Please Re-enter the email id!!", Toast.LENGTH_LONG).show();
                    etLoginEmail.setError("Valid Email is Required!!");
                    etLoginEmail.requestFocus();
                } else if (TextUtils.isEmpty(textPwd)) {
                    Toast.makeText(LoginActivity.this, "Please enter the password!!", Toast.LENGTH_LONG).show();
                    etLoginpwd.setError("Password is Required!!");
                    etLoginpwd.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(textEmail, textPwd);
                }
            }
        });
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

    private void loginUser(String textEmail, String textPwd) {
        authProfile.signInWithEmailAndPassword(textEmail, textPwd).addOnCompleteListener(LoginActivity.this, new  OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    //get the instance of the current user
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();

                    //check if email is verified before user can access their profile
                    if(firebaseUser.isEmailVerified()){
                        Toast.makeText(LoginActivity.this, "You are logged in now ", Toast.LENGTH_SHORT).show();

                        //open user Profile
                        //start the UserProfileActivity
                        startActivity(new Intent(LoginActivity.this, UserProfileActivity.class));
                        finish(); //close the Login Activity
                    } else {
                        firebaseUser.sendEmailVerification();
                        authProfile.signOut(); // sign out user
                        showAlertDialog();
                    }
                }else {
                    try {
                        throw task.getException();

                    } catch (FirebaseAuthInvalidUserException e){
                        etLoginEmail.setError("User does not exists or is not longer valid, Please register agian");
                        etLoginEmail.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e){
                        etLoginEmail.setError("Invalid Credentials. kindly check and reenter. ");
                        etLoginEmail.requestFocus();
                    } catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(LoginActivity.this, "Something went wrong ", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showAlertDialog() {
        //setup the Alert Builder
        AlertDialog.Builder builder= new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please Verify your email now. You can not login without email verification.");

        //open email apps if user clicks/taps Continue
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent emailIntent = new Intent(Intent.ACTION_MAIN);
                emailIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
                emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // to email app in new window not in out app
                startActivity(emailIntent);
            }
        });
        //create the AlertDialog
        AlertDialog alertDialogEmail = builder.create();

        //show the alertdialog
        alertDialogEmail.show();
    }

    //check if user is already logged in. in such case, take the user to the User's profile
    @Override
    protected void onStart() {

        super.onStart();
        if(authProfile.getCurrentUser() != null){
            Toast.makeText(this, "You are Already Logged In ", Toast.LENGTH_SHORT).show();

            //start the UserProfileActivity
            startActivity(new Intent(LoginActivity.this, UserProfileActivity.class));
            finish(); //close the Login Activity
        } else {
            Toast.makeText(this, "You can Login now ", Toast.LENGTH_SHORT).show();
        }
    }
}