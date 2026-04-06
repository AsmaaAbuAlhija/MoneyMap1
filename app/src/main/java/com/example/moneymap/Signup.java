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

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {

    FirebaseAuth MydbS,mAuth;
    FirebaseFirestore db;
    FirebaseUser currentUser;
    SharedPreferences sharedPreferences;

    ImageView imageButton;
    EditText email,password,phone,address,age,name;
    Button sign;
    TextView tvPass;

    String em,pass,addre,ph,ag,n,uid;

    Intent intent;
    Map<String, Object> information=new HashMap<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        sharedPreferences=getSharedPreferences("MoneyMapPrefs",MODE_PRIVATE);
        MydbS=FirebaseAuth.getInstance();
        mAuth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();

        imageButton=findViewById(R.id.cancelS);
        sign=findViewById(R.id.signDone);
        email=findViewById(R.id.emailS);
        password=findViewById(R.id.passS);
        phone=findViewById(R.id.phoneS);
        address=findViewById(R.id.adressS);
        age=findViewById(R.id.ageS);
        name=findViewById(R.id.nameS);
        tvPass=findViewById(R.id.tvPass);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack2();
            }
        });

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { signup();}
        });
    }

    private void signup() {
        em=email.getText().toString().trim();
        pass=password.getText().toString().trim();
        addre=address.getText().toString().trim();
        ph=phone.getText().toString().trim();
        ag=age.getText().toString().trim();
        n=name.getText().toString().trim();

        if (!em.isEmpty() && !em.equals(null) && !pass.isEmpty() && !pass.equals(null) && !addre.isEmpty() && !addre.equals(null)
        && !ph.isEmpty() && !ph.equals(null) && !ag.isEmpty() && !ag.equals(null)){
            if (pass.length() >= 6) {
                mAuth.createUserWithEmailAndPassword(em, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    saveUserData(n, em, addre, ph, ag);
                                    saveToFirebase(n, em, addre, ph, ag);

                                    tvPass.setVisibility(View.GONE);
                                    moveToBudget();
                                } else {
                                    Toast.makeText(Signup.this, "create user failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
            else {
                tvPass.setVisibility(View.VISIBLE);
            }
        }
        else{
            Toast.makeText(Signup.this, "fill the details...", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserData(String n, String em, String addre, String ph, String ag) {

        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("Name",n);
        editor.putString("Email",em);
        editor.putString("Address",addre);
        editor.putString("Phone",ph);
        editor.putString("Age",ag);
        editor.apply();

    }
    private void saveToFirebase(String n,String em, String addre, String ph, String ag) {
       currentUser=mAuth.getCurrentUser();
       uid=currentUser.getUid();

        information.put("Name",n);
        information.put("Email",em);
        information.put("Address",addre);
        information.put("Phone",ph);
        information.put("Age",ag);

        db.collection("information")
                .document(uid)
                .set(information);
    }

    private void moveToBudget() {

        intent=new Intent(this, signup2.class);
        startActivity(intent);
    }

    private void goBack2() {

        intent=new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}