package com.example.lota.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lota.adapters.LoginReportAdapter;
import com.example.lota.R;
import com.example.lota.adapters.ReportAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EmployeeDetailActivity extends AppCompatActivity {

    private TextView empDetailsText, noReportsText, noLoginReportsText;
    private RecyclerView reportsRecyclerView, loginReportsRecyclerView;
    private ReportAdapter reportAdapter;
    private LoginReportAdapter loginReportAdapter;
    private List<ReportAdapter.Report> reportList;
    private List<LoginReportAdapter.LoginReport> loginReportList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_detail);

        // Initialize UI elements
        empDetailsText = findViewById(R.id.empDetailsText);
        noReportsText = findViewById(R.id.noReportsText);
        noLoginReportsText = findViewById(R.id.noLoginReportsText);
        reportsRecyclerView = findViewById(R.id.reportsRecyclerView);
        loginReportsRecyclerView = findViewById(R.id.loginReportsRecyclerView);

        // Setup Out-of-Range Reports RecyclerView
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(reportList);
        reportsRecyclerView.setAdapter(reportAdapter);

        // Setup Login/Logout Reports RecyclerView
        loginReportsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loginReportList = new ArrayList<>();
        loginReportAdapter = new LoginReportAdapter(loginReportList);
        loginReportsRecyclerView.setAdapter(loginReportAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        String empId = getIntent().getStringExtra("empId");
        String empName = getIntent().getStringExtra("empName");

        if (empId != null && empName != null) {
            fetchEmployeeDetails(empId, empName);
            fetchEmployeeReports(empId);
            fetchLoginLogoutReports(empId);
        } else {
            Toast.makeText(this, "Employee data not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchEmployeeDetails(String empId, String empName) {
        DatabaseReference empRef = databaseReference.child("Employees").child(empId);
        empRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String assignedSiteId = snapshot.child("assignedSite").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);
                    String reportingAuthorityId = snapshot.child("reportingAuthority").getValue(String.class);

                    fetchSiteName(assignedSiteId, siteName -> {
                        fetchReportingAuthorityName(reportingAuthorityId, raName -> {
                            StringBuilder details = new StringBuilder();
                            details.append("Name: ").append(empName).append("\n")
                                    .append("ID: ").append(empId).append("\n")
                                    .append("Username: ").append(username != null ? username : "N/A").append("\n")
                                    .append("Assigned Site: ").append(siteName != null ? siteName : "N/A").append("\n")
                                    .append("Reporting Authority: ").append(raName != null ? raName : "N/A");
                            empDetailsText.setText(details.toString());
                        });
                    });
                } else {
                    empDetailsText.setText("Employee details not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmployeeDetailActivity.this, "Error fetching details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSiteName(String siteId, Callback<String> callback) {
        if (siteId == null) {
            callback.onResult("N/A");
            return;
        }
        DatabaseReference siteRef = databaseReference.child("SiteLocations").child(siteId);
        siteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String siteName = snapshot.child("name").getValue(String.class);
                callback.onResult(siteName != null ? siteName : siteId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(siteId);
            }
        });
    }

    private void fetchReportingAuthorityName(String raId, Callback<String> callback) {
        if (raId == null) {
            callback.onResult("N/A");
            return;
        }
        DatabaseReference raRef = databaseReference.child("ReportingAuthority").child(raId);
        raRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String raName = snapshot.child("name").getValue(String.class);
                callback.onResult(raName != null ? raName : raId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(raId);
            }
        });
    }

    private void fetchEmployeeReports(String empId) {
        Query reportsRef = databaseReference.child("report").orderByChild("empId").equalTo(empId);
        reportsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportList.clear();
                if (snapshot.exists()) {
                    List<ReportAdapter.Report> tempList = new ArrayList<>();
                    for (DataSnapshot reportSnapshot : snapshot.getChildren()) {
                        String date = reportSnapshot.child("date").getValue(String.class);
                        String time = reportSnapshot.child("time").getValue(String.class);
                        String siteId = reportSnapshot.child("siteId").getValue(String.class);
                        Double distance = reportSnapshot.child("distance").getValue(Double.class);

                        String distanceStr = distance != null ? String.format("%.2f", distance) : "N/A";
                        tempList.add(new ReportAdapter.Report(
                                date != null ? date : "N/A",
                                time != null ? time : "N/A",
                                siteId != null ? siteId : "N/A",
                                distanceStr
                        ));
                    }
                    // Sort by date and time in descending order
                    Collections.sort(tempList, new Comparator<ReportAdapter.Report>() {
                        @Override
                        public int compare(ReportAdapter.Report r1, ReportAdapter.Report r2) {
                            String r1DateTime = r1.date + " " + r1.time;
                            String r2DateTime = r2.date + " " + r2.time;
                            return r2DateTime.compareTo(r1DateTime); // Latest first
                        }
                    });
                    reportList.addAll(tempList);

                    noReportsText.setVisibility(View.GONE);
                    reportsRecyclerView.setVisibility(View.VISIBLE);
                    reportAdapter.notifyDataSetChanged();
                } else {
                    noReportsText.setVisibility(View.VISIBLE);
                    reportsRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmployeeDetailActivity.this, "Error fetching reports", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLoginLogoutReports(String empId) {
        DatabaseReference loginReportsRef = databaseReference.child("LoginLogoutRecords").child(empId);
        loginReportsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loginReportList.clear();
                if (snapshot.exists()) {
                    List<LoginReportAdapter.LoginReport> tempList = new ArrayList<>();
                    for (DataSnapshot reportSnapshot : snapshot.getChildren()) {
                        String type = reportSnapshot.child("type").getValue(String.class);
                        String date = reportSnapshot.child("date").getValue(String.class);
                        String time = reportSnapshot.child("time").getValue(String.class);

                        tempList.add(new LoginReportAdapter.LoginReport(
                                type != null ? type : "N/A",
                                date != null ? date : "N/A",
                                time != null ? time : "N/A"
                        ));
                    }
                    // Reverse the list to show latest first
                    Collections.reverse(tempList);
                    loginReportList.addAll(tempList);

                    noLoginReportsText.setVisibility(View.GONE);
                    loginReportsRecyclerView.setVisibility(View.VISIBLE);
                    loginReportAdapter.notifyDataSetChanged();
                } else {
                    noLoginReportsText.setVisibility(View.VISIBLE);
                    loginReportsRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmployeeDetailActivity.this, "Error fetching login/logout reports", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private interface Callback<T> {
        void onResult(T result);
    }
}