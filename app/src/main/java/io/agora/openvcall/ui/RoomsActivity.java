package io.agora.openvcall.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import io.agora.adapter.RoomsAdapter;
import io.agora.openvcall.R;
import io.agora.openvcall.model.ConstantApp;

public class RoomsActivity extends BaseActivity implements RoomsAdapter.Listener {


    private static final String LOG_TAG = RoomsActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_CODE = 5;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        context = this;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showProgress();
                forwardToRoom(getIntent().getStringExtra(ConstantApp.DEVICE_ID), getIntent().getStringExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME), getIntent().getStringExtra(ConstantApp.ACTION_KEY_TOKEN));
            }
        }, 5000);


    }

    @Override
    protected void initUIandEvent() {


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
    public void onClick(int position) {
    }


    public void forwardToRoom(String deviceId, String channel, String accessToken) {

        if (!accessToken.isEmpty()) {

            vSettings().mChannelName = channel;
            String encryption = null;
            vSettings().mEncryptionKey = encryption;

            dismissProgress();
            Intent i = new Intent(RoomsActivity.this, CallActivity.class);
            i.putExtra(ConstantApp.DEVICE_ID, deviceId);
            i.putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME, channel);
            i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_KEY, encryption);
            i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_MODE, getResources().getStringArray(R.array.encryption_mode_values)[vSettings().mEncryptionModeIndex]);
            i.putExtra(ConstantApp.ACTION_KEY_TOKEN, accessToken);
            startActivity(i);
            finish();

        }
    }


    public void onClickDoNetworkTest(View view) {
        Intent i = new Intent(RoomsActivity.this, NetworkTestActivity.class);
        startActivity(i);
    }

    @Override
    public void workerThreadReady() {

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(LOG_TAG, "stop");
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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


}

