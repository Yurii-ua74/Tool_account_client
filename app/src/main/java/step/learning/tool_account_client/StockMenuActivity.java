package step.learning.tool_account_client;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import step.learning.tool_account_client.utils.LocaleHelper;

public class StockMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Завантаження збереженої мови
        LocaleHelper.loadLocale(this);
        // Викликаємо setContentView після оновлення локалі
        setContentView(R.layout.activity_stock_menu);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.stock_menu), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button buttonMyTools = findViewById(R.id.tools_my);
        Button buttonInStock = findViewById(R.id.tools_in_stock);
        Button buttonIssued = findViewById(R.id.tools_issued);
        Button buttonOrderDesk = findViewById(R.id.tools_order_desk);

        buttonMyTools.setOnClickListener(v -> openToolsActivity());
        buttonInStock.setOnClickListener(v -> openToolsStockActivity("inStock"));
        buttonIssued.setOnClickListener(v -> openToolsStockActivity("issued"));
        buttonOrderDesk.setOnClickListener(v -> openDeskActivity());

    }

    private void openToolsActivity() {
        Intent intent = new Intent(this, ToolsMyActivity.class);
        intent.putExtra("listType", "myTools");
        startActivity(intent);
    }
    private void openDeskActivity() {
        Intent intent = new Intent(this, OrderDeskActivity.class);
        intent.putExtra("listType", "orderDesk");
        startActivity(intent);
    }
    private void openToolsStockActivity(String listType) {
        Intent intent = new Intent(this, ToolsStockActivity.class);
        intent.putExtra("listType", listType);
        startActivity(intent);
    }

}