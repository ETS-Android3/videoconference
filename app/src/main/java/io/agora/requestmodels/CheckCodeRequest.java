package io.agora.requestmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CheckCodeRequest implements Serializable {

    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("imei")
    @Expose
    private String iEMI;
    @SerializedName("company_id")
    @Expose
    private String companyId;
    @SerializedName("device_token")
    @Expose
    private String deviceToken;
    @SerializedName("new_name")
    @Expose
    private String newName;
    @SerializedName("setting")
    @Expose
    private String setting;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public CheckCodeRequest withCode(String code) {
        this.code = code;
        return this;
    }

    public String getIEMI() {
        return iEMI;
    }

    public void setIEMI(String iEMI) {
        this.iEMI = iEMI;
    }

    public CheckCodeRequest withIEMI(String iEMI) {
        this.iEMI = iEMI;
        return this;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String comapanyId) {
        this.companyId = comapanyId;
    }

    public CheckCodeRequest withCompanyId(String comapanyId) {
        this.companyId = comapanyId;
        return this;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public CheckCodeRequest withDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
        return this;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public CheckCodeRequest withNewName(String newName) {
        this.newName = newName;
        return this;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public CheckCodeRequest withSetting(String setting) {
        this.setting = setting;
        return this;
    }
}