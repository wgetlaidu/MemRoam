package com.example.MemRoam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;


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
    }

    public void Onclick(View V){
        switch (V.getId()){
            case R.id.input:
                EditText editText = findViewById(R.id.edit_query);
                CName = editText.getText().toString();
                File eStorage = Environment.getExternalStoragePublicDirectory("class.txt");
                File file = new File(eStorage.toString());
                try {
                    FileUtils.writeStringToFile(file,editText.getText().toString(),"utf-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
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