package io.agora.openvcall.ui;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.agora.api.APIClient;
import io.agora.api.APIInterface;
import io.agora.openvcall.R;
import io.agora.openvcall.databinding.ActivityLoginBinding;
import io.agora.propeller.Constant;
import io.agora.requestmodels.LoginRequest;
import io.agora.responsemodels.LoginResponse;
import io.agora.util.ManageSession;
import io.agora.util.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.RECORD_AUDIO;

public class LoginActivity extends BaseActivity {


    private View main;
    private Context context;
    ActivityLoginBinding binding;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        context = this;
        setFont(binding.tvAppDescription);
        setFont(binding.etUserId);
        setFont(binding.etPassword);
        setFont(binding.btnLogin);
        main = findViewById(R.id.main);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("Base Activity", "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    token = task.getResult().getToken();
                    ManageSession.setPreference(context, ManageSession.DEVICE_TOKEN, token);
                    System.out.println("token in Splash activity ==" + token);

                });


        if (ManageSession.getBooleanPreference(context, ManageSession.LOGIN_STATUS)) {
            Intent intent = new Intent(context, GridViewActivity.class);
            intent.putExtra("token", token);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = binding.etUserId.getText().toString().toLowerCase().trim();
                String password = binding.etPassword.getText().toString();

                if (userId.isEmpty()) {
                    Utils.hideKeyboard(LoginActivity.this);
                    showSnackBar(main, getString(R.string.userid_required));
                } else if (password.isEmpty()) {
                    Utils.hideKeyboard(LoginActivity.this);
                    showSnackBar(main, getString(R.string.password_required));
                } else {

                   // throw new RuntimeException("Test Crash");
                   login(userId, password, IMEI);
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
    }


    @Override
    protected void initUIandEvent() {
    //    ((TextView) findViewById(R.id.ovc_page_title)).setText(R.string.app_name);
    }

    @Override
    protected void deInitUIandEvent() {

    }


    /*Login API*/
    public void login(String userName, String password, String imei) {
        showProgress();

        LoginRequest request = new LoginRequest()
                .withEmail(userName)
                .withPassword(password)
                .withIEMI(imei);

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.login(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                dismissProgress();
                if (response.code() == Constant.SUCCESS_CODE) {

                    String str = Utils.getParsedString(response.body().byteStream());


                    try {
                        JSONObject object = new JSONObject(str);
                        JSONObject data = object.optJSONObject(Constant.DATA);

                        if (object.getString(Constant.REPLY_STATUS).equals(Constant.SUCCESS)) {
                            GsonBuilder builder = new GsonBuilder();
                            Gson gson = builder.serializeNulls().create();
                            LoginResponse bean = gson.fromJson(data.toString(), LoginResponse.class);

                            boolean isCodeActivated = object.getBoolean("imei_is");

                            ManageSession.setPreference(context, ManageSession.COMPANY_ID, bean.getId());
                            ManageSession.setPreference(context, ManageSession.COMPANY_NAME, bean.getCompanyName());
                            ManageSession.setPreference(context, ManageSession.COMPANY_EMAIL, bean.getCompanyEmail());
                            ManageSession.setBooleanPreference(context, ManageSession.LOGIN_STATUS, true);

                            ManageSession.setBooleanPreference(context, ManageSession.CODE_ACTIVATED, isCodeActivated);

                            Intent homeIntent = new Intent(context, GridViewActivity.class);
                            homeIntent.putExtra("token", token);
                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(homeIntent);
                            finish();


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
}
