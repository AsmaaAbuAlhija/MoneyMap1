package com.example.moneymap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    Intent intent;
    FirebaseAuth mydbL;
    FirebaseUser user;
    FirebaseFirestore db;
    SharedPreferences sharedPreferences;
    String email,password,userName;

    ImageView imageButton;
    Button log;
    TextView tx;
    EditText em,pass;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mydbL=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        sharedPreferences=getSharedPreferences("MoneyMapPrefs",MODE_PRIVATE);

        imageButton=findViewById(R.id.cancelL);
        em=findViewById(R.id.emailL);
        pass=findViewById(R.id.passL);
        log=findViewById(R.id.logDone);
        tx=findViewById(R.id.signL);

        tx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               moveToSign();
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email=em.getText().toString().trim();
                password=pass.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty())
                    Toast.makeText(Login.this, "Please fill the details...", Toast.LENGTH_SHORT).show();

                else {
                    mydbL.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        user=mydbL.getCurrentUser();
                                        if (user!=null)
                                            loadUserNameAndNavigate(user.getUid());

                                    }
                                    else {
                                        Toast.makeText(Login.this, "login failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    private void loadUserNameAndNavigate(String uid) {
        db.collection("information")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()){
                        userName=documentSnapshot.getString("Name");
                        if (userName!=null){
                            sharedPreferences.edit().putString("Name",userName).apply();
                        }
                    }
                    moveToDash();
                });
    }

    private void moveToSign() {
        intent=new Intent(this, Signup.class);
        startActivity(intent);
    }

    private void moveToDash() {
        intent=new Intent(this, DashBoard.class);
        startActivity(intent);
    }

    private void goBack() {
        intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}