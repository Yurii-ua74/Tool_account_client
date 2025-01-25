package step.learning.tool_account_client;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import step.learning.tool_account_client.desine.ToolCustomerAdapter;
import step.learning.tool_account_client.models.ToolModel;
import step.learning.tool_account_client.services.ConnectionDb;

public class ToolsMyActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ToolCustomerAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools_my);

        recyclerView = findViewById(R.id.t_RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Ініціалізація GestureDetector для обробки довгого натискання
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null) {
                    int position = recyclerView.getChildAdapterPosition(child);
                    ToolModel selectedItem = adapter.toolList.get(position); // Отримуємо об'єкт ToolModel

                    new AlertDialog.Builder(ToolsMyActivity.this)
                            .setTitle("Підтвердження дії")
                            .setMessage("Ви впевнені, що хочете віддати \"" + selectedItem.getName() + "\"?")
                            .setPositiveButton("Так", (dialog, which) -> {

                                ToolModel tool = new ToolModel(
                                        selectedItem.getId(),
                                        selectedItem.getName(),
                                        selectedItem.getModel(),
                                        selectedItem.getNumber(),
                                        selectedItem.getFirstname(),
                                        selectedItem.getLastname()
                                );
                                sendToOrderDesk(tool);
                            })
                            .setNegativeButton("Ні", (dialog, which) -> {
                                Toast.makeText(ToolsMyActivity.this, "Дія скасована", Toast.LENGTH_SHORT).show();
                            })
                            .show();
                }
            }
        });


        // Додавання обробника дотиків до RecyclerView
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                gestureDetector.onTouchEvent(e);
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

        choiceList();
    }



    private void sendToOrderDesk(ToolModel tool) {
        // Створюємо новий потік для виконання мережевої операції
        new Thread(() -> {
            // Підключення до бази даних
            String connectionString = ConnectionDb.getInstance().getConnectionString();

            // Перше з'єднання для перевірки наявності інструменту
            try (Connection connection = DriverManager.getConnection(connectionString)) {
                // Перевірка наявності інструменту з таким же Number
                String checkQuery = "SELECT COUNT(*) FROM DeskOfOrders WHERE Number = ?";

                try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                    checkStatement.setString(1, tool.getNumber());  // Встановлюємо значення Number

                    try (ResultSet checkResultSet = checkStatement.executeQuery()) {
                        if (checkResultSet.next() && checkResultSet.getInt(1) > 0) {
                            // Якщо такий інструмент вже існує (COUNT > 0)
                            runOnUiThread(() -> {
                                Toast.makeText(ToolsMyActivity.this, "Такий інструмент вже є в замовленнях.", Toast.LENGTH_SHORT).show();
                            });
                            return; // Не вставляємо новий запис
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(ToolsMyActivity.this, "Помилка при перевірці інструменту: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(ToolsMyActivity.this, "Помилка з'єднання з базою даних: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                return;
            }

            // Після успішної перевірки, відкриваємо нове з'єднання для вставки
            try (Connection connection = DriverManager.getConnection(connectionString)) {
                // SQL запит для вставки даних
                String sql = "INSERT INTO DeskOfOrders (Name, Model, Number, FromName) VALUES (?, ?, ?, ?)";

                // Підготовка запиту
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    // Встановлюємо значення параметрів запиту
                    preparedStatement.setString(1, tool.getName());       // Name
                    preparedStatement.setString(2, tool.getModel());      // Model
                    preparedStatement.setString(3, tool.getNumber());     // Number
                    preparedStatement.setString(4, tool.getFirstname() + " " + tool.getLastname()); // FromName

                    // Виконання запиту на вставку
                    int rowsAffected = preparedStatement.executeUpdate();

                    // Перевірка, чи був запис успішно доданий
                    runOnUiThread(() -> {
                        if (rowsAffected > 0) {
                            Toast.makeText(ToolsMyActivity.this, "Замовлення надіслано!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ToolsMyActivity.this, "Помилка при відправці замовлення.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(ToolsMyActivity.this, "Помилка при вставці інструменту: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(ToolsMyActivity.this, "Помилка з'єднання з базою даних: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }



    private void choiceList() {
        String listType = getIntent().getStringExtra("listType");

        if (Objects.requireNonNull(listType).equals("myTools")) {// інструменти, які належать мені
            myTools();
        }
    }

    private void myTools() {

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedPhoneNumber = sharedPreferences.getString("phoneNumber", "");

        new Thread(() -> {
            String connectionString = ConnectionDb.getInstance().getConnectionString();

                try (Connection connection = DriverManager.getConnection(connectionString)) {
                String query = "SELECT t.Id, t.Name, t.Model, t.Number, u.Name AS Firstname, u.Surname AS Lastname " +
                        "FROM Tools t " +
                        "INNER JOIN UserTool ut ON t.Id = ut.ToolId " +
                        "INNER JOIN Users u ON ut.UserId = u.Id " +
                        "WHERE u.PhoneNumber = ?";

                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, savedPhoneNumber);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {

                        List<ToolModel> toolList = new ArrayList<>();
                        while (resultSet.next()) {
                            ToolModel tool = new ToolModel(
                                    resultSet.getString("Id"),
                                    resultSet.getString("Name"),
                                    resultSet.getString("Model"),
                                    resultSet.getString("Number"),
                                    resultSet.getString("Firstname"),
                                    resultSet.getString("Lastname")
                            );
                            toolList.add(tool);
                        }

                        runOnUiThread(() -> {
                            if (toolList.isEmpty()) {
                                Log.d("ToolInfo", "No tools found.");
                            } else {
                                adapter = new ToolCustomerAdapter(toolList);
                                recyclerView.setAdapter(adapter);

                            }
                        });

                    }
                }
            } catch (Exception ex) {
                runOnUiThread(() -> Toast.makeText(this, getString(R.string.error_occurred, ex.getMessage()), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void  orderDesk() {

    }
}
