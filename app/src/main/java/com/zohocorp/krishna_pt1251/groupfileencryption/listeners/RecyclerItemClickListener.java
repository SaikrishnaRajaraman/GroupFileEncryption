package com.zohocorp.krishna_pt1251.groupfileencryption.listeners;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);

    }

    GestureDetector gestureDetector;

    public RecyclerItemClickListener(Context context, OnItemClickListener listener)
    {
        itemClickListener=listener;
        gestureDetector=new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){


            @Override
            public boolean onSingleTapUp(MotionEvent e) {

                return true;
            }


        });
    }


    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView=view.findChildViewUnder(e.getX(),e.getY());

        if(childView!=null && itemClickListener!=null && gestureDetector.onTouchEvent(e)) {
            itemClickListener.onItemClick(childView,view.getChildAdapterPosition(childView));
        }

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }


}
