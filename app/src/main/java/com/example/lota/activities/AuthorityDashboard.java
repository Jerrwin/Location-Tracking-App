package com.example.lota.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lota.R;
import com.example.lota.helpers.SQLiteHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AuthorityDashboard extends AppCompatActivity {

    private TextView welcomeText;
    private DatabaseReference databaseReference;
    private SQLiteHelper dbHelper;
    private String raId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authority_dashboard);

        dbHelper = new SQLiteHelper(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        welcomeText = findViewById(R.id.welcomeText);

        FloatingActionButton empListBtn = findViewById(R.id.fabBtn1);
        FloatingActionButton logoutBtn = findViewById(R.id.UserLogoutBtn);

        empListBtn.setImageResource(R.drawable.list); // Ensure you have this icon
        logoutBtn.setImageResource(R.drawable.logout_icon);

        empListBtn.setOnClickListener(view -> {
            Intent intent = new Intent(AuthorityDashboard.this, EmployeeListActivity.class);
            intent.putExtra("raId", raId);
            startActivity(intent);
        });

        logoutBtn.setOnClickListener(view -> logoutUser());

        raId = dbHelper.getStoredEmpId();
        if (raId != null) {
            fetchRaName(raId);
        } else {
            welcomeText.setText("Welcome");
        }
    }

    private void fetchRaName(String raId) {
        DatabaseReference raRef = databaseReference.child("ReportingAuthority").child(raId);

        raRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String raName = snapshot.child("name").getValue(String.class);
                    welcomeText.setText("Welcome, " + (raName != null ? raName : "Authority"));
                } else {
                    welcomeText.setText("Welcome");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AuthorityDashboard.this, "Error fetching RA name", Toast.LENGTH_SHORT).show();
                welcomeText.setText("Welcome");
            }
        });
    }

    private void logoutUser() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbHelper.clearUser();
                    Toast.makeText(AuthorityDashboard.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AuthorityDashboard.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create();

        alertDialog.setOnShowListener(dialog -> {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            TextView messageView = alertDialog.findViewById(android.R.id.message);
            int titleId = getResources().getIdentifier("alertTitle", "id", getPackageName());
            TextView titleView = alertDialog.findViewById(titleId);
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (messageView != null) messageView.setTextColor(Color.BLACK);
            if (titleView != null) titleView.setTextColor(Color.BLACK);
            if (positiveButton != null) positiveButton.setTextColor(Color.BLACK);
            if (negativeButton != null) negativeButton.setTextColor(Color.BLACK);
        });

        alertDialog.show();
    }
}