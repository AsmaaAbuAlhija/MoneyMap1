package com.example.moneymap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class History extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    ImageButton btnBack;
    LinearLayout listContainer;
    TextView tvEmpty;
    String uid;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();

        btnBack=findViewById(R.id.cancelH);
        listContainer=findViewById(R.id.historyContainer);
        tvEmpty=findViewById(R.id.tvEmpty);

        btnBack.setOnClickListener(v -> finish());

        uid=currentUser.getUid();
        loadHistory();
    }

    private void loadHistory() {
        db.collection("inputs")
                .document(uid)
                .collection("days")
                .orderBy("Timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()){
                        tvEmpty.setVisibility(View.VISIBLE);
                        return;
                    }
                    tvEmpty.setVisibility(View.GONE);
                    listContainer.removeAllViews();

                    int dayNum=1;
                    for (QueryDocumentSnapshot doc:querySnapshot){
                        addDayCard(doc.getData(), dayNum++);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show());
    }

    private void addDayCard(Map<String, Object> day, int dayNum) {
        double spend=day.get("Spend") != null ? ((Number) day.get("Spend")).doubleValue():0;
        double budget=day.get("DailyBudget") != null ? ((Number) day.get("DailyBudget")).doubleValue():0;

        String dataStr="";
        if (day.get("Timestamp") != null){
            long ts=((Number) day.get("Timestamp")).longValue();
            dataStr=new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(ts));
        }

        CardView card=new CardView(this);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0,0,0,20);
        card.setLayoutParams(p);
        card.setRadius(24f);
        card.setCardElevation(6f);

        LinearLayout inner=new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        int pad=dpToPx(16);
        inner.setPadding(pad, pad, pad, pad);

        TextView tvDay=new TextView(this);
        tvDay.setText("Day " + dayNum + (dataStr.isEmpty() ? "" : " . " + dataStr));
        tvDay.setTextSize(20f);
        tvDay.setTypeface(null, Typeface.BOLD);
        tvDay.setTextColor(ContextCompat.getColor(this, R.color.black));

        TextView tvSpent=new TextView(this);
        tvSpent.setText("Spent: " +String.format(Locale.getDefault(), "%.2f", spend));
        tvSpent.setTextSize(18f);
        tvSpent.setPadding(0,dpToPx(6),0,0);
        tvSpent.setTextColor(spend > budget
                ? ContextCompat.getColor(this, android.R.color.holo_red_dark)
                : ContextCompat.getColor(this, android.R.color.holo_green_dark));

        TextView tvBudget=new TextView(this);
        tvBudget.setText("DailyBudget: " + String.format(Locale.getDefault(), "%.2f",budget));
        tvBudget.setTextSize(18f);
        tvBudget.setPadding(0,dpToPx(4),0,0);
        tvBudget.setTextColor(ContextCompat.getColor(this, R.color.black));

        TextView tvStatus=new TextView(this);
        tvStatus.setTextSize(17f);
        tvStatus.setPadding(0, dpToPx(4), 0,0);
        if (spend > budget){
            tvStatus.setText("⚠ Over budget by " + String.format(Locale.getDefault(),"%.2f", spend-budget));
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
        else {
            tvStatus.setText("✓ Saved " + String.format(Locale.getDefault(),"%.2f", budget-spend));
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }

        inner.addView(tvDay);
        inner.addView(tvSpent);
        inner.addView(tvBudget);
        inner.addView(tvStatus);
        card.addView(inner);
        listContainer.addView(card);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}