package step.learning.tool_account_client.db;

import static android.provider.Telephony.Carriers.PASSWORD;
import static android.provider.UserDictionary.Words.WORD;
import static android.util.JsonToken.NAME;

import android.net.Credentials;

// створення локальної бази даних SQLite
public class MyCredentials {
    public static final String TABLE_NAME = "CredentialsTable";
    public static final String _ID = "_id";
    public static final String Name = "name";
    public static final String Password = "password";
    public static final String Word = "word";
    public static final String DB_NAME = "my_db.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_STRUCTURE = "CREATE TABLE IF NOT EXIST " +
            TABLE_NAME + "(" + _ID +  " INTEGER PRIMARY KEY, " +
            NAME + " TEXT," +
            PASSWORD + " TEXT," +
            WORD + " TEXT)";

    public static final String DROP_TABLE = "DROP TABLE IF EXIST " + TABLE_NAME;





}
