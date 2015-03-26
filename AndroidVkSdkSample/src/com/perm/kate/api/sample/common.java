package com.perm.kate.api.sample;
import java.io.*;
import java.io.PrintWriter;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.FileInputStream;
import android.os.Environment;
import android.widget.*;
import android.app.AlertDialog;
import android.content.*;
import java.nio.charset.*;
import java.util.regex.*;
import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;


public class common
{
	public static boolean writeStringToFile(String FileName, String str, boolean append){
		try{
			PrintWriter out=new PrintWriter(new BufferedWriter(new FileWriter(FileName, append)));
			out.print(str);
			out.close();
			return true;
			}catch(Exception e){return false;}
	}//writeStringToFile
	public static String readFileToString(String fileName){
		String ret="";
		try{
			File fl=new File(fileName);
			FileInputStream fin=new FileInputStream(fl);
			ret = convertStreamToString(fin);
			fin.close();
		}catch(Exception e){}
		return ret;
	}
	public static ArrayList<String> ReadFileToArray(String FileName){
		ArrayList<String> ret=new ArrayList<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(FileName));
			String line="";
			while ((line = br.readLine()) != null) {
				if(line.length()>0) ret.add(line);
				// process the line.
			}
			br.close();
		}
		catch(IOException ex){}
		return ret;
	}
	public static boolean WriteArrayListToFile(ArrayList<String> Lines, String FileName, boolean append){
		if(Lines.size()==0) return false;
		try{
			PrintWriter out;
			if(!append){
				out=new PrintWriter(new BufferedWriter(new FileWriter(FileName, append)));
				out.print("");
				out.close();
				}
			
			out=new PrintWriter(new BufferedWriter(new FileWriter(FileName, true)));
			int i=0;
			for(i=0;i<Lines.size()-1;i++){
				if(Lines.get(i).length()>0)
				out.print(Lines.get(i)+"\n");
			}
			i=Lines.size()-1;
			if(Lines.get(i).length()>0)
			out.print(Lines.get(i));
			out.close();
			return true;
		}catch(Exception e){return false;}
	}
	public static String convertStreamToString(InputStream is) throws Exception{
		BufferedReader reader=new BufferedReader(new InputStreamReader(is));
		StringBuilder sb=new StringBuilder();
		String line = null;
		while((line=reader.readLine())!=null){
			sb.append(line).append("\n");
		}
		reader.close();
		return sb.toString();
	}//convertStreamToString
	public static ArrayList<String> parse(String sourse, String RegExp, int pattern){
		//Pattern.CASE_INSENSITIVE
		ArrayList<String> results = new ArrayList<String>();
		Pattern re11=Pattern.compile(RegExp, pattern);
		Matcher m11=re11.matcher(sourse);
		while(m11.find()){
			results.add(m11.group(1));
		}
		return results;
	}//parse
	public static Map<String,String> getKeyValuePairsFromFile(String fileName){
		Map<String,String> ret = new HashMap<String, String>();
		String fileContent=readFileToString(fileName);
		Pattern re11=Pattern.compile("START_(.*?)=(.*?)_END");
		Matcher m11=re11.matcher(fileContent);
		while(m11.find()){
			ret.put(m11.group(1), m11.group(2));
		}
		fileContent=null;
		return ret;
	}
	public static boolean writeKeyValuePairsToFile(Map<String, String> keyValPairs, String fileName){
		if(!writeStringToFile( fileName, "", false)) return false;
		try{
			PrintWriter out=new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
			for(Map.Entry <String, String> entr : keyValPairs.entrySet()){
				out.print("START_"+entr.getKey()+"="+entr.getValue()+"_END\n");
			}
			out.close();
			return true;
		}catch(Exception e){return false;}
		/*
		for(Map.Entry <String, String> entr : keyValPairs.entrySet()){
			res=writeStringToFile(fileName, "START_"+entr.getKey()+"="+entr.getValue()+"_END\n", true);
		}*/
	}//writeKeyValuePairsToFile
}
