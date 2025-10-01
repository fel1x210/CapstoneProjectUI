package ca.gbc.comp3074.uiprototype;

import android.app.Application;

import ca.gbc.comp3074.uiprototype.data.QuietSpaceDatabase;

public class QuietSpaceApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        QuietSpaceDatabase.getInstance(this);
    }
}
