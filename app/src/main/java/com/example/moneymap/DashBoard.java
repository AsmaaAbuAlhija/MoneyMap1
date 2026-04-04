package com.example.moneymap;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class DashBoard extends AppCompatActivity {

    int id;

    Fragment active;
    AddFragment addFragment;
    BottomNavigationView nav;
    HomeFragment homeFragment;
    ProfileFragment profileFragment;
    FragmentTransaction fragmentTransaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        nav=findViewById(R.id.navi);

        addFragment = new AddFragment();
        homeFragment = new HomeFragment();
        profileFragment = new ProfileFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame,profileFragment,"profile")
                .hide(profileFragment)
                .commit();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame,addFragment,"add")
                .hide(addFragment)
                .commit();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame,homeFragment,"home")
                .commit();

        active=homeFragment;

        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                fragmentTransaction=getSupportFragmentManager().beginTransaction();
                id=menuItem.getItemId();

                if (id==R.id.home){
                    fragmentTransaction.hide(active).show(homeFragment).commit();
                    active=homeFragment;
                    return true;
                }

                if (id==R.id.add){
                    fragmentTransaction.hide(active).show(addFragment).commit();
                    active=addFragment;
                    return true;
                }

                if (id==R.id.profile){
                    fragmentTransaction.hide(active).show(profileFragment).commit();
                    active=profileFragment;
                    return true;
                }
                return false;
            }
        });
    }
}