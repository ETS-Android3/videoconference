package io.agora.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.agora.openvcall.R;


public class Utils {

    public static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    public static String deviceName = "";
    public static String callSettings = "";


    public static void noInternet(Context context, View view) {
        Snackbar.make(view, context.getString(R.string.no_internet_connection),
                Snackbar.LENGTH_LONG).show();
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean isMobileValid(String phone) {
        if (!Pattern.matches("[a-zA-Z]+", phone)) {
            return phone.length() > 6 && phone.length() <= 13;
        }
        return false;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @SuppressLint("RestrictedApi")
    public static void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                //noinspection RestrictedApi
//                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }

    /*public static String getToken(Context context){

        return ManageSession.getPreference(context, ManageSession.TOKEN);
    }*/

    public static Locale getLocale() {

        return new Locale("en", "in");

    }

    public static void callIntent(Context context, String mobileNo) {

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(String.format("%s%s", "tel:", mobileNo)));

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }

    }

    public static void mailIntent(Context context, String email) {

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public static void webIntent(Context context, String url) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }

    }

    public static void shareIntent(Context context, String subject, String message) {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setType("text/plain");
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }

    }

    public static Intent newFacebookIntent(PackageManager pm, String message) {

        Intent fbIntent = new Intent(Intent.ACTION_SEND);
        fbIntent.putExtra(Intent.EXTRA_TEXT, message);
        fbIntent.setType("text/plain");


        List<ResolveInfo> resolvedInfoList = pm.queryIntentActivities(fbIntent,
                PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for (ResolveInfo resolveInfo : resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.facebook.katana")) {
                fbIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }
        if (resolved) {
            return fbIntent;
        } else {
            return null;
        }
    }

    public static Intent newGmailIntent(PackageManager pm, String subject, String message) {


        Intent gmailIntent = new Intent(Intent.ACTION_SEND);
        gmailIntent.putExtra(Intent.EXTRA_TEXT, message);
        gmailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        gmailIntent.setType("text/plain");


        List<ResolveInfo> resolvedInfoList = pm.queryIntentActivities(gmailIntent,
                PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for (ResolveInfo resolveInfo : resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.google.android.gm")) {
                gmailIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }
        if (resolved) {
            return gmailIntent;
        } else {
            return null;
        }

    }

    public static Intent newWhatsAppIntent(PackageManager pm, String message) {

        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.whatsapp", 0);
            if (applicationInfo.enabled) {

                Intent whatsAppIntent = new Intent();
                whatsAppIntent.setAction(Intent.ACTION_SEND);
                whatsAppIntent.putExtra(Intent.EXTRA_TEXT, message);
                whatsAppIntent.setType("text/plain");
                whatsAppIntent.setPackage("com.whatsapp");
                return whatsAppIntent;

            }
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }

        return null;
    }

    public static Intent newTwitterIntent(PackageManager pm, String message) {
        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
        tweetIntent.putExtra(Intent.EXTRA_TEXT, message);
        tweetIntent.setType("text/plain");


        List<ResolveInfo> resolvedInfoList = pm.queryIntentActivities(tweetIntent,
                PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for (ResolveInfo resolveInfo : resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                tweetIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }
        if (resolved) {
            return tweetIntent;
        } else {
            return null;
        }
    }

    public static ArrayList<String> getSkillLevelList() {

        return new ArrayList<String>(Arrays.asList("All", "Beginner", "Advanced", "Expert"));

    }

    public static Intent openFbPageIntent(PackageManager pm, String url) {
        Uri uri = Uri.parse(url);
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {

                uri = Uri.parse("fb://facewebmodal/f?href=" + url);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    public static Intent openTwitterHandleIntent(PackageManager pm, String twitterHandle) {
        Uri uri = Uri.parse("https://twitter.com/" + twitterHandle);
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.twitter.android", 0);
            if (applicationInfo.enabled) {
                uri = Uri.parse("twitter://user?screen_name=" + twitterHandle);
            }
        } catch (PackageManager.NameNotFoundException ignored) {

        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    public static Intent openInstagramPageIntent(PackageManager pm, String url) {
        Uri uri = Uri.parse(url);

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.instagram.android", 0);
            if (applicationInfo.enabled) {
                intent.setPackage("com.instagram.android");
            }
        } catch (PackageManager.NameNotFoundException ignored) {

        }

        return intent;
    }


    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    /*
     * Getting Distance from two latitude and longitude
     * */
    public static double GetdistanceInKM(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    public static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    public static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    public static int getDifferenceInMonths(int m1, int y1, int m2, int y2) {

        return (y2 - y1) * 12 + (m2 - m1) + 1;

    }

    public static String getParsedString(InputStream response) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(response));
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        Log.e("LogView", "" + sb.toString());
        return sb.toString();
    }

    /*public static Uri getOutputMediaFileUri() {
        File file = getOutputMediaFile();
        if (file != null)
            return Uri.fromFile(file);
        else
            return null;
    }*/

    /*public static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Constant.IMAGE_DIRECTORY_NAME);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                if (BuildConfig.DEBUG)
                    Log.d(Constant.IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                            + Constant.IMAGE_DIRECTORY_NAME + " directory");
                return null;

            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".png");
        return mediaFile;
    }*/


}
