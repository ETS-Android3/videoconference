package io.agora.responsemodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class NotificationList {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("imei")
    @Expose
    private String imei;
    @SerializedName("for")
    @Expose
    private String _for;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("time_string")
    @Expose
    private String timeString;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NotificationList withId(String id) {
        this.id = id;
        return this;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public NotificationList withImei(String imei) {
        this.imei = imei;
        return this;
    }

    public String getFor() {
        return _for;
    }

    public void setFor(String _for) {
        this._for = _for;
    }

    public NotificationList withFor(String _for) {
        this._for = _for;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationList withMessage(String message) {
        this.message = message;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public NotificationList withStatus(String status) {
        this.status = status;
        return this;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public NotificationList withTime(String time) {
        this.time = time;
        return this;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    public NotificationList withTimeString(String timeString) {
        this.timeString = timeString;
        return this;
    }

}
