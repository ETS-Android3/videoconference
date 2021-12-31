package io.agora.responsemodels;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotificationData {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("data")
    @Expose
    private List<NotificationList> data = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public NotificationData withStatus(String status) {
        this.status = status;
        return this;
    }

    public List<NotificationList> getData() {
        return data;
    }

    public void setData(List<NotificationList> data) {
        this.data = data;
    }

    public NotificationData withData(List<NotificationList> data) {
        this.data = data;
        return this;
    }

}