package step.learning.tool_account_client;

import android.app.Application;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import step.learning.tool_account_client.utils.LocaleHelper;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Завантаження збереженої мови при старті додатка
        LocaleHelper.loadLocale(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleHelper.loadLocale(this);
    }
}
