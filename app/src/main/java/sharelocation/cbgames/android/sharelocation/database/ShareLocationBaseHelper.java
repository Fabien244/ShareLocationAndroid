package sharelocation.cbgames.android.sharelocation.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import sharelocation.cbgames.android.sharelocation.database.ShareLocationDbSchema.UserTable;

public class ShareLocationBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "usersBase.db";

    public ShareLocationBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + UserTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                UserTable.Cols.UUID + ", " +
                UserTable.Cols.CODE + ", " +
                UserTable.Cols.LOCATION_LAT + ", " +
                UserTable.Cols.LOCATION_LNG + ", " +
                UserTable.Cols.USERNAME + ", " +
                UserTable.Cols.STATUS_CODE +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
