package com.example.lota.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lota.adapters.EmployeeAdapter;
import com.example.lota.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EmployeeListActivity extends AppCompatActivity {

    private static final String TAG = "EmployeeListActivity";
    private RecyclerView recyclerView;
    private EmployeeAdapter adapter;
    private List<Employee> employeeList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);
        Log.d(TAG, "onCreate called");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        employeeList = new ArrayList<>();
        adapter = new EmployeeAdapter(employeeList, employee -> {
            Log.d(TAG, "Employee clicked: " + employee.empId);
            Intent intent = new Intent(EmployeeListActivity.this, EmployeeDetailActivity.class);
            intent.putExtra("empId", employee.empId);
            intent.putExtra("empName", employee.name);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "RecyclerView initialized");

        databaseReference = FirebaseDatabase.getInstance().getReference("Employees");
        String raId = getIntent().getStringExtra("raId");
        Log.d(TAG, "raId received: " + raId);

        if (raId != null) {
            fetchEmployees(raId);
        } else {
            Log.e(TAG, "raId is null");
            Toast.makeText(this, "Authority ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchEmployees(String raId) {
        databaseReference.orderByChild("reportingAuthority").equalTo(raId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "DataSnapshot received, exists: " + snapshot.exists());
                        employeeList.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot empSnapshot : snapshot.getChildren()) {
                                String empId = empSnapshot.child("empId").getValue(String.class);
                                String name = empSnapshot.child("name").getValue(String.class);
                                if (empId != null && name != null) {
                                    employeeList.add(new Employee(name, empId));
                                    Log.d(TAG, "Added employee: " + name + " (" + empId + ")");
                                }
                            }
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "Adapter notified, item count: " + adapter.getItemCount());
                        } else {
                            Log.w(TAG, "No employees found for raId: " + raId);
                            Toast.makeText(EmployeeListActivity.this, "No employees found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database error: " + error.getMessage());
                        Toast.makeText(EmployeeListActivity.this, "Error fetching employees: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class Employee {
        public String name, empId;

        public Employee(String name, String empId) {
            this.name = name;
            this.empId = empId;
        }
    }
}