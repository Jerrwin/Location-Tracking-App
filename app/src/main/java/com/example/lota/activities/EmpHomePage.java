package com.example.lota.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EmpHomePage extends AppCompatActivity {

    private SQLiteHelper dbHelper;
    private TextView infoText;
    private DatabaseReference databaseReference;
    private double siteLatitude, siteLongitude;
    private String empId, assignedSite;

    private static final long REPORT_INTERVAL = 30 * 60 * 1000; // 30 minutes in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_home_page);

        dbHelper = new SQLiteHelper(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        infoText = findViewById(R.id.infotext);
        FloatingActionButton profileBtn = findViewById(R.id.fabBtn1);
        FloatingActionButton logoutBtn = findViewById(R.id.UserLogoutBtn);

        profileBtn.setImageResource(R.drawable.profile_icon);
        logoutBtn.setImageResource(R.drawable.logout_icon);

        profileBtn.setOnClickListener(view -> startActivity(new Intent(EmpHomePage.this, EmpProfilePage.class)));
        logoutBtn.setOnClickListener(view -> logoutUser());

        empId = dbHelper.getStoredEmpId();
        if (empId != null) {
            fetchEmployeeDetails(empId);
        } else {
            infoText.setText("Hello User");
        }
    }

    private void fetchEmployeeDetails(String empId) {
        DatabaseReference empRef = databaseReference.child("Employees").child(empId);

        empRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    assignedSite = snapshot.child("assignedSite").getValue(String.class);
                    String reportingAuthorityId = snapshot.child("reportingAuthority").getValue(String.class);

                    if (name != null && assignedSite != null && reportingAuthorityId != null) {
                        fetchReportingAuthority(name, empId, assignedSite, reportingAuthorityId);
                    } else {
                        infoText.setText("Employee details not found");
                    }
                } else {
                    infoText.setText("Employee details not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmpHomePage.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchReportingAuthority(String name, String empId, String siteKey, String reportingAuthorityId) {
        DatabaseReference authorityRef = databaseReference.child("ReportingAuthority").child(reportingAuthorityId);

        authorityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String reportingAuthorityName = snapshot.child("name").getValue(String.class);
                    fetchSiteDetailsAndMonitorLocation(name, empId, siteKey, reportingAuthorityName);
                } else {
                    infoText.setText("Reporting authority details not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmpHomePage.this, "Error fetching reporting authority", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSiteDetailsAndMonitorLocation(String name, String empId, String siteKey, String reportingAuthorityName) {
        DatabaseReference siteRef = databaseReference.child("SiteLocations").child(siteKey);
        DatabaseReference empRef = databaseReference.child("Employees").child(empId);
        DatabaseReference reportRef = databaseReference.child("report");

        siteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot siteSnapshot) {
                if (siteSnapshot.exists()) {
                    String siteName = siteSnapshot.child("name").getValue(String.class);
                    siteLatitude = siteSnapshot.child("latitude").getValue(Double.class) != null ?
                            siteSnapshot.child("latitude").getValue(Double.class) : 0.0;
                    siteLongitude = siteSnapshot.child("longitude").getValue(Double.class) != null ?
                            siteSnapshot.child("longitude").getValue(Double.class) : 0.0;

                    if (siteName != null) {
                        // Monitor employee location in real-time
                        empRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot empSnapshot) {
                                double empLatitude = empSnapshot.child("location").child("latitude").getValue(Double.class) != null ?
                                        empSnapshot.child("location").child("latitude").getValue(Double.class) : 0.0;
                                double empLongitude = empSnapshot.child("location").child("longitude").getValue(Double.class) != null ?
                                        empSnapshot.child("location").child("longitude").getValue(Double.class) : 0.0;
                                Long lastReported = empSnapshot.child("lastReported").getValue(Long.class);

                                float distance = calculateDistance(empLatitude, empLongitude, siteLatitude, siteLongitude);
                                String distanceText = distance > 0 ?
                                        String.format("Distance from site: %.2f km", distance / 1000) :
                                        "Distance unavailable";

                                // Check if distance exceeds 2 km and 30 minutes have passed
                                long currentTime = System.currentTimeMillis();
                                if (distance > 2000 && (lastReported == null || (currentTime - lastReported >= REPORT_INTERVAL))) {
                                    logOutOfRangeEvent(empId, siteKey, distance, currentTime);
                                }

                                String displayText = "Hello " + name + " (" + empId + ")\n" +
                                        "Your assigned site is " + siteName + ".\n" +
                                        "Reporting Authority: " + reportingAuthorityName + "\n" +
                                        distanceText + "\n" +
                                        "To know your site's location, ";
                                String clickableText = "click here!";
                                setClickableText(displayText, clickableText);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(EmpHomePage.this, "Error fetching employee location", Toast.LENGTH_SHORT).show();
                                String displayText = "Hello " + name + " (" + empId + ")\n" +
                                        "Your assigned site is " + siteName + ".\n" +
                                        "Reporting Authority: " + reportingAuthorityName + "\n" +
                                        "Distance unavailable\n" +
                                        "To know your site's location, ";
                                String clickableText = "click here!";
                                setClickableText(displayText, clickableText);
                            }
                        });
                    }
                } else {
                    infoText.setText("Site details not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmpHomePage.this, "Error fetching site data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == 0 || lon1 == 0 || lat2 == 0 || lon2 == 0) {
            return 0;
        }
        Location empLocation = new Location("employee");
        empLocation.setLatitude(lat1);
        empLocation.setLongitude(lon1);

        Location siteLocation = new Location("site");
        siteLocation.setLatitude(lat2);
        siteLocation.setLongitude(lon2);

        return empLocation.distanceTo(siteLocation); // In meters
    }

    private void logOutOfRangeEvent(String empId, String siteId, float distance, long currentTime) {
        DatabaseReference reportRef = databaseReference.child("report").push();
        DatabaseReference empRef = databaseReference.child("Employees").child(empId);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String date = dateFormat.format(new Date(currentTime));
        String time = timeFormat.format(new Date(currentTime));

        // Log the report
        ReportEntry entry = new ReportEntry(empId, date, time, siteId, distance / 1000);
        reportRef.setValue(entry)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to log out-of-range event", Toast.LENGTH_SHORT).show());

        // Update lastReported timestamp
        empRef.child("lastReported").setValue(currentTime)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update last reported time", Toast.LENGTH_SHORT).show());
    }

    private void setClickableText(String displayText, String clickableText) {
        SpannableString spannableString = new SpannableString(displayText + clickableText);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                openGoogleMaps();
            }
        };
        int startIndex = displayText.length();
        int endIndex = startIndex + clickableText.length();
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoText.setText(spannableString);
        infoText.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }

    private void openGoogleMaps() {
        if (siteLatitude != 0 && siteLongitude != 0) {
            String uri = "geo:" + siteLatitude + "," + siteLongitude + "?q=" + siteLatitude + "," + siteLongitude;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void logoutUser() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbHelper.clearUser();
                    stopService(new Intent(this, LocationTrackingService.class));
                    LoginLogoutTracker.recordLogoutTime(empId);
                    Toast.makeText(EmpHomePage.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EmpHomePage.this, MainActivity.class);
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

    private static class ReportEntry {
        public String empId, date, time, siteId;
        public double distance;

        public ReportEntry(String empId, String date, String time, String siteId, float distance) {
            this.empId = empId;
            this.date = date;
            this.time = time;
            this.siteId = siteId;
            this.distance = distance;
        }
    }
}