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
    
    Account account=new Account();
    Api api;
	//public volatile ArrayList<User> myUsers=new ArrayList<User>();
    //my test test1
	public volatile User myUsr=new User();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setupUI();
        
        //Восстановление сохранённой сессии
        account.restore(this);
        
        //Если сессия есть создаём API для обращения к серверу
        if(account.access_token!=null)
            api=new Api(account.access_token, Constants.API_ID);
        
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
    
	private OnClickListener myButtonClick1=new OnClickListener(){
		@Override
		public void onClick(View p1)
		{
			// TODO: Implement this method
			startMyButtonActivity1();
		}
	};
    private OnClickListener authorizeClick=new OnClickListener(){
        @Override
        public void onClick(View v) {
            startLoginActivity();
        }
    };
    
    private OnClickListener logoutClick=new OnClickListener(){
        @Override
        public void onClick(View v) {
            logOut();
        }
    };
    
    private OnClickListener postClick=new OnClickListener(){
        @Override
        public void onClick(View v) {
            postToWall();
        }
    };
    
    private void startLoginActivity() {
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }
	private ArrayList<User> getNRealVkUsers(int NResults){
		String root = Environment.getExternalStorageDirectory().toString();
		File myDir = new File(root + "/MyFolder");  
		if(!myDir.exists()) myDir.mkdirs();    
		String fileName=myDir+"/StoredData.txt";
		Map <String, String> myMap =new HashMap<String, String>();
		myMap = common.getKeyValuePairsFromFile(fileName);
		int BirthDay=Integer.parseInt(myMap.get("BirthDay"));
		int BirthMonth=Integer.parseInt(myMap.get("BirthMonth"));
		int BirthYear=Integer.parseInt(myMap.get("BirthYear"));
		int Direction=Integer.parseInt(myMap.get("Direction"));
		Calendar calend = Calendar.getInstance();
		calend.set(BirthYear, BirthMonth-1, BirthDay);
		ArrayList<User> usersRealFinded=new ArrayList<User>();
		ArrayList<User> currUsers=new ArrayList<User>();
		ArrayList<User> currUsersReal=new ArrayList<User>();
		long Count=0;
		double NcircleMaxDouble=(double)NResults/((double)Constants.count*Constants.RealUserRatio);
		int NcircleMaxInt=1;
		int MinCountCapacity= 1;
		if(Constants.MinResFind>0){
			MinCountCapacity=(int)(Constants.count/Constants.MinResFind);
			}//ask vk for 1000, but it return 10 only => capacity=100
		
		if(NcircleMaxDouble<1.0){
			NcircleMaxInt=MinCountCapacity;
			Count=(long)(0.5+(double)NResults/Constants.RealUserRatio);
			if (Count>Constants.count) Count=Constants.count;
		}else{
			Count=Constants.count;
			NcircleMaxInt=MinCountCapacity*(int)(0.5+NcircleMaxDouble);
			}
			
		for(int i1=0;i1<NcircleMaxInt;i1++){
			currUsersReal.clear();
			currUsers.clear();
			try{
				currUsers = api.searchUser(Constants.q, Constants.fields, Count, Constants.offset,Constants.sort,
									   Constants.city,Constants.country,Constants.hometown,
									   Constants.uni_country,Constants.uni,Constants.uni_year,
									   Constants.sex,Constants.status,Constants.age_from,Constants.age_to,
									   BirthDay, BirthMonth, BirthYear,Constants.online,Constants.has_photo,
									   Constants.school_country,Constants.school_city,Constants.school,Constants.school_year,"",
									   "","","",0L);
				Thread.sleep(Constants.delay);
			}
			catch(Exception e){
				Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			}
			for(int i=0; i<currUsers.size();i++){
				if(isReal(currUsers.get(i))){//user is real
				//if(i<0.1*currUsers.size()){
					currUsersReal.add(myUsr);//add external myUsr!!!!
				}//is real
			}//for i
			int UsersWere=usersRealFinded.size();
			if(UsersWere==0 || (UsersWere+currUsersReal.size())<=NResults){
				for(User usr:currUsersReal){
					usersRealFinded.add(usr);
				}
				//iterate date
				calend.add(Calendar.DATE, Direction);
				BirthDay=calend.get(Calendar.DAY_OF_MONTH);
				BirthMonth=calend.get(Calendar.MONTH)+1;
				BirthYear=calend.get(Calendar.YEAR);
				if(UsersWere==0 && currUsersReal.size()>NResults) break;
				if((UsersWere+currUsersReal.size())==NResults) break;
				continue;
			}
			else break;
		}
		myMap.clear();
		//save new date
		myMap.put("Direction",""+Direction);
		myMap.put("BirthDay",""+BirthDay);
		myMap.put("BirthMonth",""+BirthMonth);
		myMap.put("BirthYear",""+BirthYear);
		common.writeKeyValuePairsToFile(myMap, fileName);
		//myEditText1.setText("Nreal="+currUsersReal.size()+" Nusers="+currUsers.size()+" count="+Count);
		//Toast.makeText(getApplicationContext(), ""+currUsers.get(0).uid, Toast.LENGTH_LONG).show();
		myEditText1.setText(BirthDay+"."+BirthMonth+"."+BirthYear+" Ufinded="+usersRealFinded.size()+" Nreal="+currUsersReal.size()+" Nusers="+currUsers.size()+" count="+Count);
		currUsers=null;
		currUsersReal=null;
		
		return usersRealFinded;
	}
	
	private void startMyButtonActivity1(){
		ArrayList<User> Nusers = new ArrayList<User>();
        String NUsersStr = myEditText1.getText().toString();
        int Nusr = Integer.parseInt(NUsersStr);
        if(Nusr>0) Nusers=getNRealVkUsers(Nusr);
		String root = Environment.getExternalStorageDirectory().toString();
		File myDir = new File(root + "/MyFolder");  
		String fileName=myDir+"/Users.txt";
		//myEditText1.setText(""+Nusers.size());
		common.writeStringToFile(fileName,"id name surname\n",false);
		for(User usr:Nusers){
			common.writeStringToFile(fileName, usr.uid+" "+usr.first_name+" "+usr.last_name+"\n",true);
		}
		
		try{
			//Toast.makeText(getApplicationContext(), "roma", Toast.LENGTH_SHORT).show();
		}catch (Exception e){
			//myEditText1.setText(e.getMessage());
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}

	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                //авторизовались успешно 
                account.access_token=data.getStringExtra("token");
                account.user_id=data.getLongExtra("user_id", 0);
                account.save(MainActivity.this);
                api=new Api(account.access_token, Constants.API_ID);
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
                    api.createWallPost(account.user_id, text, null, null, false, false, false, null, null, null, 0L, null, null);
                    //Показать сообщение в UI потоке 
                    runOnUiThread(successRunnable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
	
	private boolean isReal(User usr){
		myUsr=usr;
		try{
			if (myUsr.followers_count==0){
				myUsr.followers_count=api.getFollowers(myUsr.uid, 0,0, null, null).size();
				Thread.sleep(Constants.delay);
			}
			if (myUsr.friends_count==0){
				myUsr.friends_count=api.getFriends(myUsr.uid, "name",0, null, null).size();
				Thread.sleep(Constants.delay);
			}
			if (myUsr.last_seen==0){
				myUsr.last_seen=api.getLastActivity(myUsr.uid).last_seen;
				Thread.sleep(Constants.delay);
			}
			long unixTime=System.currentTimeMillis()/1000L;//milisec to seconds
			double LastVizitDaysAgo=(unixTime-myUsr.last_seen)/(60*60*24);//sec to days
			if(	0<myUsr.followers_count
			   && myUsr.followers_count<Constants.MaxFollovers 
			   && Constants.MinFriends<myUsr.friends_count
			   && myUsr.friends_count<Constants.MaxFriends
			   && LastVizitDaysAgo<Constants.LastVizitDaysAgoMax
			   )
				return true;
			else return false;
		}catch(Exception e){return false;}
	}
	private void getUserInfo() {
        //Общение с сервером в отдельном потоке чтобы не блокировать UI поток
        new Thread(){
            @Override
            public void run(){
                try {
					if (myUsr.followers_count==0)
					myUsr.followers_count =api.getFollowers(myUsr.uid, 0,0, null, null).size();
					if (myUsr.friends_count==0)
					myUsr.friends_count=api.getFriends(myUsr.uid, "name",0, null, null).size();
					if (myUsr.last_seen==0)
					myUsr.last_seen=api.getLastActivity(myUsr.uid).last_seen;
					long unixTime=System.currentTimeMillis()/1000L;//milisec to seconds
					double LastVizitDaysAgo=(unixTime-myUsr.last_seen)/(60*60*24);//sec to days
					if(	0<myUsr.followers_count
						&& myUsr.followers_count<Constants.MaxFollovers 
						&& Constants.MinFriends<myUsr.friends_count
						&& myUsr.friends_count<Constants.MaxFriends
						&& LastVizitDaysAgo<Constants.LastVizitDaysAgoMax
					)
					myR.IsReal=true;
					else myR.IsReal=false;
					
					int sex=1, city=1, status=1;
					int BirthDay=20, BirthMonth=8, BirthYear=1987;
					Long offset=0L, count= 100L;
					String fields="status,last_seen";
					/*myUsers.clear();
					myUsers=null;
					myUsers=api.searchUser("", fields, count, offset,0,
					city,0,"",0,0,
					0,sex,status,0,0,
					BirthDay, BirthMonth, BirthYear,0,0,
					0,0,0,0,"",
					"","","",0L);
					*/
					myR.msg=""+myR.IsReal; //+(unixTime-usrs.get(0).last_seen)/(60*60);//sec to hours
                } catch (Exception e) {
					myR.msg=(e.getMessage());
                    e.printStackTrace();
                }
				//Показать сообщение в UI потоке
				runOnUiThread(myR);
            }
        }.start();
    }

	MyRunnable myR=new MyRunnable(){
		@Override
		public void run(){
			myEditText1.setText("value="+IsReal+" msg="+msg);
			//super.run();
		}
	};
    Runnable successRunnable=new Runnable(){
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), "Запись успешно добавлена", Toast.LENGTH_LONG).show();
        }
    };
	
    private void logOut() {
        api=null;
        account.access_token=null;
        account.user_id=0;
        account.save(MainActivity.this);
        showButtons();
    }
    
    void showButtons(){
        if(api!=null){
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
