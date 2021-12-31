package io.agora.responsemodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RoomRequest implements Serializable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("device_name")
    @Expose
    private String deviceName;
    @SerializedName("app_id")
    @Expose
    private String appId;
    @SerializedName("app_token")
    @Expose
    private String appToken;
    @SerializedName("availability")
    @Expose
    private String availability;
    @SerializedName("active_user_count")
    @Expose
    private String activeUserCount;
    @SerializedName("host_imei")
    @Expose
    private String hostImei;
    @SerializedName("device_description")
    @Expose
    private String deviceDescription;
    @SerializedName("device_approve")
    @Expose
    private String deviceApprove;



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RoomRequest withId(String id) {
        this.id = id;
        return this;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public RoomRequest withDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public RoomRequest withAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public String getAppToken() {
        return appToken;
    }

    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    public RoomRequest withAppToken(String appToken) {
        this.appToken = appToken;
        return this;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public RoomRequest withAvailability(String availability) {
        this.availability = availability;
        return this;
    }

    public String getActiveUserCount() {
        return activeUserCount;
    }

    public void setActiveUserCount(String activeUserCount) {
        this.activeUserCount = activeUserCount;
    }

    public RoomRequest withActiveUserCount(String activeUserCount) {
        this.activeUserCount = activeUserCount;
        return this;
    }

    public String getHostImei() {
        return hostImei;
    }

    public void setHostImei(String hostImei) {
        this.hostImei = hostImei;
    }

    public RoomRequest withHostImei(String hostImei) {
        this.hostImei = hostImei;
        return this;
    }

    public String getDeviceDescription() {
        return deviceDescription;
    }

    public void setDeviceDescription(String deviceDescription) {
        this.deviceDescription = deviceDescription;
    }

    public RoomRequest withDeviceDescription(String deviceDescription) {
        this.deviceDescription = deviceDescription;
        return this;
    }

    public String getDeviceApprove() {
        return deviceApprove;
    }

    public void setDeviceApprove(String deviceApprove) {
        this.deviceApprove = deviceApprove;
    }

    public RoomRequest withDeviceApprove(String deviceDescription) {
        this.deviceDescription = deviceDescription;
        return this;
    }

}