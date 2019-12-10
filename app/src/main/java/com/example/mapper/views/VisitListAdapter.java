package com.example.mapper.views;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.services.models.Visit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class VisitListAdapter extends RecyclerView.Adapter<VisitListAdapter.VisitViewHolder> implements Filterable {

    class VisitViewHolder extends RecyclerView.ViewHolder {
        private final TextView visitTitleView;
        private final TextView visitDescriptionView;
        private final TextView visitDistanceView;
        RelativeLayout parentLayout;


        private VisitViewHolder(View itemView) {
            super(itemView);
            visitTitleView = itemView.findViewById(R.id.title);
            visitDescriptionView = itemView.findViewById(R.id.description);
            visitDistanceView = itemView.findViewById(R.id.distance);

            parentLayout = itemView.findViewById(R.id.parent_layout);
        }



    }

    private final LayoutInflater mInflater;
    private final Context mContext;
    private List<Visit> mVisits; // Cached copy of visits
    private List<Visit> mVisitListFiltered;
    public static final String EXTRA_VISIT_VIEW = "com.example.mapper.VISIT_VIEW";


    VisitListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }


    @Override
    public VisitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_visititem, parent, false);
        return new VisitViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(VisitViewHolder holder, final int position) {
        if (mVisitListFiltered != null) {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Visit current = mVisitListFiltered.get(position);
            holder.visitTitleView.setText(current.getTitle() + " - " + dateFormat.format(current.getVisitDate()));
            holder.visitDescriptionView.setText(current.getDescription());

            // Set distance (units dependant on distance, <100m = M, else KM)
            float dist = (float)current.getDistance();
            if (dist < 100) {
                holder.visitDistanceView.setText(String.format("%.1f m", dist));
            } else {
                holder.visitDistanceView.setText(String.format("%.2f Km", dist / 1000.0));
            }


        } else {
            // Covers the case of data not being ready yet.
            holder.visitTitleView.setText("No Title");
        }

        holder.parentLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Visit visit = mVisitListFiltered.get(position);

                Intent intent = new Intent(v.getContext(), VisitView.class);
                intent.putExtra(EXTRA_VISIT_VIEW, visit);
                v.getContext().startActivity(intent);
            }
        });
    }

    void setVisits(List<Visit> visits){
        mVisits = visits;
        mVisitListFiltered = mVisits;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mVisitListFiltered != null)
            return mVisitListFiltered.size();
        else return 0;
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mVisitListFiltered = mVisits;
                } else {
                    List<Visit> filteredList = new ArrayList<>();
                    for (Visit visit : mVisits) {

                        if (visit.getTitle().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(visit);
                        }
                    }

                    mVisitListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mVisitListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mVisitListFiltered = (ArrayList<Visit>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}