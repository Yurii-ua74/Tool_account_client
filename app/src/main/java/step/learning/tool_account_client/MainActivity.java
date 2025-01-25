package step.learning.tool_account_client;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import step.learning.tool_account_client.services.ConnectionDb;
import step.learning.tool_account_client.utils.LocaleHelper;
import step.learning.tool_account_client.utils.PasswordHasher;

public class MainActivity extends AppCompatActivity {
    private EditText phoneEditText;
    private EditText passwordEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Завантаження збереженої мови
        LocaleHelper.loadLocale(this);
        // Викликаємо setContentView після оновлення локалі
        setContentView(R.layout.activity_main);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView flagUK = findViewById(R.id.flag_uk);
        ImageView flagUA = findViewById(R.id.flag_ua);
        ImageView flagIT = findViewById(R.id.flag_it);

        flagUK.setOnClickListener(view -> {
            LocaleHelper.setLanguage(MainActivity.this, "en");
            recreate();
        });
        flagUA.setOnClickListener(view -> {
            LocaleHelper.setLanguage(MainActivity.this, "ua");
            recreate();
        });
        flagIT.setOnClickListener(view -> {
            LocaleHelper.setLanguage(MainActivity.this, "it");
            recreate();
        });

        // Ініціалізація EditText елементів
        phoneEditText = findViewById(R.id.phone_number);
        passwordEditText = findViewById(R.id.enter_password);

        findViewById(R.id.main_btn_login).setOnClickListener(this::onLoginBtnClick);
        findViewById(R.id.textRegistration).setOnClickListener(this::onRegistrationBtnClick);
        findViewById(R.id.textForgotPassword).setOnClickListener(this::onForgotPasswordBtnClick);

        // отримання даних строки підключення
        getNameWord("data.txt");

    }


    public static String modifyKey(String input) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            // Додаємо символ до результату, якщо його індекс не кратний 2
            if ((i + 1) % 2 != 0) {
                result.append(input.charAt(i));
            }
        }
        return result.toString();
    }


    public void getNameWord(String data) {
        String name = null;
        String password = null;
        String key = null;

        try (InputStream is = getResources().openRawResource(R.raw.data);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            // Зчитування першого рядка - name
            String encryptedKey = reader.readLine();
            if (encryptedKey != null) {
                key = encryptedKey;
            }

            // Зчитування першого рядка - name
            String encryptedName = reader.readLine();
            if (encryptedName != null) {
                name = decrypts(encryptedName, key); // Розшифровуємо name
            }

            // Зчитування другого рядка - password
            String encryptedPassword = reader.readLine();
            if (encryptedPassword != null) {
                password = decrypts(encryptedPassword, key); // Розшифровуємо password
            }

        } catch (IOException e) {
            Log.e("LOG_TAG", "Помилка читання файлу з raw", e);
        }

        // Ініціалізація ConnectionDb
        if (name != null && password != null) {
            ConnectionDb.getInstance().initializeConnectionString(name, password);
        } else {
            Log.e("LOG_TAG", "Не вдалося ініціалізувати рядок підключення. Дані відсутні.");
        }
    }


    // Ваша логіка розшифрування
    public static String decrypts(String data, String key) {
        StringBuilder decrypted = new StringBuilder();
        String modifiedKey = modifyKey(key);
        for (int i = 0; i < data.length(); i++) {
            decrypted.append((char) (data.charAt(i) ^ modifiedKey.charAt(i % modifiedKey.length())));
        }
        return decrypted.toString();
    }


    private void onForgotPasswordBtnClick(View view) {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void onRegistrationBtnClick(View view) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);

    }

    private void onLoginBtnClick(View view) {

        String phone = phoneEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        new Thread(() -> {
            String connectionString = ConnectionDb.getInstance().getConnectionString();

            try (Connection connection = DriverManager.getConnection(connectionString)) {
                String query = "SELECT HashPassword, Role FROM Users WHERE PhoneNumber = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, phone);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            String storedHashedPassword = resultSet.getString("HashPassword");
                            String role = resultSet.getString("Role");

                            if (PasswordHasher.verifyPassword(password, storedHashedPassword) && "User".equals(role)) {
                                runOnUiThread(() -> {
                                    // Збереження телефону в SharedPreferences
                                    String phoneNumber = phoneEditText.getText().toString(); // або userId, якщо у вас є ID
                                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("phoneNumber", phoneNumber); // або "userId" для збереження ID
                                    editor.apply();

                                    Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(this, StockMenuActivity.class);
                                    startActivity(intent);
                                });
                            } else if (!"User".equals(role)) {
                                runOnUiThread(() -> Toast.makeText(this, getString(R.string.higher_privileges), Toast.LENGTH_SHORT).show());
                            } else {
                                runOnUiThread(() -> Toast.makeText(this, getString(R.string.incorrect_password), Toast.LENGTH_SHORT).show());
                            }
                        } else {
                            runOnUiThread(() -> Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show());
                        }
                    }
                }
            } catch (Exception ex) {
                runOnUiThread(() -> Toast.makeText(this, getString(R.string.error_occurred, ex.getMessage()), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}