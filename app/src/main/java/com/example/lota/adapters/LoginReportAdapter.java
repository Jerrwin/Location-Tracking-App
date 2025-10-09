package com.example.lota.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lota.R;

import java.util.List;

public class LoginReportAdapter extends RecyclerView.Adapter<LoginReportAdapter.LoginReportViewHolder> {

    private List<LoginReport> loginReportList;

    public LoginReportAdapter(List<LoginReport> loginReportList) {
        this.loginReportList = loginReportList;
    }

    @NonNull
    @Override
    public LoginReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_login_report, parent, false);
        return new LoginReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LoginReportViewHolder holder, int position) {
        LoginReport report = loginReportList.get(position);
        holder.typeText.setText("Type: " + report.type);
        holder.dateTimeText.setText("Date: " + report.date + ", Time: " + report.time);
    }

    @Override
    public int getItemCount() {
        return loginReportList.size();
    }

    static class LoginReportViewHolder extends RecyclerView.ViewHolder {
        TextView typeText, dateTimeText;

        LoginReportViewHolder(@NonNull View itemView) {
            super(itemView);
            typeText = itemView.findViewById(R.id.loginReportType);
            dateTimeText = itemView.findViewById(R.id.loginReportDateTime);
        }
    }

    public static class LoginReport {
        public String type, date, time;

        public LoginReport(String type, String date, String time) {
            this.type = type;
            this.date = date;
            this.time = time;
        }
    }
}