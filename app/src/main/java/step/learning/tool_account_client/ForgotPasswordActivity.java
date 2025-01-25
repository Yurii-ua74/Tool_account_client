package step.learning.tool_account_client;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import step.learning.tool_account_client.services.ConnectionDb;
import step.learning.tool_account_client.utils.PasswordHasher;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText passwordNewText;
    private EditText passwordRepeatText;
    private EditText phoneNumberText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgot_password), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ініціалізація EditText елементів
        passwordNewText = findViewById(R.id.txt_new_password);
        passwordRepeatText = findViewById(R.id.txt_repeat_password);
        phoneNumberText = findViewById(R.id.txt_number);

        findViewById(R.id.btn_getPassword).setOnClickListener(this::onGetPasswordBtnClick);
        findViewById(R.id.btn_backToMain).setOnClickListener(this::onBackToMainBtnClick);
    }

    private void onGetPasswordBtnClick(View view) {
        String newPassword = passwordNewText.getText().toString();
        String repeatPassword = passwordRepeatText.getText().toString();
        String phoneNumber = phoneNumberText.getText().toString();

        if (newPassword.equals(repeatPassword)) {
            String hashedPassword = PasswordHasher.hashPassword(newPassword);

            new Thread(() -> {
                String connectionString = ConnectionDb.getInstance().getConnectionString();
                try (Connection connection = DriverManager.getConnection(connectionString)) {
                    String query = "SELECT ChangePassword FROM Users WHERE PhoneNumber = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, phoneNumber);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        String change = resultSet.getString("ChangePassword");
                        if (change == null || change.trim().isEmpty()) {
                            runOnUiThread(() -> showAlert("Зверніться до адміністратора"));
                        } else {
                            // Update HashPassword and clear Change
                            String updateQuery = "UPDATE Users SET HashPassword = ?, ChangePassword = '' WHERE PhoneNumber = ?";
                            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                            updateStatement.setString(1, hashedPassword);
                            updateStatement.setString(2, phoneNumber);
                            updateStatement.executeUpdate();

                            runOnUiThread(() -> showAlert("Пароль змінено успішно"));
                            backToMain();
                        }
                    } else {
                        runOnUiThread(() -> showAlert("Користувача з таким номером телефону не знайдено"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> showAlert("Сталася помилка: " + e.getMessage()));
                }
            }).start();
        } else {
            showAlert("Введені паролі не співпадають");
        }
    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("OK", null)
                .create()
                .show();
    }

    private String hashPassword(String password) {
        // Implement your password hashing logic here
        return password; // Replace with actual hashing logic
    }

    private void onBackToMainBtnClick(View view) {
        backToMain();
    }

    private void backToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}