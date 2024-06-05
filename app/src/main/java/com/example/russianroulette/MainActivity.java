package com.example.russianroulette;

import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private MediaPlayer mediaPlayer;
    private ImageView revolver;
    private final AppCompatButton[] bullets = new AppCompatButton[6];

    private boolean isSpinning = false;
    private boolean hammerPressed = false;
    private final boolean[] isChambersLoaded = new boolean[] {false, false, false, false, false, false};
    private int onChamber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        revolver = findViewById(R.id.revolver);

        ConstraintLayout constraintLayout = findViewById(R.id.bg);
        Button hammerButton = findViewById(R.id.hammer_button);
        Button spinButton = findViewById(R.id.spin_button);
        Button shootButton = findViewById(R.id.shoot_button);

        bullets[0] = findViewById(R.id.bullet_1);
        bullets[1] = findViewById(R.id.bullet_2);
        bullets[2] = findViewById(R.id.bullet_3);
        bullets[3] = findViewById(R.id.bullet_4);
        bullets[4] = findViewById(R.id.bullet_5);
        bullets[5] = findViewById(R.id.bullet_6);

        constraintLayout.setBackground(convertInputStreamToDrawable("bg/bg.png"));
        revolver.setBackground(convertInputStreamToDrawable("revolver/idle.png"));

        hammerButton.setOnClickListener(this::hammerButtonOnClickHandler);
        spinButton.setOnClickListener(this::spinButtonOnClickHandler);
        shootButton.setOnClickListener(this::shootButtonOnClickHandler);

        for (int i = 0; i < 6; i++) {
            int j = i;
            bullets[j].setOnClickListener(view -> bulletClickHandler(j));
        }

    }

    @Override
    protected void onResume() {

        super.onResume();
        hideStatusBar();

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        hideStatusBar();
        return super.dispatchTouchEvent(ev);

    }

    private void hideStatusBar() {

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

    }

    private void hammerButtonOnClickHandler(View view) {

        if (hammerPressed) return;

        hammerPressed = true;
        playAudio("hammer.mp3");
        revolver.setBackground(convertInputStreamToDrawable("revolver/hammer.png"));

    }

    private void spinButtonOnClickHandler(View view) {

        if (isSpinning) return;

        playAudio("spin.mp3");

        // stop the recursion at 3 seconds
        long startTime = System.currentTimeMillis();
        long waitTime = 3000;
        long endTime = startTime + waitTime;
        isSpinning = true;
        onChamber = (new Random()).nextInt(6);
        startSpinAnimation(endTime, 0);

    }

    private void startSpinAnimation(long endTime, int i) {

        handler.postDelayed(() -> {

            revolver.setBackground(convertInputStreamToDrawable(
                    // each round, the cylinder is made to spin by alternating between
                    // no spin and spin images
                    i % 2 == 0 ?
                            hammerPressed ? "revolver/hammer.png" : "revolver/idle.png" :
                            hammerPressed ? "revolver/hammer_with_spin.png" : "revolver/idle_with_spin.png"
            ));
            // stop if 3 seconds is over
            if (System.currentTimeMillis() >= endTime) {
                revolver.setBackground(convertInputStreamToDrawable(
                            hammerPressed ? "revolver/hammer.png" : "revolver/idle.png"
                ));
                isSpinning = false;
                return;
            }
            startSpinAnimation(endTime, i + 1);

        // give the first round a fast start
        }, i == 0 ? 50 : 150);

    }

    private void shootButtonOnClickHandler(View view) {

        if (isSpinning) return;

        hammerPressed = false;
        revolver.setBackground(convertInputStreamToDrawable("revolver/hammer.png"));

        if (isChambersLoaded[onChamber]) {
            playAudio("gunshot.mp3");
            handler.postDelayed(() -> revolver.setBackground(convertInputStreamToDrawable("revolver/gunshot.png")),  200);
        } else {
            playAudio("dry_fire.mp3");
        }
        
        handler.postDelayed(() -> revolver.setBackground(convertInputStreamToDrawable("revolver/idle_with_spin.png")), 250);
        handler.postDelayed(() -> revolver.setBackground(convertInputStreamToDrawable("revolver/idle.png")), 300);

        onChamber = onChamber == 5 ? 0 : onChamber + 1;

    }

    private void bulletClickHandler(int j) {

        if (isChambersLoaded[j]) {
            bullets[j].setBackgroundColor(0x00000000);
            isChambersLoaded[j] = false;
        } else {
            bullets[j].setBackground(convertInputStreamToDrawable("revolver/bullet.png"));
            isChambersLoaded[j] = true;
        }

    }

    private Drawable convertInputStreamToDrawable(String imageUrl) {

        try {

            InputStream is = getAssets().open("images/" + imageUrl);
            return Drawable.createFromStream(is, null);

        } catch (IOException e) {

            Toast.makeText(this, "Image loading failed", Toast.LENGTH_SHORT).show();
            return null;

        }

    }

    public void playAudio(String audioUrl) {

        try {

            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();

            AssetFileDescriptor descriptor = this.getAssets().openFd("audios/" + audioUrl);
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (Exception e) {

            Toast.makeText(this, "Audio playing failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();

        }

    }
}
