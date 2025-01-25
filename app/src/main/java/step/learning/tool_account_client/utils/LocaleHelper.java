package step.learning.tool_account_client.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {
    private static String lang = "en";

    public static String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        LocaleHelper.lang = lang;
    }

    public static void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        context.createConfigurationContext(config);
    }

    public static void setLanguage(Context context, String languageCode) {
        // Збереження вибраної мови у SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("My_Lang", languageCode);
        editor.apply();

        // Використання LocaleHelper для оновлення локалі
        LocaleHelper.setLocale(context, languageCode);
    }

    public static void loadLocale(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Settings", MODE_PRIVATE);
        String language = sharedPreferences.getString("My_Lang", "en");
        LocaleHelper.setLocale(context, language);
    }

    // Метод для перевірки, чи змінилась мова
    public static boolean isLanguageChanged(Context context, String languageCode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Settings", MODE_PRIVATE);
        String currentLanguage = sharedPreferences.getString("My_Lang", getLang());
        return !currentLanguage.equals(languageCode); // Повертає true, якщо мова вже така сама
    }
}
