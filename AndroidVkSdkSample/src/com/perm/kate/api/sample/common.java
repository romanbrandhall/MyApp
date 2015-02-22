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
	public static void writeStringToFile(String FileName, String str, boolean append){
		try{
			PrintWriter out=new PrintWriter(new BufferedWriter(new FileWriter(FileName, append)));
			out.print(str);
			out.close();
			}catch(Exception e){
				}
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
	public static void writeKeyValuePairsToFile(Map<String, String> keyValPairs, String fileName){
		writeStringToFile(fileName, "", false);
		for(Map.Entry <String, String> entr : keyValPairs.entrySet()){
			writeStringToFile(fileName, "START_"+entr.getKey()+"="+entr.getValue()+"_END\n", true);
		}
	}//writeKeyValuePairsToFile
}
