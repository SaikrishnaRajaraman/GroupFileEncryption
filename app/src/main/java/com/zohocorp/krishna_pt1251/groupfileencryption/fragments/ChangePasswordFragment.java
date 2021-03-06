package com.zohocorp.krishna_pt1251.groupfileencryption.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
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
import com.zohocorp.krishna_pt1251.groupfileencryption.utils.PasswordChange;
import com.zohocorp.krishna_pt1251.groupfileencryption.utils.Receiver;
import com.zohocorp.krishna_pt1251.groupfileencryption.utils.Sender;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class ChangePasswordFragment extends Fragment implements AsyncFileList,AsyncReceiver{


    private String userName;
    private RecyclerView recyclerView;
    private TextView noFileDisplay;
    private Receiver receiver = new Receiver(1500);
    private String fileName;
    private String newFilePassword;
    private String filePassword;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        userName=getArguments().getString("userName");
        View view=inflater.inflate(R.layout.fragment_share_file, container, false);
        recyclerView=(RecyclerView)view.findViewById(R.id.share_file_list);
        noFileDisplay=(TextView)view.findViewById(R.id.no_file_share_view);

        changePassword();

        return view;
    }


    public void changePassword()
    {
        FileList fileList=new FileList();
        fileList.asyncFileList=this;
        fileList.execute(userName);
    }


    @Override
    public void processFinish(JSONObject outputObject) {

        try {
            /*
            After receiving the halfKey,salt and others check if it's null if not use it to check for password
             */
            String receivedSecondHalfKey = outputObject.getString("halfKey").trim();
            String fileIV = outputObject.getString("initVector").trim();
            String saltText = outputObject.getString("salt").trim();
            String encryptedKeyText = outputObject.getString("encryptedKey").trim();
            if(!receivedSecondHalfKey.equals("null")) {
                PasswordChange passwordChange = new PasswordChange();
                JSONObject sendObject = passwordChange.decryptFileKey(filePassword, newFilePassword, receivedSecondHalfKey, fileIV, saltText, encryptedKeyText);
                /*
                    If password is correct then send the new encryptedKey,salt and others to the server
                 */

                if (sendObject != null) {
                    sendObject.put("userName", userName);
                    sendObject.put("fileName", fileName);
                    sendObject.put("encryptedFile", outputObject.getString("encryptedFile"));
                    Sender sender = new Sender(2500);
                    sender.execute(sendObject);
                    Toast.makeText(getContext(),"Password Changed Successfully",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Password is incorrect", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(getContext(),"Username/Filename is Incorrect",Toast.LENGTH_SHORT).show();
            }


        } catch (JSONException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | BadPaddingException | NoSuchPaddingException | InvalidKeySpecException | IllegalBlockSizeException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void populateRecyclerView(final ArrayList<String> outputArrayList) throws JSONException {
        if(!outputArrayList.isEmpty()) {
            RecyclerView.Adapter adapter;
            LinearLayoutManager layoutManager;

            recyclerView.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(layoutManager);
            DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    layoutManager.getOrientation());
            recyclerView.addItemDecoration(mDividerItemDecoration);
            receiver.delegate=this;

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
                            final EditText newPasswordView=new EditText(getContext());
                            newPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            newPasswordView.setHint("Enter the new Password");
                            AlertDialog.Builder passwordDialog = new AlertDialog.Builder(getContext());
                            passwordDialog.setTitle(fileName);
                            passwordDialog.setMessage("Enter the Password");

                            LinearLayout layout=new LinearLayout(getContext());
                            layout.setOrientation(LinearLayout.VERTICAL);
                            layout.addView(passwordTextView);
                            layout.addView(newPasswordView);

                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            passwordTextView.setLayoutParams(layoutParams);
                            passwordDialog.setView(layout);
                            passwordDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    filePassword = passwordTextView.getText().toString();

                                    newFilePassword=newPasswordView.getText().toString();
                                    if (filePassword.equals("")) {
                                        passwordTextView.setError("Password cant be blank");
                                        Toast.makeText(getContext(), "Password is Invalid", Toast.LENGTH_SHORT).show();
                                    }
                                    else if(newFilePassword.equals(""))
                                    {
                                        newPasswordView.setError("Password cant be blank");
                                    }
                                    else {

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
    }
}
