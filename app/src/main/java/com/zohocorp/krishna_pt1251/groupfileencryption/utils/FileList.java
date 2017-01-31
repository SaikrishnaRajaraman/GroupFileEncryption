package com.zohocorp.krishna_pt1251.groupfileencryption.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.zohocorp.krishna_pt1251.groupfileencryption.interfaces.AsyncFileList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by krishna-pt1251 on 24/01/17.
 */

public class FileList extends AsyncTask<String,Void,Void> {

    private ArrayList<String> fileArrayList= new ArrayList<>();
    private byte[] incomingData=new byte[2048];
    public AsyncFileList asyncFileList=null;

    @Override
    protected Void doInBackground(String... params) {


        try {

            Log.v("Inside Async Class","Hello");
            Socket clientSocket = new Socket("172.22.136.1", 2501);
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            JSONObject sendObject=new JSONObject();
            sendObject.put("userName",params[0]);
            bufferedWriter.write(sendObject.toString());
            bufferedWriter.flush();


            inputStream.read(incomingData);

            JSONObject receiveObject = new JSONObject(new String(incomingData));
            Log.v("JSON obje",receiveObject.toString());
            if(!receiveObject.isNull("fileArray")) {
                JSONArray jsonArray = receiveObject.getJSONArray("fileArray");


                for (int i = 0; i < jsonArray.length(); i++) {
                    fileArrayList.add(jsonArray.getString(i));
                }
            }


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

                /*
                    After receiving the filenames populate it in Recycler view
                 */
        try {
            asyncFileList.populateRecyclerView(fileArrayList);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
