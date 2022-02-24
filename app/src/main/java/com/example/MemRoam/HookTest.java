package com.example.MemRoam;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


import javax.net.ssl.SSLSession;

import de.robv.android.xposed.IXposedHookLoadPackage;

import de.robv.android.xposed.XC_MethodHook;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

import de.robv.android.xposed.XposedHelpers;

import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class HookTest implements IXposedHookLoadPackage {

    private static String TAG = "MemoryRoaming";
    private XSharedPreferences shared;
    private String claName;
    private ArrayList<ClassLoader> AppAllCLassLoaderList = new ArrayList<>();
    private ClassLoader mLoader = null;
    private Context mOtherContext;
    private String PWD = "1234";


    private String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : Objects.requireNonNull(activityManager)
                .getRunningAppProcesses()) {

            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private void HookAttach() {

        XposedHelpers.findAndHookMethod(Application.class,
                "attach",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        mOtherContext = (Context) param.args[0];
                        mLoader = mOtherContext.getClassLoader();
                    }

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        String processName = getCurProcessName(mOtherContext);
                        if (processName != null && processName.equals(mOtherContext.getPackageName())) {
                            batchHook(mLoader);
                        }
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void batchHook(ClassLoader finalCL) {
        String[] classList = new String[0];
        try {
            classList = getClassNameList(finalCL);
        } catch (IOException e) {
            Log.e(TAG, "classList batchHook: "+e );
        }
        try {
            claName=loadConf().getString("CLASSNAME");
            Log.e(TAG, "batchHook: "+claName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "batchHook: "+classList.length);
        //21692
        for (String className : classList) {

            if (!claName.isEmpty() && className.indexOf(claName) != -1) {
//                        Log.e(TAG, "class "+className);
                Log.e(TAG, "CN=> " + claName + "===" + className);
                Class clz = null;
                try {
                    clz = finalCL.loadClass(className);
                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
                    Log.e(TAG, "loadClass batchHook: "+e );
                }
                printLog(clz);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void printLog(Class clz){
        XposedBridge.hookAllConstructors(clz, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                int count = param.args.length;
//                Log.w(TAG, className + ":Constructor");
                for (int i = 0; i < count; i++) {
                    Log.w(TAG, "Constructor: parms" + i + "=> " + param.args[i]);
                }
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                Log.w(TAG, "Constructor: retval=> " + param.getResult());
            }
        });

        Method[] declaredMethods = clz.getDeclaredMethods();
        for (Method m : declaredMethods) {
            int count = m.getParameterCount();
            XposedBridge.hookAllMethods(clz, m.getName(), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Log.w(TAG, "declaredMethod:" + m);
                    for (int i = 0; i < count; i++) {
                        Log.w(TAG, "declaredMethod:" + m.getName() + ";parms" + i + "=> " + param.args[i]);
                    }
                    Log.e(TAG, Log.getStackTraceString(new Throwable()));
                    Log.w(TAG, "declaredMethod:" + m.getName() + ";retval=> " + param.getResult());
                }
            });
        }
    }

    public JSONObject loadConf() {
        File eStorage = Environment.getExternalStoragePublicDirectory("/MemRoam/class.txt");
        File file = new File(eStorage.toString());

        try {
            String confData = FileUtils.readFileToString(file, "utf-8");
            JSONObject confJson = new JSONObject(confData);
            return confJson;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invokeStaticMethod(String class_name,
                                            String method_name, Class[] pareTyple, Object[] pareVaules) {

        try {
            Class obj_class = Class.forName(class_name);
            Method method = obj_class.getMethod(method_name, pareTyple);
            return method.invoke(null, pareVaules);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Object getFieldOjbect(String class_name, Object obj,
                                        String filedName) {
        try {
            Class obj_class = Class.forName(class_name);
            Field field = obj_class.getDeclaredField(filedName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Field getClassField(ClassLoader classloader, String class_name,
                                      String filedName) {

        try {
            Class obj_class = classloader.loadClass(class_name);
            Field field = obj_class.getDeclaredField(filedName);
            field.setAccessible(true);
            return field;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }


    public static Object getClassFieldObject(ClassLoader classloader, String class_name, Object obj,
                                             String filedName) {

        try {
            Class obj_class = classloader.loadClass(class_name);
            Field field = obj_class.getDeclaredField(filedName);
            field.setAccessible(true);
            Object result = null;
            result = field.get(obj);
            return result;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static String[] getClassNameList(ClassLoader clzloader) throws IOException {
        ClassLoader appClassloader = clzloader;
        List<Object> dexFilesArray = new ArrayList<Object>();
        Field pathList_Field = (Field) getClassField(appClassloader, "dalvik.system.BaseDexClassLoader", "pathList");
        Object pathList_object = getFieldOjbect("dalvik.system.BaseDexClassLoader", appClassloader, "pathList");
        Object[] ElementsArray = (Object[]) getFieldOjbect("dalvik.system.DexPathList", pathList_object, "dexElements");
        Field dexFile_fileField = null;
        try {
            dexFile_fileField = (Field) getClassField(appClassloader, "dalvik.system.DexPathList$Element", "dexFile");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Class DexFileClazz = null;
        try {
            DexFileClazz = appClassloader.loadClass("dalvik.system.DexFile");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Method getClassNameList_method = null;
        Method defineClass_method = null;
        Method dumpDexFile_method = null;
        Method dumpMethodCode_method = null;

        for (Method field : DexFileClazz.getDeclaredMethods()) {
            if (field.getName().equals("getClassNameList")) {
                getClassNameList_method = field;
                getClassNameList_method.setAccessible(true);
            }
            if (field.getName().equals("defineClassNative")) {
                defineClass_method = field;
                defineClass_method.setAccessible(true);
            }
            if (field.getName().equals("dumpMethodCode")) {
                dumpMethodCode_method = field;
                dumpMethodCode_method.setAccessible(true);
            }
        }
        Field mCookiefield = getClassField(appClassloader, "dalvik.system.DexFile", "mCookie");
        for (int j = 0; j < ElementsArray.length; j++) {
            Object element = ElementsArray[j];
            Object dexfile = null;
            try {
                dexfile = (Object) dexFile_fileField.get(element);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (dexfile == null) {
                continue;
            }
            if (dexfile != null) {
                dexFilesArray.add(dexfile);
                Object mcookie = getClassFieldObject(appClassloader, "dalvik.system.DexFile", dexfile, "mCookie");
                if (mcookie == null) {
                    continue;
                }
                String[] classnames = null;
                try {
                    classnames = (String[]) getClassNameList_method.invoke(dexfile, mcookie);

                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                } catch (Error e) {
                    e.printStackTrace();
                    continue;
                }
                if (classnames != null) {
                    return classnames;
                }

            }
        }

        return null;
    }

    private static KeyPair getKey()  {
        // 密钥对 生成器，RSA算法 生成的  提供者是 BouncyCastle
        KeyPairGenerator generator = null;

        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 密钥长度 1024
        generator.initialize(1024);
        // 证书中的密钥 公钥和私钥
        KeyPair keyPair = generator.generateKeyPair();
        return keyPair;
    }


    private Class getClass(String cn, ClassLoader classLoader){

        return XposedHelpers.findClass(cn,classLoader);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void HookSystem(ClassLoader classLoader) throws JSONException {
        String sysClsName = loadConf().getString("SYSTEMCLASSNAME");
        Log.e(TAG, "HookSystem: "+sysClsName);
        if(!sysClsName.isEmpty()){
            Class cls = getClass(sysClsName,classLoader);
            printLog(cls);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        Log.e(TAG, "handleLoadPackage: "+loadPackageParam.packageName);
        if(!loadPackageParam.packageName.equals("com.example.MemRoam")){
            HookAttach();
            HookSystem(loadPackageParam.classLoader);
        }
        Class keyStore = XposedHelpers.findClass("java.security.KeyStore",loadPackageParam.classLoader);
        //java.security.KeyStore$LoadStoreParameter
        //java.security.cert.CertificateFactory
        Class LoadStoreParameter = XposedHelpers.findClass("java.security.KeyStore$LoadStoreParameter",loadPackageParam.classLoader);
        Class PrivateKeyEntry = XposedHelpers.findClass("java.security.KeyStore$PrivateKeyEntry",loadPackageParam.classLoader);
        Class CertificateFactory = XposedHelpers.findClass("java.security.cert.CertificateFactory",loadPackageParam.classLoader);
        Class NetworkInterface = XposedHelpers.findClass("java.net.NetworkInterface",loadPackageParam.classLoader);
        Class NetworkCapabilities = XposedHelpers.findClass("android.net.NetworkCapabilities",loadPackageParam.classLoader);
        Class NetworkInfo = XposedHelpers.findClass("android.net.NetworkInfo",loadPackageParam.classLoader);


        if(loadConf().getBoolean(MainActivity.ISONVPN)){
            XposedHelpers.findAndHookMethod(NetworkInfo, "isConnected", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return false;
                }
            });

            XposedHelpers.findAndHookMethod(NetworkInterface, "getName", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
//                String result = (String) param.getResult();
//                if(result.equals("tun0")||result.equals("ppp0")){
//                    return null;
//                }else{
//                    return param.getResult();
//                }
                    return null;
                }
            });

            XposedHelpers.findAndHookMethod(NetworkCapabilities, "hasTransport", int.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return false;
                }
            });
        }

        if(loadConf().getBoolean(MainActivity.DUMPCERT)){
            XposedHelpers.findAndHookMethod(keyStore,"load", LoadStoreParameter, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("KeyStore.load1:"+param.args[0]);


                }
            });

            XposedHelpers.findAndHookMethod(keyStore,"load", InputStream.class, char[].class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("KeyStore.load2:"+param.args[0]+ param.args[1]);

                }
            });
            XposedHelpers.findAndHookMethod(PrivateKeyEntry,"getPrivateKey",  new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("PrivateKeyEntry getPrivateKey:"+param.getResult());
                    XposedBridge.log("PrivateKeyEntry getCertificate:"+XposedHelpers.callMethod(param.thisObject,"getCertificate"));
                    File eStorage = Environment.getExternalStoragePublicDirectory("/certs/"+System.currentTimeMillis()+loadPackageParam.packageName+".p12");
                    File file = new File(eStorage.toString());
//                    Object obj = XposedHelpers.callMethod(param.getResult(),"getPublicKey");

                    PrivateKey privateKey = (PrivateKey) param.getResult();
                    Certificate cert = (Certificate) XposedHelpers.callMethod(param.thisObject,"getCertificate");

                    KeyStore ks = KeyStore.getInstance("PKCS12");
                    ks.load(null, null);
                    ks.setKeyEntry("cretkey",  privateKey,  PWD.toCharArray(),  new Certificate[] { cert });
                    //                ks.setCertificateEntry("cretkey", cert);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    //                cert.verify(keyPair.getPublic());
                    ks.store(out, PWD.toCharArray());
                    byte[] keyStoreData = out.toByteArray();

                    if(keyStoreData.length!=0){
                        try {
                            //                        XposedBridge.log("CertificateFactory file:"+new String(keyStoreData));
                            FileUtils.writeByteArrayToFile(file, keyStoreData);
                        } catch (IOException e) {
                            XposedBridge.log("CertificateFactory file create:"+e);
                        }
                    }

                }
            });
            XposedHelpers.findAndHookMethod(PrivateKeyEntry,"getCertificateChain",  new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("PrivateKeyEntry getPrivateKey:"+param.getResult());
                    XposedBridge.log("PrivateKeyEntry getCertificate:"+XposedHelpers.callMethod(param.thisObject,"getCertificate"));
                    File eStorage = Environment.getExternalStoragePublicDirectory("/certs/"+System.currentTimeMillis()+loadPackageParam.packageName+".p12");
                    File file = new File(eStorage.toString());
//                    Object obj = XposedHelpers.callMethod(param.getResult(),"getPublicKey");

                    PrivateKey privateKey = (PrivateKey) XposedHelpers.callMethod(param.thisObject,"getPrivateKey");;
                    Certificate cert = (Certificate) XposedHelpers.callMethod(param.thisObject,"getCertificate");

                    KeyStore ks = KeyStore.getInstance("PKCS12");
                    ks.load(null, null);
                    ks.setKeyEntry("cretkey",  privateKey,  PWD.toCharArray(),  new Certificate[] { cert });
                    //                ks.setCertificateEntry("cretkey", cert);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    //                cert.verify(keyPair.getPublic());
                    ks.store(out, PWD.toCharArray());
                    byte[] keyStoreData = out.toByteArray();

                    if(keyStoreData.length!=0){
                        try {
                            //                        XposedBridge.log("CertificateFactory file:"+new String(keyStoreData));
                            FileUtils.writeByteArrayToFile(file, keyStoreData);
                        } catch (IOException e) {
                            XposedBridge.log("CertificateFactory file create:"+e);
                        }
                    }

                }
            });
        }


//        Class ActivityThread = XposedHelpers.findClass("android.app.ActivityThread",loadPackageParam.classLoader);
//        XposedBridge.hookAllMethods(ActivityThread, "performLaunchActivity", new XC_MethodHook() {
//            @RequiresApi(api = Build.VERSION_CODES.O)
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                Object mInitialApplication = (Application) XposedHelpers.getObjectField(param.thisObject,"mInitialApplication");
//                ClassLoader finalCL = (ClassLoader) XposedHelpers.callMethod(mInitialApplication,"getClassLoader");
//                int c = param.args.length;
//                for(int i=0;i<c;i++){
//                    XposedBridge.log("performLaunchActivity param"+i+" => "+param.args[i]);
//                }
//                XposedBridge.log("found classload is => "+finalCL.toString());
//                batchHook(finalCL);
//            }
//        });
    }
}


