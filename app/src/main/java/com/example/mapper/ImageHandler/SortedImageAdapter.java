package com.example.mapper.ImageHandler;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.services.ImageFetchService;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SortedImageAdapter extends RecyclerView.Adapter<SortedImageAdapter.View_Holder> {

    private Context context;

    private List<File> items;

    /**
     * An Constructor method for the class SortedImageAdapter, that takes a list of ImageObj objects,
     * that are populated within the recycler view.
     * @param items
     */
    public SortedImageAdapter(List<File> items) {
        this.items = items;
    }

    /**
     * Creating holders for the recycler view. This is when the row layout is inflated,
     * passed to the ViewHolder object and each child view can be found, stored and manipulated
     * using the holder
     * @param parent
     * @param viewType
     * @return
     */
    @NotNull
    @Override
    public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_sorted_image_item,
                parent, false);
        View_Holder holder = new View_Holder(v);

        context = parent.getContext();
        return holder;
    }

    /**
     * Binder for the adapter method.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NotNull final View_Holder holder, final int position) {

        int numberOfColumns = 3;

        if (items.get(position)!=null) {
            if (items.get(position).listFiles() !=null ) {
                List<ImageObj> myPictureList = new ArrayList<>();
                String contentTitle = items.get(position).getAbsolutePath().split("/")[9];
                List<File> imageFiles = Arrays.asList(items.get(position).listFiles());
                RecyclerView.Adapter mAdapter = new ImageAdapter(myPictureList);
                holder.recyclerView.setLayoutManager(new GridLayoutManager(context, numberOfColumns));
                holder.textView.setText(contentTitle);
                holder.recyclerView.setAdapter(mAdapter);
                myPictureList.addAll(ImageFetchService.getImageElements(imageFiles));
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Get the size of the items passed into the Adapter.
     * An integer value of the size of the adapter is returned
     * @return
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Constructor method for the View_Holder class. Instantiates the view, and assigns the values of
     * the Layout .
     */
    class View_Holder extends RecyclerView.ViewHolder  {
        RecyclerView recyclerView;
        TextView textView;

        View_Holder(View itemView) {
            super(itemView);
             recyclerView = itemView.findViewById(R.id.sorted_visit_gallery);
             textView = itemView.findViewById(R.id.Sorted_Title);

        }

    }
}