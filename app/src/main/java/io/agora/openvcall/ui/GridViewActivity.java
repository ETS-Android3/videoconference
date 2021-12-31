package io.agora.openvcall.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;
import com.vikramezhil.droidspeech.OnDSPermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.agora.adapter.GridViewAdapter;
import io.agora.api.APIClient;
import io.agora.api.APIInterface;
import io.agora.openvcall.R;
import io.agora.openvcall.model.ConstantApp;
import io.agora.propeller.Constant;
import io.agora.requestmodels.CheckCodeRequest;
import io.agora.requestmodels.GetRoomsRequest;
import io.agora.responsemodels.RoomRequest;
import io.agora.util.ManageSession;
import io.agora.util.Utils;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GridViewActivity extends BaseActivity implements GridViewAdapter.Listener, OnDSListener, OnDSPermissionsListener {


    //public static boolean IS_SCREEN_SAVER_RUNNING = true;
    private static final String LOG_TAG = GridViewActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_CODE = 5;
    private long TIME_IN_MLS;


    private EditText v_channel;
    private RecyclerView rvRooms;
    private SwipeRefreshLayout swipeToRefresh;
    private ArrayList<RoomRequest> mRoomRequestList = new ArrayList<>();
    private GridViewAdapter roomsAdapter;
    private View main;
    private Context context;
    private AlertDialog dialogActivation;
    private AlertDialog inviteDialog;
    private static final String TAG = "GridViewActivity";
    String token;
    ProgressDialog dialog;
    String socketUserCount, socketDeviceId;
    CountDownTimer countDownTimer;
    TextView txtTitle;
    ImageView micBtn;
    private DroidSpeech droidSpeech;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.grid_activity_main);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

        context = this;
        dialog = new ProgressDialog(this);
        main = findViewById(R.id.main);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ab.setCustomView(R.layout.ard_agora_actionbar_with_title);
        }

        token = getIntent().getStringExtra("token");

        System.out.println("token===" + token);


        v_channel = findViewById(R.id.etChannelName);
        rvRooms = findViewById(R.id.rvRooms);
        txtTitle = findViewById(R.id.txt_title);
        setFont(txtTitle);
        micBtn = findViewById(R.id.mic_view);


        TextView txtAllRooms = findViewById(R.id.txt_all_rooms);
        setFont(txtAllRooms);
        swipeToRefresh = findViewById(R.id.swipeToRefresh);
        swipeToRefresh.setColorSchemeResources(R.color.colorAccent);

        dialog.setMessage("sending your request, please wait...");

        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRoomRequestList.clear();
                getRooms(IMEI);
                swipeToRefresh.setRefreshing(false);
            }
        });

        roomsAdapter = new GridViewAdapter(context, mRoomRequestList, this);
        if (ManageSession.getPreference(context, ManageSession.GRID_TYPE).equals("3X2")) {
            rvRooms.setLayoutManager(new GridLayoutManager(this, 3));
        } else if (ManageSession.getPreference(context, ManageSession.GRID_TYPE).equals("4X2")) {
            rvRooms.setLayoutManager(new GridLayoutManager(this, 4));
        } else {
            rvRooms.setLayoutManager(new GridLayoutManager(this, 3));
        }

        rvRooms.setAdapter(roomsAdapter);

        if (getIntent().getStringExtra("messageBody") != null) {
            String messageBody = getIntent().getStringExtra("messageBody");
            String requestedIMEI = getIntent().getStringExtra("requestedIMEI");
            String channelName = getIntent().getStringExtra("channel_name");
            String accessToken = getIntent().getStringExtra("access_token");
            String deviceId = getIntent().getStringExtra("device_id");
            System.out.println("invite RequestedIMEI===" + requestedIMEI);
            if (ManageSession.getPreference(context, ManageSession.PICK_UP_SETTINGS).equals(getString(R.string.auto))) {
                inviteAccept(IMEI, deviceId, channelName, accessToken, "invite_accept");
            } else {
                showInviteDialog(requestedIMEI, channelName, accessToken, deviceId, messageBody);
            }
        }

        // showProgress();
        dialog.setMessage("please wait...");
        dialog.show();
        updateDeviceToken(token);
        if (!ManageSession.getBooleanPreference(context, ManageSession.CODE_ACTIVATED)) {
            showActivationDialog();
        } else if (!IMEI.isEmpty()) {
            mRoomRequestList.clear();
            getRooms(IMEI);
        }


        if (droidSpeech != null) {
            droidSpeech = new DroidSpeech(this, null);
            droidSpeech.setOnDroidSpeechListener(this);
            droidSpeech.setOneStepResultVerify(true);
            droidSpeech.startDroidSpeechRecognition();
        }

        //if (mSocket.connected()) {
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
            }

        }).

                on("devices_update", new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {

                        if (args != null) {

                            JSONArray jsonArray = null;
                            try {
                                jsonArray = new JSONArray(args);

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                                    socketUserCount = jsonObj.getString("socket_user_count");
                                    socketDeviceId = jsonObj.getString("socket_device_id");
                                }
                                for (int j = 0; j < mRoomRequestList.size(); j++) {
                                    if (mRoomRequestList.get(j).getId().equals(socketDeviceId)) {
                                        mRoomRequestList.get(j).setActiveUserCount(socketUserCount);
                                    }
                                }
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        roomsAdapter.notifyDataSetChanged();
                                    }
                                });


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).


                on("reload_devices", new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {

                        if (args != null) {
                            mRoomRequestList.clear();
                            getRooms(IMEI);

                        }
                    }
                }).

                on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        System.out.println("args disconnect -----" + args);

                    }
                });
    }


    private void updateDeviceToken(String token) {

        System.out.println("token in update device====" + token);

        CheckCodeRequest request = new CheckCodeRequest()
                .withIEMI(IMEI)
                .withDeviceToken(token);


        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.updateToken(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

//                // dismissProgress();
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (response.code() == Constant.SUCCESS_CODE) {

                    String str = Utils.getParsedString(response.body().byteStream());

                    try {
                        JSONObject object = new JSONObject(str);
                        JSONArray data = object.optJSONArray(Constant.DATA);

                        if (object.getString(Constant.REPLY_STATUS).equals(Constant.SUCCESS)) {

                            System.out.println("token updated success===");

                        } else if (object.getString(Constant.REPLY_STATUS).equals(Constant.FAIL)) {

                            //  showSnackBar(main, object.getString(Constant.REPLY_MESSAGE));

                        } else {
                            showSnackBar(main, getString(R.string.some_error));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showSnackBar(main, getString(R.string.some_error));
                    }
                } else {
                    showSnackBar(main, getString(R.string.some_error));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissProgress();
            }
        });
    }


    @Override
    protected void initUIandEvent() {
        ((TextView) findViewById(R.id.ovc_page_title)).setText(R.string.video_conference);
        ((TextView) findViewById(R.id.ovc_page_title)).setTextSize(20);


        EditText v_channel = findViewById(R.id.etChannelName);
        v_channel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isEmpty = TextUtils.isEmpty(s.toString());
                findViewById(R.id.btnConnect).setEnabled(!isEmpty);
            }
        });

        Spinner encryptionSpinner = (Spinner) findViewById(R.id.encryption_mode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.encryption_mode_values, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        encryptionSpinner.setAdapter(adapter);

        encryptionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vSettings().mEncryptionModeIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        encryptionSpinner.setSelection(vSettings().mEncryptionModeIndex);

        String lastChannelName = vSettings().mChannelName;
        if (!TextUtils.isEmpty(lastChannelName)) {
            v_channel.setText(lastChannelName);
            v_channel.setSelection(lastChannelName.length());
        }

        EditText v_encryption_key = (EditText) findViewById(R.id.encryption_key);
        String lastEncryptionKey = vSettings().mEncryptionKey;
        if (!TextUtils.isEmpty(lastEncryptionKey)) {
            v_encryption_key.setText(lastEncryptionKey);
        }
    }

    @Override
    protected void deInitUIandEvent() {
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                forwardToSettings();
                return true;
            case R.id.action_notification:
                Intent i = new Intent(this, NotificationActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onClick(int position) {
        String deviceId = mRoomRequestList.get(position).getId();
        String accessToken = mRoomRequestList.get(position).getAppToken();
        String channel = mRoomRequestList.get(position).getDeviceName();
        if (Integer.parseInt(mRoomRequestList.get(position).getActiveUserCount()) >= 2 && !IMEI.equals(mRoomRequestList.get(position).getHostImei())) {
            // if (!accessToken.isEmpty()) {

            dialog.setMessage("sending your request, please wait...");
            dialog.show();
            requestToJoin(IMEI, deviceId, "request", channel, accessToken);
            //}
        } else {
            // if (!accessToken.isEmpty()) {
            forwardToRoom(channel, accessToken, deviceId);
            // }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction().equals(Constant.INVITE_SENT)) {
            String messageBody = intent.getStringExtra("messageBody");
            String requestedIMEI = intent.getStringExtra("requestedIMEI");
            String channelName = intent.getStringExtra("channel_name");
            String accessToken = intent.getStringExtra("access_token");
            String deviceId = intent.getStringExtra("device_id");
            System.out.println("invite RequestedIMEI===" + requestedIMEI);
            if (ManageSession.getPreference(context, ManageSession.PICK_UP_SETTINGS).equals(getString(R.string.auto))) {
                inviteAccept(IMEI, deviceId, channelName, accessToken, "invite_accept");
            } else {
                showInviteDialog(requestedIMEI, channelName, accessToken, deviceId, messageBody);
            }
        }
    }

    /*code for open invite dialog*/
    private void showInviteDialog(String requestedIMEI, String channelName, String
            accessToken, String deviceId, String messageBody) {
        Rect displayRectangle = new Rect();
        Window window = GridViewActivity.this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        inviteDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.call_request_popup_layout, null);
        dialogView.setMinimumWidth((int) (displayRectangle.width() * 1f));
        dialogView.setMinimumHeight((int) (displayRectangle.height() * 1f));
        inviteDialog.setView(dialogView);
        TextView tvMessage = dialogView.findViewById(R.id.tv_message);
        ImageView imgAccept = dialogView.findViewById(R.id.img_accept);
        ImageView imgReject = dialogView.findViewById(R.id.img_reject);

        TextView btnActionYes = dialogView.findViewById(R.id.btn_action_yes);
        setFont(btnActionYes);
        btnActionYes.setText("Invite Accept");
        TextView btnActionNo = dialogView.findViewById(R.id.btn_action_no);
        setFont(btnActionNo);
        btnActionNo.setText("Invite Reject");
        setFont(tvMessage);
        ImageView btnActionCancel = dialogView.findViewById(R.id.btn_action_cancel);
        tvMessage.setText(messageBody);
        inviteDialog.setCancelable(false);

        imgAccept.setOnClickListener(v -> {

            inviteAccept(IMEI, deviceId, channelName, accessToken, "invite_accept");
            inviteDialog.dismiss();
        });

        imgReject.setOnClickListener(v -> {

            inviteAccept(IMEI, deviceId, channelName, accessToken, "invite_cancel");
            inviteDialog.dismiss();

        });

        btnActionYes.setOnClickListener(v -> {

            inviteAccept(IMEI, deviceId, channelName, accessToken, "invite_accept");
            inviteDialog.dismiss();
        });

        btnActionNo.setOnClickListener(v -> {

            inviteAccept(IMEI, deviceId, channelName, accessToken, "invite_cancel");
            inviteDialog.dismiss();

        });

        btnActionCancel.setOnClickListener(v -> {
            inviteDialog.dismiss();
        });

        inviteDialog.setView(dialogView);
        inviteDialog.show();

    }

    private void requestToJoin(String imei, String deviceId, String type, String
            channel, String accessToken) {

        GetRoomsRequest request = new GetRoomsRequest().withIEMI(imei).withDeviceId(deviceId).withSetType(type)
                .withChannelName(channel).withAppToken(accessToken);

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.sendNotification(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (response.code() == Constant.SUCCESS_CODE) {
                    showSnackBar(main, getString(R.string.request_sent_successfully));
                } else {
                    showSnackBar(main, getString(R.string.some_error));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissProgress();
            }
        });

    }


    public void forwardToRoom(String channel, String accessToken, String deviceId) {

        // if (!accessToken.isEmpty()) {

        vSettings().mChannelName = channel;

        EditText v_encryption_key = (EditText) findViewById(R.id.encryption_key);
        String encryption = v_encryption_key.getText().toString();
        vSettings().mEncryptionKey = encryption;

        Intent i = new Intent(GridViewActivity.this, CallActivity.class);
        i.putExtra(ConstantApp.DEVICE_ID, deviceId);
        i.putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME, channel);
        i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_KEY, encryption);
        i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_MODE, getResources().getStringArray(R.array.encryption_mode_values)[vSettings().mEncryptionModeIndex]);
        i.putExtra(ConstantApp.ACTION_KEY_TOKEN, accessToken);
        startActivity(i);

        //}
    }

    public void forwardToSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    public void onClickDoNetworkTest(View view) {
        Intent i = new Intent(GridViewActivity.this, NetworkTestActivity.class);
        startActivity(i);
    }

    @Override
    public void workerThreadReady() {

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (countDownTimer != null) {
            countDownTimer.start();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (droidSpeech != null) {
            droidSpeech.closeDroidSpeechOperations();
        }
    }

    @Override
    protected void onStop() {
        Log.i(LOG_TAG, "stop");
        super.onStop();
//        if(countDownTimer!=null) {
//            countDownTimer.cancel();
//        }


        if (droidSpeech != null) {
            droidSpeech.closeDroidSpeechOperations();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //    mSocket.disconnect();
    }

    public void showActivationDialog() {

        dialogActivation = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_activation, null);


        TextView tvActivateMessage = dialogView.findViewById(R.id.tvActivate);
        setFont(tvActivateMessage);

        EditText etCode = dialogView.findViewById(R.id.etCode);

        Button btnActivate = dialogView.findViewById(R.id.btnActivate);
        setFont(btnActivate);

        dialogActivation.setCancelable(false);

        btnActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String code = etCode.getText().toString().trim();

                if (code.isEmpty()) {
                    Toast.makeText(GridViewActivity.this, getString(R.string.code_required),
                            Toast.LENGTH_SHORT).show();
                    dialogActivation.setCancelable(false);
                } else {

                    checkCode(ManageSession.getPreference(context, ManageSession.COMPANY_ID),
                            IMEI, code, token);
                }


            }
        });


        dialogActivation.setView(dialogView);
        dialogActivation.show();
    }

    /*Get Rooms API*/
    public void getRooms(String imei) {
        //  showProgress();
        // dialog.setMessage("please wait....");
        // dialog.show();

        GetRoomsRequest request = new GetRoomsRequest().withIEMI(imei);

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.getRooms(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.code() == Constant.SUCCESS_CODE) {


                    String str = Utils.getParsedString(response.body().byteStream());

                    try {
                        JSONObject object = new JSONObject(str);
                        JSONArray data = object.optJSONArray(Constant.DATA);
                        JSONObject settingObject = object.getJSONObject("settings");
                        TIME_IN_MLS = TimeUnit.MINUTES.toMillis(settingObject.getLong("screen_time_out"));
                        //TIME_IN_MLS = 5000;

                        System.out.println("time in milis===" + TIME_IN_MLS);


                        if (object.getString(Constant.REPLY_STATUS).equals(Constant.SUCCESS)) {
                            GsonBuilder builder = new GsonBuilder();
                            Gson gson = builder.serializeNulls().create();

                            int length = data.length();

                            if (length > 0) {
                                for (int i = 0; i < length; i++) {
                                    RoomRequest bean = gson.fromJson(data.getJSONObject(i).toString(), RoomRequest.class);
                                    mRoomRequestList.add(bean);
                                }
                                for (int i = 0; i < length; i++) {
                                    if (mRoomRequestList.get(i).getHostImei().equals(imei)) {
                                        Utils.deviceName = mRoomRequestList.get(i).getDeviceName();
                                        Utils.callSettings = mRoomRequestList.get(i).getDeviceApprove();
                                        ManageSession.setPreference(context, ManageSession.PICK_UP_SETTINGS, Utils.callSettings);
                                        System.out.println("call settings ====" + Utils.callSettings);
                                        txtTitle.setText("Hi " + Utils.deviceName + ", " + "Please connect to a room below");
                                    }
                                }

                            }

                            if (mRoomRequestList.isEmpty()) {

                            } else {
                                roomsAdapter.notifyDataSetChanged();
                            }

                            countDownTimer = new CountDownTimer(TIME_IN_MLS, 1000) { // adjust the milli seconds here

                                public void onTick(long millisUntilFinished) {

                                }

                                public void onFinish() {
                                    Intent i = new Intent(GridViewActivity.this, ScreenSaverActivity.class);
                                    startActivity(i);

                                }
                            }.start();


                        } else if (object.getString(Constant.REPLY_STATUS).equals(Constant.FAIL)) {

                            ManageSession.clearPreference(context);
                            Intent loginIntent = new Intent(context, LoginActivity.class);
                            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(loginIntent);

                        } else {
                            showSnackBar(main, getString(R.string.some_error));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showSnackBar(main, getString(R.string.some_error));
                    }
                } else {
                    showSnackBar(main, getString(R.string.some_error));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissProgress();
            }
        });
    }

    /*Check Code API*/
    public void checkCode(String companyId, String imei, String code, String token) {
        //showProgress();
        dialog.setMessage("please wait....");
        dialog.show();

        // System.out.println("token2==" + token);

        CheckCodeRequest request = new CheckCodeRequest()
                .withCompanyId(companyId)
                .withIEMI(imei)
                .withCode(code)
                .withDeviceToken(token);

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.checkCode(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                //   dismissProgress();
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (response.code() == Constant.SUCCESS_CODE) {

                    String str = Utils.getParsedString(response.body().byteStream());


                    try {
                        JSONObject object = new JSONObject(str);
                        JSONObject data = object.optJSONObject(Constant.DATA);

                        if (object.getString(Constant.REPLY_STATUS).equals(Constant.SUCCESS)) {
                            GsonBuilder builder = new GsonBuilder();
                            Gson gson = builder.serializeNulls().create();

                            if (dialogActivation != null && dialogActivation.isShowing()) {
                                dialogActivation.dismiss();
                                ManageSession.setBooleanPreference(context, ManageSession.CODE_ACTIVATED, true);
                                if (!IMEI.isEmpty()) {
                                    mSocket.emit("reload_devices", data);
                                    //mRoomRequestList.clear();
                                    // getRooms(IMEI);
                                }
                            }

                            showSnackBar(main, object.getString(Constant.REPLY_MESSAGE));

                        } else if (object.getString(Constant.REPLY_STATUS).equals(Constant.FAIL)) {

                            showSnackBar(main, object.getString(Constant.REPLY_MESSAGE));

                            if (dialogActivation != null && dialogActivation.isShowing()) {
                                dialogActivation.findViewById(R.id.btnActivate).setEnabled(false);
                            }

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (dialogActivation != null && dialogActivation.isShowing()) {
                                        dialogActivation.dismiss();
                                    }
                                    ManageSession.clearPreference(context);
                                    Intent loginIntent = new Intent(context, LoginActivity.class);
                                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(loginIntent);
                                }
                            }, 3000);

//                            Toast.makeText(context, object.getString(Constant.REPLY_MESSAGE),
//                                    Toast.LENGTH_SHORT).show();

                        } else {
                            showSnackBar(main, getString(R.string.some_error));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showSnackBar(main, getString(R.string.some_error));
                    }
                } else {
                    showSnackBar(main, getString(R.string.some_error));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                // dismissProgress();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode,
            @NonNull final String[] permissions,
            @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (0 < grantResults.length && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Audio recording permissions denied.", Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }

    private void inviteAccept(String requestedIMEI, String deviceId, String channelName, String
            accessToken, String type) {

        GetRoomsRequest request = new GetRoomsRequest().withIEMI(requestedIMEI).withDeviceId(deviceId).withSetType(type);

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.inviteApi(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (response.code() == Constant.SUCCESS_CODE) {
                    if (type.equals("request")) {
                        // showSnackBar(findViewById(R.id.main), getString(R.string.request_sent_successfully));
                    } else if (type.equals("invite_accept")) {
                        //  showSnackBar(findViewById(R.id.main), getString(R.string.invite_accept_successfully));
                        forwardToRoom(channelName, accessToken, deviceId);
                    } else if (type.equals("invite_cancel")) {
                        //  showSnackBar(findViewById(R.id.main), getString(R.string.invite_cancel_successfully));
                    }

                } else {
                    // showSnackBar(main, getString(R.string.some_error));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // dismissProgress();
            }
        });

    }

    @Override
    public void onDroidSpeechSupportedLanguages(String currentSpeechLanguage, List<String> supportedSpeechLanguages) {

    }

    @Override
    public void onDroidSpeechRmsChanged(float rmsChangedValue) {

    }

    @Override
    public void onDroidSpeechLiveResult(String liveSpeechResult) {
        Log.i(TAG, "Live speech result = " + liveSpeechResult);
    }

    @Override
    public void onDroidSpeechFinalResult(String finalSpeechResult) {
        // Setting the final speech result
        // this.finalSpeechResult.setText(finalSpeechResult);

        System.out.println("final speech result==" + finalSpeechResult.toLowerCase());

        if (finalSpeechResult.toLowerCase().length() > Constant.CONNECT.length()) {
            if (finalSpeechResult.toLowerCase().substring(0, 7).equals(Constant.CONNECT)
                    && finalSpeechResult.toLowerCase().substring(7, 8).equals(Constant.SPACE)) {

                String channelName = finalSpeechResult.toLowerCase().substring(Constant.CONNECT.length() + 1);
                v_channel.setText(channelName);
                v_channel.setSelection(channelName.length());

                //speak("Connecting " + channelName);

                String accessToken = "";
                String deviceId = "";


                for (int i = 0; i < mRoomRequestList.size(); i++) {

                    if (mRoomRequestList.get(i).getDeviceName().toLowerCase().equals(channelName)) {
                        accessToken = mRoomRequestList.get(i).getAppToken();
                        deviceId = mRoomRequestList.get(i).getId();
                        channelName = mRoomRequestList.get(i).getDeviceName();

                        break;
                    }

                }

                if (!accessToken.isEmpty()) {
                    forwardToRoom(channelName, accessToken, deviceId);
                }
            }
        }


    }

    @Override
    public void onDroidSpeechClosedByUser() {

    }

    @Override
    public void onDroidSpeechError(String errorMsg) {
        // Speech error
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
//         droidSpeech.closeDroidSpeechOperations();
    }

    @Override
    public void onDroidSpeechAudioPermissionStatus(boolean audioPermissionGiven, String errorMsgIfAny) {

    }


}

