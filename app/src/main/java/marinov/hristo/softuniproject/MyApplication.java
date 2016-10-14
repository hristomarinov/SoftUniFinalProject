package marinov.hristo.softuniproject;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.multidex.MultiDex;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.orm.SugarContext;

import java.io.File;

import marinov.hristo.softuniproject.utils.ConnectivityChangeReceiver;

/**
 * @author HristoMarinov (christo_marinov@abv.bg).
 */

public class MyApplication extends Application {

    private static MyApplication mInstance;

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Delete DataBase
//        if (doesDBExist(this, "hristo.db")) {
//            SugarDb sugarDb = new SugarDb(getApplicationContext());
//            new File(sugarDb.getDB().getPath()).delete();
//        }

        SugarContext.init(getApplicationContext());
        mInstance = this;

        // Initialize the SDK before executing any other operations
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private boolean doesDBExist(ContextWrapper context, String dbName) {

        File dbFile = context.getDatabasePath(dbName);

        return dbFile.exists();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(this);
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityChangeReceiver.ConnectivityReceiverListener listener) {
        ConnectivityChangeReceiver.connectivityReceiverListener = listener;
    }
}
