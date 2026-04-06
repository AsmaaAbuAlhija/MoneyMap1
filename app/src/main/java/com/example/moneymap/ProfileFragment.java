package com.example.moneymap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

public class ProfileFragment extends Fragment {

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    SharedPreferences sharedPreferences;

    TextView tvEmail,tvName,tvPhone, tvAddress, tvAge;
    Button btnEdit,btnLogout;

    String uid,name,email,phone,address,age;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile, container, false);

        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        sharedPreferences= requireContext().getSharedPreferences("MoneyMapPrefs",0);

        tvEmail=view.findViewById(R.id.tvEmail);
        tvName=view.findViewById(R.id.tvName);
        tvPhone=view.findViewById(R.id.tvPhone);
        tvAddress=view.findViewById(R.id.tvAddress);
        tvAge=view.findViewById(R.id.tvAge);

        btnEdit=view.findViewById(R.id.btnEdit);
        btnLogout=view.findViewById(R.id.btnLogout);

        loadProfile();

        btnEdit.setOnClickListener(v -> showEditDialog());
        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    @SuppressLint("MissingInflatedId")
    private void showEditDialog() {
        View dialogView=LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile,null);

         EditText etName=dialogView.findViewById(R.id.editName);
         EditText etPhone=dialogView.findViewById(R.id.editPhone);
         EditText etAddress=dialogView.findViewById(R.id.editAddress);
         EditText etAge=dialogView.findViewById(R.id.editAge);

         if (name!=null) etName.setText(name);
         if (phone!=null) etPhone.setText(phone);
         if (address!=null) etAddress.setText(address);
         if (age!=null) etAge.setText(age);

         new AlertDialog.Builder(requireContext())
                 .setTitle("Edit Profile")
                 .setView(dialogView)
                 .setPositiveButton("Save",(dialog, which) -> {
                     String newName=etName.getText().toString().trim();
                     String newPhone=etPhone.getText().toString().trim();
                     String newAddress=etAddress.getText().toString().trim();
                     String newAge=etAge.getText().toString().trim();

                     saveProfile(newName,newPhone,newAddress,newAge);

                 })
                 .setNegativeButton("Cancel",null)
                 .show();
    }

    private void saveProfile(String newName, String newPhone, String newAddress, String newAge) {
        currentUser=mAuth.getCurrentUser();
        uid=currentUser.getUid();

        Map<String, Object> updates=new HashMap<>();
        updates.put("Name",newName);
        updates.put("Phone",newPhone);
        updates.put("Address",newAddress);
        updates.put("Age",newAge);

        db.collection("information")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    name=newName;
                    phone=newPhone;
                    address=newAddress;
                    age=newAge;

                    sharedPreferences.edit()
                            .putString("Name",name)
                            .putString("Phone",phone)
                            .putString("Address",address)
                            .putString("Age",age)
                            .apply();

                    displayProfile();
                    Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Updated failed", Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("yes",(dialog, which) -> {
                   mAuth.signOut();
                   sharedPreferences.edit().clear().apply();
                    Intent intent=new Intent(getContext(),MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("No",null)
                .show();
    }

    private void loadProfile() {
        name=sharedPreferences.getString("Name",null);
        email=sharedPreferences.getString("Email",null);
        phone=sharedPreferences.getString("Phone",null);
        address=sharedPreferences.getString("Address",null);
        age=sharedPreferences.getString("Age",null);

        if (name!=null && email!=null && phone!=null && address!=null && age!=null)
            displayProfile();
        else
            loadFromFirebase();
    }

    private void loadFromFirebase() {
        currentUser=mAuth.getCurrentUser();
        uid=currentUser.getUid();

        db.collection("information")
                .document(uid)
                .get()
                .addOnSuccessListener(doc ->{
                    if (doc.exists()){
                        name=doc.getString("Name");
                        email=doc.getString("Email");
                        phone=doc.getString("Phone");
                        address=doc.getString("Address");
                        age=doc.getString("Age");

                        sharedPreferences.edit()
                                .putString("Name",name)
                                .putString("Email",email)
                                .putString("Phone",phone)
                                .putString("Address",address)
                                .putString("Age",age)
                                .apply();

                        displayProfile();
                    }
                });
    }

    private void displayProfile() {
        tvEmail.setText("Email: " +email);
        tvName.setText("Name: "+name);
        tvPhone.setText("Phone: "+phone);
        tvAddress.setText("Address: "+ address);
        tvAge.setText("Age: "+age);
    }
}