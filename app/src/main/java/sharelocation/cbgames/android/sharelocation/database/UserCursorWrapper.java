package sharelocation.cbgames.android.sharelocation.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

import sharelocation.cbgames.android.sharelocation.MyInformation;
import sharelocation.cbgames.android.sharelocation.MyInformation.InformationUser;
import sharelocation.cbgames.android.sharelocation.database.ShareLocationDbSchema.UserTable;

public class UserCursorWrapper extends CursorWrapper {
    public UserCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public InformationUser getUser() {
        String uuidString = getString(getColumnIndex(UserTable.Cols.UUID));
        String codeString = getString(getColumnIndex(UserTable.Cols.CODE));
        double latitudeString = getDouble(getColumnIndex(UserTable.Cols.LOCATION_LAT));
        double longitudeString = getDouble(getColumnIndex(UserTable.Cols.LOCATION_LNG));
        String usernameString = getString(getColumnIndex(UserTable.Cols.USERNAME));
        String statuscodeString = getString(getColumnIndex(UserTable.Cols.STATUS_CODE));

        InformationUser user = new InformationUser();
        user.setId(UUID.fromString(uuidString));
        user.setCode(codeString);
        user.setLocation(new LatLng(latitudeString, longitudeString));
        user.setUserName(usernameString);
        user.setStatusCode(MyInformation.InformationUser.StatusCode.valueOf(statuscodeString));
        return user;
    }
}