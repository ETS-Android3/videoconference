package io.agora.openvcall.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import io.agora.api.APIClient;
import io.agora.api.APIInterface;
import io.agora.openvcall.R;
import io.agora.openvcall.model.ConstantApp;
import io.agora.openvcall.ui.layout.SettingsButtonDecoration;
import io.agora.openvcall.ui.layout.VideoEncResolutionAdapter;
import io.agora.propeller.Constant;
import io.agora.requestmodels.CheckCodeRequest;
import io.agora.requestmodels.DeviceSettingModel;
import io.agora.util.ManageSession;
import io.agora.util.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends BaseActivity {

    private Context context;
    private AlertDialog editDialog;
    ProgressDialog dialog;
    private View main;
    TextView tvDeviceName, tvLogout;
    RadioButton gridRadioButton1, gridRadioButton2;
    RadioGroup gridRadioGroup;
    RadioButton gridRadioAuto, gridRadioRing;
    RadioGroup gridRadioPickup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        context = this;

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ab.setCustomView(R.layout.ard_agora_actionbar_with_back_btn);
        }


        setupUI();

        gridRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    RadioButton radioButton = findViewById(checkedId);
                    ManageSession.setPreference(context, ManageSession.GRID_TYPE, radioButton.getText().toString());
                    Intent i = new Intent(context, GridViewActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
        );

        gridRadioPickup.setOnCheckedChangeListener((group, checkedId) -> {
                    RadioButton radioButton = findViewById(checkedId);
                    ManageSession.setPreference(context, ManageSession.PICK_UP_SETTINGS, radioButton.getText().toString());
                    Utils.callSettings = radioButton.getText().toString();
                    changePickUpSettings(IMEI, radioButton.getText().toString());

                }
        );
    }

    @Override
    protected void initUIandEvent() {

    }

    @Override
    protected void deInitUIandEvent() {

    }

    private void setupUI() {
        ((TextView) findViewById(R.id.ovc_page_title)).setText(R.string.label_settings);

        gridRadioGroup = findViewById(R.id.gridRadioGroup);
        gridRadioButton1 = findViewById(R.id.gridRadioButton1);
        gridRadioButton2 = findViewById(R.id.gridRadioButton2);

        gridRadioPickup = findViewById(R.id.gridRadioPickup);
        gridRadioAuto = findViewById(R.id.gridRadioAuto);
        gridRadioRing = findViewById(R.id.gridRadioRing);

        if (ManageSession.getPreference(context, ManageSession.GRID_TYPE).equals("3X2")) {
            gridRadioButton1.setChecked(true);

        } else if (ManageSession.getPreference(context, ManageSession.GRID_TYPE).equals("4X2")) {
            gridRadioButton2.setChecked(true);
            ;
        } else {
            gridRadioButton1.setChecked(true);

        }


        if (ManageSession.getPreference(context, ManageSession.PICK_UP_SETTINGS).equals(getString(R.string.auto))) {
            gridRadioAuto.setChecked(true);
            Utils.callSettings = getString(R.string.auto);
        } else if (ManageSession.getPreference(context, ManageSession.PICK_UP_SETTINGS).equals(getString(R.string.manual))) {
            gridRadioRing.setChecked(true);
            Utils.callSettings = getString(R.string.manual);
        } else {
            gridRadioRing.setChecked(true);
            Utils.callSettings = getString(R.string.manual);

        }

        main = findViewById(R.id.main);
        dialog = new ProgressDialog(this);
        dialog.setMessage("sending your request, please wait...");

        RecyclerView videoResolutionList = (RecyclerView) findViewById(R.id.settings_video_resolution);
        videoResolutionList.setHasFixedSize(true);
        videoResolutionList.addItemDecoration(new SettingsButtonDecoration());

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int resolutionIdx = pref.getInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_RESOLUTION, ConstantApp.DEFAULT_VIDEO_ENC_RESOLUTION_IDX);
        int fpsIdx = pref.getInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_FPS, ConstantApp.DEFAULT_VIDEO_ENC_FPS_IDX);

        VideoEncResolutionAdapter videoResolutionAdapter = new VideoEncResolutionAdapter(this, resolutionIdx);
        videoResolutionAdapter.setHasStableIds(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3, RecyclerView.VERTICAL, false);
        videoResolutionList.setLayoutManager(layoutManager);

        videoResolutionList.setAdapter(videoResolutionAdapter);

        Spinner videoFpsSpinner = (Spinner) findViewById(R.id.settings_video_frame_rate);

        ArrayAdapter<CharSequence> videoFpsAdapter = ArrayAdapter.createFromResource(this,
                R.array.string_array_frame_rate, R.layout.simple_spinner_item_light);
        videoFpsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        videoFpsSpinner.setAdapter(videoFpsAdapter);

        videoFpsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_FPS, position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        videoFpsSpinner.setSelection(fpsIdx);
        tvDeviceName = (TextView) findViewById(R.id.tv_device_name);
        tvDeviceName.setText(Utils.deviceName);
        ImageView imgEdit = (ImageView) findViewById(R.id.img_edit);
        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInviteDialog();


            }
        });

        tvLogout = (TextView) findViewById(R.id.tvLogout);
        tvLogout.setText("Logout of " + Utils.deviceName);
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManageSession.clearPreference(context);
                Intent loginIntent = new Intent(context, LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
            }
        });
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    private void showInviteDialog() {

        editDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_device_name_layout, null);
        Button btnApply = dialogView.findViewById(R.id.btn_apply);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        EditText etNewDeviceName = dialogView.findViewById(R.id.et_new_device_name);

        editDialog.setCancelable(false);

        btnApply.setOnClickListener(v -> {

            if (etNewDeviceName.getText().toString().trim().isEmpty() || etNewDeviceName.getText().toString().length() == 0) {
                etNewDeviceName.setError(getString(R.string.field_cannot_be_empty_please_fill));
            } else {
                changeDeviceName(IMEI, ManageSession.getPreference(context, ManageSession.COMPANY_ID), etNewDeviceName.getText().toString().trim().toLowerCase());
            }

        });

        btnCancel.setOnClickListener(v -> {

            editDialog.dismiss();

        });


        editDialog.setView(dialogView);
        editDialog.show();

    }

    /*Change Device Name API*/
    public void changeDeviceName(String imei, String companyId, String deviceName) {

        dialog.show();

        CheckCodeRequest request = new CheckCodeRequest()
                .withCompanyId(companyId)
                .withIEMI(imei)
                .withNewName(deviceName);


        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.updateDeviceName(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (response.code() == Constant.SUCCESS_CODE) {

                    String str = Utils.getParsedString(response.body().byteStream());


                    try {
                        JSONObject object = new JSONObject(str);
                        JSONObject data = object.optJSONObject(Constant.DATA);

                        if (object.getString(Constant.REPLY_STATUS).equals(Constant.SUCCESS)) {

                            Utils.deviceName = deviceName;
                            //tvDeviceName.setText(Utils.deviceName);
                            // tvLogout.setText("Logout of " + Utils.deviceName);
                            editDialog.dismiss();
                            Intent i = new Intent(context, GridViewActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);


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
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                // dismissProgress();
            }
        });
    }


    /*Change Pick up settings */
    public void changePickUpSettings(String imei, String deviceSettings) {

        dialog.show();

        DeviceSettingModel request = new DeviceSettingModel()
                .withIEMI(imei)
                .withSetting(deviceSettings);


        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.updateDeviceSettings(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (response.code() == Constant.SUCCESS_CODE) {

                    String str = Utils.getParsedString(response.body().byteStream());


                    try {
                        JSONObject object = new JSONObject(str);
                        JSONObject data = object.optJSONObject(Constant.DATA);

                        if (object.getString(Constant.REPLY_STATUS).equals(Constant.SUCCESS)) {


                            Intent i = new Intent(context, GridViewActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);


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
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                // dismissProgress();
            }
        });
    }

}