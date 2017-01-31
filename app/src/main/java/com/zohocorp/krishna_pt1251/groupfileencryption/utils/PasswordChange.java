package com.zohocorp.krishna_pt1251.groupfileencryption.utils;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class PasswordChange {

    private final static int ITERATIONS=5000;
    private final static String AES_MODE="AES/CBC/PKCS5Padding";



    public JSONObject decryptFileKey(String userCurrentPassword,String userNewPassword,String receivedSecondHalfKey,String fileIV,String saltText,String encryptedKeyText) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, JSONException {


        JSONObject jsonObject=null;

        /*
            Check for the validity of password by generating the key from password
         */

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] salt = Base64.decode(saltText, Base64.DEFAULT);
        KeySpec keySpec = new PBEKeySpec(userCurrentPassword.toCharArray(), salt, ITERATIONS, 256);
        SecretKey tmp = secretKeyFactory.generateSecret(keySpec);

        SecretKeySpec userGeneratedKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        String userGeneratedKeyText = Base64.encodeToString(userGeneratedKey.getEncoded(), Base64.DEFAULT);

        int userGeneratedKeyMid = userGeneratedKeyText.length() / 2;

        String parts[] = {userGeneratedKeyText.substring(0, userGeneratedKeyMid), userGeneratedKeyText.substring(userGeneratedKeyMid)};

        String userGeneratedKeyFirstHalf = parts[0].trim();
        String userGeneratedKeySecondHalf = parts[1].trim();

        /*
        Split the generated key into two halves and check if both the second halves are equal if not then the password is incorrect
         */

        if (userGeneratedKeySecondHalf.equals(receivedSecondHalfKey.trim())) {


            /*
                If both halves are equal the decrypt the fileKey and encrypt the fileKey with key generated from new password and store it in server
             */
            Cipher keyCipher = Cipher.getInstance(AES_MODE);
            byte initializationVector[] = Base64.decode(fileIV, Base64.DEFAULT);
            Log.v("INIT_VECT", fileIV);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);
            byte[] decodedKey = Base64.decode(userGeneratedKeyFirstHalf, Base64.DEFAULT);
            SecretKey decryptHalfKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            keyCipher.init(Cipher.DECRYPT_MODE, decryptHalfKey, ivParameterSpec);
            byte decodedEncryptedKey[] = Base64.decode(encryptedKeyText, Base64.DEFAULT);
            byte decryptedKey[] = keyCipher.doFinal(decodedEncryptedKey);
            byte[] decodedFileKey = Base64.decode(decryptedKey, Base64.DEFAULT);
            SecretKey fileKey = new SecretKeySpec(decodedFileKey, 0, decodedFileKey.length, "AES");
            String fileKeyText = Base64.encodeToString(fileKey.getEncoded(), Base64.DEFAULT);
            // Encrypt the file key with share password

            byte[] shareSalt = getNextBytes();
            String shareSaltText = Base64.encodeToString(shareSalt, Base64.DEFAULT);

            Cipher shareCipher = Cipher.getInstance(AES_MODE);
            byte[] shareInitializationVector = new byte[16];

            SecureRandom randomGenerator = new SecureRandom();
            randomGenerator.nextBytes(shareInitializationVector);


            keySpec = new PBEKeySpec(userNewPassword.toCharArray(), shareSalt, ITERATIONS, 256);
            tmp = secretKeyFactory.generateSecret(keySpec);
            SecretKeySpec shareKey = new SecretKeySpec(tmp.getEncoded(), "AES");


            String shareKeyText = Base64.encodeToString(shareKey.getEncoded(), Base64.DEFAULT).trim();

            int shareKeyMid = shareKeyText.length() / 2;

            String shareParts[] = {shareKeyText.substring(0, shareKeyMid), shareKeyText.substring(shareKeyMid)};

            String newPasswordKeyFirstHalf = shareParts[0];
            String newPasswordKeySecondHalf = shareParts[1];

            byte[] decodedshareHalfKey = Base64.decode(newPasswordKeyFirstHalf, Base64.DEFAULT);

            SecretKey encryptShareKey = new SecretKeySpec(decodedshareHalfKey, 0, decodedshareHalfKey.length, "AES");

            shareCipher.init(Cipher.ENCRYPT_MODE, encryptShareKey, ivParameterSpec);
            byte encryptedShareFileKey[] = shareCipher.doFinal(fileKeyText.getBytes("UTF-8"));
            String encryptedShareKeyText = Base64.encodeToString(encryptedShareFileKey, Base64.DEFAULT);

            jsonObject=new JSONObject();
            jsonObject.put("userPasswordKey", newPasswordKeySecondHalf);
            jsonObject.put("initVector", fileIV);
            jsonObject.put("salt", shareSaltText);
            jsonObject.put("encryptedKey", encryptedShareKeyText);
        }
            return jsonObject;


    }

    private byte[] getNextBytes() {
        byte[] pass = new byte[64];
        Random random = new SecureRandom();
        random.nextBytes(pass);
        return pass;
    }

}
