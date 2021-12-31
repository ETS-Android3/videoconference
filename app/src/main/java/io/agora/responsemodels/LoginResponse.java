package io.agora.responsemodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LoginResponse implements Serializable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("company_name")
    @Expose
    private String companyName;
    @SerializedName("company_email")
    @Expose
    private String companyEmail;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("company_address")
    @Expose
    private String companyAddress;
    @SerializedName("contact_person")
    @Expose
    private String contactPerson;
    @SerializedName("contact_number")
    @Expose
    private String contactNumber;
    @SerializedName("noofdevices")
    @Expose
    private String noofdevices;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("created_on")
    @Expose
    private String createdOn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LoginResponse withId(String id) {
        this.id = id;
        return this;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public LoginResponse withCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public LoginResponse withCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LoginResponse withStatus(String status) {
        this.status = status;
        return this;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public LoginResponse withCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
        return this;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public LoginResponse withContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
        return this;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public LoginResponse withContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
        return this;
    }

    public String getNoofdevices() {
        return noofdevices;
    }

    public void setNoofdevices(String noofdevices) {
        this.noofdevices = noofdevices;
    }

    public LoginResponse withNoofdevices(String noofdevices) {
        this.noofdevices = noofdevices;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LoginResponse withPassword(String password) {
        this.password = password;
        return this;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public LoginResponse withCreatedOn(String createdOn) {
        this.createdOn = createdOn;
        return this;
    }

}
