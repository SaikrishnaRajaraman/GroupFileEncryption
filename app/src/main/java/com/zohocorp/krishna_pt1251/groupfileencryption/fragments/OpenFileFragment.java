package com.zohocorp.krishna_pt1251.groupfileencryption.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zohocorp.krishna_pt1251.groupfileencryption.R;
import com.zohocorp.krishna_pt1251.groupfileencryption.adapters.FileAdapter;
import com.zohocorp.krishna_pt1251.groupfileencryption.interfaces.AsyncFileList;
import com.zohocorp.krishna_pt1251.groupfileencryption.interfaces.AsyncReceiver;
import com.zohocorp.krishna_pt1251.groupfileencryption.listeners.RecyclerItemClickListener;
import com.zohocorp.krishna_pt1251.groupfileencryption.utils.FileList;
import com.zohocorp.krishna_pt1251.groupfileencryption.utils.Receiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class OpenFileFragment extends Fragment implements AsyncReceiver,AsyncFileList {



    private final static int ITERATIONS=5000;
    private byte[] salt=new byte[64];
    private final static String AES_MODE="AES/CBC/PKCS5Padding";


    private String userName;
    private RecyclerView recyclerView;
    private Receiver receiver=new Receiver(1500);
    private String fileName;
    private String filePassword;
    private String receivedSecondHalfKey;
    private String fileIV;
    private String saltText;
    private String encryptedKeyText;
    private String encryptedFileText;
    private String decryptedFileContents;
    private TextView decryptedTextView;
    private byte[] incomingData=new byte[2048];
    private TextView noFileDisplay;

    public OpenFileFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        userName=getArguments().getString("userName");
        sendToServer();
        View view=inflater.inflate(R.layout.fragment_open_file, container, false);
        decryptedTextView=(TextView)view.findViewById(R.id.decrypted_text_view);
        recyclerView=(RecyclerView)view.findViewById(R.id.recycler_view);
        noFileDisplay=(TextView)view.findViewById(R.id.no_file_view);
        return view;

    }



    public void sendToServer()
    {
        FileList fileList=new FileList();
        fileList.asyncFileList=this;
        fileList.execute(userName);

    }


    @Override
    public void processFinish(JSONObject outputObject) throws JSONException {
        receivedSecondHalfKey = outputObject.getString("halfKey").trim();
        fileIV = outputObject.getString("initVector").trim();
        saltText = outputObject.getString("salt").trim();
        encryptedKeyText = outputObject.getString("encryptedKey").trim();
        encryptedFileText=outputObject.getString("encryptedFile").trim();
        try {
            decryptFile();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | UnsupportedEncodingException | InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

    }

    public void decryptFile() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {

        /*
            Check for the valid username or filename
         */

        if(!receivedSecondHalfKey.equals("null")) {

            /*
                After receiving the salt,initialization vector, make use of the password to generate the key used for encrypting/decrypting the file key
             */

            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] salt = Base64.decode(saltText, Base64.DEFAULT);
            KeySpec keySpec = new PBEKeySpec(filePassword.toCharArray(), salt, ITERATIONS, 256);
            SecretKey tmp = secretKeyFactory.generateSecret(keySpec);

            SecretKey userGeneratedKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            String userGeneratedKeyText = Base64.encodeToString(userGeneratedKey.getEncoded(), Base64.DEFAULT);

            int userGeneratedKeyMid = userGeneratedKeyText.length() / 2;

            String parts[] = {userGeneratedKeyText.substring(0, userGeneratedKeyMid), userGeneratedKeyText.substring(userGeneratedKeyMid)};

            String userGeneratedKeyFirstHalf = parts[0].trim();
            String userGeneratedKeySecondHalf = parts[1].trim();

            Log.v("User2ndhalf", userGeneratedKeySecondHalf);
            Log.v("rec2ndhalf", receivedSecondHalfKey);
            Log.v("user1sthalf", userGeneratedKeyFirstHalf);

            /*
                Split the generated key into two and check if both the userGeneratedSecondHalf and storedSecondHalfKey are same if not password is wrong
             */


            if (userGeneratedKeySecondHalf.equals(receivedSecondHalfKey.trim())) {

                /*
                If both the keys are same then decrypt the fileKey with firstHalfKey
                 */
                Log.v("user1sthalf2", userGeneratedKeyFirstHalf);
                Cipher keyCipher = Cipher.getInstance(AES_MODE);
                byte initializationVector[] = Base64.decode(fileIV, Base64.DEFAULT);
                Log.v("INIT_VECT", fileIV);
                IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);
                byte[] decodedKey = Base64.decode(userGeneratedKeyFirstHalf, Base64.DEFAULT);
                SecretKey decryptHalfKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
                keyCipher.init(Cipher.DECRYPT_MODE, decryptHalfKey, ivParameterSpec);
                byte decodedEncryptedKey[] = Base64.decode(encryptedKeyText, Base64.DEFAULT);
                byte decryptedKey[] = keyCipher.doFinal(decodedEncryptedKey);
                byte decodedFileKey[] = Base64.decode(decryptedKey, Base64.DEFAULT);

                /*
                    After decrypting the fileKey use it to decrypt the file
                 */

                SecretKey fileKey = new SecretKeySpec(decodedFileKey, 0, decodedFileKey.length, "AES");
                Cipher fileCipher = Cipher.getInstance(AES_MODE);
                fileCipher.init(Cipher.DECRYPT_MODE, fileKey, ivParameterSpec);
                byte encryptedFileBytes[] = Base64.decode(encryptedFileText, Base64.DEFAULT);

                byte decryptedFile[] = fileCipher.doFinal(encryptedFileBytes);

                decryptedFileContents = new String(decryptedFile);
                Log.v("Decrypt file", decryptedFileContents);
                decryptedTextView.setText(decryptedFileContents);
                decryptedTextView.setVisibility(View.VISIBLE);


            } else {
                Toast.makeText(getContext(),"The Password entered is incorrect", Toast.LENGTH_SHORT).show();
                recyclerView.setVisibility(View.VISIBLE);
            }
        }

        else
        {
            Toast.makeText(getContext(),"No Files to Show",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void populateRecyclerView(final ArrayList<String> outputArrayList)  {

        if(!outputArrayList.isEmpty()) {
            RecyclerView.Adapter adapter;
            LinearLayoutManager layoutManager;



            recyclerView.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(layoutManager);
            DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    layoutManager.getOrientation());
            recyclerView.addItemDecoration(mDividerItemDecoration);

            receiver.delegate = this;

            adapter = new FileAdapter(outputArrayList);
            recyclerView.setAdapter(adapter);
            noFileDisplay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                /*
                    When user clicks on a filename get the password from the user and send it to server to retrieve the file
                 */

                            fileName = outputArrayList.get(position);
                            Log.v("Filename is ", fileName);
                            final EditText passwordTextView = new EditText(getContext());
                            passwordTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            AlertDialog.Builder passwordDialog = new AlertDialog.Builder(getContext());
                            passwordDialog.setTitle(fileName);
                            passwordDialog.setMessage("Enter the Password");

                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            passwordTextView.setLayoutParams(layoutParams);
                            passwordDialog.setView(passwordTextView);
                            passwordDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    filePassword = passwordTextView.getText().toString();
                                    if (filePassword.equals("")) {
                                        passwordTextView.setError("Password field cannot be blank");
                                        Toast.makeText(getContext(), "Password is Invalid", Toast.LENGTH_SHORT).show();
                                    } else {
                                        recyclerView.setVisibility(View.GONE);
                                        JSONObject sendObject = new JSONObject();
                                        try {
                                            sendObject.put("userName", userName);
                                            sendObject.put("fileName", fileName);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        receiver.execute(sendObject);
                                    }
                                }
                            });

                            passwordDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }

                            });
                            passwordDialog.show();
                        }
                    })
            );
        }

        else
        {
            decryptedTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            noFileDisplay.setVisibility(View.VISIBLE);
        }



    }
}
