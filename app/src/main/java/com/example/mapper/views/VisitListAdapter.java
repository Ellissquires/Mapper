package com.example.mapper.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.services.models.Visit;

import java.util.List;

public class VisitListAdapter extends RecyclerView.Adapter<VisitListAdapter.VisitViewHolder>{

    class VisitViewHolder extends RecyclerView.ViewHolder {
        private final TextView visitTitleView;
        private final TextView visitDescriptionView;
        RelativeLayout parentLayout;

        private VisitViewHolder(View itemView) {
            super(itemView);
            visitTitleView = itemView.findViewById(R.id.title);
            visitDescriptionView = itemView.findViewById(R.id.description);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }



    }

    private final LayoutInflater mInflater;
    private final Context mContext;
    private List<Visit> mVisits; // Cached copy of visit

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
        if (mVisits != null) {
            Visit current = mVisits.get(position);
            holder.visitTitleView.setText(current.getTitle());
            holder.visitDescriptionView.setText(current.getDescription());

        } else {
            // Covers the case of data not being ready yet.
            holder.visitTitleView.setText("No Title");
        }

        holder.parentLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Visit current = mVisits.get(position);
                Toast.makeText(mContext, current.getTitle(), Toast.LENGTH_SHORT).show();

            }
        });
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