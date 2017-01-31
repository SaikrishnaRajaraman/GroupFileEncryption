package com.zohocorp.krishna_pt1251.groupfileencryption.activites;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.zohocorp.krishna_pt1251.groupfileencryption.R;
import com.zohocorp.krishna_pt1251.groupfileencryption.interfaces.AsyncReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import cz.msebera.android.httpclient.Header;
import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,AsyncReceiver {


    private final static int ITERATIONS=5000;

    private final static String BASE_URL="http://172.22.136.1:2900";


    private EditText userNameView;
    private Button loginButton;
    private Button signUpButton;
    private EditText userPasswordView;
    private String userName;
    private String userPassword;
    private boolean isValidUser=false;
    private String userPasswordKeyText;
    private String saltText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        setContentView(R.layout.activity_login);
        userNameView=(EditText)findViewById(R.id.user_name_view);
        userPasswordView=(EditText)findViewById(R.id.user_pwd_view);
        loginButton=(Button)findViewById(R.id.login_btn);
        loginButton.setOnClickListener(this);
        signUpButton=(Button)findViewById(R.id.btn_sign_up);
        signUpButton.setOnClickListener(this);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

    }

    @Override
    public void onClick(View v) {

        userName = userNameView.getText().toString().trim();
        userPassword = userPasswordView.getText().toString().trim();

        if (v.getId() == R.id.login_btn) {


            if (userName.equals("")) {
                userNameView.setError("Username cannot be blank");
            } else if (userPassword.equals("")) {
                userPasswordView.setError("User password can not be blank");

            } else {

                authenticateUser(userName, userPassword);

            }
        } else if (v.getId() == R.id.btn_sign_up) {
            if (userName.equals("")) {
                userNameView.setError("Username cannot be blank");
            } else if (userPassword.equals("")) {
                userPasswordView.setError("User password can not be blank");

            } else {
                registerUser(userName, userPassword);
            }
        }
    }


    public void registerUser(String userName, String userPassword)
    {

        byte[] salt = getNextBytes();
        RequestParams userCredentials=generateKey(salt,userPassword);
        userCredentials.put("userName", userName);
        AsyncHttpClient asyncHttpClient=new AsyncHttpClient();
        asyncHttpClient.post(BASE_URL+"/registeruser", userCredentials,new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        try {
                            Log.v("Test",response.getString("message"));
                            Toast.makeText(LoginActivity.this,response.getString("message"),Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        //sender.execute(userCredentials);
        //Toast.makeText(this,"User Registered Successfully",Toast.LENGTH_SHORT).show();
            /*Intent mainIntent=new Intent(this,MainActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("userName", userName);
            mainIntent.putExtras(bundle);
            startActivity(mainIntent);
            */

    }



public void authenticateUser(final String userName, String userPassword)
{

    RequestParams userCredentials=new RequestParams();
    MessageDigest messageDigest = null;
    try {
        messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] hashedPassword = messageDigest.digest(userPassword.getBytes());
        String hashedPasswordText = Base64.encodeToString(hashedPassword, Base64.DEFAULT);
        userCredentials.put("userName", userName);
        userCredentials.put("hashedPassword",hashedPasswordText);
        AsyncHttpClient asyncHttpClient=new AsyncHttpClient();
        asyncHttpClient.post(BASE_URL+"/authorizeuser", userCredentials,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Log.v("Test",response.getString("message"));
                    if(response.getString("message").equals("User Authenticated")) {
                        Toast.makeText(LoginActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putString("userName", userName);
                        mainIntent.putExtras(bundle);
                        startActivity(mainIntent);
                    }
                        else
                    {
                        Toast.makeText(LoginActivity.this,response.getString("message"),Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                try {
                    Toast.makeText(LoginActivity.this,errorResponse.getString("message"),Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });



    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
    }


}

    public byte[] getNextBytes()
    {
        byte[] pass =new byte[64];
        Random random=new SecureRandom();
        random.nextBytes(pass);
        return pass;
    }

    @Override
    public void processFinish(JSONObject outputObject) throws JSONException {
        String storedKeyText=outputObject.getString("userKey");
        String storedSaltText=outputObject.getString("salt");
        byte storedSalt[]=Base64.decode(storedSaltText,Base64.DEFAULT);
        generateKey(storedSalt, userPassword);

        if(userPasswordKeyText.equals(storedKeyText))
        {
            isValidUser=true;
        }

        if(isValidUser) {
            Intent mainIntent = new Intent(this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("userName", userName);
            mainIntent.putExtras(bundle);
            startActivity(mainIntent);
            isValidUser=false;
        }

        else
        {
            Toast.makeText(this, "Username/Password is Incorrect", Toast.LENGTH_SHORT).show();
        }


    }

    public RequestParams generateKey(byte saltReceived[], String userPassword) {

        RequestParams userCredentials = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = messageDigest.digest(userPassword.getBytes());
            String hashedPasswordText = Base64.encodeToString(hashedPassword, Base64.DEFAULT);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec(hashedPasswordText.toCharArray(), saltReceived, ITERATIONS, 256);
            SecretKey tmp = secretKeyFactory.generateSecret(keySpec);
            SecretKey userPasswordKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            userCredentials = new RequestParams();

            String userPasswordKeyText = Base64.encodeToString(userPasswordKey.getEncoded(), Base64.DEFAULT).trim();
            String saltText = Base64.encodeToString(saltReceived, Base64.DEFAULT);

            userCredentials.put("userKey", userPasswordKeyText);
            userCredentials.put("salt", saltText);


        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return userCredentials;

    }


}
