package com.minh.findtheshipper.Shipper;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.minh.findtheshipper.R;
import com.minh.findtheshipper.helpers.CommentDialogHelpers;
import com.minh.findtheshipper.helpers.DirectionHelpers;
import com.minh.findtheshipper.helpers.EncodingFireBase;
import com.minh.findtheshipper.helpers.TimeAgoHelpers;
import com.minh.findtheshipper.helpers.listeners.DirectionFinderListeners;
import com.minh.findtheshipper.models.CurrentUser;
import com.minh.findtheshipper.models.Order;
import com.minh.findtheshipper.models.OrderTemp;
import com.minh.findtheshipper.models.Route;
import com.minh.findtheshipper.models.User;
import com.minh.findtheshipper.utils.AnimationUtils;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;
import io.realm.Realm;
import io.realm.RealmList;

public class DetailOrderShipperActivity extends AppCompatActivity implements OnMapReadyCallback,
        DirectionFinderListeners {
    @BindView(R.id.toolBar)
    Toolbar toolbar;
    @BindView(R.id.imgUserImage)
    ImageView imgUserImage;
    @BindView(R.id.txtUserName)
    TextView txtUserName;
    @BindView(R.id.txtTimeAgo)
    TextView txtTimeAgo;
    @BindView(R.id.txtPrice)
    TextView txtPrice;
    @BindView(R.id.txtStartPlace)
    TextView txtStartPlace;
    @BindView(R.id.txtFinishPlace)
    TextView txtFinishPlace;
    @BindView(R.id.txtPhoneNumber)
    TextView txtPhoneNumber;
    @BindView(R.id.txtNote)
    TextView txtNote;
    @BindView(R.id.txtAdvancedMoney)
    TextView txtAdvancedMoney;
    @BindView(R.id.txtDistance)
    TextView txtDistance;
    @BindView(R.id.btnGetOrder)
    Button btnGetOrder;
    @BindView(R.id.btnCall)
    at.markushi.ui.CircleButton btnCall;
    @BindView(R.id.btnComment)
    at.markushi.ui.CircleButton btnComment;
    @BindView(R.id.txtTime)
    TextView txtTime;
    @BindView(R.id.btnSave)
    at.markushi.ui.CircleButton btnSave;
    @BindView(R.id.btnDelete)
    at.markushi.ui.CircleButton btnDelete;
    @BindView(R.id.tvSaveOrDelete)
    TextView tvSaveOrDelete;
    private Unbinder unbinder;
    private String key = "";
    private List<Marker> startMarkers = new ArrayList<>();
    private List<Marker> finishMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private OrderTemp order;
    private Realm realm;
    private GoogleMap mMap;
    private String[] orderKey = new String[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order_shipper);
        unbinder = ButterKnife.bind(this);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        showToolBar();
        changeColorStatusBar();
        initRealm();
        loadDataFromServer();
        showSaveOrDeleteButton();


    }

    private void showSaveOrDeleteButton() {
        if (orderKey[1].equals("History")) {
            btnDelete.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);
            tvSaveOrDelete.setText(getString(R.string.delete));
        }
        if (orderKey[1].equals("Present")) {
            btnDelete.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
            tvSaveOrDelete.setText(getString(R.string.save));
        }
    }

    public void initRealm() {
        Realm.init(this);
        realm = null;
        realm = Realm.getDefaultInstance();
    }

    @OnClick({R.id.btnGetOrder, R.id.btnCall, R.id.btnComment, R.id.btnSave, R.id.btnDelete})
    public void eventClick(final View v) {
        switch (v.getId()) {
            case R.id.btnGetOrder:
                TastyToast.makeText(v.getContext(), "Clicked here!", TastyToast.LENGTH_LONG, TastyToast.INFO);
                break;
            case R.id.btnSave:
                final Order orderRealm = realm.where(Order.class).equalTo("orderID", order.getOrderID()).findFirst();
                if (orderRealm == null)//Check it available in database
                {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            final Order orderToRealm = realm.createObject(Order.class, order.getOrderID());
                            orderToRealm.setStatus(order.getStatus());
                            orderToRealm.setStartPoint(order.getStartPoint());
                            orderToRealm.setFinishPoint(order.getFinishPoint());
                            orderToRealm.setAdvancedMoney(order.getAdvancedMoney());
                            orderToRealm.setShipMoney(order.getShipMoney());
                            orderToRealm.setNote(order.getNote());
                            orderToRealm.setSaveOrder(order.getSavedOrder());
                            orderToRealm.setPhoneNumber(order.getPhoneNumber());
                            realm.insertOrUpdate(orderToRealm);
                            getCurrentUser().getOrderListSave().add(orderToRealm);
                            realm.insertOrUpdate(getCurrentUser());
                            if (!orderToRealm.getSaveOrder()) //Check order is false exists before handle
                            {
                                AnimationUtils.animateItemView(v);
                                //Check it already exists in list order save of user
                                boolean checkAlready = false;
                                User user = getCurrentUser();
                                RealmList<Order> orders = user.getOrderListSave();
                                for (int i = 0; i < orders.size(); i++) {
                                    if (orders.get(i).getOrderID().equals(orderToRealm.getOrderID())) {
                                        checkAlready = true;
                                    }
                                }
                                if (!checkAlready) {
                                    orderToRealm.setSaveOrder(true);
                                    realm.insertOrUpdate(orderToRealm);
                                    user.getOrderListSave().add(orderToRealm);
                                    realm.insertOrUpdate(user);
                                    AnimationUtils.animateItemView(v);
                                    TastyToast.makeText(v.getContext(), v.getResources().getString(R.string.save_order), TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);

                                }
                                if (checkAlready) {
                                    orderToRealm.setSaveOrder(true);
                                    realm.insertOrUpdate(orderToRealm);
                                    TastyToast.makeText(v.getContext(), v.getResources().getString(R.string.save_order), TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);

                                }
                            } else {
                                AnimationUtils.animateItemView(v);
                                TastyToast.makeText(v.getContext(), v.getResources().getString(R.string.save_order_exists), TastyToast.LENGTH_SHORT, TastyToast.WARNING);
                            }
                        }
                    });

                } else {
                    if (!orderRealm.getSaveOrder()) //Check order is false exists before handle
                    {
                        AnimationUtils.animateItemView(v);
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                //Check it already exists in list order save of user
                                boolean checkAlready = false;
                                User user = getCurrentUser();
                                RealmList<Order> orders = user.getOrderListSave();
                                for (int i = 0; i < orders.size(); i++) {
                                    if (orders.get(i).getOrderID().equals(order.getOrderID())) {
                                        checkAlready = true;
                                    }
                                }
                                if (!checkAlready) {
                                    //Set it again, to make sure it not null
                                    orderRealm.setSaveOrder(true);
                                    orderRealm.setShipMoney(order.getShipMoney());
                                    orderRealm.setDistance(order.getDistance());
                                    realm.insertOrUpdate(orderRealm);
                                    user.getOrderListSave().add(orderRealm);
                                    realm.insertOrUpdate(user);
                                    AnimationUtils.animateItemView(v);
                                    TastyToast.makeText(v.getContext(), v.getResources().getString(R.string.save_order), TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);

                                }
                                if (checkAlready) {
                                    //Set it again, to make sure it not null
                                    orderRealm.setSaveOrder(true);
                                    orderRealm.setShipMoney(order.getShipMoney());
                                    orderRealm.setDistance(order.getDistance());
                                    realm.insertOrUpdate(orderRealm);
                                    TastyToast.makeText(v.getContext(), v.getResources().getString(R.string.save_order), TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                }
                            }
                        });
                    } else {
                        AnimationUtils.animateItemView(v);
                        TastyToast.makeText(v.getContext(), v.getResources().getString(R.string.save_order_exists), TastyToast.LENGTH_SHORT, TastyToast.WARNING);
                    }
                }


                break;

            case R.id.btnCall:
                String tempPhone = order.getPhoneNumber().replaceAll("[^0-9]+", " ");
                List<String> phoneNumber = Arrays.asList(tempPhone.trim().split(" "));
                Intent intentPhone = new Intent(Intent.ACTION_CALL);
                intentPhone.setData(Uri.parse("tel:" + phoneNumber.get(0)));
                if (ActivityCompat.checkSelfPermission(v.getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                v.getContext().startActivity(intentPhone);
                break;
            case R.id.btnComment:
                Bundle bundle = new Bundle();
                bundle.putString("orderID", order.getOrderID());
                FragmentManager fragmentManager = getSupportFragmentManager();
                final CommentDialogHelpers dialogHelpers = new CommentDialogHelpers();
                dialogHelpers.show(fragmentManager, "New fragment");
                dialogHelpers.setArguments(bundle);
                break;
            case R.id.btnDelete:
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
                sweetAlertDialog.setTitleText(getString(R.string.are_you_sure_title));
                sweetAlertDialog.setContentText(getString(R.string.are_you_sure_content));
                sweetAlertDialog.setConfirmText(getString(R.string.ok));
                sweetAlertDialog.setCancelText(getString(R.string.cancel));
                sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                });

                sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                final Order orderRealm = realm.where(Order.class).equalTo("orderID", order.getOrderID()).findFirst();
                                orderRealm.setSaveOrder(false);
                                User user = getCurrentUser();
                                user.getOrderListSave().remove(orderRealm);
                                realm.insertOrUpdate(user);
                                onBackPressed();

                            }
                        });
                    }
                });
                sweetAlertDialog.show();

                break;
        }

    }


    private User getCurrentUser() {
        CurrentUser currentUser = realm.where(CurrentUser.class).findFirst();
        return realm.where(User.class).beginGroup().equalTo("email", currentUser.getEmail()).endGroup().findFirst();
    }

    private void showToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void changeColorStatusBar() {
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

    }

    private void loadDataFromServer() {
        order = new OrderTemp();
        orderKey = getIntent().getStringArrayExtra("orderKey");
        key = orderKey[0];
        final DatabaseReference orderDataBase = FirebaseDatabase.getInstance().getReference("order");
        final DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("user");
        final EncodingFireBase encodingFireBase = new EncodingFireBase();

        orderDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String orderKey = key;
                TimeAgoHelpers timeAgoHelpers = new TimeAgoHelpers();
                String status = dataSnapshot.child(orderKey).child("Status").getValue(String.class);
                String startPlace = dataSnapshot.child(orderKey).child("Start place").getValue(String.class);
                String finishPlace = dataSnapshot.child(orderKey).child("Finish place").getValue(String.class);
                String advancedMoney = dataSnapshot.child(orderKey).child("Advanced money").getValue(String.class);
                String phoneNumber = dataSnapshot.child(orderKey).child("Phone number").getValue(String.class);
                String shipMoney = dataSnapshot.child(orderKey).child("Ship Money").getValue(String.class);
                String note = dataSnapshot.child(orderKey).child("Note").getValue(String.class);
                String distance = dataSnapshot.child(orderKey).child("Distance").getValue(String.class);
                String dateTime = dataSnapshot.child(orderKey).child("Datetime").getValue(String.class);
                Boolean saveOrder = dataSnapshot.child(orderKey).child("Save Order").getValue(Boolean.class);
                String shortStartPlace = EncodingFireBase.getShortAddress(startPlace);
                String shortFinishPlace = EncodingFireBase.getShortAddress(finishPlace);

                order.setOrderID(orderKey);
                order.setStatus(status);
                order.setStartPoint(startPlace);
                order.setFinishPoint(finishPlace);
                order.setAdvancedMoney(advancedMoney);
                order.setShipMoney(shipMoney);
                order.setPhoneNumber(phoneNumber);
                order.setDistance(distance);
                order.setDateTime(dateTime);
                order.setSavedOrder(saveOrder);

                txtStartPlace.setText(shortStartPlace);
                txtFinishPlace.setText(shortFinishPlace);
                txtAdvancedMoney.setText(advancedMoney);
                txtPhoneNumber.setText(phoneNumber);
                txtPrice.setText(shipMoney);
                txtNote.setText(note);
                txtDistance.setText(distance);
                txtTimeAgo.setText(timeAgoHelpers.getTimeAgo(dateTime, DetailOrderShipperActivity.this));
                showPathOnMap(startPlace, finishPlace);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userKey = encodingFireBase.getEmailFromUserID(key);
                String nameCreator = dataSnapshot.child(userKey).child("Name").getValue(String.class);
                String url = dataSnapshot.child(userKey).child("Avatar").getValue(String.class);
                txtUserName.setText(nameCreator);
                Glide.with(DetailOrderShipperActivity.this).load(encodingFireBase.decodeString(url)).apply(RequestOptions.circleCropTransform()).thumbnail(0.7f).into(imgUserImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private void showPathOnMap(String startPlace, String finishPlace) {
        try {
            new DirectionHelpers(this, startPlace, finishPlace).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng HCMCity = new LatLng(10.766333, 106.694036);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HCMCity, 18));


    }

    @Override
    public void onDirectionFinderStart() {
        if (startMarkers != null) {
            for (Marker marker : startMarkers) {
                marker.remove();
            }
        }
        if (finishMarkers != null) {
            for (Marker marker : finishMarkers) {
                marker.remove();
            }
        }
        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        polylinePaths = new ArrayList<>();
        startMarkers = new ArrayList<>();
        finishMarkers = new ArrayList<>();
        for (Route route : routes) {
            // Start Zoom path fit with maps
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(route.startLocation);
            builder.include(route.endLocation);
            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
            //End Zoom path fit with maps
            txtDistance.setText(route.distance.text);
            txtTime.setText(route.duration.text);
            startMarkers.add(mMap.addMarker(new MarkerOptions().title(route.startAddress)
                    .position(route.startLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_place))));
            finishMarkers.add(mMap.addMarker(new MarkerOptions().title(route.endAddress)
                    .position(route.endLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination))));
            PolylineOptions polylineOptions = new PolylineOptions().geodesic(true).color(Color.GREEN).width(12);
            for (int i = 0; i < route.points.size(); i++) {
                polylineOptions.add(route.points.get(i));
            }
            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

}
