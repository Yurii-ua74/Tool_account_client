package step.learning.tool_account_client;

import static step.learning.tool_account_client.utils.LocaleHelper.getLang;
import static step.learning.tool_account_client.utils.LocaleHelper.loadLocale;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

import step.learning.tool_account_client.services.ConnectionDb;
import step.learning.tool_account_client.utils.LocaleHelper;
import step.learning.tool_account_client.utils.PasswordHasher;

public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Завантаження збереженої мови
        LocaleHelper.loadLocale(this);
        // Викликаємо setContentView після оновлення локалі
        setContentView(R.layout.activity_registration);

        EdgeToEdge.enable(this);

        if (!LocaleHelper.isLanguageChanged(RegistrationActivity.this, getLang())) { // Перевіряємо, чи не поточна мова "en"
            LocaleHelper.setLanguage(RegistrationActivity.this, getLang());
            if(!Objects.equals(getLang(), "en")) {
                recreate(); // Перезавантажуємо активність лише якщо мова змінилася
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registration), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.registration_btn_sign).setOnClickListener(this::onSignInBtnClick);
    }

    private void onSignInBtnClick(View view) {
    // Отримання даних з полів введення
        String phone = ((EditText) findViewById(R.id.registration_phone)).getText().toString().trim();
        String name = ((EditText) findViewById(R.id.registration_name)).getText().toString().trim();
        String surname = ((EditText) findViewById(R.id.registration_surname)).getText().toString().trim();
        String position = ((EditText) findViewById(R.id.registration_position)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.registration_password)).getText().toString().trim();
        String repeatPassword = ((EditText) findViewById(R.id.registration_repeat)).getText().toString().trim();

        if (phone.isEmpty() ||
            password.isEmpty() ||
            repeatPassword.isEmpty() ||
            name.isEmpty() ||
            surname.isEmpty() ||
            position.isEmpty()) {
            Toast.makeText(this, getString(R.string.registration_fields_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(repeatPassword)) {
            Toast.makeText(this, getString(R.string.registration_passwords_match), Toast.LENGTH_SHORT).show();
            return;
        }

        // Запуск нового потоку для роботи з базою даних
        new Thread(() -> {
            String connectionString = ConnectionDb.getInstance().getConnectionString();

            try (Connection connection = DriverManager.getConnection(connectionString)) {
                // Перевірка, чи вже існує користувач з таким номером телефону
                String checkQuery = "SELECT COUNT(*) FROM Users WHERE PhoneNumber = ?";
                try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                    checkStatement.setString(1, phone);
                    try (ResultSet checkResultSet = checkStatement.executeQuery()) {
                        if (checkResultSet.next() && checkResultSet.getInt(1) > 0) {
                            runOnUiThread(() -> Toast.makeText(this, getString(R.string.registration_phone_exists), Toast.LENGTH_SHORT).show());
                            return;
                        }
                    }
                }

                // Хешування пароля
                String hashedPassword = PasswordHasher.hashPassword(password);

                // Додавання нового користувача
                String insertQuery = "INSERT INTO Users (Name, Surname, PhoneNumber, Position, HashPassword, Role) VALUES (?, ?, ?, ?, ?, 'User')";
                try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                    insertStatement.setString(1, name);
                    insertStatement.setString(2, surname);
                    insertStatement.setString(3, phone);
                    insertStatement.setString(4, position);
                    insertStatement.setString(5, hashedPassword);

                    int rowsInserted = insertStatement.executeUpdate();
                    if (rowsInserted > 0) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, getString(R.string.registration_failed), Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, getString(R.string.registration_error), Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }


}