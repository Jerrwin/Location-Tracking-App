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

import com.example.lota.services.LocationTrackingService;
import com.example.lota.utils.LoginLogoutTracker;
import com.example.lota.R;
import com.example.lota.helpers.SQLiteHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EmpProfilePage extends AppCompatActivity {
    private TextView usernameText, empIdText, assignedSiteText, reportingAuthorityText;
    private SQLiteHelper dbHelper;
    private DatabaseReference databaseReference;
    String empId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_profile_page);

        // Initialize views
        usernameText = findViewById(R.id.username);
        empIdText = findViewById(R.id.empId);
        assignedSiteText = findViewById(R.id.assignedSite);
        reportingAuthorityText = findViewById(R.id.reportingAuthority);

        // Initialize SQLiteHelper
        dbHelper = new SQLiteHelper(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        FloatingActionButton homeBtn = findViewById(R.id.fabBtn1);
        FloatingActionButton logoutBtn = findViewById(R.id.UserLogoutBtn);

        homeBtn.setImageResource(R.drawable.home_icon);
        logoutBtn.setImageResource(R.drawable.logout_icon);

        homeBtn.setOnClickListener(view -> {
            Intent intent = new Intent(EmpProfilePage.this, EmpHomePage.class);
            startActivity(intent);
        });

        logoutBtn.setOnClickListener(view -> logoutUser());

        // Fetch empId from SQLite and retrieve data from Firebase
        empId = dbHelper.getStoredEmpId();
        if (empId != null) {
            fetchEmployeeDetails(empId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchEmployeeDetails(String empId) {
        DatabaseReference empRef = databaseReference.child("Employees").child(empId);

        empRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String assignedSite = snapshot.child("assignedSite").getValue(String.class);
                    String reportingAuthorityId = snapshot.child("reportingAuthority").getValue(String.class);

                    usernameText.setText(name);
                    empIdText.setText(empId);

                    if (assignedSite != null && reportingAuthorityId != null) {
                        fetchSiteDetails(assignedSite);
                        fetchReportingAuthority(reportingAuthorityId);
                    }
                } else {
                    Toast.makeText(EmpProfilePage.this, "Employee details not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmpProfilePage.this, "Error fetching employee data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSiteDetails(String siteKey) {
        DatabaseReference siteRef = databaseReference.child("SiteLocations").child(siteKey);

        siteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String siteName = snapshot.child("name").getValue(String.class);
                    assignedSiteText.setText(siteName);
                } else {
                    assignedSiteText.setText("Site details not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmpProfilePage.this, "Error fetching site data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchReportingAuthority(String reportingAuthorityId) {
        DatabaseReference authorityRef = databaseReference.child("ReportingAuthority").child(reportingAuthorityId);

        authorityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String reportingAuthorityName = snapshot.child("name").getValue(String.class);
                    reportingAuthorityText.setText(reportingAuthorityName);
                } else {
                    reportingAuthorityText.setText("Reporting authority details not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmpProfilePage.this, "Error fetching reporting authority", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbHelper.clearUser();
                    stopService(new Intent(this, LocationTrackingService.class));
                    LoginLogoutTracker.recordLogoutTime(empId);
                    Toast.makeText(EmpProfilePage.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EmpProfilePage.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create();

        alertDialog.setOnShowListener(dialog -> {
            // Set Background Color to White
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

            // Change Text Color to White
            TextView messageView = alertDialog.findViewById(android.R.id.message);
            int titleId = getResources().getIdentifier("alertTitle", "id", getPackageName());
            TextView titleView = alertDialog.findViewById(titleId);
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (messageView != null) messageView.setTextColor(Color.BLACK);  // Text in Black
            if (titleView != null) titleView.setTextColor(Color.BLACK);      // Title in Black
            if (positiveButton != null) positiveButton.setTextColor(Color.BLACK);
            if (negativeButton != null) negativeButton.setTextColor(Color.BLACK);
        });

        alertDialog.show();
    }

}
