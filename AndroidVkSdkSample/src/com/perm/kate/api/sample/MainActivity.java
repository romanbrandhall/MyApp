package com.perm.kate.api.sample;

import com.perm.kate.api.Api;
import com.perm.kate.api.User;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.*;
import java.io.*;
import java.io.PrintWriter;
import java.io.File;
import android.os.Environment;
import java.util.Map;
import java.util.Calendar;
import java.sql.*;


public class MainActivity extends Activity {
    
    private final int REQUEST_LOGIN=1;
    
    Button authorizeButton;
    Button logoutButton;
    Button postButton;
	Button myButton1;
	EditText myEditText1;
    EditText messageEditText;
    
	public VkProcessing vkpr=new VkProcessing();
	//public volatile ArrayList<User> myUsers=new ArrayList<User>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setupUI();
        
        //Восстановление сохранённой сессии
        vkpr.account.restore(this);
        
        //Если сессия есть создаём API для обращения к серверу
        if(vkpr.account.access_token!=null)
            vkpr.api=new Api(vkpr.account.access_token, Constants.API_ID);
        
        showButtons();
    }

    private void setupUI() {
        authorizeButton=(Button)findViewById(R.id.authorize);
        logoutButton=(Button)findViewById(R.id.logout);
        postButton=(Button)findViewById(R.id.post);
        messageEditText=(EditText)findViewById(R.id.message);
		myButton1=(Button)findViewById(R.id.myButton1);
		myEditText1=(EditText)findViewById(R.id.myEditText1);
		
        authorizeButton.setOnClickListener(authorizeClick);
        logoutButton.setOnClickListener(logoutClick);
        postButton.setOnClickListener(postClick);
		myButton1.setOnClickListener(myButtonClick1);
    }
    //getUsers button
	private OnClickListener myButtonClick1=new OnClickListener(){
		@Override
		public void onClick(View p1)
		{
			ArrayList<User> Nusers = new ArrayList<User>();
			int Nusr=0;
			String NusrStr=myEditText1.getText().toString();
			if(NusrStr.length()>0){
				try{Nusr = Integer.parseInt(NusrStr);}catch(Exception exc){}
				if(Nusr>0) Nusers=vkpr.getNRealVkUsers(Nusr);
				else return;
				String fileName = new File(Environment.getExternalStorageDirectory().toString() + "/MyFolder").toString()+"/Users.txt";  
				myEditText1.setText(fileName);
				ArrayList<String> NusersAStr=new ArrayList<String>();
				NusersAStr.add("id=0,name,surname,birthDate");
				for(User usr:Nusers){
					NusersAStr.add(usr.uid+","+usr.first_name+","+usr.last_name+","+usr.birthdate);
				}
				common.WriteArrayListToFile(NusersAStr, fileName, false);
			}//if NusrStr>0
		}
	};
    private OnClickListener authorizeClick=new OnClickListener(){
        @Override
        public void onClick(View v) {
            startLoginActivity();
        }
    };
    //exit button
    private OnClickListener logoutClick=new OnClickListener(){
        @Override
        public void onClick(View v) {
            logOut();
        }
    };
    //users process button
    private OnClickListener postClick=new OnClickListener(){
        @Override
        public void onClick(View v) {
            //postToWall();
			String root = Environment.getExternalStorageDirectory().toString();
			File myDir = new File(root + "/MyFolder");    
			String fileName=myDir+"/Users.txt";
			myEditText1.setText(fileName);
			vkpr.ProcUsers(fileName, vkpr.liker);
        }
    };
    //login button
    private void startLoginActivity() {
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                //авторизовались успешно 
                vkpr.account.access_token=data.getStringExtra("token");
                vkpr.account.user_id=data.getLongExtra("user_id", 0);
                vkpr.account.save(MainActivity.this);
                vkpr.api=new Api(vkpr.account.access_token, Constants.API_ID);
                showButtons();
            }
        }
    }
    
    private void postToWall() {
        //Общение с сервером в отдельном потоке чтобы не блокировать UI поток
        new Thread(){
            @Override
            public void run(){
                try {
                    String text=messageEditText.getText().toString();
                    vkpr.api.createWallPost(vkpr.account.user_id, text, null, null, false, false, false, null, null, null, 0L, null, null);
                    //Показать сообщение в UI потоке 
                    runOnUiThread(successRunnable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
	/*
	MyRunnable myR=new MyRunnable(){
		@Override
		public void run(){
			myEditText1.setText("value="+IsReal+" msg="+msg);
			//super.run();
		}
	};
	*/
    Runnable successRunnable=new Runnable(){
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), "Запись успешно добавлена", Toast.LENGTH_LONG).show();
        }
    };
	
    private void logOut() {
        vkpr.api=null;
        vkpr.account.access_token=null;
        vkpr.account.user_id=0;
        vkpr.account.save(MainActivity.this);
        showButtons();
    }
    
    void showButtons(){
        if(vkpr.api!=null){
            authorizeButton.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
            postButton.setVisibility(View.VISIBLE);
            messageEditText.setVisibility(View.VISIBLE);
			myButton1.setVisibility(View.VISIBLE);
			myEditText1.setVisibility(View.VISIBLE);
        }else{
            authorizeButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
            postButton.setVisibility(View.GONE);
            messageEditText.setVisibility(View.GONE);
			myButton1.setVisibility(View.GONE);
			myEditText1.setVisibility(View.GONE);
			myEditText1.setText("100");
        }
    }//showButtons
}
