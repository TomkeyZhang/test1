package com.test;

import com.test.TestActivity.DBhelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TestActivity extends Activity {
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int i = 0;
            while (true) {
                ContentValues cv = new ContentValues();
                long time = System.currentTimeMillis();
                cv.put("id", i);
                cv.put("name", "tomkey" + i);
//                synchronized (TestActivity.this) {
                    if (writeAbleDb != null && !writeAbleDb.isOpen()) {
                        writeAbleDb = dbHelper.getWritableDatabase();
                    }
                    writeAbleDb.insert("test", null, cv);
//                    writeAbleDb.close();
//                }
                Log.d("zqt", Thread.currentThread().getName() + " : time1= "
                        + (System.currentTimeMillis() - time));
                i++;
                // try {
                // Thread.sleep(10);
                // } catch (InterruptedException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // }
                if (i > 20)
                    break;
            }
        }
    };
    Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            int i = 0;
            while (true) {
                long time = System.currentTimeMillis();
//                synchronized (TestActivity.this) {
                    if (readableDb != null && !readableDb.isOpen()) {
                        readableDb = dbHelper.getReadableDatabase();
                    }
                    readableDb.query("test", null, "id=1", null, null, null,
                            null);
//                    readableDb.close();
//                }

                Log.d("zqt", Thread.currentThread().getName() + " : time2= "
                        + (System.currentTimeMillis() - time));
                i++;
                // try {
                // Thread.sleep(10);
                // } catch (InterruptedException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // }
                if (i > 20)
                    break;
            }
        }
    };
    SQLiteDatabase writeAbleDb;
    SQLiteDatabase readableDb;
    private DBhelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // WindowManager wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        // TextView tv=new TextView(this);
        // tv.setText("kajskajsk");
        // tv.setTextColor(android.R.color.white);
        // wm.addView(tv, getWmParams());
        // WindowManager mWm = (WindowManager)getSystemService(WINDOW_SERVICE);
        // Button view = new Button(this);
        // view.setText("window manager test!");
        // WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
        // mParams.width=-1;
        // mParams.height=-1;
        // mWm.addView(view, getWmParams());
//        dbHelper = new DBhelper(this);
//        writeAbleDb = dbHelper.getWritableDatabase();
//        readableDb = dbHelper.getReadableDatabase();
//        Thread t = new Thread(runnable);
        // t.setPriority(Thread.MIN_PRIORITY);
//        t.start();
//        new Thread(runnable1).start();
//        Log.d("zqt", "ajskjas");
        // EditText editText = (EditText) findViewById(R.id.edt);
        // editText.setOnKeyListener(new OnKeyListener() {
        //
        // @Override
        // public boolean onKey(View v, int keyCode, KeyEvent event) {
        // Log.d("zqt", "keyCode: " + keyCode);
        // Toast.makeText(TestActivity.this, "onKey", Toast.LENGTH_SHORT)
        // .show();
        // return false;
        // }
        // });
        // editText.addTextChangedListener(new TextWatcher() {
        //
        // @Override
        // public void onTextChanged(CharSequence s, int start, int before,
        // int count) {
        // Log.d("zqt", "onTextChanged: " + s);
        // }
        //
        // @Override
        // public void beforeTextChanged(CharSequence s, int start, int count,
        // int after) {
        // Log.d("zqt", "beforeTextChanged: " + s);
        // }
        //
        // @Override
        // public void afterTextChanged(Editable s) {
        // Log.d("zqt", "afterTextChanged: " + s.toString());
        // }
        // });
    }

    public WindowManager.LayoutParams getWmParams() {
        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        localLayoutParams.height = 100;
        localLayoutParams.width = 100;
        localLayoutParams.alpha = 1.0f;
        localLayoutParams.format = 1;
        localLayoutParams.gravity = Gravity.CENTER;
        // localLayoutParams.verticalMargin = 1028443341;
        localLayoutParams.flags = 296;// WindowManager.LayoutParams.
        return localLayoutParams;
    }

    class DBhelper extends SQLiteOpenHelper {

        DBhelper(Context context) {
            super(context, "test.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table test(id integer not null,name text not null)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

    }
}