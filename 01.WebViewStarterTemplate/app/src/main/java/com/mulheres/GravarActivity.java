package com.mulheres;

import android.Manifest;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
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
    private View wave;

    private MediaRecorder recorder;
    private boolean recording = false;

    private String currentFile;

    private Handler handler = new Handler();
    private int seconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gravar);

        btnRecord = findViewById(R.id.btnRecord);
        timer = findViewById(R.id.timer);
        list = findViewById(R.id.list);
        wave = findViewById(R.id.wave);

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                1
        );

        btnRecord.setOnClickListener(v -> {
            if (!recording) startRecord();
            else stopRecord();
        });

        animateWave();
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

            pulse(btnRecord);

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
    // LISTA
    // ==========================

    private void addToList(String path) {

        TextView item = new TextView(this);

        item.setText("🎧 Gravação");
        item.setTextColor(0xFFFFFFFF);
        item.setTextSize(16f);
        item.setPadding(28, 28, 28, 28);
        item.setBackgroundColor(0x15FFFFFF);

        item.setOnClickListener(v -> {
            MediaPlayer mp = new MediaPlayer();
            try {
                mp.setDataSource(path);
                mp.prepare();
                mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        item.setOnLongClickListener(v -> {
            item.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        list.removeView(item);
                        new File(path).delete();
                    })
                    .start();
            return true;
        });

        fadeIn(item);
        list.addView(item);
    }

    // ==========================
    // ANIMAÇÕES
    // ==========================

    private void fadeIn(View v) {
        AlphaAnimation anim = new AlphaAnimation(0f, 1f);
        anim.setDuration(350);
        v.startAnimation(anim);
    }

    private void pulse(View v) {
        v.animate()
                .scaleX(1.12f)
                .scaleY(1.12f)
                .setDuration(300)
                .withEndAction(() -> v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .start())
                .start();
    }

    // ==========================
    // WAVE ANIMADO SIMPLES
    // ==========================

    private void animateWave() {

        wave.animate()
                .alpha(0.5f)
                .setDuration(600)
                .withEndAction(() -> wave.animate()
                        .alpha(1f)
                        .setDuration(600)
                        .start())
                .start();

        handler.postDelayed(this::animateWave, 800);
    }
}