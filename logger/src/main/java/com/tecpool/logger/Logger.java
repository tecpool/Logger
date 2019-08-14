package com.tecpool.logger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by uvclient2 on 26/06/2019.
 */

public final class Logger {
    static Activity activity;
    static Context context;
    static SharedPreferences pref;
    static String deviceId="";
    static SharedPreferences.Editor editor;

    public static boolean isInitialized(Activity activity)
    {
        innerUse(activity);
        String visitorId=pref.getString("visitorId", "0");
        return visitorId.equals("0")?false : true;
    }

    public static void innerUse(Activity activit)
    {
        activity=activit;
        context=activit.getBaseContext();
        pref = context.getSharedPreferences("LOGER", 0);
        editor = pref.edit();
        deviceId= Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    public static void init(String key, final Integer minutes)
    {
        boolean res;
        WebService ws=new WebService(activity);
        String param[][]={{"key",key},{"deviceId",deviceId}};
        ws.params=param;
        ws.getString("initService",true,"","", new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                if(result.startsWith("OK"))
                {
                    editor.putString("visitorId", result.substring(result.indexOf("-")+1,result.lastIndexOf("-")));
                    editor.putString("SupportKey", result.substring(result.lastIndexOf("-")+1));
                    editor.commit();
                    setInterval(minutes);
                }
            }
        });

    }

    public static String getSupportKey()
    {
        try {
            return pref.getString("SupportKey", "[not found]");
        }catch (Exception e){
            return "[E:not found]";
        }
    }

    public static void reInit(String key,String userKey, final Integer minutes)
    {
        boolean res;
        WebService ws=new WebService(activity);
        String param[][]={{"key",key},{"deviceId",deviceId},{"userKey",userKey}};
        ws.params=param;
        ws.getString("reInitService",true,"","", new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                if(result.startsWith("OK"))
                {
                    editor.putString("visitorId", result.substring(result.indexOf("-")+1,result.lastIndexOf("-")));
                    editor.putString("SupportKey", result.substring(result.lastIndexOf("-")+1));
                    editor.commit();
                    setInterval(minutes);
                }
            }
        });

    }

    public static void setInterval(int minutes)
    {
        try {
            editor.putInt("interval", minutes);
            editor.commit();
            LoggerService.setInterval();
        }
        catch (Exception s)
        {

        }
    }

    public static int getInterwal()
    {
        try {
            return pref.getInt("interval",10);
        }
        catch (Exception s)
        {
            return 10;
        }
    }

    public static void log(String type,String title,String desc)
    {
        try{new AsyncAddLog(type,title,desc).execute();}catch (Exception e){};
    }

    public static String getSystemDate(String dateFormat)
    {
        try {
            Calendar c = Calendar.getInstance();

            SimpleDateFormat df = new SimpleDateFormat(dateFormat);
            String formattedDate = df.format(c.getTime());
            return formattedDate;
        }catch (Exception e){return "";}

    }

    public static class  AsyncAddLog extends AsyncTask<String, Integer, String> {

        String type, title, desc;

        AsyncAddLog(String type,String title,String desc)
        {
            this.type=type;
            this.title=title;
            this.desc=desc;
        }

        @Override
        protected void onPreExecute() {super.onPreExecute();}


        @Override
        protected String doInBackground(String... params) {

            String oldJson="";

            try {
                oldJson = pref.getString("json", "");
            }catch (Exception e){
                return "";
            }

            if(oldJson.equals(""))
            {
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("visitorId", pref.getString("visitorId","0"));
                    jo.put("type", type);
                    jo.put("title", title);
                    jo.put("description", desc);
                    jo.put("date",""+getSystemDate("dd/MMM/yyyy HH:mm:ss"));
                    editor.putString("json","["+jo.toString()+"]");
                    editor.commit();
                }catch (Exception e){}

            }
            else
            {
                try {
                    JSONArray js = new JSONArray(pref.getString("json", ""));
                    JSONObject jo = new JSONObject();
                    jo.put("visitorId", pref.getString("visitorId","0"));
                    jo.put("type", type);
                    jo.put("title", title);
                    jo.put("description", desc);
                    jo.put("date",""+getSystemDate("dd/MMM/yyyy HH:mm:ss"));
                    js.put(jo);
                    editor.putString("json",js.toString());
                    editor.commit();
                }
                catch (Exception e){}
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }

    }

    public static class AsyncUploadLog extends AsyncTask<String, Integer, String> {


        @Override
        protected void onPreExecute() {super.onPreExecute();}


        @Override
        protected String doInBackground(String... params) {

            try {

                String myFileName= deviceId + ".txt";
                File cDir = context.getCacheDir();
                File tempFile = new File(cDir.getPath() + "/" + myFileName) ;
                FileWriter writer = new FileWriter(tempFile);
                writer.write(pref.getString("json",""));
                writer.close();

                FileInputStream fis=new FileInputStream(tempFile);
                HttpFileUpload obj=new HttpFileUpload("http://logger.tecpool.in/upload.aspx",myFileName,"file","");
                boolean res= obj.Send_Now(fis);

                if(res)
                {
                    try {
                        editor.remove("json");
                        editor.commit();
                        WebService ws = new WebService(activity);
                        String param[][] = {{"myfile", myFileName}};
                        ws.params = param;
                        ws.getString("myFileUploaded",true,"","", new VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {

                            }
                        });
                    }catch (Exception e){}
                }


                return "OK";
            } catch (Exception exc) {
                return exc.toString();
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }

    }


}
