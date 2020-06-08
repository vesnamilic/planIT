package com.example.planit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreference {

    static final String PREF_EMAIL_NAME= "email";
    static final String PREF_USER_NAME= "name";
    static final String PREF_USER_LAST_NAME= "lastName";
    static final String PREF_USER_COLOUR= "colour";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setLoggedEmail(Context ctx, String email)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_EMAIL_NAME, email);
        editor.apply();
    }

    public static String getLoggedEmail(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_EMAIL_NAME, "");
    }

    public static void setLoggedName(Context ctx, String name)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_NAME, name);
        editor.apply();
    }

    public static String getLoggedName(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_USER_NAME, "");
    }


    public static void setLoggedLastName(Context ctx, String lastName)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_LAST_NAME, lastName);
        editor.apply();
    }

    public static String getLoggedLastName(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_USER_LAST_NAME, "");
    }

    public static void setLoggedColour(Context ctx, String colour)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_COLOUR, colour);
        editor.apply();
    }

    public static String getLoggedColour(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_USER_COLOUR, "");
    }

}