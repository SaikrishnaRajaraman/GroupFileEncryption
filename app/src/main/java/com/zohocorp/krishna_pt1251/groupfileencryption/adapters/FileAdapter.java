package com.zohocorp.krishna_pt1251.groupfileencryption.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zohocorp.krishna_pt1251.groupfileencryption.R;

import java.util.ArrayList;


public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    private ArrayList<String> fileArrayList;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView textView;
        public ViewHolder(View v)
        {
            super(v);
            textView=(TextView)v.findViewById(R.id.fileNameTextView);
        }

    }

    public FileAdapter (ArrayList<String> fileList)
    {
        fileArrayList=fileList;
    }

    @Override
    public FileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_layout,parent,false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(fileArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return fileArrayList.size();
    }
}
