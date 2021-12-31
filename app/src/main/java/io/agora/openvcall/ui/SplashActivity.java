package io.agora.openvcall.ui;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.iid.FirebaseInstanceId;

import io.agora.openvcall.R;
import io.agora.openvcall.databinding.ActivitySplashBinding;
import io.agora.util.ManageSession;

public class SplashActivity extends Activity {


    private Context context;
    String token;
    private static final String TAG = SplashActivity.class.getSimpleName();
    ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        context = this;
       // setFont(binding.tvAppDescription);
      //  setFont(binding.btnGetStarted);
        //Button btnGetStarted = findViewById(R.id.btn_get_started);
       // binding.btnGetStarted.setText(getString(R.string.get_started));
        //ImageView imgBounce = findViewById(R.id.img_bounce);
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // code for portrait mode
            Log.d("tag", "Portrait");
            binding.llBackground.setBackgroundResource(R.drawable.splash);
        } else {
            // code for landscape mode
            binding.llBackground.setBackgroundResource(R.drawable.splash_landscape);
            Log.d("tag", "Landscape");
        }


        YoYo.with(Techniques.Bounce)
                .duration(1200)
                .repeat(YoYo.INFINITE)
                .pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT)
                .interpolate(new AccelerateDecelerateInterpolator()).
                playOn(binding.imgBounce);


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


        binding.btnGetStarted.setOnClickListener(view -> {

            if (ManageSession.getBooleanPreference(context, ManageSession.LOGIN_STATUS)) {
                Intent intent = new Intent(context, GridViewActivity.class);
                intent.putExtra("token", token);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Intent i = new Intent(context, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });



    }

    public void setFont(TextView tv) {
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Poppins-Light.ttf");
        tv.setTypeface(face);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {

        Log.d("tag", "config changed");
        super.onConfigurationChanged(newConfig);

        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            Log.d("tag", "Portrait");
            binding.llBackground.setBackgroundResource(R.drawable.splash);
        }

        else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.llBackground.setBackgroundResource(R.drawable.splash_landscape);
            Log.d("tag", "Landscape");
        }
        else
            Log.w("tag", "other: " + orientation);


    }



}
