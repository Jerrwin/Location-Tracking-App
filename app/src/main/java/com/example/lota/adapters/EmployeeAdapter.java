package com.example.lota.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lota.R;
import com.example.lota.activities.EmployeeListActivity;

import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    private List<EmployeeListActivity.Employee> employeeList;
    private OnEmployeeClickListener listener;

    public EmployeeAdapter(List<EmployeeListActivity.Employee> employeeList, OnEmployeeClickListener listener) {
        this.employeeList = employeeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        EmployeeListActivity.Employee employee = employeeList.get(position);
        holder.nameText.setText(employee.name);
        holder.idText.setText(employee.empId);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEmployeeClick(employee);
            }
        });
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, idText;

        EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            idText = itemView.findViewById(R.id.idText);
        }
    }

    // Interface for click events
    public interface OnEmployeeClickListener {
        void onEmployeeClick(EmployeeListActivity.Employee employee);
    }
}