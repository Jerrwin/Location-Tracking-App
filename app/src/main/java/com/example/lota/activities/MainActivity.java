package com.example.lota.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lota.R;
import com.example.lota.helpers.SQLiteHelper;
import com.example.lota.services.LocationTrackingService;
import com.example.lota.utils.LoginLogoutTracker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextInputLayout usernameLayout, passwordLayout;
    private TextInputEditText usernameInput, passwordInput;
    private Button loginButton;
    private DatabaseReference databaseReference;
    private SQLiteHelper dbHelper;
    private String empId; // Class-level field to store empId

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new SQLiteHelper(this);

        if (dbHelper.isUserLoggedIn()) {
            String userType = dbHelper.getUserType();
            if ("employee".equals(userType)) {
                empId = dbHelper.getStoredEmpId(); // Retrieve empId from SQLite if already logged in
                startActivity(new Intent(MainActivity.this, EmpHomePage.class));
                startLocationTrackingIfPermitted();
            } else if ("authority".equals(userType)) {
                startActivity(new Intent(MainActivity.this, AuthorityDashboard.class));
            }
            finish();
            return;
        }

        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        loginButton.setOnClickListener(view -> authenticateUser());
    }

    private void authenticateUser() {
        String enteredUsername = usernameInput.getText().toString().trim();
        String enteredPassword = passwordInput.getText().toString().trim();

        if (enteredUsername.isEmpty()) {
            usernameLayout.setError("Username cannot be empty");
            return;
        } else {
            usernameLayout.setError(null);
        }

        if (enteredPassword.isEmpty()) {
            passwordLayout.setError("Password cannot be empty");
            return;
        } else {
            passwordLayout.setError(null);
        }

        // Check Employees
        databaseReference.child("Employees").orderByChild("username").equalTo(enteredUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String passwordFromDB = userSnapshot.child("password").getValue(String.class);
                                empId = userSnapshot.child("empId").getValue(String.class); // Set class-level empId

                                if (passwordFromDB != null && passwordFromDB.equals(enteredPassword)) {
                                    dbHelper.saveUser(empId, "employee");
                                    startLocationTrackingIfPermitted();
                                } else {
                                    passwordLayout.setError("Wrong password");
                                }
                                return;
                            }
                        } else {
                            // Check ReportingAuthority if not found in Employees
                            checkReportingAuthority(enteredUsername, enteredPassword);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkReportingAuthority(String username, String password) {
        databaseReference.child("ReportingAuthority").orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String passwordFromDB = userSnapshot.child("password").getValue(String.class);
                                String raId = userSnapshot.child("raId").getValue(String.class);

                                if (passwordFromDB != null && passwordFromDB.equals(password)) {
                                    dbHelper.saveUser(raId, "authority");
                                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    proceedToAuthorityDashboard();
                                } else {
                                    passwordLayout.setError("Wrong password");
                                }
                                return;
                            }
                        } else {
                            usernameLayout.setError("No such user exists");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startLocationTrackingIfPermitted() {
        if (!isLocationEnabled()) {
            Toast.makeText(this, "Please enable GPS to track location.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } else if (!checkFineLocationPermission()) {
            // Request will be triggered
        } else if (!checkForegroundServiceLocationPermission()) {
            // Request will be triggered
        } else if (!checkBackgroundLocationPermission()) {
            // Dialog will handle this
        } else if (!checkNotificationPermission()) {
            // Request will handle this
        } else {
            startLocationTracking();
            Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
            proceedToHomePage();
        }
    }

    private boolean checkFineLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private boolean checkForegroundServiceLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.FOREGROUND_SERVICE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private boolean checkBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog();
            return false;
        }
        return true;
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Location Permission Needed")
                .setMessage("Please go to Settings > Permissions > Location and select 'Allow all the time'.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "Background location denied. Tracking disabled.", Toast.LENGTH_LONG).show();
                    proceedToHomePage();
                })
                .show();
    }

    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationTrackingIfPermitted();
            } else {
                Toast.makeText(this, "Location permission denied. Tracking disabled.", Toast.LENGTH_LONG).show();
                proceedToHomePage();
            }
        }

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationTrackingIfPermitted();
            } else {
                Toast.makeText(this, "Notification permission denied. Tracking disabled.", Toast.LENGTH_LONG).show();
                proceedToHomePage();
            }
        }
    }

    private void startLocationTracking() {
        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void proceedToHomePage() {
        if (empId != null) {
            LoginLogoutTracker.recordLoginTime(empId); // Now empId is accessible
            Intent intent = new Intent(MainActivity.this, EmpHomePage.class);
            intent.putExtra("empId", empId); // Pass empId to EmpHomePage
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error: Employee ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void proceedToAuthorityDashboard() {
        startActivity(new Intent(MainActivity.this, AuthorityDashboard.class));
        finish();
    }
}