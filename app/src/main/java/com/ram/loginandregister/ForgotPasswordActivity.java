package com.ram.loginandregister;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Button btnPwdReset;
    private EditText etResetEmail;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;

    private static final String TAG = "ForgotPasswordActivity";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Toolbar resetToolbar = findViewById(R.id.forgot_password_toolbar);

        resetToolbar.setTitle("Forgot Password");
        setSupportActionBar(resetToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //swipe to refresh
        swipeToRefresh();

        etResetEmail = findViewById(R.id.et_reset_pwd_email);
        btnPwdReset = findViewById(R.id.btn_reset_pwd);
        progressBar = findViewById(R.id.reset_pwd_progressbar);

        btnPwdReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etResetEmail.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter your registerd email id", Toast.LENGTH_SHORT).show();
                    etResetEmail.setError("Email is Required!!");
                    etResetEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter Valid email id", Toast.LENGTH_SHORT).show();
                    etResetEmail.setError("Valid Email is Required!!");
                    etResetEmail.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    resetPassword(email);
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

    private void resetPassword(String email) {
        authProfile = FirebaseAuth.getInstance();
        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ForgotPasswordActivity.this, "Please check your inbox for password reset link", Toast.LENGTH_SHORT).show();

                    Intent mainActivityIntent = new Intent(ForgotPasswordActivity.this, MainActivity.class);

                    //Clear staack to prevent user comming back to forgotpasswordactivity when he click back button after logout
                    mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                            Intent.FLAG_ACTIVITY_NEW_TASK );
                    startActivity(mainActivityIntent);
                    finish();
                } else {

                    try{
                        throw task.getException();

                    }catch (FirebaseAuthInvalidUserException e){
                        etResetEmail.setError("User Does not exists or no longer valid, please register again");
                    } catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(ForgotPasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}