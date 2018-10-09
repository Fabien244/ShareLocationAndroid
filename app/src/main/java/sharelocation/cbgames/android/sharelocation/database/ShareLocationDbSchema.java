package sharelocation.cbgames.android.sharelocation.database;

public class ShareLocationDbSchema {
    public static final class UserTable {
        public static final String NAME = "users";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String CODE = "code";
            public static final String LOCATION_LAT = "latitude";
            public static final String LOCATION_LNG = "longitude";
            public static final String USERNAME = "username";
            public static final String STATUS_CODE = "status_code";
        }
    }
}
