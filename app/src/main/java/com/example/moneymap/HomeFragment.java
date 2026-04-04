package com.example.moneymap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class HomeFragment extends Fragment {

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    SharedPreferences sharedPreferences;

    int dailyB;
    double total;
    Long salary,saved;
    String uid,userName;
    int monthlySalary,savingGoal;
    double foodB,tranB,clothesB,homeB,otherB;
    double foodIn,tranIn,homeIn,otherIn,clothesIn;

    Button finish;
    TextView his,edit;
    TextView tvWelccome,tvSalary,tvSave;
    TextView food,tran,clothes,home,other;
    EditText foodInput,tranInput,homeInput,otherInput,clothesInput;

    Intent intent;

    Map<String, Object> input=new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        sharedPreferences= requireContext().getSharedPreferences("MoneyMapPrefs",0);

        his=view.findViewById(R.id.history);
        edit=view.findViewById(R.id.edit);

        tvWelccome=view.findViewById(R.id.hello);
        tvSalary=view.findViewById(R.id.budget);
        tvSave=view.findViewById(R.id.save);
        finish=view.findViewById(R.id.finishDay);

        food=view.findViewById(R.id.food);
        tran=view.findViewById(R.id.transport);
        clothes=view.findViewById(R.id.clothes);
        home=view.findViewById(R.id.need);
        other=view.findViewById(R.id.others);

        foodInput=view.findViewById(R.id.foodInput);
        tranInput=view.findViewById(R.id.tranInput);
        homeInput=view.findViewById(R.id.needInput);
        otherInput=view.findViewById(R.id.otherInput);
        clothesInput=view.findViewById(R.id.clothesInput);

        loadUserName();
        loadBudget();

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishDay();
            }
        });

        his.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToHistory();
            }
        });

        return view;
    }

    private void moveToHistory() {
        intent=new Intent(getContext(), History.class);
        startActivity(intent);
    }

    private void finishDay() {
        if (foodInput.getText().toString().isEmpty() || tranInput.getText().toString().isEmpty() || homeInput.getText().toString().isEmpty()
                || otherInput.getText().toString().isEmpty() || clothesInput.getText().toString().isEmpty()){
            Toast.makeText(getContext(), "please fill how much you have spend", Toast.LENGTH_SHORT).show();
            return;
        }

        else {
            foodIn = Double.parseDouble(foodInput.getText().toString());
            tranIn = Double.parseDouble(tranInput.getText().toString());
            homeIn = Double.parseDouble(homeInput.getText().toString());
            otherIn = Double.parseDouble(otherInput.getText().toString());
            clothesIn = Double.parseDouble(clothesInput.getText().toString());
        }

        total=foodIn+tranIn+homeIn+otherIn+clothesIn;

        currentUser=mAuth.getCurrentUser();
        uid= Objects.requireNonNull(currentUser).getUid();

        input.put("Spend",total);
        input.put("DailyBudget",dailyB);

        db.collection("inputs")
                .document(uid)
                .collection("days")
                .add(input);

        foodInput.setText("");
        tranInput.setText("");
        homeInput.setText("");
        otherInput.setText("");
        clothesInput.setText("");
    }


    private void showMoneyMap() {
        food.setText(String.valueOf(foodB));
        tran.setText(String.valueOf(tranB));
        home.setText(String.valueOf(homeB));
        other.setText(String.valueOf(otherB));
        clothes.setText(String.valueOf(clothesB));
    }

    private void calculateMoneyMap(int dailyB) {
        foodB=dailyB*0.3;
        tranB=dailyB*0.15;
        homeB=dailyB*0.25;
        otherB=dailyB*0.15;
        clothesB=dailyB*0.15;
    }

    private void loadUserName() {
        userName=sharedPreferences.getString("Name",null);

        if (userName!=null)
            tvWelccome.setText("Hi " + userName);
        else {
            loadUserNameFromFireBase();
        }
    }

    private void loadUserNameFromFireBase() {
        currentUser=mAuth.getCurrentUser();
        uid=currentUser.getUid();

        db.collection("users")
                .document(uid).get()
                .addOnSuccessListener(doc ->{
                    if (doc.exists()){
                        userName=doc.getString("Name");
                        if (userName!=null){
                            sharedPreferences.edit().putString("Name",userName).apply();
                            tvWelccome.setText("Hi " + userName);
                        }
                    }
                });
    }

    private void loadBudget() {
        currentUser=mAuth.getCurrentUser();
        uid=currentUser.getUid();

        monthlySalary=sharedPreferences.getInt("Salary",-1);
        savingGoal=sharedPreferences.getInt("Saved",-1);

        if (monthlySalary!=-1 && savingGoal!=-1){
            tvSalary.setText("Monthly Salary: " + monthlySalary);
            tvSave.setText("Saving Goal: " + savingGoal);

            dailyB=(monthlySalary-savingGoal)/30;
            calculateMoneyMap(dailyB);
            showMoneyMap();
        }
        else {
            loadBudgetFromFirebase();
        }
    }

    private void loadBudgetFromFirebase() {
        currentUser=mAuth.getCurrentUser();
        uid=currentUser.getUid();

        db.collection("users")
                .document(uid).get()
                .addOnSuccessListener(doc ->{
                    if (doc.exists()){
                        salary=doc.getLong("Salary");
                        saved=doc.getLong("Saved");

                        monthlySalary=salary.intValue();
                        savingGoal=saved.intValue();

                        sharedPreferences.edit().
                                putInt("Salary",monthlySalary)
                                .putInt("Saved",savingGoal)
                                .apply();

                        tvSalary.setText("Monthly Salary: " + monthlySalary);
                        tvSave.setText("Saving Goal: " + savingGoal);

                        dailyB=(monthlySalary-savingGoal)/30;
                        calculateMoneyMap(dailyB);
                        showMoneyMap();
                    }
                });
    }

}