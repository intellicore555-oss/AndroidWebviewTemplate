package com.mulheres;

import android.Manifest;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;

public class GravarActivity extends AppCompatActivity {

    private Button btnRecord;
    private TextView timer;
    private LinearLayout list;

    private MediaRecorder recorder;
    private boolean recording = false;

    private String currentFile;

    private Handler handler = new Handler();
    private int seconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gravar);

        setFullscreen();

        btnRecord = findViewById(R.id.btnRecord);
        timer = findViewById(R.id.timer);
        list = findViewById(R.id.list);

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                1
        );

        btnRecord.setOnClickListener(v -> {
            if (!recording) startRecord();
            else stopRecord();
        });
    }

    // ==========================
    // FULLSCREEN REAL
    // ==========================
    private void setFullscreen() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }

        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    // ==========================
    // GRAVAÇÃO
    // ==========================
    private void startRecord() {

        try {
            currentFile = getExternalFilesDir(null).getAbsolutePath()
                    + "/rec_" + System.currentTimeMillis() + ".3gp";

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(currentFile);

            recorder.prepare();
            recorder.start();

            recording = true;
            btnRecord.setText("■");

            seconds = 0;
            handler.post(timerRunnable);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecord() {

        try {
            recorder.stop();
            recorder.release();
            recorder = null;

            recording = false;
            btnRecord.setText("●");

            handler.removeCallbacks(timerRunnable);

            addToList(currentFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================
    // TIMER
    // ==========================
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            seconds++;

            int m = seconds / 60;
            int s = seconds % 60;

            timer.setText(String.format("%02d:%02d", m, s));

            handler.postDelayed(this, 1000);
        }
    };

    // ==========================
    // LISTA COM BORDA + DELETE
    // ==========================
    private void addToList(String path) {

        File file = new File(path);
        if (!file.exists()) return;

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(28, 28, 28, 28);
        container.setBackgroundResource(R.drawable.bg_record_item);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        params.setMargins(0, 0, 0, 20);
        container.setLayoutParams(params);

        TextView text = new TextView(this);
        text.setText("🎧 Gravação");
        text.setTextColor(0xFFFFFFFF);
        text.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button delete = new Button(this);
        delete.setText("🗑");
        delete.setBackgroundColor(0x00000000);
        delete.setTextColor(0xFFFF4D4D);

        container.addView(text);
        container.addView(delete);

        text.setOnClickListener(v -> {
            MediaPlayer mp = new MediaPlayer();
            try {
                mp.setDataSource(path);
                mp.prepare();
                mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        delete.setOnClickListener(v -> {
            container.animate()
                    .alpha(0f)
                    .translationX(80f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        list.removeView(container);
                        file.delete();
                    })
                    .start();
        });

        fadeIn(container);
        list.addView(container);
    }

    // ==========================
    // ANIMAÇÕES
    // ==========================
    private void fadeIn(View v) {
        AlphaAnimation anim = new AlphaAnimation(0f, 1f);
        anim.setDuration(300);
        v.startAnimation(anim);
    }
}