package io.agora.openvcall.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.agora.adapter.NotificationAdapter;
import io.agora.api.APIClient;
import io.agora.api.APIInterface;
import io.agora.openvcall.R;
import io.agora.openvcall.databinding.ActivityNotificationBinding;
import io.agora.propeller.Constant;
import io.agora.requestmodels.LoginRequest;
import io.agora.responsemodels.NotificationList;
import io.agora.util.ConnectionDetector;
import io.agora.util.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class NotificationActivity extends BaseActivity {

    private String TAG = NotificationActivity.class.getSimpleName();
    private ArrayList<NotificationList> notificationList;
    private NotificationAdapter mAdapter;
    private Context mContext;
    private static final int REQUEST = 112;
    private ConnectionDetector cd;
    private View main;
    ActivityNotificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification);

        mContext = NotificationActivity.this;
        cd = new ConnectionDetector(mContext);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        main = findViewById(R.id.main);
        notificationList = new ArrayList<>();

        mAdapter = new NotificationAdapter(mContext, notificationList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        binding.notificationContentId.recyclerView.setLayoutManager(mLayoutManager);
        binding.notificationContentId.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.notificationContentId.recyclerView.setAdapter(mAdapter);


        if (cd.isConnectingToInternet()) {
            getNotificationList();
        } else {
            Utils.noInternet(mContext, main);
        }


    }

    @Override
    protected void initUIandEvent() {

    }

    @Override
    protected void deInitUIandEvent() {

    }


    private void getNotificationList() {

        showProgress();
        LoginRequest request = new LoginRequest().withIEMI(IMEI);

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.getNotification(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                dismissProgress();
                if (response.code() == Constant.SUCCESS_CODE) {

                    String str = Utils.getParsedString(response.body().byteStream());


                    try {
                        JSONObject object = new JSONObject(str);

                        if (object.getString(Constant.REPLY_STATUS).equals(Constant.SUCCESS)) {

                            JSONArray dataArray = object.getJSONArray(Constant.DATA);
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject obj = dataArray.getJSONObject(i);
                                NotificationList notification = new NotificationList();
                                String message = obj.getString("message");
                                String time = obj.getString("time");
                                String timeString = obj.getString("time_string");
                                notification.setMessage(message);
                                notification.setTime(time);
                                notification.setTimeString(timeString);
                                notificationList.add(notification);
                            }

                            if (notificationList.size() > 0) {
                                mAdapter.notifyDataSetChanged();
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
                dismissProgress();
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //  makeCall();
                } else {
                    Toast.makeText(mContext, "The app was not allowed to call.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}