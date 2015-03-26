package com.perm.kate.api.sample;
import com.perm.kate.api.Api;
import com.perm.kate.api.User;
import com.perm.kate.api.Photo;
import java.security.*;
import java.util.*;
import java.io.*;
import java.io.PrintWriter;
import java.io.File;
import android.os.Environment;
import java.util.Map;
import java.util.Calendar;
import java.sql.*;

public class VkProcessing
{
	public volatile User myUsr=new User();
	public Account account=new Account();
    public Api api;
	public ProcForUser liker=new SetLikeToAva();
	
	public ArrayList<User> getNRealVkUsers(int NResults){
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
		}//if u ask vk for 1000, but it return 10 only => capacity=100

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
				//Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			}
			for(int i=0; i<currUsers.size();i++){
				if(isReal(currUsers.get(i))){//user is real
					//if(i<0.1*currUsers.size()){
					myUsr.birthdate=BirthDay+"."+BirthMonth+"."+BirthYear;
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
		//myEditText1.setText(BirthDay+"."+BirthMonth+"."+BirthYear+" Ufinded="+usersRealFinded.size()+" Nreal="+currUsersReal.size()+" Nusers="+currUsers.size()+" count="+Count);
		currUsers=null;
		currUsersReal=null;

		return usersRealFinded;
	}
	
	public boolean isReal(User usr){
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
	public boolean ProcUsers(String FileName, ProcForUser prc){
		ArrayList<String> UsersLines=common.ReadFileToArray(FileName);
		if(UsersLines.size()<2) return false;
		String[] firstLines=UsersLines.get(0).split(",");
		if(firstLines.length==0) return false;

		int OldI = Integer.parseInt(firstLines[0].split("=")[1]);//last position
		if(OldI >= UsersLines.size()-1) return false;
		int i;
		for(i=OldI+1;i<UsersLines.size();i++){
			String[] params=UsersLines.get(i).split(",");
			//if(i>5)break;
			//process user's id
			prc.run(params[0]);
		}//iterate i
		if(i>=UsersLines.size()) i=UsersLines.size()-1;
		//very stupid and slow algoritm below:
		//all we need is to change value i of "id=i" at the first line of FileName
		ArrayList<String> UsersLinesNew=new ArrayList<String>();
		UsersLinesNew.add("id="+i+",name,surname,birthDate");
		for(int j=1;j<UsersLines.size();j++){
			if(UsersLines.get(j).length()>0)
				UsersLinesNew.add(UsersLines.get(j));
		}
		common.WriteArrayListToFile(UsersLinesNew, FileName, false);

		/*
		 try{
		 RandomAccessFile raInputFile = new RandomAccessFile(FileName, "rw");
		 String origHeaderRow=raInputFile.readLine();
		 raInputFile.seek(0);
		 raInputFile.writeBytes("id="+i+",name,surname,birthDate\n");
		 raInputFile.close();
		 }catch(IOException ex) {return false;}
		 *///
		return true;
	}//ProcUsers
	public interface ProcForUser{
		public boolean run(String id);
	}
	public class SetLikeToAva implements ProcForUser{
		public boolean run(String idStr){
			try{
				Long uid=Long.parseLong(idStr);
				ArrayList<Photo> UserPhotos = api.getUserPhotos(uid, 0, 1000);
			}catch(Exception exc){}
			return false;
		}
	}//setliketoava class
}
