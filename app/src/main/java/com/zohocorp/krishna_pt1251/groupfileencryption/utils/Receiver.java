package com.zohocorp.krishna_pt1251.groupfileencryption.utils;

import android.os.AsyncTask;

import com.zohocorp.krishna_pt1251.groupfileencryption.interfaces.AsyncReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class Receiver extends AsyncTask<JSONObject, Void, Void> {

    public AsyncReceiver delegate = null;
    private JSONObject receivedObject;
    private byte incomingData[]=new byte[2048];
    private int portNumber;

    public Receiver(int pNo)
    {
        portNumber=pNo;
    }


    @Override
    protected Void doInBackground(JSONObject... params) {

        try {
            Socket clientSocket = new Socket("172.22.136.1", portNumber);
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);


            bufferedWriter.write(params[0].toString());
            bufferedWriter.flush();


            inputStream.read(incomingData);

            receivedObject = new JSONObject(new String(incomingData));


            //System.out.println("HalfKey received:"+receivedSecondHalfKey);
            //System.out.println("")




        } catch (IOException | JSONException e) {

            e.printStackTrace();
        }

        return null;


    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        //decryptedFileText.setText(decryptedFileContents);
        try {
            delegate.processFinish(receivedObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
