package com.example.moneymap;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AddFragment extends Fragment {

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    EditText etCategory,etValue;
    Button btnAdd;
    String uid;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();

        etCategory=view.findViewById(R.id.category);
        etValue=view.findViewById(R.id.value);
        btnAdd=view.findViewById(R.id.add);

        btnAdd.setOnClickListener(v -> addExpense());
        return view;
    }

    private void addExpense() {
        String categoryName=etCategory.getText().toString().trim();
        String valueStr=etValue.getText().toString().trim();

        if (categoryName.isEmpty()){
            Toast.makeText(getContext(), "Please enter a category name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (valueStr.isEmpty()){
            Toast.makeText(getContext(), "Please enter a value", Toast.LENGTH_SHORT).show();
            return;
        }

        double newValue;
        try {newValue=Double.parseDouble(valueStr);}
        catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid value", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newValue <= 0){
            Toast.makeText(getContext(), "Value must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser=mAuth.getCurrentUser();
        uid=currentUser.getUid();

        db.collection("inputs")
                .document(uid)
                .get().addOnSuccessListener(doc -> {
                    double currentOthers=0;
                    if (doc.exists() && doc.getDouble("Others") != null)
                        currentOthers=doc.getDouble("Others");

                    if (currentOthers < newValue){
                        Toast.makeText(getContext(), "Other balance (" + String.format("%.2f",currentOthers) + ") is less than " + String.format("%.2f",newValue), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double newOthers = currentOthers-newValue;

                    Map<String, Object> updates=new HashMap<>();
                    updates.put("Others", newOthers);
                    updates.put(categoryName,newValue);

                    db.collection("inputs")
                            .document(uid)
                            .set(updates, SetOptions.merge())
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "" + categoryName + " added!", Toast.LENGTH_SHORT).show();
                                etCategory.setText("");
                                etValue.setText("");

                                HomeFragment homeFragment=(HomeFragment)
                                        getParentFragmentManager().findFragmentByTag("home");
                                if (homeFragment != null){
                                    homeFragment.refreshOthers(newOthers);
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to add category", Toast.LENGTH_SHORT).show());
                });
    }
}