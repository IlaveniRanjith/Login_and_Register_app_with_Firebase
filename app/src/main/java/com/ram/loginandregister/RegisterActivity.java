package com.ram.loginandregister;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.DatePickerDialog;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText etregisterFullName, etregisterEmail, etregisterDOB, etregisterMobile, etregisterPwd;

    private ProgressBar progressBar;
    private RadioGroup radiogroupRegisterGender;
    private RadioButton radioButtonRegisterGenderSelected;

    private DatePickerDialog picker;
    private Button buttonRegisterActivity;
    private SwipeRefreshLayout swipeContainer;

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.register_toolbar);
        toolbar.setTitle("User Registration");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //swipe to refresh
        swipeToRefresh();

        //getSupportActionBar().setTitle("Register");

        Toast.makeText(this, "You can register now", Toast.LENGTH_LONG).show();


        etregisterFullName = findViewById(R.id.et_register_full_name);
        etregisterEmail = findViewById(R.id.et_register_email);
        etregisterDOB  = findViewById(R.id.et_register_dob);
        etregisterMobile = findViewById(R.id.et_register_mobile);
        etregisterPwd = findViewById(R.id.et_register_pwd);

        progressBar = findViewById(R.id.progressbar);


        //Radio button for Gender
        radiogroupRegisterGender = findViewById(R.id.rg_register_genger);
        radiogroupRegisterGender.clearCheck();

        //imageview datepicker
        ImageView imgDatePicker = findViewById(R.id.imageView_date_picker);
        //setting up DatePicker on Calender Image
        imgDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                // DatePicker Dialog
                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        etregisterDOB.setText(dayOfMonth + "/" + (month + 1) + "/"+ year);
                    }
                } ,year, month, day);
                picker.show();
            }
        });

        //Setting up DatePicker on EditText
        /*etregisterDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                // DatePicker Dialog
                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        etregisterDOB.setText(dayOfMonth + "/" + (month + 1) + "/"+ year);
                    }
                } ,year, month, day);
                picker.show();
            }
        });*/

        //show hide password using eye Icon
        ImageView imgShowHidepwd = findViewById(R.id.imageView_show_hide_pwd);
        imgShowHidepwd.setImageResource(R.drawable.ic_show_pwd);

        imgShowHidepwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etregisterPwd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //if password is visible then hide it
                    etregisterPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //change icon
                    imgShowHidepwd.setImageResource(R.drawable.ic_show_pwd);
                }else {
                    etregisterPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imgShowHidepwd.setImageResource(R.drawable.ic_hide_pwd);
                }
            }
        });


        buttonRegisterActivity = findViewById(R.id.btn_register_activity);
        buttonRegisterActivity.setEnabled(false);
        //setting the buttonRegisterActivity is disabled until checkbox checked
        CheckBox chkBoxTerms = findViewById(R.id.chkbox_terms_conditions);
        chkBoxTerms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    buttonRegisterActivity.setEnabled(true);
                } else {
                    buttonRegisterActivity.setEnabled(false);
                    chkBoxTerms.setError("Please Accept Terms and Conditions!!");
                }
            }
        });

        buttonRegisterActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedGenderId = radiogroupRegisterGender.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelected = findViewById(selectedGenderId);

                //Obtained the Entered data
                String textFullName = etregisterFullName.getText().toString().trim();
                String textemail = etregisterEmail.getText().toString().trim();
                String textDOB = etregisterDOB.getText().toString().trim();
                String textMobile = etregisterMobile.getText().toString().trim();
                String textPwd = etregisterPwd.getText().toString().trim();

                String textGender; //can't obtain the value before verifying if any button was selected or not

                //Validate the Mobile Number using matcher and pattern (Regular Expression)
                String mobileRegex = "[6-9][0-9]{9}"; //First no. can be {6,8,9} and rest 9 nos can be any no.
                Matcher mobileMatcher;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher(textMobile);

                if(TextUtils.isEmpty(textFullName)){
                    Toast.makeText(RegisterActivity.this, "Plaese enter your full name", Toast.LENGTH_LONG).show();
                    etregisterFullName.setError("Full Name is Required");
                    etregisterFullName.requestFocus();
                } else if (TextUtils.isEmpty(textemail)) {
                    Toast.makeText(RegisterActivity.this, "Plaese enter your Email id", Toast.LENGTH_LONG).show();
                    etregisterEmail.setError("Email id is Required");
                    etregisterEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textemail).matches()) {
                    Toast.makeText(RegisterActivity.this, "Plaese re-enter your Email id", Toast.LENGTH_LONG).show();
                    etregisterEmail.setError("Valid Email id is Required");
                    etregisterEmail.requestFocus();
                } else if (TextUtils.isEmpty(textDOB)) {
                    Toast.makeText(RegisterActivity.this, "Plase enter your DOB", Toast.LENGTH_SHORT).show();
                    etregisterDOB.setError("Dob is required");
                    etregisterDOB.requestFocus();
                } else if (radiogroupRegisterGender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(RegisterActivity.this, "Please select your gender", Toast.LENGTH_SHORT).show();
                    radioButtonRegisterGenderSelected.setError("Gender is Required!!");
                    radioButtonRegisterGenderSelected.requestFocus();
                } else if (TextUtils.isEmpty(textMobile)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your Mobile", Toast.LENGTH_SHORT).show();
                    etregisterMobile.setError("Mobile Number is required!!");
                    etregisterMobile.requestFocus();
                } else if (textMobile.length() != 10) {
                    Toast.makeText(RegisterActivity.this, "Please Re-enter your Mobile", Toast.LENGTH_SHORT).show();
                    etregisterMobile.setError("Mobile number should be 10 digits");
                    etregisterMobile.requestFocus();
                } else if (! mobileMatcher.find() ) {
                    Toast.makeText(RegisterActivity.this, "Please Re-enter your Mobile", Toast.LENGTH_SHORT).show();
                    etregisterMobile.setError("Mobile number is not valid");
                    etregisterMobile.requestFocus();
                } else if(TextUtils.isEmpty(textPwd)){
                    Toast.makeText(RegisterActivity.this, "Please enter your Password", Toast.LENGTH_SHORT).show();
                    etregisterPwd.setError("Pass word is required!!");
                    etregisterPwd.requestFocus();
                } else if (textPwd.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password should atlease 6 digits", Toast.LENGTH_SHORT).show();
                    etregisterPwd.setError("Password is too weak");
                    etregisterPwd.requestFocus();
                }  else {
                    textGender = radioButtonRegisterGenderSelected.getText().toString().trim();
                    progressBar.setVisibility(View.VISIBLE);
                    registerUser(textFullName, textemail, textDOB,textGender, textMobile, textPwd);
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

    //Register user using Credentials given
    private void registerUser(String textFullName, String textemail, String textDOB, String textGender, String textMobile, String textPwd) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        //Create User Profile
        auth.createUserWithEmailAndPassword(textemail, textPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(RegisterActivity.this, "User Registerd successfully", Toast.LENGTH_LONG).show();
                    FirebaseUser firebaseUser = auth.getCurrentUser();




                    //Update Display Name of User
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                    firebaseUser.updateProfile(profileChangeRequest);

                    //Enter user data into the Firebase realtime database.
                    ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails( textDOB, textGender, textMobile);

                    //Extract User Reference from DataBase for "Registered User"
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                //Registered Date
                                // Get the current date and time when the user registers
                                long registrationDate = System.currentTimeMillis();
                                // Store the registration date in Firebase Realtime Database or Firestore under the user's data
                                referenceProfile.child("registrationDate").setValue(registrationDate);

                                //send verification email
                                firebaseUser.sendEmailVerification();
                                Toast.makeText(RegisterActivity.this, "User registered successfully, plaese verify your email", Toast.LENGTH_LONG).show();

                                //open user profile after successful registration
                                Intent userProfileIntent = new Intent(RegisterActivity.this, UserProfileActivity.class);
                                //to prevent user from returning back to register acitvity on pressing back button after registration
                                userProfileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(userProfileIntent);
                                finish(); // to close the Register Activity
                            }else {
                                Toast.makeText(RegisterActivity.this, "User Registation Failed please try again", Toast.LENGTH_SHORT).show();
                            }
                            //Hide the progress bar user creation is successfull or failed
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e){
                        etregisterPwd.setError("Your password is too weak use a mix of characters , numbers and special  characters");
                        etregisterPwd.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e){
                        etregisterPwd.setError("Your Email is invalid or already in use. kindly re-enter");
                        etregisterPwd.requestFocus();
                    } catch (FirebaseAuthUserCollisionException e){
                        etregisterPwd.setError("user is already registered with this email. Use another email.");
                        etregisterPwd.requestFocus();
                    } catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                    //Hide the progress bar user creation is successfull or failed
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}