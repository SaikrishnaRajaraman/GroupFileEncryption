package com.zohocorp.krishna_pt1251.groupfileencryption.interfaces;

import org.json.JSONException;
import org.json.JSONObject;


public interface AsyncReceiver {
    void processFinish(JSONObject outputObject) throws JSONException;
}
