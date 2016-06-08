package com.example.eliam.challengingfruits;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

public class Challenge {

    public static final String CHALLENGE_JSON = "CHALLENGE_JSON";

    public static final String ARG_CHALLENGED_BY = "ARG_CHALLENGED_BY";
    public static final String ARG_DEADLINE = "ARG_DEADLINE";
    public static final String ARG_POINTS = "ARG_POINTS";
    public static final String ARG_WHAT = "ARG_WHAT";

    String challengedBy = "";
    Date deadline = null;
    int points = -1;
    String what = "";

    public Challenge(String challengedBy, Date deadline, int points, String what) {
        this.challengedBy = challengedBy;
        this.deadline = deadline;
        this.points = points;
        this.what = what;
    }

    public Challenge(JSONObject obj) {
        if (obj != null) {
            try {
                if (!obj.isNull(ARG_CHALLENGED_BY))
                    setChallengedBy(obj.getString(ARG_CHALLENGED_BY));
                if (!obj.isNull(ARG_DEADLINE))
                    setDeadline(Utils.date.parse(obj.getString(ARG_DEADLINE)));
                if (!obj.isNull(ARG_POINTS))
                    setPoints(obj.getInt(ARG_POINTS));
                if (!obj.isNull(ARG_WHAT))
                    setWhat(obj.getString(ARG_WHAT));
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public String getChallengedBy() {
        return challengedBy;
    }

    public void setChallengedBy(String challengedBy) {
        this.challengedBy = challengedBy;
    }

    public Date getDeadline() {
        return deadline;
    }

    public String printDeadline() {
        return Utils.date.format(getDeadline());
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }


    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put(ARG_CHALLENGED_BY, getChallengedBy());
            obj.put(ARG_DEADLINE, Utils.date.format(getDeadline()));
            obj.put(ARG_POINTS, getPoints());
            obj.put(ARG_WHAT, getWhat());
        } catch (JSONException e) {
            obj = new JSONObject();
        }

        return obj;
    }

    public String toSMS() {
        return getWhat() + "#" + getPoints() + "#" + printDeadline();
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    public static Challenge parseFromSMS(String sms, String from) {
        String what;
        Date deadline;
        int points;

        what = sms.substring(0, sms.indexOf("#"));
        sms = sms.substring(sms.indexOf("#") + 1);

        points = Integer.parseInt(sms.substring(0, sms.indexOf("#")));
        sms = sms.substring(sms.indexOf("#") + 1);

        try {
            deadline = Utils.date.parse(sms);
        } catch (ParseException e) {
            deadline = null;
        }

        return new Challenge(from, deadline, points, what);
    }
}
