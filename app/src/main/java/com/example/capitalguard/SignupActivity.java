package com.example.capitalguard;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private View loginForm;
    private TextInputEditText nameView;
    private TextInputEditText emailView;
    private TextInputEditText passwordView;
    private View progressSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        View moveToLogin = findViewById(R.id.LoginScreen);
        moveToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        nameView = (TextInputEditText) findViewById(R.id.fullName);
        emailView = (TextInputEditText) findViewById(R.id.email);
        passwordView = (TextInputEditText) findViewById(R.id.password);
        Button signUpButton = (Button) findViewById(R.id.signUpB);
        loginForm = findViewById(R.id.loginSection);
        progressSpinner = findViewById(R.id.signUpProg);
        mAuth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View w) {
                String inputEmail = emailView.getText().toString();
                String inputPassword = passwordView.getText().toString();

                if (TextUtils.isEmpty(inputEmail)) {
                    emailView.setError("Email field is empty");
                    return;
                } else if (TextUtils.isEmpty(inputPassword)) {
                    passwordView.setError("Inputted password is empty");
                    return;
                } else if (inputPassword.length() < 6) {
                    passwordView.setError("Your password is too short. Password must be longer " +
                            "than 6 characters!");
                    return;
                }
                progressSpinner.setVisibility(View.VISIBLE);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                        .child("test user");
                ref.child("name").setValue(nameView.getText().toString());
                ref.child("email").setValue(emailView.getText().toString());

                mAuth.createUserWithEmailAndPassword(inputEmail, inputPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this,
                                    "User signed up successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),
                                    MainActivityJava.class));
                        } else {
                            startActivity(new Intent(getApplicationContext(), SignupActivity.class));
                            Toast.makeText(SignupActivity.this,
                                    "Error during sign up: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            progressSpinner.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }
}