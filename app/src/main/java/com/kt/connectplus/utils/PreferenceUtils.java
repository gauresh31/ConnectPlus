package com.kt.connectplus.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

public class PreferenceUtils {

    public static final String PREFS_NAME = "ConnectPrefs";
    private static final String BLANK = "";

    private PreferenceUtils() {
    }

    /**
     * Removes all SharedPreference
     *
     * @param context
     */
    public static void removeAll(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit();
    }

    /**
     * @param context
     * @param Key     to remove
     */
    public static void remove(Context context, String Key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Key);
        editor.apply();
    }

    /**
     * Gets string data saved in SharedPreference
     *
     * @param context
     * @param key
     * @return String data. Default return value is ""
     */
    public static String getStringPreference(Context context, final String key) {

        String value = "";
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        try {
            value = preferences.getString(key, BLANK);
        } catch (Exception e) {
            e.printStackTrace();
            return value;
        }
        return value;

    }

    /**
     * Set String data in SharedPreference
     *
     * @param context
     * @param key
     * @param value
     */
    public static void setStringPreference(Context context, final String key, final String value) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Get boolean data saved in SharedPreference.
     *
     * @param context
     * @param key
     * @return boolean data. Default return value is "false".
     */
    public static boolean getBooleanPreference(Context context, final String key) {

        boolean value = false;
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        try {
            value = preferences.getBoolean(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return value;
        }
        return value;
    }

    /**
     * Set boolean data in SharedPreference
     *
     * @param context
     * @param key
     * @param value
     */
    public static void setBooleanPreference(Context context, final String key, final boolean value) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Get int data from SharedPreference
     *
     * @param context
     * @param key
     * @return int data. Default return value is "0"
     */
    public static int getIntPreference(Context context, final String key) {

        int value = 0;
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        value = preferences.getInt(key, 0);
        return value;
    }

    /**
     * Get int data from SharedPreference
     *
     * @param context
     * @param key
     * @param defValue
     * @return int data. Default return value is defValue
     */
    public static int getIntPreference(Context context, final String key, final int defValue) {

        int value = defValue;
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        value = preferences.getInt(key, defValue);
        return value;
    }

    /**
     * Set int data in SharedPreference
     *
     * @param context
     * @param key
     * @param value
     */
    public static void setIntPreference(Context context, final String key, final int value) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Set long data in SharedPreference
     *
     * @param context
     * @param key
     * @param value
     */
    public static void setLongPreference(Context context, final String key, final long value) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    /**
     * Get long data from SharedPreference
     *
     * @param context
     * @param key
     * @return long data. Default return type is "0"
     */
    public static long getLongPreference(Context context, final String key) {

        long value = 0;
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        value = preferences.getLong(key, 0);
        return value;
    }

    /**
     * Set String data in SharedPreference
     *
     * @param context
     * @param key
     * @param value
     */
    public static boolean setArrayStringPreference(Context context, final String key, final ArrayList<String> value) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(key + "Size", value.size());

        for (int i = 0; i < value.size(); i++) {
            editor.remove(key + i);
            editor.putString(key + i, value.get(i));
        }

        return editor.commit();

    }

    /**
     * Get String data in SharedPreference
     *
     * @param context
     * @param key
     */
    public static ArrayList<String> getArrayStringPreference(Context context, final String key) {
        ArrayList<String> stringArray = new ArrayList<>();
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        stringArray.clear();
        int size = preferences.getInt(key + "Size", 0);

        for (int i = 0; i < size; i++) {
            stringArray.add(preferences.getString(key + i, null));
        }
        return stringArray;

    }

    public static boolean isEmpty(String str) {
        return null == str || "".equalsIgnoreCase(str) || "null".equalsIgnoreCase(str);
    }
}
