package com.example.superpositionexample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.superpositionexample.utilities.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Map;

public class BubbleHeadService extends Service implements Runnable{
    TimerPermissionV2 time;
    Thread t;
    boolean running=true;

    public BubbleHeadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();
        //Crear DB
        //setExerciceNum();
        updateExerciceNum(2);
        getExerciceNum();
        //Crear timer
        time= new TimerPermissionV2();
        time.setTimeLimit(10);
        time.start();

        t= new Thread(this);
        t.start();

    }
    public void updateExerciceNum(int num)
    {
        if(num>0)
        {
            DbSQL conn = new DbSQL(this, "db_config", null, 1);
            SQLiteDatabase db = conn.getWritableDatabase();

            String[] pram={"1"};
            ContentValues values= new ContentValues();
            values.put(Utils.FIELD_TOTALGAMESTOPLAY,num-1);

            db.update(Utils.TABLE_CONFIG,values,Utils.FIELD_ID+"=?",pram);
            db.close();
        }

    }
    private void setExerciceNum()
    {
        DbSQL conn = new DbSQL(this, "db_config", null, 1);
        SQLiteDatabase db = conn.getWritableDatabase();

        String insert = "INSERT INTO "+ Utils.TABLE_CONFIG+" ( "+Utils.FIELD_ID+","+Utils.FIELD_TOTALGAMESTOPLAY+") " +
                "VALUES "+"("+1+","+2+")";

        db.execSQL(insert);
        db.close();
    }
    private int getExerciceNum()
    {
        DbSQL conn = new DbSQL(this, "db_config", null, 1);
        SQLiteDatabase db = conn.getReadableDatabase();

        String[] pram={"1"};
        String[] fields={Utils.FIELD_TOTALGAMESTOPLAY,Utils.FIELD_TOTALGAMESTOPLAY};

        try
        {
            Cursor cursor=db.query(Utils.TABLE_CONFIG, fields, Utils.FIELD_ID+"=?",pram,null,null,null);
            cursor.moveToFirst();
            //Toast.makeText(this, "cantidad de ejercicios :"+cursor.getString(0), Toast.LENGTH_LONG).show();
            int result = cursor.getInt(0);

            cursor.close();
            return result;
        }
        catch(Exception e)
        {
            //Toast.makeText(this, "No se encontro campo id=1", Toast.LENGTH_LONG).show();
            return 0;
        }
    }

    public void goToHome()
    {
        Intent intent= new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    @Nullable
    public static Activity getActivity() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {

        Class activityThreadClass = Class.forName("android.app.ActivityThread");
        Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
        Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
        activitiesField.setAccessible(true);

        Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
        if (activities == null)
            return null;

        for (Object activityRecord : activities.values()) {
            Class activityRecordClass = activityRecord.getClass();
            Field pausedField = activityRecordClass.getDeclaredField("paused");
            pausedField.setAccessible(true);
            if (!pausedField.getBoolean(activityRecord)) {
                Field activityField = activityRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);
                return activity;
            }
        }

        return null;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        startService(new Intent(BubbleHeadService.this, BlockService.class));
        Toast.makeText(this, "bloqueado", Toast.LENGTH_SHORT).show();
        running=false;
    }


   @Override
    public void run() {
    int actualExcerciceNum=0;
        while(running==true)
        {

            try {
                t.sleep(500);
                if (actualExcerciceNum<=0)
                {
                    time.setTimeLimit(10);
                    time.restart();
                    updateExerciceNum(2);
                }
                t.sleep(500);
                actualExcerciceNum= getExerciceNum();
                if(time.isTimeFinish() && !(getActivity()+"").equals("superpositionexample") && actualExcerciceNum>0)
                {
                    Intent intent = new Intent(BubbleHeadService.this, Game1.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

 /*           try {
                t.sleep(1500);

                Intent intent = new Intent(BubbleHeadService.this, Game1.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
*/
        }



   }
}