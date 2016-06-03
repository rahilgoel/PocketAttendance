package iit.iitr.pocketattendance;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by rahil on 6/1/2016.
 */
public class SessionManager {
    private static final String PREF_NAME = "AndroidHiveLogin";
    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static final String Email = "Email";
    private static String TAG = SessionManager.class.getSimpleName();
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Context context;
    int PRIVATE_MODE = 0;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = prefs.edit();
    }


    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.commit();
        Log.d(TAG, "User login session modified!");
    }

    public void setEmail(String email) {
        editor.putString(Email, email);
        editor.commit();
        Log.d(TAG, "user email has been stored");
    }
    public String getEmail(){
        return prefs.getString(Email,"");

    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGEDIN, false);
    }

}
