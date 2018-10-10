package sharelocation.cbgames.android.sharelocation;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.UUID;

import sharelocation.cbgames.android.sharelocation.database.ShareLocationBaseHelper;
import sharelocation.cbgames.android.sharelocation.database.ShareLocationDbSchema;
import sharelocation.cbgames.android.sharelocation.database.ShareLocationDbSchema.UserTable;
import sharelocation.cbgames.android.sharelocation.database.UserCursorWrapper;

import static android.content.Context.MODE_PRIVATE;

public class MyInformation {
    private static final String TAG = "MyInformation";
    private static MyInformation sMyInformation;


    private Context mContext;
    private SQLiteDatabase mDatabase;
    private UUID mMyId;

    public static class InformationUser{
        public StatusCode getStatusCode() {
            return mStatusCode;
        }

        public void setStatusCode(StatusCode statusCode) {
            mStatusCode = statusCode;
        }

        public UUID getId() {
            return mUUID;
        }

        public void setId(UUID uuid) {
            mUUID = uuid;
        }

        public enum StatusCode {
            NONE, AVAILABLE, NOT_AVAILABLE, ONLINE, OFFLINE
        }

        private UUID mUUID;
        private LatLng mLocation;
        private String mUserName;
        private String mCode;
        private StatusCode mStatusCode;

        public InformationUser(){
            mUUID = UUID.randomUUID();
            mLocation = new LatLng(0,0);
            mUserName = "";
            mCode = "";
            mStatusCode = StatusCode.NONE;
        }

        public LatLng getLocation() {
            if(mLocation.latitude != 0)
                return mLocation;
            else
                return new LatLng(0, 0);
        }

        public void setLocation(LatLng location) {
            mLocation = location;
        }

        public String getUserName() {
            return mUserName;
        }

        public void setUserName(String userName) {
            mUserName = userName;
        }

        public String getCode() {
            return mCode;
        }

        public void setCode(String code) {
            mCode = code;
        }
    }

    public static MyInformation get(Context context){
        if(sMyInformation == null){
            sMyInformation = new MyInformation(context);
        }
        return sMyInformation;
    }

    private MyInformation(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new ShareLocationBaseHelper(mContext)
                .getWritableDatabase();
    }

    private static ContentValues getContentValues(InformationUser informUser) {
        ContentValues values = new ContentValues();
        values.put(UserTable.Cols.UUID, informUser.getId().toString());
        values.put(UserTable.Cols.CODE, informUser.getCode().toString());
        values.put(UserTable.Cols.LOCATION_LAT, informUser.getLocation().latitude+"");
        values.put(UserTable.Cols.LOCATION_LNG, informUser.getLocation().longitude+"");
        values.put(UserTable.Cols.USERNAME, informUser.getUserName().toString());
        values.put(UserTable.Cols.STATUS_CODE, informUser.getStatusCode().toString());
        return values;
    }

    public InformationUser getUser(String code){
        //return mUsers.get(userId);
        if(code.equals("0") || code == null || code.length() == 0)
            code = QueryPreferences.getShareCode(mContext);

        UserCursorWrapper cursor = queryUsers(
                UserTable.Cols.CODE + " = ?",
                new String[] { code+"" }
        );
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            mMyId = cursor.getUser().mUUID;
            return cursor.getUser();
        } finally {
            cursor.close();
        }
    }

    public ArrayList<InformationUser> getUsers(){
        ArrayList<InformationUser> users = new ArrayList<>();
        UserCursorWrapper cursor = queryUsers(null, null);
        String myCode = QueryPreferences.getShareCode(mContext);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                if(!cursor.getUser().getCode().equals(myCode))
                    users.add(cursor.getUser());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return users;
    }

    public void addUser(InformationUser informUser){
        ContentValues values = getContentValues(informUser);
        if(getUser(informUser.getCode()) == null) {
            informUser.setId(UUID.randomUUID());
            mDatabase.insert(UserTable.NAME, null, values);
        }else{
            mDatabase.update(UserTable.NAME, values,
                    UserTable.Cols.CODE + " = ?",
                    new String[] { informUser.getCode() });
        }
    }

    public void updateUser(InformationUser informUser){
        updateUser(informUser, false);
    }

    public void updateUser(InformationUser informUser, boolean isChangeCode){
        ContentValues values = getContentValues(informUser);

        if(!isChangeCode) {
            String code = informUser.getCode().toString();
            mDatabase.update(UserTable.NAME, values,
                    UserTable.Cols.CODE + " = ?",
                    new String[]{code});
        }else{
            mDatabase.update(UserTable.NAME, values,
                    UserTable.Cols.UUID + " = ?",
                    new String[]{mMyId.toString()});
        }
    }

    /*
    public void updateUser(String code, InformationUser.StatusCode statusCode, String username, double lat, double lon){
        code = code.toLowerCase();
        for(int i=1; i<mUsers.size(); i++){
            if(mUsers.get(i).getCode().equals(code)){
                mUsers.get(i).getLocation().setLatitude(lat);
                mUsers.get(i).getLocation().setLongitude(lon);
                mUsers.get(i).setStatusCode(statusCode);
                return;
            }
        }

        InformationUser informUser = new InformationUser();
        informUser.setCode(code);
        informUser.setUserName(username);
        informUser.setStatusCode(statusCode);

        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(lat);
        location.setLongitude(lon);

        informUser.setLocation(location);
        mUsers.add(informUser);
    }
    */

    public void removeUser(String code){
        mDatabase.delete(UserTable.NAME,
                UserTable.Cols.CODE + " = ?",
                new String[] { code });
        /*
        for(int i=0; i<mUsers.size(); i++){
            if(mUsers.get(i).getCode().equals(code)){
                mUsers.remove(i);
                break;
            }
        }
        */
    }

    private UserCursorWrapper queryUsers(String whereClause, String[] whereArgs){
    Cursor cursor = mDatabase.query(
                UserTable.NAME,
                null, // columns - с null выбираются все столбцы
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null  // orderBy
        );
        return new UserCursorWrapper(cursor);
    }
}