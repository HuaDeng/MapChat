package com.example.dyyao.mapchat;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.internal.LocationRequestUpdateData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Hua Deng on 11/15/2015.
 */
public class ClientTaskWR extends AsyncTask<Void, String, Void> {

    private Queue<String> mCommandBuffer;
    private String mCommand;
    private Socket mSocket;
    private String serverResponse = "";
    private PrintWriter mPrintWriterOut;
    private Login login;
    private Register register;
    public static AddFriend addFriend;
    //Queue<String> sResponseBuffer = new LinkedList<>();
    private static final String TAG = "ClientTaskWR";

    public ClientTaskWR(Queue<String> c, Login l, Register r, AddFriend af) {
        mCommandBuffer = c;
        login = l;
        register = r;
        addFriend = af;
    }

    protected Void doInBackground(Void... arg0){
        try {
            mSocket = new Socket(Login.SERVER_IP_ADDRESS, Login.SERVER_PORT_WR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            //Log.d(TAG, "before isEmpty()");
            if (!mCommandBuffer.isEmpty()){
                Log.d(TAG, "enter isEmpty()");
                mCommand = mCommandBuffer.remove();
                Log.d(TAG,"mCommand is: " + mCommand);
                try{
                    OutputStream mOutStream = mSocket.getOutputStream();
                    mPrintWriterOut = new PrintWriter(mOutStream, true);
                    Log.d(TAG, mCommand);
                    mPrintWriterOut.println(mCommand);

                    InputStream mInputStream = mSocket.getInputStream();
                    BufferedReader mBufferedReader = new BufferedReader(new InputStreamReader(mInputStream));

                    Log.d(TAG, "execute line");
                    serverResponse = mBufferedReader.readLine();
                    //sResponseBuffer.add(serverResponse);
                    Log.d(TAG, serverResponse);
                    /*
                    if (line.equals("no") && mSocket != null){
                        mSocket.close();
                    }
                    */
                    //Log.d(TAG, serverResponse);
                    publishProgress(serverResponse);
                } catch (UnknownHostException e){
                    e.printStackTrace();
                    serverResponse = "UnknownHostException: " + e.toString();
                } catch (IOException e){
                    e.printStackTrace();
                    serverResponse = "IOException: " + e.toString();
                }
            }
        }
    }

    protected void onPostExecute(Void result){

        super.onPostExecute(result);
    }

    protected void onProgressUpdate(String... result){
        super.onProgressUpdate(result);
        String[] sResponses = result[0].split(":");
        //int cmdsLength = cmds.length;
        String command = sResponses[0];
        Log.d(TAG, command);
        String status = sResponses[1];
        Log.d(TAG, status);

        switch (command){
            case "login":{
                Log.d(TAG, "login response received");
                if(status.equals("yes")){
                    Log.d(TAG, "Login Succeed!");
                    Intent loginIntent = new Intent(login, friendList.class);
                    loginIntent.putExtra("friendNames", sResponses);
                    login.startActivity(loginIntent);
                    //login.startActivity(new Intent(login, friendList.class));
                } else {
                    Log.d(TAG, "Login Failed");
                    Toast.makeText(login, "Login Failed!",Toast.LENGTH_SHORT).show();
                    login.etUsername.setText("");
                    login.etPassword.setText("");
                }
                break;
            }
            case "register":{
                Log.d(TAG, "register response received");
                if (status.equals("yes")){
                    Log.d(TAG, "Register Succeed!");
                    register.startActivity(new Intent(register, friendList.class));
                } else {
                    Log.d(TAG, "Register Failed!");
                    Toast.makeText(register,"Register Failed!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case "add_friend":{
                Log.d(TAG, "add_friend response received");
                if(status.equals("yes")){
                    String[] fNames = Arrays.copyOfRange(sResponses, 2, sResponses.length);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result",fNames);
                    addFriend.setResult(Activity.RESULT_OK,returnIntent);
                    addFriend.finish();
                } else {
                    Log.d(TAG, "Add_Friend Failed!");
                    Toast.makeText(addFriend, "Add Friend Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        }

        //Log.d(TAG, result[0]);
    }
}
