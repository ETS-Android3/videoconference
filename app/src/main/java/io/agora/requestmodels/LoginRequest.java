package io.agora.requestmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LoginRequest implements Serializable {

    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("imei")
    @Expose
    private String iEMI;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LoginRequest withEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LoginRequest withPassword(String password) {
        this.password = password;
        return this;
    }

    public String getIEMI() {
        return iEMI;
    }

    public void setIEMI(String iEMI) {
        this.iEMI = iEMI;
    }

    public LoginRequest withIEMI(String iEMI) {
        this.iEMI = iEMI;
        return this;
    }

}