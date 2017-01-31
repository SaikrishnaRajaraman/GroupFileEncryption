package com.zohocorp.krishna_pt1251.groupfileencryption.interfaces;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by krishna-pt1251 on 24/01/17.
 */

public interface AsyncFileList {
    void populateRecyclerView(ArrayList<String> outputArrayList) throws JSONException;
}
