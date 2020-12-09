package ie.bookeo;

import android.app.Application;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import ie.bookeo.activity.ErrorActivity;

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

