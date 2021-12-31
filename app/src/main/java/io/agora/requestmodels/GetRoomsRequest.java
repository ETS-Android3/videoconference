package io.agora.requestmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GetRoomsRequest implements Serializable {

    @SerializedName("imei")
    @Expose
    private String iMei;
    @SerializedName("availability")
    @Expose
    private String availability;
    @SerializedName("device_id")
    @Expose
    private String deviceId;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("channel_name")
    @Expose
    private String channelName;
    @SerializedName("access_token")
    @Expose
    private String appToken;
    @SerializedName("device_ids")
    @Expose
    private String deviceIds;


    public String getiMei() {
        return iMei;
    }

    public void setiMei(String imei) {
        this.iMei = imei;
    }

    public GetRoomsRequest withIEMI(String imei) {
        this.iMei = imei;
        return this;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public GetRoomsRequest withAvailability(String availability) {
        this.availability = availability;
        return this;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


    public GetRoomsRequest withDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GetRoomsRequest withSetType(String type) {
        this.type = type;
        return this;
    }


    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public GetRoomsRequest withChannelName(String channelName) {
        this.channelName = channelName;
        return this;
    }


    public String getAppToken() {
        return appToken;
    }

    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    public GetRoomsRequest withAppToken(String appToken) {
        this.appToken = appToken;
        return this;
    }

    public String getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(String deviceIds) {
        this.deviceIds = deviceIds;
    }

    public GetRoomsRequest withDeviceIds(String deviceIds) {
        this.deviceIds = deviceIds;
        return this;
    }

}