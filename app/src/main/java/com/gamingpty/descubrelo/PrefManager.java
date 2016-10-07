package com.gamingpty.descubrelo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;


public class PrefManager {

    private String TAG = PrefManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared pref file name
    private static final String PREF_NAME = "Descubrelo";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_AVATAR = "user_avatar";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_AUTH_TOKEN = "user_auth_token";
    private static final String KEY_USER_PROVIDER_ID = "user_provider_id";
    private static final String KEY_NOTIFICATIONS = "notifications";

    // Constructor
    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String email) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing email in pref
        editor.putString(KEY_EMAIL, email);

        // commit changes
        editor.commit();
    }

    public void storeUser(User user) {
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_AUTH_TOKEN, user.getAuthToken());
        editor.putString(KEY_USER_PROVIDER_ID, user.getProviderId());
        editor.putString(KEY_USER_AVATAR, user.getAvatar());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.commit();

        Log.e(TAG, "User is stored in shared preferences. " + user.getName() + ", " + user.getEmail());
    }

    public User getUser() {
        if (pref.getString(KEY_USER_ID, null) != null) {
            String id, authToken, avatar, name, email, providerId;
            authToken = pref.getString(KEY_USER_AUTH_TOKEN, null);
            providerId = pref.getString(KEY_USER_PROVIDER_ID, null);
            id = pref.getString(KEY_USER_ID, null);
            avatar = pref.getString(KEY_USER_AVATAR, null);
            name = pref.getString(KEY_USER_NAME, null);
            email = pref.getString(KEY_USER_EMAIL, null);

            User user = new User(id, authToken, providerId, avatar, name, email);
            return user;
        }
        return null;
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    public void addNotification(String notification) {

        // get old notifications
        String oldNotifications = getNotifications();

        if (oldNotifications != null) {
            oldNotifications += "|" + notification;
        } else {
            oldNotifications = notification;
        }

        editor.putString(KEY_NOTIFICATIONS, oldNotifications);
        editor.commit();
    }

    public String getNotifications() {
        return pref.getString(KEY_NOTIFICATIONS, null);
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }
}
