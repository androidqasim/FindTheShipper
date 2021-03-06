package com.minh.findtheshipper.helpers;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * To decode from server
 * Created by trinh on 7/11/2017.
 */

public class EncodingFirebase {


    public EncodingFirebase() {
    }

    public static String encodeString(String string) {
        return string.replace(".", ",");
    }

    public static String decodeString(String string) {
        return string.replace(",", ".");
    }

    public static String getShortAddress(String string) {
        String result;
        String[] arrayString = string.split(",");
        if (arrayString.length > 2) {
            result = arrayString[0] + ", " + arrayString[1] + ", " + arrayString[2] + ".";
        } else {
            result = string;
        }
        return result;
    }

    public static String getEmailFromUserID(String result) {
        String[] split = result.split("_");
        return split[1];
    }

    public static String convertToRightEmail(String email) {
        email = getEmailFromUserID(email);
        return email.replaceAll(",", ".");
    }

    public static String getCompleteAddressString(Context context, double latitude, double longitude) {
        String fullAddress = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Address address = addresses.get(0);
            StringBuilder stringBuilder = new StringBuilder("");
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                stringBuilder.append(address.getAddressLine(i)).append(",");
            }
            stringBuilder.append(address.getAddressLine(address.getMaxAddressLineIndex())).append(".");
            fullAddress = stringBuilder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return fullAddress;
    }

    public static LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;
        try {
            address = coder.getFromLocationName(strAddress, 3);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng((location.getLatitude()), (location.getLongitude()));

            return p1;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return p1;
    }

    public static Calendar convertDateToCalendar(String inputDate) throws ParseException {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy-HH:mm", Locale.getDefault());
        Date date = df.parse(inputDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        return calendar;
    }

    public static String getNameOfUser(final String emailAddress){
        final String[] username = {""};
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user/" + emailAddress);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username[0] = dataSnapshot.child("user").child(emailAddress).child("Name").getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return username[0];
    }



}
