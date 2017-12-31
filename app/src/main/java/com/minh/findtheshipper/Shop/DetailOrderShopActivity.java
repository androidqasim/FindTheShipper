package com.minh.findtheshipper.Shop;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kofigyan.stateprogressbar.StateProgressBar;
import com.minh.findtheshipper.R;
import com.minh.findtheshipper.common.DialogUserInformation;
import com.minh.findtheshipper.helpers.EncodingFirebase;
import com.minh.findtheshipper.helpers.TimeAgoHelpers;
import com.minh.findtheshipper.models.OrderTemp;
import com.sdsmdg.tastytoast.TastyToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;
import io.realm.Realm;

public class DetailOrderShopActivity extends AppCompatActivity {
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
    @BindView(R.id.btnCall)
    at.markushi.ui.CircleButton btnCall;
    @BindView(R.id.btnComment)
    at.markushi.ui.CircleButton btnComment;
    @BindView(R.id.txtTime)
    TextView txtTime;
    @BindView(R.id.btnShipperInformation)
    at.markushi.ui.CircleButton btnShipperInformation;
    @BindView(R.id.stateProgressBar)
    StateProgressBar stateProgressBar;
    @BindView(R.id.spinner)
    Spinner spinner;
    @BindView(R.id.btnUpdate)
    Button btnUpdate;
    private Unbinder unbinder;
    private String key = "";
    private String userEmailFromServer = "";
    private OrderTemp order;
    private Realm realm;
    private String currentShipper;
    private int currentStateOrder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order_shop);
        unbinder = ButterKnife.bind(this);
        initRealm();
        showToolBar();
        changeColorStatusBar();
        loadDataFromServer();
        initSpinner();
       // updateStateProgressBar();

    }

    private void initSpinner(){
        btnUpdate.setVisibility(View.GONE);
        ArrayAdapter<CharSequence>adapter = ArrayAdapter.createFromResource(DetailOrderShopActivity.this,
                R.array.list_state_order, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TastyToast.makeText(DetailOrderShopActivity.this, String.valueOf(spinner.getSelectedItemPosition()), TastyToast.LENGTH_SHORT,TastyToast.CONFUSING);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                TastyToast.makeText(DetailOrderShopActivity.this, "not Changed", TastyToast.LENGTH_SHORT,TastyToast.CONFUSING);

            }
        });
    }


    private void changeColorStatusBar() {
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick({R.id.btnShipperInformation})
    public void evenClick(final View view) {
        switch (view.getId()) {
            case R.id.btnShipperInformation:
                showDialogShipperInformation();
                break;
        }
    }

    private void showDialogShipperInformation() {
        Bundle bundle = new Bundle();
        if(currentShipper != null && !currentShipper.equals("")){
            bundle.putString("userEmail", currentShipper);
            FragmentManager fragmentManager = getSupportFragmentManager();
            final DialogUserInformation dialogHelpers = new DialogUserInformation();
            dialogHelpers.show(fragmentManager, "New fragment");
            dialogHelpers.setArguments(bundle);
        }else{
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(DetailOrderShopActivity.this);
            sweetAlertDialog.setTitle(R.string.warning);
            sweetAlertDialog.setContentText(getString(R.string.shipper_not_found));
            sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismissWithAnimation();
                }
            });
        }

    }

    private void loadDataFromServer() {
        order = new OrderTemp();
        key = getIntent().getStringExtra("orderKey");
        TastyToast.makeText(this, key, TastyToast.LENGTH_SHORT, TastyToast.CONFUSING);
        final DatabaseReference orderDataBase = FirebaseDatabase.getInstance().getReference("order");
        final DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("user");

        orderDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String orderKey = key;
                userEmailFromServer = EncodingFirebase.convertToRightEmail(key);
                String status = dataSnapshot.child(orderKey).child("Status").getValue(String.class);
                String startPlace = dataSnapshot.child(orderKey).child("Start place").getValue(String.class);
                String finishPlace = dataSnapshot.child(orderKey).child("Finish place").getValue(String.class);
                String advancedMoney = dataSnapshot.child(orderKey).child("Advanced money").getValue(String.class);
                String phoneNumber = dataSnapshot.child(orderKey).child("Phone number").getValue(String.class);
                String shipMoney = dataSnapshot.child(orderKey).child("Ship Money").getValue(String.class);
                String note = dataSnapshot.child(orderKey).child("Note").getValue(String.class);
                currentShipper  = dataSnapshot.child(orderKey).child("Shipper").getValue(String.class);
                String deliveryTime = dataSnapshot.child(orderKey).child("Delivery time").getValue(String.class);
                String distance = dataSnapshot.child(orderKey).child("Distance").getValue(String.class);
                String dateTime = dataSnapshot.child(orderKey).child("Datetime").getValue(String.class);
                Boolean saveOrder = dataSnapshot.child(orderKey).child("Save Order").getValue(Boolean.class);
                String currentState = dataSnapshot.child(orderKey).child("State Order").getValue(String.class);
                String shortStartPlace = EncodingFirebase.getShortAddress(startPlace);
                String shortFinishPlace = EncodingFirebase.getShortAddress(finishPlace);

                order.setOrderID(orderKey);
                order.setStatus(status);
                order.setStartPoint(startPlace);
                order.setFinishPoint(finishPlace);
                order.setAdvancedMoney(advancedMoney);
                order.setShipMoney(shipMoney);
                order.setPhoneNumber(phoneNumber);
                order.setDistance(distance);
                order.setDateTime(dateTime);
                order.setDeliveryTime(deliveryTime);
                order.setSavedOrder(saveOrder);
                if (txtStartPlace != null && txtFinishPlace != null && txtAdvancedMoney != null && txtTime != null
                        && txtPrice != null && txtNote != null && txtDistance != null && txtTimeAgo != null) {
                    txtStartPlace.setText(shortStartPlace);
                    txtFinishPlace.setText(shortFinishPlace);
                    txtAdvancedMoney.setText(advancedMoney);
                    txtPhoneNumber.setText(phoneNumber);
                    txtPrice.setText(shipMoney);
                    txtNote.setText(note);
                    txtTime.setText(deliveryTime);
                    txtDistance.setText(distance);
                    txtTimeAgo.setText(TimeAgoHelpers.getTimeAgo(dateTime, DetailOrderShopActivity.this));
                }
                if(currentState != null){
                    currentStateOrder = Integer.parseInt(currentState);
                    String[] descriptionData = {"Find", "Confirm", "Delivery", "Done"};
                    if(stateProgressBar != null){
                        stateProgressBar.setStateDescriptionData(descriptionData);
                        if(currentState.equals("1")){
                            stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE);
                            spinner.setSelection(0, true);

                        }
                        if(currentState.equals("2")){
                            stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                            spinner.setSelection(1, true);
                        }
                        if (currentState.equals("3")){
                            stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
                            spinner.setSelection(2, true);
                        }
                        if (currentState.equals("4")){
                            stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR);
                            spinner.setSelection(3, true);
                        }
                        stateProgressBar.setAnimationDuration(3000);
                        stateProgressBar.enableAnimationToCurrentState(true);
                    }
                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(DetailOrderShopActivity.this,
                            SweetAlertDialog.CUSTOM_IMAGE_TYPE);
                    sweetAlertDialog.setTitle(getString(R.string.ask_for_accept_shipper_title));
                    sweetAlertDialog.setContentText(getString(R.string.ask_for_accept_shipper));
                    sweetAlertDialog.setCustomImage(R.drawable.ic_ask_question);
                    sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
                    sweetAlertDialog.show();
                    }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userKey = EncodingFirebase.getEmailFromUserID(key);
                String nameCreator = dataSnapshot.child(userKey).child("Name").getValue(String.class);
                String url = dataSnapshot.child(userKey).child("Avatar").getValue(String.class);
                if (nameCreator != null && url != null && txtUserName != null && imgUserImage != null) {
                    txtUserName.setText(nameCreator);
                    Glide.with(DetailOrderShopActivity.this).load(EncodingFirebase.decodeString(url))
                            .apply(RequestOptions.circleCropTransform()).thumbnail(0.7f).into(imgUserImage);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initRealm() {
        Realm.init(this);
        realm = null;
        realm = Realm.getDefaultInstance();
    }

}
