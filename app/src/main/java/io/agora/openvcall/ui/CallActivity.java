package io.agora.openvcall.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import io.agora.adapter.InviteDeviceAdapter;
import io.agora.adapter.RoomsAdapter;
import io.agora.api.APIClient;
import io.agora.api.APIInterface;
import io.agora.openvcall.R;
import io.agora.openvcall.databinding.ActivityCallBinding;
import io.agora.openvcall.model.AGEventHandler;
import io.agora.openvcall.model.ConstantApp;
import io.agora.openvcall.model.DuringCallEventHandler;
import io.agora.openvcall.model.Message;
import io.agora.openvcall.model.User;
import io.agora.openvcall.ui.layout.GridVideoViewContainer;
import io.agora.openvcall.ui.layout.InChannelMessageListAdapter;
import io.agora.openvcall.ui.layout.MessageListDecoration;
import io.agora.openvcall.ui.layout.SmallVideoViewAdapter;
import io.agora.openvcall.ui.layout.SmallVideoViewDecoration;
import io.agora.propeller.Constant;
import io.agora.propeller.UserStatusData;
import io.agora.propeller.VideoInfoData;
import io.agora.propeller.ui.RecyclerItemClickListener;
import io.agora.propeller.ui.RtlLinearLayoutManager;
import io.agora.requestmodels.GetRoomsRequest;
import io.agora.responsemodels.RoomRequest;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.util.ManageSession;
import io.agora.util.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallActivity extends BaseActivity implements DuringCallEventHandler, RoomsAdapter.Listener, InviteDeviceAdapter.OnItemClickListener {

    private static final String LOG_TAG = CallActivity.class.getSimpleName();

    public static final int LAYOUT_TYPE_DEFAULT = 0;
    public static final int LAYOUT_TYPE_SMALL = 1;
    private final static Logger log = LoggerFactory.getLogger(CallActivity.class);

    // should only be modified under UI thread
    private final HashMap<Integer, SurfaceView> mUidsList = new HashMap<>(); // uid = 0 || uid == EngineConfig.mUid
    public int mLayoutType = LAYOUT_TYPE_DEFAULT;
    private GridVideoViewContainer mGridVideoViewContainer;
    private RelativeLayout mSmallVideoViewDock;

    private volatile boolean mVideoMuted = false;
    private volatile boolean mAudioMuted = false;
    private volatile boolean mMixingAudio = false;

    private volatile int mAudioRouting = Constants.AUDIO_ROUTE_DEFAULT;

    private volatile boolean mFullScreen = false;

    private boolean mIsLandscape = false;

    private InChannelMessageListAdapter mMsgAdapter;
    private ArrayList<Message> mMsgList;

    private SmallVideoViewAdapter mSmallVideoViewAdapter;

    private final Handler mUIHandler = new Handler();

    private RoomsAdapter roomsAdapter;
    private InviteDeviceAdapter inviteDeviceAdapter;
    private ArrayList<RoomRequest> mRoomRequestList = new ArrayList<>();
    private ArrayList<RoomRequest> mRoomInviteList = new ArrayList<>();
    private Context context;
    String accessToken, channelName, deviceId, activeCount;
    private AlertDialog requestDialog, inviteDialog, inviteListDialog, switchRoomListDialog;
    ProgressDialog dialog;
    ArrayList<String> inviteDeviceList = new ArrayList<>();
    ActivityCallBinding binding;
    //  TextView tvConfRoomName;
    JSONObject object;
    String joinedDeviceName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeActivityContentShownUnderStatusBar();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call);

        context = this;

        dialog = new ProgressDialog(this);
        dialog.setMessage("sending your request, please wait...");

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ab.setCustomView(R.layout.ard_agora_actionbar_with_title);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_call, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
        /*    case R.id.action_options:
                showCallOptions();
                return true;*/

            case R.id.action_invite:

                showRoomInviteListDialog();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showRoomInviteListDialog() {

        Rect displayRectangle = new Rect();
        Window window = CallActivity.this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        inviteListDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View inviteContentDialog = inflater.inflate(R.layout.invite_device_layout, null);
        inviteContentDialog.setMinimumWidth((int) (displayRectangle.width() * 1f));
        inviteContentDialog.setMinimumHeight((int) (displayRectangle.height() * 1f));
        RecyclerView rvRooms = inviteContentDialog.findViewById(R.id.rvRooms);
        TextView inviteBelowDevice = inviteContentDialog.findViewById(R.id.invite_below_device);
        setFont(inviteBelowDevice);
        TextView txtAllDevice = inviteContentDialog.findViewById(R.id.txt_all_devices);
        setFont(txtAllDevice);
        Button btnCancel = inviteContentDialog.findViewById(R.id.btn_cancel);
        Button btnInvites = inviteContentDialog.findViewById(R.id.btn_invite_device);
        ProgressBar progressbar = inviteContentDialog.findViewById(R.id.progressbar);
        LinearLayout main = inviteContentDialog.findViewById(R.id.main);
        SwipeRefreshLayout swipeToRefresh = inviteContentDialog.findViewById(R.id.swipeToRefresh);
        swipeToRefresh.setColorSchemeResources(R.color.colorAccent);

        inviteDeviceAdapter = new InviteDeviceAdapter(context, mRoomInviteList, this);
        rvRooms.setAdapter(inviteDeviceAdapter);

        getInviteRooms(IMEI, btnInvites, deviceId, progressbar, rvRooms, main, swipeToRefresh);

        swipeToRefresh.setOnRefreshListener(() -> {
            mRoomInviteList.clear();
            progressbar.setVisibility(View.VISIBLE);
            getInviteRooms(IMEI, btnInvites, deviceId, progressbar, rvRooms, main, swipeToRefresh);
            swipeToRefresh.setRefreshing(false);
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inviteListDialog.dismiss();
            }
        });


        btnInvites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inviteDeviceList.size() > 0) {
                    inviteListDialog.dismiss();
                    inviteSent(IMEI, deviceId, channelName, accessToken, "invite_sent", inviteDeviceList.toString());
                } else {
                    showSnackBar(main, getString(R.string.please_select_device_first));
                }

                System.out.println("invite list==" + inviteDeviceList);
            }
        });

        inviteListDialog.setView(inviteContentDialog);
        inviteListDialog.setCancelable(false);
        inviteListDialog.show();

    }

    /*Get Invite Rooms API*/
    private void getInviteRooms(String imei, Button btnInvites, String deviceId, ProgressBar progressbar, RecyclerView rvRooms, LinearLayout main, SwipeRefreshLayout swipeToRefresh) {

        GetRoomsRequest request = new GetRoomsRequest().withIEMI(imei).withDeviceId(deviceId);

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.getInviteUserList(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.code() == Constant.SUCCESS_CODE) {

                    String str = Utils.getParsedString(response.body().byteStream());
                    mRoomInviteList.clear();

                    try {
                        JSONObject object = new JSONObject(str);
                        JSONArray data = object.optJSONArray(Constant.DATA);

                        if (object.getString(Constant.REPLY_STATUS).equals(Constant.SUCCESS)) {
                            GsonBuilder builder = new GsonBuilder();
                            Gson gson = builder.serializeNulls().create();

                            int length = data.length();

                            if (length > 0) {
                                for (int i = 0; i < length; i++) {
                                    RoomRequest bean = gson.fromJson(data.getJSONObject(i).toString(), RoomRequest.class);
                                    mRoomInviteList.add(bean);
                                }
                            }

                            if (mRoomInviteList.isEmpty()) {
                                progressbar.setVisibility(View.GONE);
                                swipeToRefresh.setVisibility(View.GONE);
                                rvRooms.setVisibility(View.GONE);
                                btnInvites.setVisibility(View.GONE);
                                inviteDeviceAdapter.notifyDataSetChanged();

                            } else {
                                progressbar.setVisibility(View.GONE);
                                swipeToRefresh.setVisibility(View.VISIBLE);
                                rvRooms.setVisibility(View.VISIBLE);
                                btnInvites.setVisibility(View.VISIBLE);
                                inviteDeviceAdapter.notifyDataSetChanged();
                            }

                        } else if (object.getString(Constant.REPLY_STATUS).equals(Constant.FAIL)) {
                            showSnackBar(main, object.getString(Constant.REPLY_MESSAGE));
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

                showSnackBar(main, getString(R.string.some_error));
            }
        });
    }


    private void inviteSent(String requestedIMEI, String deviceId, String channelName, String accessToken, String type, String inviteIds) {

        GetRoomsRequest request = new GetRoomsRequest().withIEMI(requestedIMEI).withDeviceId(deviceId).withSetType(type)
                .withChannelName(channelName).withAppToken(accessToken).withDeviceIds(inviteIds.replace("[", "").replace("]", ""));

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.inviteApi(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (response.code() == Constant.SUCCESS_CODE) {
                    if (type.equals("invite_sent")) {
                        // showSnackBar(findViewById(R.id.main), getString(R.string.invite_sent_successfully));
                    }
                } else {
                    // showSnackBar(findViewById(R.id.main), getString(R.string.some_error));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //  dismissProgress();
            }
        });

    }


    @Override
    protected void initUIandEvent() {
        event().addEventHandler(this);
        Intent i = getIntent();

        channelName = i.getStringExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME);
        accessToken = i.getStringExtra(ConstantApp.ACTION_KEY_TOKEN);
        deviceId = i.getStringExtra(ConstantApp.DEVICE_ID);


        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            TextView channelNameView = ((TextView) findViewById(R.id.ovc_page_title));
            channelNameView.setText(channelName);
            setFont(channelNameView);
        }


        setRoomStatus(IMEI, deviceId, "1", "", "", "", "");

        // programmatically layout ui below of status bar/action bar
        LinearLayout eopsContainer = findViewById(R.id.extra_ops_container);
        RelativeLayout.MarginLayoutParams eofmp = (RelativeLayout.MarginLayoutParams) eopsContainer.getLayoutParams();
        eofmp.topMargin = getStatusBarHeight() + getActionBarHeight() + getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin) / 2; // status bar + action bar + divider

        String encryptionKey = getIntent().getStringExtra(ConstantApp.ACTION_KEY_ENCRYPTION_KEY);
        final String encryptionMode = getIntent().getStringExtra(ConstantApp.ACTION_KEY_ENCRYPTION_MODE);

        doConfigEngine("", "AES-128-XTS");

        //    tvConfRoomName = ((TextView) findViewById(R.id.tv_conf_room_name));
        mGridVideoViewContainer = (GridVideoViewContainer) findViewById(R.id.grid_video_view_container);
        mGridVideoViewContainer.setItemEventHandler(new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                onBigVideoViewClicked(view, position);
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }

            @Override
            public void onItemDoubleClick(View view, int position) {
                onBigVideoViewDoubleClicked(view, position);
            }
        });


        initMessageList();

        notifyMessageChanged(new Message(new User(0, null), "start join " + channelName + " as " + (config().mUid & 0xFFFFFFFFL)));

        worker().joinChannel(channelName, accessToken, config().mUid);


        optional();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction().equals(Constant.CALL_REQUEST)) {
            String requestedBy = intent.getStringExtra("requestedBy");
            String messageBody = intent.getStringExtra("messageBody");
            String requestedIMEI = intent.getStringExtra("requestedIMEI");
            String channelName = intent.getStringExtra("channel_name");
            String accessToken = intent.getStringExtra("access_token");
            String deviceId = intent.getStringExtra("device_id");
            System.out.println("requestedIMEI===" + requestedIMEI);
           // if (ManageSession.getPreference(context, ManageSession.PICK_UP_SETTINGS).equals(getString(R.string.auto))) {
             //   requestToJoin(requestedIMEI, deviceId, channelName, accessToken, "yes");
         //   } else {
                showRequestDialog(requestedIMEI, channelName, accessToken, deviceId, requestedBy);
        //    }
        } else if (intent.getAction().equals(Constant.INVITE_SENT)) {
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
        } else {
            channelName = intent.getStringExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME);
            accessToken = intent.getStringExtra(ConstantApp.ACTION_KEY_TOKEN);
            deviceId = intent.getStringExtra(ConstantApp.DEVICE_ID);
        }

    }

    private void onBigVideoViewClicked(View view, int position) {
        log.debug("onItemClick " + view + " " + position + " " + mLayoutType);
        toggleFullscreen();
    }

    private void onBigVideoViewDoubleClicked(View view, int position) {
        log.debug("onItemDoubleClick " + view + " " + position + " " + mLayoutType);

        if (mUidsList.size() < 2) {
            return;
        }

        UserStatusData user = mGridVideoViewContainer.getItem(position);
        int uid = (user.mUid == 0) ? config().mUid : user.mUid;

        if (mLayoutType == LAYOUT_TYPE_DEFAULT && mUidsList.size() != 1) {
            //switchToSmallVideoView(uid);
        } else {
            switchToDefaultVideoView();
        }
    }

    private void onSmallVideoViewDoubleClicked(View view, int position) {
        log.debug("onItemDoubleClick small " + view + " " + position + " " + mLayoutType);

        switchToDefaultVideoView();
    }

    private void makeActivityContentShownUnderStatusBar() {
        // https://developer.android.com/training/system-ui/status
        // May fail on some kinds of devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

            decorView.setSystemUiVisibility(uiOptions);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.agora_blue));
            }
        }
    }

    private void showOrHideStatusBar(boolean hide) {
        // May fail on some kinds of devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            View decorView = getWindow().getDecorView();
            int uiOptions = decorView.getSystemUiVisibility();

            if (hide) {
                uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            } else {
                uiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }

            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void toggleFullscreen() {
        mFullScreen = !mFullScreen;

        showOrHideCtrlViews(mFullScreen);

        mUIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showOrHideStatusBar(mFullScreen);
            }
        }, 200); // action bar fade duration
    }

    private void showOrHideCtrlViews(boolean hide) {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            if (hide) {
                ab.hide();
                //  tvConfRoomName.setVisibility(View.VISIBLE);
                //    tvConfRoomName.setText(joinedDeviceName);
            } else {
                ab.show();
                //  tvConfRoomName.setVisibility(View.GONE);
            }
        }

        findViewById(R.id.extra_ops_container).setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
        findViewById(R.id.bottom_action_container).setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
        //findViewById(R.id.msg_list).setVisibility(hide ? View.INVISIBLE : (Constant.DEBUG_INFO_ENABLED ? View.VISIBLE : View.INVISIBLE));
    }

    private void relayoutForVirtualKeyPad(int orientation) {
        int virtualKeyHeight = virtualKeyHeight();

        LinearLayout eopsContainer = findViewById(R.id.extra_ops_container);
        FrameLayout.MarginLayoutParams eofmp = (FrameLayout.MarginLayoutParams) eopsContainer.getLayoutParams();

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            eofmp.rightMargin = virtualKeyHeight;
            eofmp.leftMargin = 0;
        } else {
            eofmp.leftMargin = 0;
            eofmp.rightMargin = 0;
        }

        LinearLayout bottomContainer = findViewById(R.id.bottom_container);
        FrameLayout.MarginLayoutParams fmp = (FrameLayout.MarginLayoutParams) bottomContainer.getLayoutParams();

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fmp.bottomMargin = 0;
            fmp.rightMargin = virtualKeyHeight;
            fmp.leftMargin = 0;
        } else {
            fmp.bottomMargin = virtualKeyHeight;
            fmp.leftMargin = 0;
            fmp.rightMargin = 0;
        }
    }

    private static final int CALL_OPTIONS_REQUEST = 3222;

    public synchronized void showCallOptions() {
        Intent i = new Intent(this, CallOptionsActivity.class);
        startActivityForResult(i, CALL_OPTIONS_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CALL_OPTIONS_REQUEST) {
            RecyclerView msgListView = (RecyclerView) findViewById(R.id.msg_list);
            msgListView.setVisibility(Constant.DEBUG_INFO_ENABLED ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void onClickHideIME(View view) {
        log.debug("onClickHideIME " + view);

        closeIME(findViewById(R.id.msg_content));

        findViewById(R.id.msg_input_container).setVisibility(View.GONE);
        findViewById(R.id.bottom_action_container).setVisibility(View.VISIBLE);
    }

    private void initMessageList() {
        mMsgList = new ArrayList<>();
        RecyclerView msgListView = (RecyclerView) findViewById(R.id.msg_list);

        mMsgAdapter = new InChannelMessageListAdapter(this, mMsgList);
        mMsgAdapter.setHasStableIds(true);
        msgListView.setAdapter(mMsgAdapter);
        msgListView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false));
        msgListView.addItemDecoration(new MessageListDecoration());
    }

    private void notifyMessageChanged(Message msg) {
        mMsgList.add(msg);

        int MAX_MESSAGE_COUNT = 16;

        if (mMsgList.size() > MAX_MESSAGE_COUNT) {
            int toRemove = mMsgList.size() - MAX_MESSAGE_COUNT;
            for (int i = 0; i < toRemove; i++) {
                mMsgList.remove(i);
            }
        }

        mMsgAdapter.notifyDataSetChanged();


    }

    private void optional() {
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    private void optionalDestroy() {
    }

    private int getVideoEncResolutionIndex() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int videoEncResolutionIndex = pref.getInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_RESOLUTION, ConstantApp.DEFAULT_VIDEO_ENC_RESOLUTION_IDX);
        if (videoEncResolutionIndex > ConstantApp.VIDEO_DIMENSIONS.length - 1) {
            videoEncResolutionIndex = ConstantApp.DEFAULT_VIDEO_ENC_RESOLUTION_IDX;

            // save the new value
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_RESOLUTION, videoEncResolutionIndex);
            editor.apply();
        }
        return videoEncResolutionIndex;
    }

    private int getVideoEncFpsIndex() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int videoEncFpsIndex = pref.getInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_FPS, ConstantApp.DEFAULT_VIDEO_ENC_FPS_IDX);
        if (videoEncFpsIndex > ConstantApp.VIDEO_FPS.length - 1) {
            videoEncFpsIndex = ConstantApp.DEFAULT_VIDEO_ENC_FPS_IDX;

            // save the new value
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_FPS, videoEncFpsIndex);
            editor.apply();
        }
        return videoEncFpsIndex;
    }

    private void doConfigEngine(String encryptionKey, String encryptionMode) {
        VideoEncoderConfiguration.VideoDimensions videoDimension = ConstantApp.VIDEO_DIMENSIONS[getVideoEncResolutionIndex()];
        VideoEncoderConfiguration.FRAME_RATE videoFps = ConstantApp.VIDEO_FPS[getVideoEncFpsIndex()];

        worker().configEngine(videoDimension, videoFps, encryptionKey, encryptionMode);
    }

    public void onSwitchCameraClicked(View view) {
        RtcEngine rtcEngine = rtcEngine();
        rtcEngine.switchCamera();
    }

    public void onSwitchSpeakerClicked(View view) {
        RtcEngine rtcEngine = rtcEngine();
        rtcEngine.setEnableSpeakerphone(mAudioRouting != Constants.AUDIO_ROUTE_SPEAKERPHONE);
    }

    public void onFilterClicked(View view) {
        Constant.BEAUTY_EFFECT_ENABLED = !Constant.BEAUTY_EFFECT_ENABLED;

        if (Constant.BEAUTY_EFFECT_ENABLED) {
            worker().setBeautyEffectParameters(Constant.BEAUTY_EFFECT_DEFAULT_LIGHTNESS, Constant.BEAUTY_EFFECT_DEFAULT_SMOOTHNESS, Constant.BEAUTY_EFFECT_DEFAULT_REDNESS);
            worker().enablePreProcessor();
        } else {
            worker().disablePreProcessor();
        }

        ImageView iv = (ImageView) view;

        iv.setImageResource(Constant.BEAUTY_EFFECT_ENABLED ? R.drawable.btn_filter : R.drawable.btn_filter_off);
    }

    @Override
    protected void deInitUIandEvent() {
        optionalDestroy();

        doLeaveChannel();
        event().removeEventHandler(this);

        mUidsList.clear();
    }

    private void doLeaveChannel() {
        worker().leaveChannel(config().mChannel);
        worker().preview(false, null, 0);
    }

    public void onHangupClicked(View view) {
        log.info("onHangupClicked " + view);
        //   setRoomStatus(IMEI, deviceId, "0", "", "", "", "");
        Intent i = new Intent(context, GridViewActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();

    }

    public void onVideoMuteClicked(View view) {
        log.info("onVoiceChatClicked " + view + " " + mUidsList.size() + " video_status: " + mVideoMuted + " audio_status: " + mAudioMuted);
        if (mUidsList.size() == 0) {
            return;
        }

        SurfaceView surfaceV = getLocalView();
        ViewParent parent;
        if (surfaceV == null || (parent = surfaceV.getParent()) == null) {
            log.warn("onVoiceChatClicked " + view + " " + surfaceV);
            return;
        }

        RtcEngine rtcEngine = rtcEngine();
        mVideoMuted = !mVideoMuted;

        if (mVideoMuted) {
            rtcEngine.disableVideo();
        } else {
            rtcEngine.enableVideo();
        }

        ImageView iv = (ImageView) view;

        iv.setImageResource(mVideoMuted ? R.drawable.btn_camera_off : R.drawable.btn_camera);

        hideLocalView(mVideoMuted);
    }

    private SurfaceView getLocalView() {
        for (HashMap.Entry<Integer, SurfaceView> entry : mUidsList.entrySet()) {
            if (entry.getKey() == 0 || entry.getKey() == config().mUid) {
                return entry.getValue();
            }
        }

        return null;
    }

    private void hideLocalView(boolean hide) {
        int uid = config().mUid;
        doHideTargetView(uid, hide);
    }

    private void doHideTargetView(int targetUid, boolean hide) {
        HashMap<Integer, Integer> status = new HashMap<>();
        status.put(targetUid, hide ? UserStatusData.VIDEO_MUTED : UserStatusData.DEFAULT_STATUS);
        if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
            mGridVideoViewContainer.notifyUiChanged(mUidsList, targetUid, status, null);
        } else if (mLayoutType == LAYOUT_TYPE_SMALL) {
            UserStatusData bigBgUser = mGridVideoViewContainer.getItem(0);
            if (bigBgUser.mUid == targetUid) { // big background is target view
                mGridVideoViewContainer.notifyUiChanged(mUidsList, targetUid, status, null);
            } else { // find target view in small video view list
                log.warn("SmallVideoViewAdapter call notifyUiChanged " + mUidsList + " " + (bigBgUser.mUid & 0xFFFFFFFFL) + " target: " + (targetUid & 0xFFFFFFFFL) + "==" + targetUid + " " + status);
                mSmallVideoViewAdapter.notifyUiChanged(mUidsList, bigBgUser.mUid, status, null);
            }
        }
    }

    public void onVoiceMuteClicked(View view) {
        log.info("onVoiceMuteClicked " + view + " " + mUidsList.size() + " video_status: " + mVideoMuted + " audio_status: " + mAudioMuted);
        if (mUidsList.size() == 0) {
            return;
        }

        RtcEngine rtcEngine = rtcEngine();
        rtcEngine.muteLocalAudioStream(mAudioMuted = !mAudioMuted);

        ImageView iv = (ImageView) view;

        iv.setImageResource(mAudioMuted ? R.drawable.btn_microphone_off : R.drawable.btn_microphone);
    }

    public void onMixingAudioClicked(View view) {
        log.info("onMixingAudioClicked " + view + " " + mUidsList.size() + " video_status: " + mVideoMuted + " audio_status: " + mAudioMuted + " mixing_audio: " + mMixingAudio);

        if (mUidsList.size() == 0) {
            return;
        }

        mMixingAudio = !mMixingAudio;

        RtcEngine rtcEngine = rtcEngine();
        if (mMixingAudio) {
            rtcEngine.startAudioMixing(Constant.MIX_FILE_PATH, false, false, -1);
        } else {
            rtcEngine.stopAudioMixing();
        }

        ImageView iv = (ImageView) view;
        iv.setImageResource(mMixingAudio ? R.drawable.btn_audio_mixing : R.drawable.btn_audio_mixing_off);
    }

    @Override
    public void onUserJoined(int uid) {
        log.debug("onUserJoined " + (uid & 0xFFFFFFFFL));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyMessageChanged(new Message(new User(0, null), "user " + (uid & 0xFFFFFFFFL) + " joined"));
            }
        });
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        log.debug("onFirstRemoteVideoDecoded " + (uid & 0xFFFFFFFFL) + " " + width + " " + height + " " + elapsed);

        doRenderRemoteUi(uid);
    }

    private void doRenderRemoteUi(final int uid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                if (mUidsList.containsKey(uid)) {
                    return;
                }

                SurfaceView surfaceV = RtcEngine.CreateRendererView(getApplicationContext());
                mUidsList.put(uid, surfaceV);

                boolean useDefaultLayout = mLayoutType == LAYOUT_TYPE_DEFAULT;

                surfaceV.setZOrderOnTop(true);
                surfaceV.setZOrderMediaOverlay(true);

                rtcEngine().setupRemoteVideo(new VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_HIDDEN, uid));

                if (useDefaultLayout) {
                    log.debug("doRenderRemoteUi LAYOUT_TYPE_DEFAULT " + (uid & 0xFFFFFFFFL));
                    switchToDefaultVideoView();
                } else {
                    int bigBgUid = mSmallVideoViewAdapter == null ? uid : mSmallVideoViewAdapter.getExceptedUid();
                    log.debug("doRenderRemoteUi LAYOUT_TYPE_SMALL " + (uid & 0xFFFFFFFFL) + " " + (bigBgUid & 0xFFFFFFFFL));
                    //switchToSmallVideoView(bigBgUid);
                }

                notifyMessageChanged(new Message(new User(0, null), "video from user " + (uid & 0xFFFFFFFFL) + " decoded"));
            }
        });
    }

    @Override
    public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
        log.debug("onJoinChannelSuccess " + channel + " " + (uid & 0xFFFFFFFFL) + " " + elapsed);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                notifyMessageChanged(new Message(new User(0, null), "join " + channel + " success as " + (uid & 0xFFFFFFFFL) + " in " + elapsed + "ms"));

                SurfaceView local = mUidsList.remove(0);

                if (local == null) {
                    return;
                }

                mUidsList.put(uid, local);

            }
        });
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        log.debug("onUserOffline " + (uid & 0xFFFFFFFFL) + " " + reason);

        doRemoveRemoteUi(uid);
    }

    @Override
    public void onExtraCallback(final int type, final Object... data) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                doHandleExtraCallback(type, data);
            }
        });
    }

    private void doHandleExtraCallback(int type, Object... data) {
        int peerUid;
        boolean muted;

        switch (type) {
            case AGEventHandler.EVENT_TYPE_ON_USER_AUDIO_MUTED:
                peerUid = (Integer) data[0];
                muted = (boolean) data[1];

                if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
                    HashMap<Integer, Integer> status = new HashMap<>();
                    status.put(peerUid, muted ? UserStatusData.AUDIO_MUTED : UserStatusData.DEFAULT_STATUS);
                    mGridVideoViewContainer.notifyUiChanged(mUidsList, config().mUid, status, null);
                }

                break;

            case AGEventHandler.EVENT_TYPE_ON_USER_VIDEO_MUTED:
                peerUid = (Integer) data[0];
                muted = (boolean) data[1];

                doHideTargetView(peerUid, muted);

                break;

            case AGEventHandler.EVENT_TYPE_ON_USER_VIDEO_STATS:
                IRtcEngineEventHandler.RemoteVideoStats stats = (IRtcEngineEventHandler.RemoteVideoStats) data[0];

                if (Constant.SHOW_VIDEO_INFO) {
                    if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
                        mGridVideoViewContainer.addVideoInfo(stats.uid, new VideoInfoData(stats.width, stats.height, stats.delay, stats.rendererOutputFrameRate, stats.receivedBitrate));
                        int uid = config().mUid;
                        int profileIndex = getVideoEncResolutionIndex();
                        String resolution = getResources().getStringArray(R.array.string_array_resolutions)[profileIndex];
                        String fps = getResources().getStringArray(R.array.string_array_frame_rate)[profileIndex];

                        String[] rwh = resolution.split("x");
                        int width = Integer.valueOf(rwh[0]);
                        int height = Integer.valueOf(rwh[1]);

                        mGridVideoViewContainer.addVideoInfo(uid, new VideoInfoData(width > height ? width : height,
                                width > height ? height : width,
                                0, Integer.valueOf(fps), Integer.valueOf(0)));
                    }
                } else {
                    mGridVideoViewContainer.cleanVideoInfo();
                }

                break;

            case AGEventHandler.EVENT_TYPE_ON_SPEAKER_STATS:
                IRtcEngineEventHandler.AudioVolumeInfo[] infos = (IRtcEngineEventHandler.AudioVolumeInfo[]) data[0];

                if (infos.length == 1 && infos[0].uid == 0) { // local guy, ignore it
                    break;
                }

                if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
                    HashMap<Integer, Integer> volume = new HashMap<>();

                    for (IRtcEngineEventHandler.AudioVolumeInfo each : infos) {
                        peerUid = each.uid;
                        int peerVolume = each.volume;

                        if (peerUid == 0) {
                            continue;
                        }
                        volume.put(peerUid, peerVolume);
                    }
                    mGridVideoViewContainer.notifyUiChanged(mUidsList, config().mUid, null, volume);
                }

                break;

            case AGEventHandler.EVENT_TYPE_ON_APP_ERROR:
                int subType = (int) data[0];

                if (subType == ConstantApp.AppError.NO_CONNECTION_ERROR) {
                    String msg = getString(R.string.msg_connection_error);
                    notifyMessageChanged(new Message(new User(0, null), msg));
                    //  showLongToast(msg);
                    //   rtcEngine().renewToken(accessToken);
                }

                break;

            case AGEventHandler.EVENT_TYPE_ON_DATA_CHANNEL_MSG:

                peerUid = (Integer) data[0];
                final byte[] content = (byte[]) data[1];
                notifyMessageChanged(new Message(new User(peerUid, String.valueOf(peerUid)), new String(content)));

                break;

            case AGEventHandler.EVENT_TYPE_ON_AGORA_MEDIA_ERROR: {
                int error = (int) data[0];
                String description = (String) data[1];

                notifyMessageChanged(new Message(new User(0, null), error + " " + description));

                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_AUDIO_ROUTE_CHANGED:
                notifyHeadsetPlugged((int) data[0]);

                break;

        }
    }

    private void requestRemoteStreamType(final int currentHostCount) {
        log.debug("requestRemoteStreamType " + currentHostCount);
    }

    private void doRemoveRemoteUi(final int uid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                Object target = mUidsList.remove(uid);
                if (target == null) {
                    return;
                }

                int bigBgUid = -1;
                if (mSmallVideoViewAdapter != null) {
                    bigBgUid = mSmallVideoViewAdapter.getExceptedUid();
                }

                log.debug("doRemoveRemoteUi " + (uid & 0xFFFFFFFFL) + " " + (bigBgUid & 0xFFFFFFFFL) + " " + mLayoutType);

                if (mLayoutType == LAYOUT_TYPE_DEFAULT || uid == bigBgUid) {
                    switchToDefaultVideoView();
                } else {
                    //switchToSmallVideoView(bigBgUid);
                }

                notifyMessageChanged(new Message(new User(0, null), "user " + (uid & 0xFFFFFFFFL) + " left"));
            }
        });
    }

    private void switchToDefaultVideoView() {
        if (mSmallVideoViewDock != null) {
            mSmallVideoViewDock.setVisibility(View.GONE);
        }
        mGridVideoViewContainer.initViewContainer(this, config().mUid, mUidsList, mIsLandscape);

        mLayoutType = LAYOUT_TYPE_DEFAULT;
        boolean setRemoteUserPriorityFlag = false;
        int sizeLimit = mUidsList.size();
        if (sizeLimit > ConstantApp.MAX_PEER_COUNT + 1) {
            sizeLimit = ConstantApp.MAX_PEER_COUNT + 1;
        }
        for (int i = 0; i < sizeLimit; i++) {
            int uid = mGridVideoViewContainer.getItem(i).mUid;
            if (config().mUid != uid) {
                if (!setRemoteUserPriorityFlag) {
                    setRemoteUserPriorityFlag = true;
                    rtcEngine().setRemoteUserPriority(uid, Constants.USER_PRIORITY_HIGH);
                    log.debug("setRemoteUserPriority USER_PRIORITY_HIGH " + mUidsList.size() + " " + (uid & 0xFFFFFFFFL));
                } else {
                    rtcEngine().setRemoteUserPriority(uid, Constants.USER_PRIORITY_NORANL);
                    log.debug("setRemoteUserPriority USER_PRIORITY_NORANL " + mUidsList.size() + " " + (uid & 0xFFFFFFFFL));
                }
            }
        }
    }

    private void switchToSmallVideoView(int bigBgUid) {
        HashMap<Integer, SurfaceView> slice = new HashMap<>(1);
        slice.put(bigBgUid, mUidsList.get(bigBgUid));
        Iterator<SurfaceView> iterator = mUidsList.values().iterator();
        while (iterator.hasNext()) {
            SurfaceView s = iterator.next();
            s.setZOrderOnTop(true);
            s.setZOrderMediaOverlay(true);
        }

        mUidsList.get(bigBgUid).setZOrderOnTop(false);
        mUidsList.get(bigBgUid).setZOrderMediaOverlay(false);

        mGridVideoViewContainer.initViewContainer(this, bigBgUid, slice, mIsLandscape);

        bindToSmallVideoView(bigBgUid);

        mLayoutType = LAYOUT_TYPE_SMALL;

        requestRemoteStreamType(mUidsList.size());
    }

    private void bindToSmallVideoView(int exceptUid) {
        if (mSmallVideoViewDock == null) {
            ViewStub stub = (ViewStub) findViewById(R.id.small_video_view_dock);
            mSmallVideoViewDock = (RelativeLayout) stub.inflate();
        }

        boolean twoWayVideoCall = mUidsList.size() == 2;

        RecyclerView recycler = (RecyclerView) findViewById(R.id.small_video_view_container);

        boolean create = false;

        if (mSmallVideoViewAdapter == null) {
            create = true;
            mSmallVideoViewAdapter = new SmallVideoViewAdapter(this, config().mUid, exceptUid, mUidsList);
            mSmallVideoViewAdapter.setHasStableIds(true);
        }
        recycler.setHasFixedSize(true);

        log.debug("bindToSmallVideoView " + twoWayVideoCall + " " + (exceptUid & 0xFFFFFFFFL));

        if (twoWayVideoCall) {
            recycler.setLayoutManager(new RtlLinearLayoutManager(getApplicationContext(), RtlLinearLayoutManager.HORIZONTAL, false));
        } else {
            recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        }
        recycler.addItemDecoration(new SmallVideoViewDecoration());
        recycler.setAdapter(mSmallVideoViewAdapter);
        recycler.addOnItemTouchListener(new RecyclerItemClickListener(getBaseContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onItemDoubleClick(View view, int position) {
                onSmallVideoViewDoubleClicked(view, position);
            }
        }));

        recycler.setDrawingCacheEnabled(true);
        recycler.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);

        if (!create) {
            mSmallVideoViewAdapter.setLocalUid(config().mUid);
            mSmallVideoViewAdapter.notifyUiChanged(mUidsList, exceptUid, null, null);
        }
        for (Integer tempUid : mUidsList.keySet()) {
            if (config().mUid != tempUid) {
                if (tempUid == exceptUid) {
                    rtcEngine().setRemoteUserPriority(tempUid, Constants.USER_PRIORITY_HIGH);
                    log.debug("setRemoteUserPriority USER_PRIORITY_HIGH " + mUidsList.size() + " " + (tempUid & 0xFFFFFFFFL));
                } else {
                    rtcEngine().setRemoteUserPriority(tempUid, Constants.USER_PRIORITY_NORANL);
                    log.debug("setRemoteUserPriority USER_PRIORITY_NORANL " + mUidsList.size() + " " + (tempUid & 0xFFFFFFFFL));
                }
            }
        }
        recycler.setVisibility(View.VISIBLE);
        mSmallVideoViewDock.setVisibility(View.VISIBLE);
    }

    public void notifyHeadsetPlugged(final int routing) {
        log.info("notifyHeadsetPlugged " + routing + " " + mVideoMuted);

        mAudioRouting = routing;

        ImageView iv = (ImageView) findViewById(R.id.switch_speaker_id);
        if (mAudioRouting == Constants.AUDIO_ROUTE_SPEAKERPHONE) {
            iv.setImageResource(R.drawable.btn_speaker);
        } else {
            iv.setImageResource(R.drawable.btn_speaker_off);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mIsLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
            switchToDefaultVideoView();
        } else if (mSmallVideoViewAdapter != null) {
            //switchToSmallVideoView(mSmallVideoViewAdapter.getExceptedUid());
        }
    }


    public void onSwitchRoom(View v) {

        showRoomListDialog();
    }


    /*display switch room list*/
    private void showRoomListDialog() {

        Rect displayRectangle = new Rect();
        Window window = CallActivity.this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        switchRoomListDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View switchContentDialog = inflater.inflate(R.layout.switch_device_list_layout, null);
        switchContentDialog.setMinimumWidth((int) (displayRectangle.width() * 1f));
        switchContentDialog.setMinimumHeight((int) (displayRectangle.height() * 1f));
        inviteDialog.setView(switchContentDialog);
        TextView txtTitle = switchContentDialog.findViewById(R.id.txt_title);
        setFont(txtTitle);
        TextView txtAllDevices = switchContentDialog.findViewById(R.id.txt_all_devices);
        setFont(txtAllDevices);
        RecyclerView rvRooms = switchContentDialog.findViewById(R.id.rvRooms);
        Button btnCancel = switchContentDialog.findViewById(R.id.btn_cancel);
        ProgressBar progressbar = switchContentDialog.findViewById(R.id.progressbar);
        LinearLayout main = switchContentDialog.findViewById(R.id.main);
        SwipeRefreshLayout swipeToRefresh = switchContentDialog.findViewById(R.id.swipeToRefresh);
        swipeToRefresh.setColorSchemeResources(R.color.colorAccent);

        roomsAdapter = new RoomsAdapter(context, mRoomRequestList, this);
        rvRooms.setAdapter(roomsAdapter);

        /*to get room list*/
        getRooms(IMEI, rvRooms, main, swipeToRefresh, progressbar);


        swipeToRefresh.setOnRefreshListener(() -> {
            mRoomRequestList.clear();
            progressbar.setVisibility(View.VISIBLE);
            getRooms(IMEI, rvRooms, main, swipeToRefresh, progressbar);
            swipeToRefresh.setRefreshing(false);
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchRoomListDialog.dismiss();
            }
        });


        switchRoomListDialog.setView(switchContentDialog);
        switchRoomListDialog.setCancelable(false);
        switchRoomListDialog.show();

    }

    /*code for join request dialog*/
    private void showRequestDialog(String requestedIMEI, String channelName, String accessToken, String deviceId, String requestedBy) {

        Rect displayRectangle = new Rect();
        Window window = CallActivity.this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        requestDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.call_request_popup_layout, null);
        dialogView.setMinimumWidth((int) (displayRectangle.width() * 1f));
        dialogView.setMinimumHeight((int) (displayRectangle.height() * 1f));
        requestDialog.setView(dialogView);
        TextView tvMessage = dialogView.findViewById(R.id.tv_message);
        setFont(tvMessage);
        ImageView imgAccept = dialogView.findViewById(R.id.img_accept);
        ImageView imgReject = dialogView.findViewById(R.id.img_reject);
        TextView btnActionYes = (TextView) dialogView.findViewById(R.id.btn_action_yes);
        // setFont(btnActionYes);
        TextView btnActionNo = (TextView) dialogView.findViewById(R.id.btn_action_no);
        // setFont(btnActionNo);
        ImageView btnActionCancel = dialogView.findViewById(R.id.btn_action_cancel);
        //setFont(btnActionCancel);
        requestDialog.setCancelable(false);
        tvMessage.setText(requestedBy + " is interested to join conference " + channelName + " do you want to allow him.");

        btnActionYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestToJoin(requestedIMEI, deviceId, channelName, accessToken, "yes");
                requestDialog.dismiss();
            }
        });

        btnActionNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestToJoin(requestedIMEI, deviceId, channelName, accessToken, "no");
                requestDialog.dismiss();

            }
        });



        imgAccept.setOnClickListener(v -> {

            requestToJoin(requestedIMEI, deviceId, channelName, accessToken, "yes");
            requestDialog.dismiss();

        });


        imgReject.setOnClickListener(v -> {

            requestToJoin(requestedIMEI, deviceId, channelName, accessToken, "no");
            requestDialog.dismiss();

        });




        requestDialog.setView(dialogView);
        requestDialog.show();

    }

    /*code for open invite dialog*/
    private void showInviteDialog(String requestedIMEI, String channelName, String accessToken, String deviceId, String messageBody) {

        Rect displayRectangle = new Rect();
        Window window = CallActivity.this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        inviteDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.call_request_popup_layout, null);
        dialogView.setMinimumWidth((int) (displayRectangle.width() * 1f));
        dialogView.setMinimumHeight((int) (displayRectangle.height() * 1f));
        inviteDialog.setView(dialogView);
        TextView tvMessage = dialogView.findViewById(R.id.tv_message);
        setFont(tvMessage);
        ImageView imgAccept = dialogView.findViewById(R.id.img_accept);
        ImageView imgReject = dialogView.findViewById(R.id.img_reject);
        TextView btnActionYes = dialogView.findViewById(R.id.btn_action_yes);
        btnActionYes.setText("Invite Accept");
        //setFont(btnActionYes);
        TextView btnActionNo = dialogView.findViewById(R.id.btn_action_no);
        btnActionNo.setText("Invite Reject");
        // setFont(btnActionNo);
        ImageView btnActionCancel = dialogView.findViewById(R.id.btn_action_cancel);
        //setFont(btnActionCancel);
        tvMessage.setText(messageBody);
        inviteDialog.setCancelable(false);

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

        imgAccept.setOnClickListener(v -> {

            inviteAccept(IMEI, deviceId, channelName, accessToken, "invite_accept");
            inviteDialog.dismiss();
        });

        imgReject.setOnClickListener(v -> {

            inviteAccept(IMEI, deviceId, channelName, accessToken, "invite_cancel");
            inviteDialog.dismiss();

        });


        inviteDialog.setView(dialogView);
        inviteDialog.show();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.i(LOG_TAG, "stop");
        super.onStop();
        doLeaveChannel();

        /*code for switch to new channel*/
        /*leave channel*/
        deInitUIandEvent();

        //  vSettings().mChannelName = channel;
        // String encryption = "";
        //vSettings().mEncryptionKey = encryption;
        setRoomStatus(IMEI, deviceId, "0", "", "", "", "");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (switchRoomListDialog != null) {
            switchRoomListDialog.dismiss();
            switchRoomListDialog = null;
        }


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //   setRoomStatus(IMEI, deviceId, "0", "", "", "", "");
        Intent i = new Intent(context, GridViewActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();

    }

    @Override
    public void onClick(int position) {
        if (switchRoomListDialog.isShowing()) {
            switchRoomListDialog.dismiss();
        }


        String newDeviceId = mRoomRequestList.get(position).getId();
        String accessToken = mRoomRequestList.get(position).getAppToken();
        String channelName = mRoomRequestList.get(position).getDeviceName();
        activeCount = mRoomRequestList.get(position).getActiveUserCount();
        if (!accessToken.isEmpty() && Integer.parseInt(activeCount) >= 2) {
            dialog.show();
            requestToJoin(IMEI, deviceId, channelName, accessToken, "request");
        } else {
            setRoomStatus(IMEI, deviceId, "0", "invite_accept", channelName, accessToken, newDeviceId);
            // forwardToRoom(channelName, accessToken, deviceId);
        }
    }

    private void requestToJoin(String requestedIMEI, String deviceId, String channelName, String accessToken, String type) {

        GetRoomsRequest request = new GetRoomsRequest().withIEMI(requestedIMEI).withDeviceId(deviceId)
                .withChannelName(channelName).withAppToken(accessToken).withSetType(type);

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.sendNotification(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (response.code() == Constant.SUCCESS_CODE) {
                    if (type.equals("request")) {
                        // showSnackBar(findViewById(R.id.main), getString(R.string.request_sent_successfully));
                    } else {
                        // showSnackBar(findViewById(R.id.main), getString(R.string.action_performed_successfully));
                    }

                } else {
                    // showSnackBar(findViewById(R.id.main), getString(R.string.some_error));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissProgress();
            }
        });

    }


    private void inviteAccept(String requestedIMEI, String device_Id, String channelName, String accessToken, String type) {

        GetRoomsRequest request = new GetRoomsRequest().withIEMI(requestedIMEI).withDeviceId(device_Id).withSetType(type);

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
                        // will write later on
                    } else if (type.equals("invite_accept")) {
                        setRoomStatus(IMEI, deviceId, "0", type, channelName, accessToken, device_Id);
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


    public void forwardToRoom(String channel, String accessToken, String deviceId) {

        if (!accessToken.isEmpty()) {

            doLeaveChannel();

            /*code for switch to new channel*/
            /*leave channel*/
            deInitUIandEvent();

            vSettings().mChannelName = channel;
            String encryption = "";
            vSettings().mEncryptionKey = encryption;

            /*join channel*/
            //  initUIandEvent();


            Intent i = new Intent(this, RoomsActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.putExtra(ConstantApp.DEVICE_ID, deviceId);
            i.putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME, channel);
            i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_KEY, encryption);
            i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_MODE, getResources().getStringArray(R.array.encryption_mode_values)[vSettings().mEncryptionModeIndex]);
            i.putExtra(ConstantApp.ACTION_KEY_TOKEN, accessToken);
            startActivity(i);
            finish();

        }
    }

    /*Get Rooms API*/
    public void getRooms(String imei, RecyclerView rvRooms, LinearLayout main, SwipeRefreshLayout swipeToRefresh, ProgressBar progressbar) {

        GetRoomsRequest request = new GetRoomsRequest().withIEMI(imei);

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.getRooms(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                if (response.code() == Constant.SUCCESS_CODE) {

                    mRoomRequestList.clear();
                    String str = Utils.getParsedString(response.body().byteStream());


                    try {
                        JSONObject object = new JSONObject(str);
                        JSONArray data = object.optJSONArray(Constant.DATA);

                        if (object.getString(Constant.REPLY_STATUS).equals(Constant.SUCCESS)) {
                            GsonBuilder builder = new GsonBuilder();
                            Gson gson = builder.serializeNulls().create();

                            int length = data.length();

                            if (length > 0) {
                                for (int i = 0; i < length; i++) {
                                    RoomRequest bean = gson.fromJson(data.getJSONObject(i).toString(), RoomRequest.class);
                                    if (!bean.getId().equals(deviceId)) {
                                        mRoomRequestList.add(bean);
                                    }
                                }
                            }

                            if (mRoomRequestList.isEmpty()) {
                                progressbar.setVisibility(View.GONE);
                                swipeToRefresh.setVisibility(View.GONE);
                                rvRooms.setVisibility(View.GONE);
                                roomsAdapter.notifyDataSetChanged();

                            } else {
                                progressbar.setVisibility(View.GONE);
                                swipeToRefresh.setVisibility(View.VISIBLE);
                                rvRooms.setVisibility(View.VISIBLE);
                                roomsAdapter.notifyDataSetChanged();
                            }

                        } else if (object.getString(Constant.REPLY_STATUS).equals(Constant.FAIL)) {

                            showSnackBar(main, object.getString(Constant.REPLY_MESSAGE));

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
                showSnackBar(main, getString(R.string.some_error));

            }
        });
    }

    private void setRoomStatus(String imei, String deviceId, String availability, String type, String channelName, String access_Token, String device_Id) {

        GetRoomsRequest request = new GetRoomsRequest().withIEMI(imei).withDeviceId(deviceId).withAvailability(availability);

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.changeAvailability(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // dismissProgress();
                if (response.code() == Constant.SUCCESS_CODE) {
                    if (availability.equals("1")) {
                        Constant.IS_CONFERENCE_GOING = true;
                    } else {
                        Constant.IS_CONFERENCE_GOING = false;
                    }
                    System.out.println("action performed successfully");

                    if (mSocket.connected()) {

                        String str = Utils.getParsedString(response.body().byteStream());

                        try {
                            object = new JSONObject(str);

                            if (object.getString(Constant.REPLY_STATUS).equals(Constant.SUCCESS)) {

                                JSONObject json = new JSONObject();
                                try {
                                    json.put("socket_device_id", object.getString("socket_device_id"));
                                    json.put("socket_user_count", object.getString("socket_user_count"));
                                    accessToken = object.getString("access_token");
                                    joinedDeviceName = object.getString("joined_device");


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                mSocket.emit("devices_update", json);

                                for (int j = 0; j < mRoomRequestList.size(); j++) {
                                    if (mRoomRequestList.get(j).getId().equals(object.getString("socket_device_id"))) {
                                        mRoomRequestList.get(j).setActiveUserCount(object.getString("socket_user_count"));
                                    }
                                }
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (roomsAdapter != null) {
                                            roomsAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                    } else {
                        // Toast.makeText(CallActivity.this, "internal connection error, please restart app", Toast.LENGTH_SHORT).show();
                    }

                    if (type.equals("invite_accept")) {
                        forwardToRoom(channelName, accessToken, device_Id);
                    }

                } else {
                    //  showSnackBar(main, getString(R.string.some_error));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissProgress();
            }
        });
    }

    @Override
    public void onItemClick(String id, boolean isChecked) {

        if (isChecked) {
            if (!inviteDeviceList.contains(id)) {
                inviteDeviceList.add(id);
            }
        } else {
            inviteDeviceList.remove(id);
        }
    }
}
