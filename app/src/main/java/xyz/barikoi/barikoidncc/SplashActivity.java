package xyz.barikoi.barikoidncc;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class SplashActivity extends AppCompatActivity {
    ImageView dncc;
    ProgressBar pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        dncc=findViewById(R.id.dncc);
        pd=findViewById(R.id.pd);
        pd.setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
              Intent intent=new Intent(SplashActivity.this,MainActivity.class);
              startActivity(intent);
              finish();
            }
        }, 1000);
    }
}
