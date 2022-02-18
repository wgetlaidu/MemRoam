package com.example.MemRoam;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;

import de.robv.android.xposed.XC_MethodHook;

import de.robv.android.xposed.XposedBridge;

import de.robv.android.xposed.XposedHelpers;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookTest implements IXposedHookLoadPackage {

    private static String TAG="MemoryRoaming";

    public String load(){
        File eStorage = Environment.getExternalStoragePublicDirectory("class.txt");
        File file = new File(eStorage.toString());

        try {
            return FileUtils.readFileToString(file,"utf-8");
        } catch (IOException e) {
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

    public static ClassLoader getClassloader() {
        ClassLoader resultClassloader = null;

        Object currentActivityThread = invokeStaticMethod(
                "android.app.ActivityThread", "currentActivityThread",
                new Class[]{}, new Object[]{});
        Object mBoundApplication = getFieldOjbect(
                "android.app.ActivityThread", currentActivityThread,
                "mBoundApplication");
        Object loadedApkInfo = getFieldOjbect(
                "android.app.ActivityThread$AppBindData",
                mBoundApplication, "info");
        Application mApplication = (Application) getFieldOjbect("android.app.LoadedApk", loadedApkInfo, "mApplication");
        resultClassloader = mApplication.getClassLoader();
        return resultClassloader;
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


//                    for (String eachclassname : classnames) {
//                        String log = "ClassNameis::" +eachclassname +"  :: "+ dumpMethodCode_method +"::"+appClassloader.toString() +"\n";
//                        XposedBridge.log("classes=>"+log);
//                    }
                    return classnames;
                }

            }
        }

        return null;
    }

    private void printDeclaredMethods(Class clz, Method m, int count) {

        switch (count){
            case 0:{
                XposedHelpers.findAndHookMethod(clz, m.getName(), new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Log.e(TAG, "    "+m );
                        Log.e(TAG, Log.getStackTraceString(new Throwable()) );
                        Log.e(TAG, "    "+m.getReturnType()+" retval: "+param.getResult() );
                    }
                });
                break;
            }
            case 1:{
                XposedHelpers.findAndHookMethod(clz, m.getName(), m.getParameterTypes()[0].getName(), new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Log.e(TAG, "    "+m+" param[0]: "+param.args[0] );
                        Log.e(TAG, Log.getStackTraceString(new Throwable()) );
                        Log.e(TAG, "    "+m.getReturnType()+"retval: "+param.getResult() );
                    }
                });
                break;
            }
            case 2: {
                XposedHelpers.findAndHookMethod(clz, m.getName(), m.getParameterTypes()[0].getName(), m.getParameterTypes()[1].getName(),
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                Log.e(TAG, "    "+m+" param[0]: " + param.args[0] + " param[1]: " + param.args[1]);
                                Log.e(TAG, Log.getStackTraceString(new Throwable()) );
                                Log.e(TAG, "    "+m.getReturnType()+"retval: " + param.getResult());
                            }
                        });
                break;
            }
            case 3: {
                XposedHelpers.findAndHookMethod(clz, m.getName(), m.getParameterTypes()[0].getName(), m.getParameterTypes()[1].getName(),
                        m.getParameterTypes()[2].getName(), new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                Log.e(TAG, "    "+m+" param[0]: " + param.args[0] + " param[1]: " + param.args[1] + " param[2]: " + param.args[2]);
                                Log.e(TAG, Log.getStackTraceString(new Throwable()) );
                                Log.e(TAG, "    "+m.getReturnType()+"retval: " + param.getResult());
                            }
                        });
                break;
            }
            case 4: {
                XposedHelpers.findAndHookMethod(clz, m.getName(), m.getParameterTypes()[0].getName(), m.getParameterTypes()[1].getName(),
                        m.getParameterTypes()[2].getName(), m.getParameterTypes()[3].getName(), new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                Log.e(TAG, "    "+m+" param[0]: " + param.args[0] + " param[1]: " + param.args[1] +
                                        " param[2]: " + param.args[2]+" param[3]: " + param.args[3]);
                                Log.e(TAG, Log.getStackTraceString(new Throwable()) );
                                Log.e(TAG, "    "+m.getReturnType()+"retval: " + param.getResult());
                            }
                        });
                break;
            }
            case 5: {
                XposedHelpers.findAndHookMethod(clz, m.getName(), m.getParameterTypes()[0].getName(), m.getParameterTypes()[1].getName(),
                        m.getParameterTypes()[2].getName(),  m.getParameterTypes()[3].getName(), m.getParameterTypes()[4].getName(), new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                Log.e(TAG, "    "+m+" param[0]: " + param.args[0] + " param[1]: " + param.args[1] +
                                        " param[2]: " + param.args[2]+" param[3]: " + param.args[3]+" param[4]: " + param.args[4]);
                                Log.e(TAG, Log.getStackTraceString(new Throwable()) );
                                Log.e(TAG, "    "+m.getReturnType()+"retval: " + param.getResult());
                            }
                        });
                break;
            }
        }
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

//        XposedHelpers.findAndHookMethod(PrivateKeyEntry.class,"getPrivateKey", new XC_MethodHook() {
//
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//
//                XposedBridge.log("PrivateKeyEntry:"+param.getResult());
//
//            }
//        });
//
//        XposedHelpers.findAndHookMethod(KeyStore.class,"load", InputStream.class, char[].class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                XposedBridge.log("KeyStore.load2:"+param.args[0]+ param.args[1]);
//
//            }
//        });
//
//        XposedHelpers.findAndHookMethod(KeyStore.class,"load", InputStream.class, char[].class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                XposedBridge.log("KeyStore.load2:"+param.args[0]+ param.args[1]);
//
//            }
//        });

        Class ActivityThread = XposedHelpers.findClass("android.app.ActivityThread",loadPackageParam.classLoader);
        XposedBridge.hookAllMethods(ActivityThread, "performLaunchActivity", new XC_MethodHook() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object mInitialApplication = (Application) XposedHelpers.getObjectField(param.thisObject,"mInitialApplication");
                ClassLoader finalCL = (ClassLoader) XposedHelpers.callMethod(mInitialApplication,"getClassLoader");
                XposedBridge.log("found classload is => "+finalCL.toString());
                String[] classList = getClassNameList(finalCL);
                for (String className : classList) {
                    String claName = load();
                    if(claName!=null&&className.indexOf(claName)!=-1){
                        Log.e(TAG, "class "+className);
                        Class clz = finalCL.loadClass(className);
//                        Field[] declaredFields =clz.getDeclaredFields();
//                        Constructor constructor = clz.getDeclaredConstructor();
//                        constructor.setAccessible(true);
//                        Log.e(TAG, "    // DeclaredFields");
//                        Object obj=constructor.newInstance();
//                        for(Field f:declaredFields){
//                            String type = f.getType().getName();
//                            f.setAccessible(true);
////                            Object value = f.get(obj);
//                            Log.e(TAG, "    "+type+" "+f.getName()+"=>"+value);
//                        }

                        Log.e(TAG, "    // Constructors");
                        Constructor[] construcrors = clz.getDeclaredConstructors();
                        for (Constructor c:construcrors){
                            Log.e(TAG, "    "+c);
                        }

                        Log.e(TAG, "    // declaredMethods");
                        Method[] declaredMethods = clz.getDeclaredMethods();
                        for (Method m : declaredMethods) {
                            int count = m.getParameterCount();
                            printDeclaredMethods(clz, m,count);
                        }
                    }
                }
            }
        });
    }
}


