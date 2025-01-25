package step.learning.tool_account_client;

import static android.content.Intent.getIntent;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import step.learning.tool_account_client.desine.ToolCustomerAdapter;
import step.learning.tool_account_client.models.ToolModel;
import step.learning.tool_account_client.services.ConnectionDb;

public class OrderDeskActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ToolCustomerAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_desk);

        recyclerView = findViewById(R.id.d_RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Ініціалізація GestureDetector для обробки довгого натискання
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null) {
                    int position = recyclerView.getChildAdapterPosition(child);
                    ToolModel selectedItem = adapter.toolList.get(position); // Отримуємо об'єкт ToolModel

                    new AlertDialog.Builder(OrderDeskActivity.this)
                            .setTitle("Підтвердження дії")
                            .setMessage("Ви впевнені, що хочете прийняти \"" + selectedItem.getName() + "\"?")
                            .setPositiveButton("Так", (dialog, which) -> {

                                ToolModel tool = new ToolModel(
                                        selectedItem.getId(),
                                        selectedItem.getName(),
                                        selectedItem.getModel(),
                                        selectedItem.getNumber(),
                                        selectedItem.getFirstname(),
                                        selectedItem.getLastname()
                                );
                                removeFromOrderDesk(tool);
                            })
                            .setNegativeButton("Ні", (dialog, which) -> {
                                Toast.makeText(OrderDeskActivity.this, "Дія скасована", Toast.LENGTH_SHORT).show();
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

    private void removeFromOrderDesk(ToolModel tool) {
        new Thread(() -> {
            String connectionString = ConnectionDb.getInstance().getConnectionString();

            int toolId;
            String userId;

            try (Connection connection = DriverManager.getConnection(connectionString)) {
                // 1. Знайти інструмент за іменем і номером в таблиці Tools
                String findToolQuery = "SELECT Id FROM Tools WHERE Name = ? AND Number = ?";
                try (PreparedStatement findToolStatement = connection.prepareStatement(findToolQuery)) {
                    findToolStatement.setString(1, tool.getName());
                    findToolStatement.setString(2, tool.getNumber());

                    try (ResultSet resultSet = findToolStatement.executeQuery()) {
                        if (resultSet.next()) {
                            toolId = resultSet.getInt("Id");
                        } else {
                            runOnUiThread(() -> Toast.makeText(OrderDeskActivity.this, "Інструмент не знайдено в базі даних.", Toast.LENGTH_SHORT).show());
                            return;
                        }
                    }
                }

                // 2. Видалити рядок з таблиці DeskOfOrders
                String deleteDeskOrderQuery = "DELETE FROM DeskOfOrders WHERE Name = ? AND Number = ?";
                try (PreparedStatement deleteDeskOrderStatement = connection.prepareStatement(deleteDeskOrderQuery)) {
                    deleteDeskOrderStatement.setString(1, tool.getName());
                    deleteDeskOrderStatement.setString(2, tool.getNumber());
                    deleteDeskOrderStatement.executeUpdate();
                }

                // 2. Видалити рядок з таблиці UserTool, де ToolId відповідає знайденому
                String deleteUserToolQuery = "DELETE FROM UserTool WHERE ToolId = ?";
                try (PreparedStatement deleteUserToolStatement = connection.prepareStatement(deleteUserToolQuery)) {
                    deleteUserToolStatement.setInt(1, toolId);
                    deleteUserToolStatement.executeUpdate();
                }

                // Отримати номер телефону користувача з SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String phoneNumber = sharedPreferences.getString("phoneNumber", null);

                // Знайти UserId за номером телефону
                String findUserQuery = "SELECT Id FROM Users WHERE PhoneNumber = ?";
                try (PreparedStatement findUserStatement = connection.prepareStatement(findUserQuery)) {
                    findUserStatement.setString(1, phoneNumber);

                    try (ResultSet userResultSet = findUserStatement.executeQuery()) {
                        if (userResultSet.next()) {
                            userId = userResultSet.getString("Id");
                        } else {
                            runOnUiThread(() -> Toast.makeText(OrderDeskActivity.this, "Користувача не знайдено.", Toast.LENGTH_SHORT).show());
                            return;
                        }
                    }
                }



                // 3. Вставити новий рядок до таблиці UserTool
                String insertUserToolQuery = "INSERT INTO UserTool (ToolId, UserId) VALUES (?, ?)";
                try (PreparedStatement insertUserToolStatement = connection.prepareStatement(insertUserToolQuery)) {
                    insertUserToolStatement.setInt(1, toolId);
                    insertUserToolStatement.setString(2, userId);
                    int rowsAffected = insertUserToolStatement.executeUpdate();

                    runOnUiThread(() -> {
                        if (rowsAffected > 0) {
                            Toast.makeText(OrderDeskActivity.this, "Інструмент успішно оновлено для користувача.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OrderDeskActivity.this, "Помилка при оновленні інструменту для користувача.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(OrderDeskActivity.this, "Помилка з базою даних: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }


    private void choiceList() {
        String listType = getIntent().getStringExtra("listType");
        //Button transferButton = findViewById(R.id.transfer_button);

         if (Objects.requireNonNull(listType).equals("orderDesk")) {// інструменти, які належать мені
             orderDesk();
         }
    }


    private void  orderDesk() {

        new Thread(() -> {
            String connectionString = ConnectionDb.getInstance().getConnectionString();

             try (Connection connection = DriverManager.getConnection(connectionString)) {
                String query = "SELECT t.Id AS Id, t.Name AS Name, t.Model AS Model, t.Number AS ToolNumber, t.FromName AS FN, t.ToName AS TN  " +
                        "FROM DeskOfOrders t";

                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    //preparedStatement.setString(1, savedPhoneNumber);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {

                        List<ToolModel> toolList = new ArrayList<>();
                        while (resultSet.next()) {
                            String tn = resultSet.getString("TN");
                            if (tn == null) {
                                tn = "";
                            }
                            ToolModel tool = new ToolModel(
                                    resultSet.getString("Id"),
                                    resultSet.getString("Name"),
                                    resultSet.getString("Model"),
                                    resultSet.getString("ToolNumber"),
                                    resultSet.getString("FN"),
                                    tn
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

}