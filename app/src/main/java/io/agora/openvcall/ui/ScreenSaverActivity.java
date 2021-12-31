package io.agora.openvcall.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.iid.FirebaseInstanceId;
import io.agora.openvcall.R;
import io.agora.openvcall.databinding.ActivityScreenSaverBinding;
import io.agora.util.ManageSession;

public class ScreenSaverActivity extends Activity implements Animation.AnimationListener {

    Animation animSequential;
    private Context context;
    String token;
    private static final String TAG = SplashActivity.class.getSimpleName();
    ActivityScreenSaverBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_screen_saver);
        animSequential = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.sequential);
        animSequential.setAnimationListener(this);

        context = this;
        setFont(binding.tvAppDescription);

       // setFont(binding.llClose);
       // Button btnGetStarted = findViewById(R.id.btn_get_started);
        //ImageView imgBounce = findViewById(R.id.img_bounce);

        YoYo.with(Techniques.Bounce)
                .duration(1200)
                .repeat(YoYo.INFINITE)
               // .pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT)
                .interpolate(new AccelerateDecelerateInterpolator()).
                playOn(binding.imgBounce);

        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // code for portrait mode
            Log.d("tag", "Portrait");
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)binding.scrollContainer.getLayoutParams();
            params.setMargins(0, 450, 0, 0);
            binding.scrollContainer.setLayoutParams(params);
            binding.container.setBackgroundResource(R.drawable.splash);
        } else {
            // code for landscape mode
            binding.container.setBackgroundResource(R.drawable.splash_landscape);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)binding.scrollContainer.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            binding.scrollContainer.setLayoutParams(params);
            Log.d("tag", "Landscape");
        }

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


        binding.llClose.setOnClickListener(view -> {

               // MainActivity.IS_SCREEN_SAVER_RUNNING=false;
            Intent i = new Intent(context, GridViewActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();

           // finish();

        });

        binding.scrollContainer.setOnClickListener(view -> {

            // MainActivity.IS_SCREEN_SAVER_RUNNING=false;
            Intent i = new Intent(context, GridViewActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();

            // finish();

        });

//        binding.scrollContainer1.setOnClickListener(view -> {
//
//            // MainActivity.IS_SCREEN_SAVER_RUNNING=false;
//            Intent i = new Intent(context, GridViewActivity.class);
//            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(i);
//            finish();
//
//            // finish();
//
//        });



        binding.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // GridViewActivity.IS_SCREEN_SAVER_RUNNING=false;
                Intent i = new Intent(context, GridViewActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
              //  finish();

                finish();
            }
        });

        binding.tvMessage.startAnimation(animSequential);

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
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

//    private int getDisplayHeight() {
//        return this.getResources().getDisplayMetrics().heightPixels;
//    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
      //  MainActivity.IS_SCREEN_SAVER_RUNNING=false;
        Intent i = new Intent(context, GridViewActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.d("tag", "config changed");
        super.onConfigurationChanged(newConfig);

        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            Log.d("tag", "Portrait");
            binding.container.setBackgroundResource(R.drawable.splash);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)binding.scrollContainer.getLayoutParams();
            params.setMargins(0, 450, 0, 0);
            binding.scrollContainer.setLayoutParams(params);
        }

        else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.container.setBackgroundResource(R.drawable.splash_landscape);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)binding.scrollContainer.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            binding.scrollContainer.setLayoutParams(params);
            Log.d("tag", "Landscape");
        }
        else
            Log.w("tag", "other: " + orientation);


    }
}
