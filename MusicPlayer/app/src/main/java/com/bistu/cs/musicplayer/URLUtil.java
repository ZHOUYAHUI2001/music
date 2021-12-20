package com.bistu.cs.musicplayer;

public class  URLUtil {
      public static String url1="https://cloud-music-api-f494k233x-mgod-monkey.vercel.app/search?keywords=";
      public  static String url2="https://cloud-music-api-f494k233x-mgod-monkey.vercel.app/song/url?id=";
      public  static  String url3="";

      public static String getUrl3(int id) {
            url3="http://music.163.com/api/song/enhance/player/url?&ids=[";
            url3=url3+id+"]&br=3200000";
            return url3;
      }
}

