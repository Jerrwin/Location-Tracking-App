package com.example.lota.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginLogoutTracker {

    private static final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("LoginLogoutRecords");

    public static void recordLoginTime(String empId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String date = dateFormat.format(new Date());
        String time = timeFormat.format(new Date());
        String key = keyFormat.format(new Date()); // Just the timestamp, no prefix

        DatabaseReference recordRef = databaseReference.child(empId).child(key);
        recordRef.child("type").setValue("login");
        recordRef.child("date").setValue(date);
        recordRef.child("time").setValue(time);
    }

    public static void recordLogoutTime(String empId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String date = dateFormat.format(new Date());
        String time = timeFormat.format(new Date());
        String key = keyFormat.format(new Date()); // Just the timestamp, no prefix

        DatabaseReference recordRef = databaseReference.child(empId).child(key);
        recordRef.child("type").setValue("logout");
        recordRef.child("date").setValue(date);
        recordRef.child("time").setValue(time);
    }
}