
package io.agora.requestmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DeviceSettingModel implements Serializable {

    @SerializedName("imei")
    @Expose
    private String iEMI;
    @SerializedName("setting")
    @Expose
    private String setting;

    public String getIEMI() {
        return iEMI;
    }

    public void setIEMI(String iEMI) {
        this.iEMI = iEMI;
    }

    public DeviceSettingModel withIEMI(String iEMI) {
        this.iEMI = iEMI;
        return this;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public DeviceSettingModel withSetting(String setting) {
        this.setting = setting;
        return this;
    }
}