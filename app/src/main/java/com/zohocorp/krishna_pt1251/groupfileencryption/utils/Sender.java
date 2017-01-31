package com.zohocorp.krishna_pt1251.groupfileencryption.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class Sender extends AsyncTask<JSONObject, Void, Void> {

    private int portNumber;


    public Sender(int pNo)
    {
        portNumber=pNo;
    }

    @Override
    protected Void doInBackground(JSONObject... params) {

        try {

                Socket clientSocket = new Socket("172.22.136.1", portNumber);
                OutputStream outputStream = clientSocket.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

                Log.v("JSON Object", params[0].toString());

                bufferedWriter.write(params[0].toString());

                bufferedWriter.flush();



        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;


    }

}