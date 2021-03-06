package com.minh.findtheshipper.models.RealmObject;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Minh on 6/1/2017.
 * This user is in database Realm
 * This user including List orders and list orders saved
 */

public class User extends RealmObject {
    @PrimaryKey
    private String email;
    private String fullName;
    private String phoneNumber;
    private int avatar;
    private RealmList<Order> orderArrayList;
    private RealmList<Order> orderListSave;


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public RealmList<Order> getOrderArrayList() {
        return orderArrayList;
    }

    public void setOrderArrayList(RealmList<Order> orderArrayList) {
        this.orderArrayList = orderArrayList;
    }

    public RealmList<Order> getOrderListSave() {
        return orderListSave;
    }

    public void setOrderListSave(RealmList<Order> orderListSave) {
        this.orderListSave = orderListSave;
    }
}
