package com.perm.kate.api.sample;

public class Constants {
    //Этот ID предназначен только для примера. Пожалуйста замение его ID своего приложения.
    public static String API_ID="2904017";
	public static int MyInt=25;
	public static int MaxFollovers=100;
	public static int MinFriends=100;
	public static int MaxFriends=500;
	public static double LastVizitDaysAgoMax=10.0;
	public static int NLikesPerDay=500;
	public static int delay=1010;//in miliseconds
	public static int sex=1, city=1, country=0, 
					status=1, sort=1, 
					uni_country=0, uni=0, uni_year=0, 
					age_from=0, age_to=0, online=0, has_photo=0,
					school_country=0, school_city=0, school=0, school_year=0;
	public static Long offset=0L, count= 1000L, MinResFind=10L;
	public static String fields="status,last_seen", q="", hometown="";
	public static double RealUserRatio = 0.1;
}
