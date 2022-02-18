package com.example.MemRoam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.apache.commons.io.FileUtils;

public class MainActivity extends AppCompatActivity {

    public static String CName;
    private static String TAG;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestMyPermissions();
        setContentView(R.layout.activity_main);
        Button input = findViewById(R.id.input);
        input.setOnClickListener(MainActivity.this::Onclick);


//        if(className!=null)new HookTest(className);
    }

    public void Onclick(View V){
        switch (V.getId()){
            case R.id.input:
                EditText editText = findViewById(R.id.edit_query);
//                CName = editText.getText().toString();
//                BufferedWriter writer;
//                FileOutputStream fout;
//                try{
//                    fout =openFileOutput("class.txt", MODE_PRIVATE);
//                    writer = new BufferedWriter(new OutputStreamWriter(fout));
//                    writer.write(CName);
//                }
//                catch(Exception e){
//                    e.printStackTrace();
//                }
                File eStorage = Environment.getExternalStoragePublicDirectory("class.txt");
                File file = new File(eStorage.toString());

                try {
                    FileUtils.writeStringToFile(file,editText.getText().toString(),"utf-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }


//                Log.e("11111", "Cname=>"+CName );
        }
    }

    private static SharedPreferences a(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String a(Context context, String str, String str2) {
        return a(context).getString(str, str2);
    }

    private void requestMyPermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        } else {
            Log.d(TAG, "requestMyPermissions: 有写SD权限");
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        } else {
            Log.d(TAG, "requestMyPermissions: 有读SD权限");
        }
    }

}