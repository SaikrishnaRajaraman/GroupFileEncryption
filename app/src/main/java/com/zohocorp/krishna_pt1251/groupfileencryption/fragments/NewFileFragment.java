package com.zohocorp.krishna_pt1251.groupfileencryption.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zohocorp.krishna_pt1251.groupfileencryption.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class NewFileFragment extends Fragment implements View.OnClickListener {


    private EditText fileNameView;
    private EditText filePasswordView;
    private EditText fileContentView;

    /*
     ITERATIONS is for number of iterations in Key derivation function
     AES_MODE is used to set the type of AES MODE used for encryption
     */

    private final static int ITERATIONS=5000;
    private byte[] salt=new byte[64];
    private final static String AES_MODE="AES/CBC/PKCS5Padding";




    /*
     userName is the user name of the user
     fileName is the name of the file being created
     password is the password for the encryption of the fileKey
     fileContents is the contents of the file
     */




    private String userName;
    private String fileName;

    private String userPasswordKeySecondHalf;
    private String initVectorText;
    private String encryptedKeyText;
    private String encyptFileText;



    public NewFileFragment() {
        // Required empty public constructor
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v=inflater.inflate(R.layout.fragment_new_file, container, false);
        Button saveButton=(Button)v.findViewById(R.id.save_btn);
        saveButton.setOnClickListener(this);
        fileNameView=(EditText)v.findViewById(R.id.file_name_view);
        filePasswordView=(EditText)v.findViewById(R.id.file_password_view);
        fileContentView=(EditText)v.findViewById(R.id.file_content_view);
        userName=getArguments().getString("userName");
        return v;

    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {


            case R.id.save_btn: {
                try {
                    if(fileNameView.getText().toString().equals(""))
                    {
                        fileNameView.setError("Filename cannot be blank");
                    }
                    else if (filePasswordView.getText().toString().equals(""))
                    {
                        filePasswordView.setError("File password cannot be blank");
                    }
                    else {
                        encryptFile();
                    }
                } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeySpecException e) {
                    e.printStackTrace();
                }
                writeToServer();
            }break;

            default:break;
        }
    }
    public void encryptFile() throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException {


        Log.v("userName",userName);
        fileName= fileNameView.getText().toString().trim().replaceAll("\\s+","");
        String password = filePasswordView.getText().toString().trim();
        String fileContents = fileContentView.getText().toString().trim();

        KeyGenerator keyGenerator=KeyGenerator.getInstance("AES");
        keyGenerator.init(128);

        /*
            fileCipher is the cipher used for file encryption and decryption
            fileKey is the key used for file encryption or decryption
        */

        SecretKey fileKey = keyGenerator.generateKey();
        Cipher keyCipher=Cipher.getInstance(AES_MODE);

        /*
            Generate a random key of size 128 bits and initialization vector of 128 bits for file encryption and decryption
         */

        byte[] initializationVector = new byte[keyCipher.getBlockSize()];
        System.out.println("The block size is :"+keyCipher.getBlockSize());

        SecureRandom randomGenerator = new SecureRandom();
        randomGenerator.nextBytes(initializationVector);

        IvParameterSpec ivParameterSpec=new IvParameterSpec(initializationVector);

        /*
            Encrypt the file using generated random key and initialization vector using the AES_MODE specified
         */

        Cipher fileCipher = Cipher.getInstance(AES_MODE);

        fileCipher.init(Cipher.ENCRYPT_MODE, fileKey,ivParameterSpec);

        byte[] encryptedFile= fileCipher.doFinal(fileContents.getBytes("UTF-8"));

        /*
            Generate a key of size 256 bits from user password using salt and PBKDF algorithm
         */

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        salt=getNextBytes();
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(),salt, ITERATIONS, 256);
        SecretKey tmp = secretKeyFactory.generateSecret(keySpec);


        SecretKey userPasswordKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        String userPasswordKeyText = Base64.encodeToString(userPasswordKey.getEncoded(), Base64.DEFAULT).trim();

        /*
            After generating the key split the key into two (2 128 bit keys) and encrypt the file key with the first half of the user generated key
         */

        int userPasswordKeyMid= userPasswordKeyText.length()/2;

        String parts[]={userPasswordKeyText.substring(0,userPasswordKeyMid), userPasswordKeyText.substring(userPasswordKeyMid)};

        String userPasswordKeyFirstHalf = parts[0];
        userPasswordKeySecondHalf=parts[1];

        Log.v("userp1", userPasswordKeyFirstHalf);
        Log.v("userp2",userPasswordKeySecondHalf);

        byte [] decodedKey=Base64.decode(userPasswordKeyFirstHalf,Base64.DEFAULT);

        SecretKey encryptHalfKey=new SecretKeySpec(decodedKey,0,decodedKey.length,"AES");


        /*
            Use the same initialization vector for encryption of the fileKey which you used for file encryption
         */

        keyCipher.init(Cipher.ENCRYPT_MODE,encryptHalfKey,ivParameterSpec);

        String fileKeyText=Base64.encodeToString(fileKey.getEncoded(),Base64.DEFAULT);

        Log.v("filekey",fileKeyText);

        byte encryptedKey[]=keyCipher.doFinal(fileKeyText.getBytes("UTF-8"));

        /*
            After encrypting the fileKey Store the salt,initialization vector,encrypted file,encryptedFileKey,secondHalfKey from user generated key and encrypted file onto the server
         */

        encryptedKeyText=Base64.encodeToString(encryptedKey,Base64.DEFAULT);

        initVectorText=Base64.encodeToString(initializationVector,Base64.DEFAULT);

        encyptFileText=Base64.encodeToString(encryptedFile,Base64.DEFAULT);



    }

    public void writeToServer()
    {
        Sender sender=new Sender();
        sender.execute();
    }

    public byte[] getNextBytes()
    {
        byte[] pass =new byte[64];
        Random random=new SecureRandom();
        random.nextBytes(pass);
        return pass;
    }


    private class Sender extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {

            try {
                Socket clientSocket = new Socket("172.22.136.1", 1200);
                OutputStream outputStream = clientSocket.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

                String saltText=Base64.encodeToString(salt,Base64.DEFAULT);


                Log.v("Username",userName);
                Log.v("userpasswordSecond",userPasswordKeySecondHalf);
                Log.v("Init Vector is ",initVectorText);
                Log.v("Filename",fileName);
                Log.v("Salt",saltText);
                Log.v("encrypted Key",encryptedKeyText);
                Log.v("encrypted File",encyptFileText);


                JSONObject jsonObject=new JSONObject();
                jsonObject.put("userName",userName);
                jsonObject.put("userPasswordKey",userPasswordKeySecondHalf);
                jsonObject.put("initVector",initVectorText);
                jsonObject.put("fileName",fileName);
                jsonObject.put("salt",saltText);
                jsonObject.put("encryptedKey",encryptedKeyText);
                jsonObject.put("encryptedFile",encyptFileText);

                Log.v("JSON Object",jsonObject.toString());

                bufferedWriter.write(jsonObject.toString());

                bufferedWriter.flush();


            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;



        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            fileNameView.setText("");
            filePasswordView.setText("");
            fileContentView.setText("");
            Toast.makeText(getContext(),"File Saved Successfully",Toast.LENGTH_SHORT).show();
        }
    }

}
