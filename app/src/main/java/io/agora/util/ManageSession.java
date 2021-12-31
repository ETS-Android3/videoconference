package io.agora.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;


public class ManageSession {

    private static final String PREFS_NAME = "App_Manage_Prefrence";

    private static SharedPreferences settings ;
    private static SharedPreferences.Editor editor ;


    public static final String PROFILE = "profile";
    public static final String COMPANY_ID = "company_id";
    public static final String COMPANY_NAME = "company_name";
    public static final String COMPANY_EMAIL = "company_email";
    public static final String PHONE = "phone";
    public static final String ADDRESS = "address";
    public static final String DEVICE_TOKEN = "token";
    public static final String LOGIN_STATUS = "login_status";
    public static final String CODE_ACTIVATED = "code_activated";
    public static final String IMEI = "imei";
    public static final String GRID_TYPE = "grid_type";
    public static final String PICK_UP_SETTINGS = "pick_up_settings";


    private static final String SESSION_NAME = "CAMERAIMAGE";
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor prefsEditor;

    public Uri getImageUri() {
        String imageUri = mSharedPreferences.getString("getImageUri", "");
        if (imageUri == null || imageUri.equals("")) return null;
        return Uri.parse(imageUri);
    }

    public void setImageUri(Uri imageUri) {
        prefsEditor = mSharedPreferences.edit();
        prefsEditor.putString("getImageUri", imageUri.toString());
        prefsEditor.commit();
    }

    public String getImagePath() {
        return mSharedPreferences.getString("getImagePath", "");
    }

    public void setImagePath(String imagePath) {
        prefsEditor = mSharedPreferences.edit();
        prefsEditor.putString("getImagePath", imagePath);
        prefsEditor.commit();
    }

    public String getCropImagePath() {
        return mSharedPreferences.getString("getCropImagePath", "");
    }

    public void setCropImagePath(String cropImagePath) {
        prefsEditor = mSharedPreferences.edit();
        prefsEditor.putString("getCropImagePath", cropImagePath);
        prefsEditor.commit();
    }

    ManageSession(Context context) {
        mSharedPreferences = context.getSharedPreferences(SESSION_NAME, Context.MODE_PRIVATE);
        prefsEditor = mSharedPreferences.edit();
    }


    /*Set App preferences*/
    public static boolean setPreference(Context context, String key, String value) {
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    /*Get App Preferences*/
    public static String getPreference(Context context, String key) {
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getString(key, "");
    }

    /*Set App preferences*/
    public static boolean setBooleanPreference(Context context, String key, boolean value) {
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    /*Get App Preferences*/
    public static boolean getBooleanPreference(Context context, String key) {
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(key, false);
    }

    /*Set App preferences*/
    public static boolean setIntPreference(Context context, String key, int value) {
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    /*Get App Preferences*/
    public static int getIntPreference(Context context, String key) {
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getInt(key, -1);
    }


    /*Clear App Preferences*/
    public static void clearPreference(Context context) {
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.clear();
        editor.commit();
    }


}
