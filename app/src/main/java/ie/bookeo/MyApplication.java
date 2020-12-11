package ie.bookeo;

import android.app.Application;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import ie.bookeo.activity.ErrorActivity;

/**
 * Reference
 *  - URL - //https://github.com/Ereza/CustomActivityOnCrash/blob/master/sample/src/main/java/cat/ereza/customactivityoncrash/sample/activity/CustomErrorActivity.java | https://github.com/Ereza/CustomActivityOnCrash
 *  - Creator - Eduard Ereza Martínez
 *  - Modified by Cian O Sullivan
 *
 */

public class MyApplication extends Application {
        // Overriding this method is totally optional!
        @Override
        public void onCreate() {
            super.onCreate();
            // Required initialization logic here!
            CaocConfig.Builder.create()
                    .trackActivities(true) //default: false
                    .errorActivity(ErrorActivity.class) //default: null (default error activity)
                    .apply();
        }
    }

