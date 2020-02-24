package com.pano;

import android.content.Context;
import android.content.SharedPreferences;


public class PrefManager {
    // Shared preferences file name
    private static final String PREF_NAME = "pano";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    // shared pref mode
    int PRIVATE_MODE = 0;

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
    }

    public void saveUser(String id, String name) {
        editor.putString("user_id", id);
        editor.putString("name", name);
        editor.putString("type", "passenger");
        editor.apply();
    }

    public void setRegistrationtoken(String token) {
        editor.putString("token", token);
        editor.apply();
    }

    public void logout() {
        editor.clear().apply();
    }

    public boolean getDeleteReminders() {
        return pref.getBoolean("delete", false);
    }

    public void setDeleteReminders(boolean del) {
        editor.putBoolean("delete", del);
        editor.apply();
    }

    public boolean getOpenBubble() {
        return pref.getBoolean("open", true);
    }

    public void setOpenBubble(boolean open) {
        editor.putBoolean("open", open);
        editor.apply();
    }

    public String getRegistrationToken() {
        return pref.getString("token", "none");
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.apply();
    }

    public String getName() {
        return pref.getString("name", "none");
    }

    public void setName(String name) {
        editor.putString("name", name);
        editor.apply();
    }

    public String getType() {
        return pref.getString("type", "none");
    }

    public void setType(String pin) {
        editor.putString("type", pin);
        editor.apply();
    }

    public String getId() {
        return pref.getString("user_id", "none");
    }

    public String getPhone() {
        return pref.getString("phone", "none");
    }

    public void setPhone(String phone) {
        editor.putString("phone", phone);
        editor.apply();
    }

    public String getAbout() {
        return pref.getString("about", "About yourself");
    }

    public void setAbout(String about) {
        editor.putString("about", about);
        editor.apply();
    }


}
