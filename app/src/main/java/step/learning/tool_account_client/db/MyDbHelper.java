package step.learning.tool_account_client.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyDbHelper extends SQLiteOpenHelper {

    public MyDbHelper(@Nullable Context context) {
        super(context, MyCredentials.DB_NAME, null, MyCredentials.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
         db.execSQL(MyCredentials.TABLE_STRUCTURE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         db.execSQL(MyCredentials.DROP_TABLE);
         onCreate(db);
    }
}
