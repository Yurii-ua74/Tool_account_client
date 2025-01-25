package step.learning.tool_account_client;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import step.learning.tool_account_client.desine.ToolCustomerAdapter;
import step.learning.tool_account_client.desine.ToolStockAdapter;
import step.learning.tool_account_client.models.ToolModel;
import step.learning.tool_account_client.services.ConnectionDb;

public class ToolsStockActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ToolStockAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools_stock);

        recyclerView = findViewById(R.id.s_RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        choiceList();
    }

    private void choiceList() {
        String listType = getIntent().getStringExtra("listType");

        switch (Objects.requireNonNull(listType)) {

            case "inStock":
                // інструменти на складі
                inStock();
                break;
            case "issued":
                // видані інструменти
                issued();
                break;
        }
    }

    private void inStock() {

        new Thread(() -> {
            String connectionString = ConnectionDb.getInstance().getConnectionString();

            try (Connection connection = DriverManager.getConnection(connectionString)) {
                String query = "SELECT t.Id AS Id, t.Name AS Name, t.Model AS Model, t.Number AS ToolNumber " +
                        "FROM Tools t " +
                        "LEFT JOIN UserTool ut ON t.Id = ut.ToolId " +
                        "WHERE ut.ToolId IS NULL";

                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    //preparedStatement.setString(1, savedPhoneNumber);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {

                        List<ToolModel> toolList = new ArrayList<>();
                        while (resultSet.next()) {
                            ToolModel tool = new ToolModel(
                                    resultSet.getString("Id"),
                                    resultSet.getString("Name"),
                                    resultSet.getString("Model"),
                                    resultSet.getString("ToolNumber")
                            );
                            toolList.add(tool);
                        }

                        runOnUiThread(() -> {

                            if (toolList.isEmpty()) {
                                Log.d("ToolInfo", "No tools found.");
                            } else {
                                adapter = new ToolStockAdapter(toolList);
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

    private void issued() {
        new Thread(() -> {
            String connectionString = ConnectionDb.getInstance().getConnectionString();

            try (Connection connection = DriverManager.getConnection(connectionString)) {
                String query = "SELECT t.Id AS Id, t.Name AS Name, t.Model AS Model, t.Number AS ToolNumber, u.Name AS Firstname, u.Surname AS Lastname " +
                        "FROM Tools t " +
                        "INNER JOIN UserTool ut ON t.Id = ut.ToolId " +
                        "INNER JOIN Users u ON ut.UserId = u.Id";


                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    //preparedStatement.setString(1, savedPhoneNumber);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {

                        List<ToolModel> toolList = new ArrayList<>();
                        while (resultSet.next()) {
                            ToolModel tool = new ToolModel(
                                    resultSet.getString("Id"),
                                    resultSet.getString("Name"),
                                    resultSet.getString("Model"),
                                    resultSet.getString("ToolNumber"),
                                    resultSet.getString("Firstname"),
                                    resultSet.getString("Lastname")
                            );
                            toolList.add(tool);
                        }

                        runOnUiThread(() -> {

                            if (toolList.isEmpty()) {
                                Log.d("ToolInfo", "No tools found.");
                            } else {
                                adapter = new ToolStockAdapter(toolList);
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