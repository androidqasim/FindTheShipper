package com.minh.findtheshipper.realm;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;

import com.minh.findtheshipper.models.Order;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by trinh on 6/13/2017.
 */

public class RealmController {
    public static RealmController instance;
    private static Realm realm;

    public RealmController(Application application) {
        realm = Realm.getDefaultInstance();
    }

    public static RealmController with(Fragment fragment) {
        if (instance == null) {
            instance = new RealmController(fragment.getActivity().getApplication());
        }
        return instance;
    }

    public static RealmController with(Activity activity) {
        if (instance == null) {
            instance = new RealmController(activity.getApplication());
        }
        return instance;
    }

    public static RealmController with(Application application) {
        if (instance == null) {
            instance = new RealmController(application);
        }
        return instance;
    }

    public static RealmController getInstance() {
        return instance;
    }

    public static void setInstance(RealmController instance) {
        RealmController.instance = instance;
    }

    public static Realm getRealm() {
        return realm;
    }

    public static void setRealm(Realm realm) {
        RealmController.realm = realm;
    }

    public void refresh() {
        realm.refresh();
    }

    public RealmResults<Order> getOrder() {
        return realm.where(Order.class).findAll();
    }

    public Order getOrder(String orderID) {
        return realm.where(Order.class).equalTo("orderID", orderID).findFirst();
    }

    public boolean hasOrder() {
        return !realm.isEmpty();
    }

    public void insertOrder()
    {
        realm.beginTransaction();
        Order order = realm.createObject(Order.class);


    }
}
