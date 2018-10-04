package sharelocation.cbgames.android.sharelocation;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import java.util.ArrayList;

class MyInformation {
    private static MyInformation sMyInformation;
    private ArrayList<InformationUser> mUsers;


    public static class InformationUser{
        public StatusCode getStatusCode() {
            return mStatusCode;
        }

        public void setStatusCode(StatusCode statusCode) {
            mStatusCode = statusCode;
        }

        public enum StatusCode {
            NONE, AVAILABLE, NOT_AVAILABLE, ONLINE, OFFLINE
        }

        private Location mLocation;
        private String mUserName;
        private String mAuthHash;
        private String mCode;
        private StatusCode mStatusCode;

        private InformationUser(){
            mLocation = new Location(LocationManager.GPS_PROVIDER);
            mUserName = "";
            mAuthHash = "";
            mCode = "";
            mStatusCode = StatusCode.NONE;
        }

        public Location getLocation() {
            if(mLocation.getLatitude() != 0)
                return mLocation;
            else
                return null;
        }

        public void setLocation(Location location) {
            mLocation = location;
        }

        public String getUserName() {
            return mUserName;
        }

        public void setUserName(String userName) {
            mUserName = userName;
        }

        public String getAuthHash() {
            return mAuthHash;
        }

        public void setAuthHash(String authHash) {
            mAuthHash = authHash;
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
        mUsers = new ArrayList<>();
        mUsers.add(new InformationUser()); //мой акк, всегда под ID: 0
    }

    public InformationUser getUser(int userId){
        return mUsers.get(userId);
    }

    public ArrayList<InformationUser> getUsers(){
        return mUsers;
    }

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

    public void removeUser(String code){
        for(int i=0; i<mUsers.size(); i++){
            if(mUsers.get(i).getCode().equals(code)){
                mUsers.remove(i);
                break;
            }
        }
    }
}