package step.learning.tool_account_client.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class MyDbManager {
    private Context context;
    private final MyDbHelper myDbHelper;
    private SQLiteDatabase db;

    public MyDbManager(Context context) {
        this.context = context;
        myDbHelper = new MyDbHelper(context);
    }

    // функція для відкриття
    public void openDb() {
        db = myDbHelper.getWritableDatabase();
    }

    // функція для запису в db
    public void InsertToDb(String name, String password, String word) {
        ContentValues cv = new ContentValues();
        cv.put(MyCredentials.Name, name);
        cv.put(MyCredentials.Password, password);
        cv.put(MyCredentials.Word, word);
        db.insert(MyCredentials.TABLE_NAME, null, cv);
    }

    public List<String> getFromDb() {
        List<String> temp_list = new ArrayList<>();
        Cursor cursor = db.query(MyCredentials.TABLE_NAME, null, null, null, null, null, null);
         while (cursor.moveToNext()) {
               @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(MyCredentials.Name));
               temp_list.add(name);
         }
         cursor.close();
        return temp_list;
    }

    public void closeDb() {
        myDbHelper.close();
    }
}
