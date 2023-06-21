package org.pytorch.demo.objectdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class splash_screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash_screen);
        //Agregar animaciones
        Animation animacion1 = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_arriba);
        Animation animacion2 = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_abajo);

        TextView byTextView = findViewById(R.id.by_textview);
        TextView devTextView = findViewById(R.id.dev_textview);
        ImageView logoImageView = findViewById(R.id.logoimageview);

        byTextView.setAnimation(animacion2);
        devTextView.setAnimation(animacion2);
        logoImageView.setAnimation(animacion1);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent =new Intent(splash_screen.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        },4000);
    }
}