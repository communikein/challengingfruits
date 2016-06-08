package com.example.eliam.challengingfruits;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by eliam on 01/06/2016.
 */
public class UserInfo {

    public static final String USERINFO_JSON = "USERINFO_JSON";

    public static final String PARAM_MAIL = "pref_mail";
    public static final String PARAM_PASSWORD = "login_password";
    public static final String PARAM_NAME = "user_name";
    public static final String PARAM_SURNAME = "user_surname";
    public static final String PARAM_PHONE = "pref_phone";
    public static final String PARAM_ALL_APPLIED_JOBS = "pref_allAppliedJobs";
    public static final String PARAM_LOCAL_PROFILE_PIC = "pref_localProfilePic";


    private String name = "";
    private String surname = "";
    private Bitmap picture = null;
    private String picLocal = "";
    private String password = "";
    private String mail = "";
    private String phone = "";

    public UserInfo(String mail, String password, String name, String surname, String phone){
        setMail(mail);
        setPassword(password);
        setFirstName(name);
        setLastName(surname);
        setPhone(phone);
    }

    public UserInfo(JSONObject objUser, Context context) throws JSONException {
        if (objUser.has("user")) {
            JSONObject user = objUser.getJSONObject("user");
            if (user.has("email") && user.getString("email").contains("@"))
                setMail(user.getString("email"));
            if (user.has("password"))
                setPassword(user.getString("password"));
        }

        if (objUser.has("first_name"))
            setFirstName(objUser.getString("first_name"));
        if (objUser.has("last_name"))
            setLastName(objUser.getString("last_name"));
        if (objUser.has("phonenr"))
            setPhone(objUser.getString("phonenr"));
        if (objUser.has("pic_local"))
            setPicLocal(objUser.getString("pic_local"));
    }

    public UserInfo(String mail, String password){
        setMail(mail);
        setPassword(password);
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        if(mail != null) this.mail = mail;
        else this.mail = "";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password != null) this.password = password;
        else this.password = "";
    }

    public String getFirstName() {
        return name;
    }

    public void setFirstName(String name) {
        if (name != null) this.name = name;
        else this.name = "";
    }

    public String getPicLocalPath() {
        return picLocal;
    }

    public File getPicLocal() {
        return (picLocal == null) ? null : new File(picLocal);
    }

    public void setPicLocal(String path) {
        this.picLocal = path;
    }

    public String getLastName() {
        return surname;
    }

    public void setLastName(String surname) {
        this.surname = surname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfilePictureUrl() {
        return "";
        //return "http://cash2go.it/media/user_profile_img/" + getId() + "/profile_img.jpg";
    }

    public String printName() {
        return getFirstName() + " " + getLastName();
    }


    public JSONObject credentialsToJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("email", getMail());
            obj.put("password", getPassword());
        } catch (JSONException e){
            Log.e("JSON", e.getMessage());
            return null;
        }

        return obj;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("user", credentialsToJSON());
            obj.put("first_name", getFirstName());
            obj.put("last_name", getLastName());
            obj.put("phonenr", getPhone());
            obj.put("pic_local", getPicLocalPath());
        } catch (JSONException e){
            Log.e("JSON", e.getMessage());
            return null;
        }

        return obj;
    }

    public JSONObject updateInfoJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("user", credentialsToJSON());
            obj.put("first_name", getFirstName());
            obj.put("last_name", getLastName());
            obj.put("phonenr", getPhone());
        } catch (JSONException e){
            Log.e("JSON", e.getMessage());
            return null;
        }

        return obj;
    }

    public JSONObject loginJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("username", getMail());
            obj.put("password", getPassword());
        } catch (JSONException e){
            Log.e("JSON", e.getMessage());
            return null;
        }

        return obj;
    }

    public JSONObject verifyCodeJSON(String code) {
        JSONObject obj = new JSONObject();

        try {
            obj.put("email", getMail());
            obj.put("sms_code", code);
        } catch (JSONException e){
            Log.e("JSON", e.getMessage());
            return null;
        }

        return obj;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }
}

