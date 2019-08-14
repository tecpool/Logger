package com.tecpool.logger;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by uvclient2 on 24/06/2019.
 */


public class LoggerService extends Service {
    static Timer mTimer;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    try{
        mTimer=new Timer();
        mTimer.schedule(timerTask,1000,(Logger.getInterwal())*60*1000);
        Logger.log("BG","logger service started","");

    }catch (Exception e){}

    }


    public static TimerTask timerTask=new TimerTask() {
        @Override
        public void run() {

            Logger.log("BG","timer task executed","");
            new Logger.AsyncUploadLog().execute();
        }
    };


    public static void  setInterval()
    {
        try {
            try {
                timerTask.cancel();
                Logger.log("BG", "timer task canceled", "");
            } catch (Exception e) {
            }

            mTimer.schedule(timerTask, 1000, (Logger.getInterwal()) * 60 * 1000);
            Logger.log("BG", "timer task started again", "");
        }catch (Exception e){}
    }




}