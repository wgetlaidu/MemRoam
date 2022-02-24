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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private static String CName;
    public static String CLASSNAME = "CLASSNAME";
    public static String SYSTEMCLASSNAME = "SYSTEMCLASSNAME";
    public static String ISONVPN = "ISONVPN";
    public static String DUMPCERT = "DUMPCERT";
    private static String TAG="Main";
    private JSONObject confData = new JSONObject();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestMyPermissions();
        setContentView(R.layout.activity_main);
//        CheckBox onVpn = findViewById(R.id.on_vpn);
//        onVpn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView,
//                                         boolean isChecked) {
//                // TODO Auto-generated method stub
//                if(isChecked){
//                    try {
//                        confData.put(ISONVPN,true);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }else{
//                    try {
//                        confData.put(ISONVPN,false);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
        Button input = findViewById(R.id.input);
        input.setOnClickListener(MainActivity.this::Onclick);
    }

    public void Onclick(View V){
        switch (V.getId()){
            case R.id.input:
                EditText editText = findViewById(R.id.edit_query);
                CName = editText.getText().toString();
                EditText system_class = findViewById(R.id.system_class);
                String systemClass = system_class.getText().toString();
                try {
                    confData.put(CLASSNAME,CName);
                    confData.put(SYSTEMCLASSNAME,systemClass);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, "Onclick: "+confData);
                File eStorage = Environment.getExternalStoragePublicDirectory("/MemRoam/class.txt");
                File file = new File(eStorage.toString());
                try {
                    FileUtils.writeStringToFile(file,confData.toString(),"utf-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public void onCheckboxClicked(View view) throws JSONException {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.on_vpn:
                if (checked)
                    confData.put(ISONVPN, true);
            else
                confData.put(ISONVPN,false);
                break;
            case R.id.dump_cert:
                if (checked)
                    confData.put(DUMPCERT,true);
            else
                confData.put(DUMPCERT,false);
                break;

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