package com.example.moneymap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class signup2 extends AppCompatActivity {
    FirebaseFirestore db;
    FirebaseAuth mAuthB;
    FirebaseUser currentU;
    SharedPreferences sharedPreferences;

    String s,sa,uid;
    int salaryVal,savedVal;

    Button done;
    ImageView imageButton;
    EditText salary,saved;

    Intent intent;
    Map<String, Object> budget=new HashMap<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup2);

        sharedPreferences=getSharedPreferences("MoneyMapPrefs",MODE_PRIVATE);
        mAuthB=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();

        imageButton=findViewById(R.id.cancelB);
        done=findViewById(R.id.sign2Done);
        salary=findViewById(R.id.salary);
        saved=findViewById(R.id.saveB);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToDash();
            }
        });
    }

    private void moveToDash() {
        s=salary.getText().toString().trim();
        sa=saved.getText().toString().trim();

        if(s.isEmpty() || sa.isEmpty())
            Toast.makeText(this, "fill the details...", Toast.LENGTH_SHORT).show();

        else {
            salaryVal = Integer.parseInt(s);
            savedVal = Integer.parseInt(sa);

            if (salaryVal > savedVal) {

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("Salary", salaryVal);
                editor.putInt("Saved", savedVal);
                editor.apply();

                saveToFirebase(salaryVal, savedVal);

                intent = new Intent(this, DashBoard.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Saved money can't be higher than the salary", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void saveToFirebase(int salaryVal2, int savedVal2) {
        currentU=mAuthB.getCurrentUser();
        uid=currentU.getUid();

        budget.put("Salary",salaryVal2);
        budget.put("Saved",savedVal2);
        budget.put("UserId",uid);
        budget.put("Email",currentU.getEmail());

        db.collection("budget")
                .document(uid)
                .set(budget);
    }

    private void goBack() {
        intent=new Intent(this, Signup.class);
        startActivity(intent);
    }
}