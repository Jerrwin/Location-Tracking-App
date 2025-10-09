package com.example.lota.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lota.R;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<Report> reportList;

    public ReportAdapter(List<Report> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);
        holder.dateTimeText.setText("Date: " + report.date + ", Time: " + report.time);
        holder.siteDistanceText.setText("Site: " + report.siteId + ", Distance: " + report.distance + " km");
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView dateTimeText, siteDistanceText;

        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTimeText = itemView.findViewById(R.id.reportDateTime);
            siteDistanceText = itemView.findViewById(R.id.reportSiteDistance);
        }
    }

    // Report model class
    public static class Report {
        public String date, time, siteId, distance;

        public Report(String date, String time, String siteId, String distance) {
            this.date = date;
            this.time = time;
            this.siteId = siteId;
            this.distance = distance;
        }
    }
}