package com.example.moneymap;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class HomeFragment extends Fragment {

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    SharedPreferences sharedPreferences;

    private static final List<String> FIXED_CATEGORIES =
            Arrays.asList("Food","Transport","Clothes","HomeSupplies","Other");

    int dailyB;
    double total;
    Long salary,saved;
    String uid,userName;
    int monthlySalary,savingGoal;
    double foodB,tranB,clothesB,homeB,otherB;
    double foodIn,tranIn,homeIn,otherIn,clothesIn;

    Button finish;
    TextView his,edit;
    ImageView exit;
    TextView tvWelccome,tvSalary,tvSave;
    TextView food,tran,clothes,home,other;
    EditText foodInput,tranInput,homeInput,otherInput,clothesInput;

    LinearLayout extraCategoryContainer;

    Intent intent;
    private static final String CHANNEL_ID = "money_map_channel";

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        sharedPreferences= requireContext().getSharedPreferences("MoneyMapPrefs",0);

        createNotificationChannel();

        his=view.findViewById(R.id.history);
        edit=view.findViewById(R.id.edit);
        exit=view.findViewById(R.id.exit);

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

        extraCategoryContainer=view.findViewById(R.id.exyraCategoryContainer);

        loadUserName();
        loadBudget();
        loadExtraCategories();

        finish.setOnClickListener(v -> finishDay());
        his.setOnClickListener(v -> moveToHistory());
        edit.setOnClickListener(v -> showEditBudgetDialog());
        exit.setOnClickListener(view1 -> exit2());

        return view;
    }

    private void exit2() {
        requireActivity().finishAffinity();
    }

    private void showEditBudgetDialog() {
        View dialogView=LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_edit_budget,null);
        EditText etSalary=dialogView.findViewById(R.id.editSalary);
        EditText etSaved=dialogView.findViewById(R.id.editSaved);
        etSalary.setText(String.valueOf(monthlySalary));
        etSaved.setText(String.valueOf(savingGoal));

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Budget")
                .setView(dialogView)
                .setPositiveButton("Save",(dialog, which)->{
                    String s=etSalary.getText().toString().trim();
                    String sa=etSaved.getText().toString().trim();

                    int newSalary=Integer.parseInt(s);
                    int newSaved=Integer.parseInt(sa);
                    if (newSaved >= newSalary){
                        Toast.makeText(getContext(), "Saving goal must be less than salary", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    monthlySalary=newSalary;
                    savingGoal=newSaved;
                    sharedPreferences.edit()
                            .putInt("Salary",monthlySalary)
                            .putInt("Saved",savingGoal)
                            .apply();

                    currentUser=mAuth.getCurrentUser();
                    uid=currentUser.getUid();

                    Map<String, Object> u=new HashMap<>();
                    u.put("Salary",monthlySalary);
                    u.put("Saved",savingGoal);

                    db.collection("budget")
                            .document(uid).update(u);

                    tvSalary.setText("Monthly Salary: "+monthlySalary);
                    tvSave.setText("Saving Goal: "+ savingGoal);

                    dailyB=(monthlySalary-savingGoal)/30;
                    calculateMoneyMap(dailyB);
                    showMoneyMap();
                })
                .setNegativeButton("Cancel",null)
                .show();
    }

    private void loadExtraCategories() {
        currentUser=mAuth.getCurrentUser();
        uid=currentUser.getUid();

        db.collection("inputs")
                .document(uid).get()
                .addOnSuccessListener(doc ->{
                    if (!doc.exists()) return;
                    extraCategoryContainer.removeAllViews();

                    for (Map.Entry<String, Object> entry:doc.getData().entrySet()){
                        String key=entry.getKey();
                        if (FIXED_CATEGORIES.contains(key)) continue;

                        double val=0;
                        if (entry.getValue() instanceof Number)
                            val=((Number) entry.getValue()).doubleValue();

                        addExtraCategoryRow(key, val);
                    }
                });
    }

    private void addExtraCategoryRow(String categoryName, double value) {
        LinearLayout row=new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams rowParams=new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(59));
        rowParams.setMargins(0,0,0,dpToPx(10));
        row.setLayoutParams(rowParams);
        row.setBackgroundResource(R.drawable.bg_input);

        TextView tvName=new TextView(getContext());
        LinearLayout.LayoutParams nameParams=new LinearLayout.LayoutParams(
                0,ViewGroup.LayoutParams.MATCH_PARENT,2.5f);
        tvName.setLayoutParams(nameParams);
        tvName.setText(categoryName);
        tvName.setTextSize(20f);
        tvName.setTextColor(getResources().getColor(R.color.black,null));
        tvName.setPadding(dpToPx(10),0,0,0);
        tvName.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvValue=new TextView(getContext());
        LinearLayout.LayoutParams valParams=new LinearLayout.LayoutParams(
                0,ViewGroup.LayoutParams.MATCH_PARENT,2f);
        tvValue.setLayoutParams(valParams);
        tvValue.setText(String.format("%.1f", value));
        tvValue.setTextSize(20f);
        tvValue.setTextColor(getResources().getColor(R.color.black, null));
        tvValue.setGravity(Gravity.CENTER);

        EditText etInput=new EditText(getContext());
        LinearLayout.LayoutParams inputParams=new LinearLayout.LayoutParams(
                0,ViewGroup.LayoutParams.MATCH_PARENT,2f);
        inputParams.setMargins(0,dpToPx(8),dpToPx(20),dpToPx(4));
        etInput.setLayoutParams(inputParams);
        etInput.setHint("enter");
        etInput.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etInput.setTextSize(18f);
        etInput.setTag(categoryName);

        row.addView(tvName);
        row.addView(tvValue);
        row.addView(etInput);

        extraCategoryContainer.addView(row);

    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel=new NotificationChannel(
                    CHANNEL_ID,"MoneyMap", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Budget notifications");
            requireContext().getSystemService(NotificationManager.class)
                    .createNotificationChannel(channel);
        }
    }

    private void saveCategoryValuesToFirebase() {
        currentUser=mAuth.getCurrentUser();
        uid=currentUser.getUid();

        db.collection("inputs")
                .document(uid)
                .get().addOnSuccessListener(doc -> {
                    if(!doc.exists() || doc.getDouble("Others") == null){
                        Map<String, Object> cats=new HashMap<>();
                        cats.put("Food",foodB);
                        cats.put("Transport",tranB);
                        cats.put("Clothes",clothesB);
                        cats.put("HomeSupplies",homeB);
                        cats.put("Others",otherB);

                        db.collection("inputs")
                                .document(uid)
                                .set(cats, SetOptions.merge());
                    }
                });
    }

    private void moveToHistory() {
        intent=new Intent(getContext(), History.class);
        startActivity(intent);
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void finishDay() {
        if (foodInput.getText().toString().isEmpty() || tranInput.getText().toString().isEmpty() || homeInput.getText().toString().isEmpty()
                || otherInput.getText().toString().isEmpty() || clothesInput.getText().toString().isEmpty()){
            Toast.makeText(getContext(), "please fill how much you have spent", Toast.LENGTH_SHORT).show();
            return;
        }

        foodIn = Double.parseDouble(foodInput.getText().toString());
        tranIn = Double.parseDouble(tranInput.getText().toString());
        homeIn = Double.parseDouble(homeInput.getText().toString());
        otherIn = Double.parseDouble(otherInput.getText().toString());
        clothesIn = Double.parseDouble(clothesInput.getText().toString());

        total=foodIn+tranIn+homeIn+otherIn+clothesIn;

        for (int i=0; i<extraCategoryContainer.getChildCount(); i++){
            View child=extraCategoryContainer.getChildAt(i);
            if (child instanceof LinearLayout){
                LinearLayout row =(LinearLayout) child;
                for (int j = 0; j <row.getChildCount(); j++){
                    View v=row.getChildAt(j);
                    if (v instanceof EditText){
                        String val=((EditText) v).getText().toString().trim();
                        if (!val.isEmpty()) total+=Double.parseDouble(val);
                    }
                }
            }
        }
        currentUser=mAuth.getCurrentUser();
        uid= Objects.requireNonNull(currentUser).getUid();

        Map<String, Object> dayData=new HashMap<>();
        dayData.put("Spend",total);
        dayData.put("DailyBudget",dailyB);
        dayData.put("Timestamp",System.currentTimeMillis());

        db.collection("inputs")
                .document(uid)
                .collection("days")
                .add(dayData).addOnSuccessListener(ref ->{
                    sendNotification(total,dailyB);
                    clearInputs();
                });
    }

    private void clearInputs() {
        foodInput.setText("");
        tranInput.setText("");
        homeInput.setText("");
        otherInput.setText("");
        clothesInput.setText("");

        for (int i=0; i<extraCategoryContainer.getChildCount(); i++){
            View child=extraCategoryContainer.getChildAt(i);
            if (child instanceof LinearLayout){
                LinearLayout row =(LinearLayout) child;
                for (int j = 0; j <row.getChildCount(); j++){
                    View v=row.getChildAt(j);
                    if (v instanceof EditText) ((EditText) v).setText("");
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void sendNotification(double spent, int budget) {
        String title,message;
        if (spent > budget){
            title="⚠ Over Budget!";
            message="You spent" + String.format("%.2f",spent) + " but your budget was" + budget + ". You have to control yourself!!";
        }
        else {
            if (spent == budget){
                title="Budget Reached";
                message="Good job, continue like that!";
            }
            else {
                title="✓ Great job";
                message="You saved" + String.format("%.2f",budget-spent) + " today! You've done a great job!";
            }
        }

        NotificationCompat.Builder builder= new NotificationCompat.Builder(requireContext(),CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(requireContext()).notify(1,builder.build());
        } catch (SecurityException e) {
            Toast.makeText(getContext(), "Permission noy granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMoneyMap() {
        food.setText(String.format("%.1f",foodB));
        tran.setText(String.format("%.1f",tranB));
        home.setText(String.format("%.1f",homeB));
        other.setText(String.format("%.1f",otherB));
        clothes.setText(String.format("%.1f",clothesB));
    }

    private void calculateMoneyMap(int dailyB) {
        foodB=dailyB*0.25;
        tranB=dailyB*0.15;
        homeB=dailyB*0.25;
        otherB=dailyB*0.2;
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

        db.collection("information")
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
            saveCategoryValuesToFirebase();
        }
        else {
            loadBudgetFromFirebase();
        }
    }

    private void loadBudgetFromFirebase() {
        currentUser=mAuth.getCurrentUser();
        uid=currentUser.getUid();

        db.collection("budget")
                .document(uid).get()
                .addOnSuccessListener(doc ->{
                    if (doc.exists()){
                        salary=doc.getLong("Salary");
                        saved=doc.getLong("Saved");
                    }
                    if (salary!=null && saved!=null)
                        applyBudget();
                });
    }

    private void applyBudget() {
        monthlySalary=salary.intValue();
        savingGoal=saved.intValue();

        sharedPreferences.edit()
                .putInt("Salary",monthlySalary)
                .putInt("Saved",savingGoal)
                .apply();

        tvSalary.setText("Monthly Salary: " + monthlySalary);
        tvSave.setText("Saving Goal: " + savingGoal);

        dailyB=(monthlySalary-savingGoal)/30;
        calculateMoneyMap(dailyB);
        showMoneyMap();
        saveCategoryValuesToFirebase();
    }

}