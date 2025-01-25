package step.learning.tool_account_client.services;

public class ConnectionDb {
    private static ConnectionDb instance; // Singleton instance
    private String connectionString;


    // Приватний конструктор для Singleton
    private ConnectionDb() {

    }

    // Метод для отримання єдиного екземпляра
    public static synchronized ConnectionDb getInstance() {
        if (instance == null) {
            instance = new ConnectionDb();
        }
        return instance;
    }

    // Метод для ініціалізації даних
    public void initializeConnectionString(String name, String password) {
        if (name == null || password == null) {
            throw new IllegalArgumentException("Ім'я користувача або пароль не може бути null");
        }
        connectionString = "jdbc:jtds:sqlserver://toolsDB.mssql.somee.com:1433/toolsDB;user="
                + name + ";password=" + password + ";";
    }

    // Метод для отримання рядка підключення
    public String getConnectionString() {
        if (connectionString == null) {
            throw new IllegalStateException("Рядок підключення не ініціалізовано. Використайте метод initializeConnectionString");
        }
        return connectionString;
    }
}
