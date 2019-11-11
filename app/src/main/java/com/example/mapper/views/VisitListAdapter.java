package com.example.mapper.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.services.models.Visit;

import java.util.List;

public class VisitListAdapter extends RecyclerView.Adapter<VisitListAdapter.VisitViewHolder> {

    class VisitViewHolder extends RecyclerView.ViewHolder {
        private final TextView visitItemView;

        private VisitViewHolder(View itemView) {
            super(itemView);
            visitItemView = itemView.findViewById(R.id.title);
        }
    }

    private final LayoutInflater mInflater;
    private List<Visit> mVisits; // Cached copy of visit

    VisitListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public VisitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_visit_item, parent, false);
        return new VisitViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(VisitViewHolder holder, int position) {
        if (mVisits != null) {
            Visit current = mVisits.get(position);
            holder.visitItemView.setText(current.getTitle());
        } else {
            // Covers the case of data not being ready yet.
            holder.visitItemView.setText("No Title");
        }
    }

    void setVisits(List<Visit> visits){
        mVisits = visits;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mVisits != null)
            return mVisits.size();
        else return 0;
    }
}