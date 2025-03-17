package com.example.home_buddy_captain.model;

public class NewServiceManModel {
    private String name, email, password, gender, age, mobile, serviceCat, experience, charges, workingDays, sub_locality;

    public NewServiceManModel(String name, String email, String password, String gender, String age, String mobile, String serviceCat, String experience, String charges, String workingDays, String sub_locality) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.age = age;
        this.mobile = mobile;
        this.serviceCat = serviceCat;
        this.experience = experience;
        this.charges = charges;
        this.workingDays = workingDays;
        this.sub_locality = sub_locality;
    }

    public NewServiceManModel() {

    }

    public NewServiceManModel(String email, String password, String serviceCat) {
        this.email = email;
        this.password = password;
        this.serviceCat = serviceCat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getServiceCat() {
        return serviceCat;
    }

    public void setServiceCat(String serviceCat) {
        this.serviceCat = serviceCat;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getCharges() {
        return charges;
    }

    public void setCharges(String charges) {
        this.charges = charges;
    }

    public String getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(String workingDays) {
        this.workingDays = workingDays;
    }

    public String getSub_locality() {
        return sub_locality;
    }

    public void setSub_locality(String sub_locality) {
        this.sub_locality = sub_locality;
    }
}
