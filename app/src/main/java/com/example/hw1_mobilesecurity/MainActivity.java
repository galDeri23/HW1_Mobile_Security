package com.example.hw1_mobilesecurity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hw1_mobilesecurity.databinding.ActivityMainBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private  ActivityMainBinding binding;
    private boolean isColorDetected = false;
    private boolean isStepsPassed = false;
    private boolean isBatteryMatched = false;
    private boolean isWifiConnected = false;
    private final int CAMERA_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();


        binding.tvCamera.setOnClickListener(v -> {checkColorCondition();});
        binding.tvStepsCondition.setOnClickListener(v -> {checkStepsCondition();});
        binding.tvBatteryStatus.setOnClickListener(v -> {checkBatteryCondition();});
        binding.tvWifiStatus.setOnClickListener(v -> {checkWifiCondition();});

        // מאזין לשינוי סיסמה
        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkBatteryCondition(); // כל שינוי בטקסט - נבדוק מחדש התאמת סיסמה
            }
            @Override public void afterTextChanged(Editable s) {}
        });


        binding.btnLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Login successful 🎉", Toast.LENGTH_SHORT).show();
        });
    }

    private void init () {
        binding.tvColorStatus.setText("✕");
        binding.tvStepsStatus.setText("✕");
        binding.tvBatteryStatus.setText("✕");
        binding.tvWifiStatus.setText("✕");
        binding.btnLogin.setEnabled(false);
    }

    private void checkColorCondition() {
        Toast.makeText(this, "Please photograph a WHITE surface (like a sheet of paper)", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // קיבלנו את התמונה שצולמה
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            if (isWhiteSurface(imageBitmap)) {
                // ✔️ המשטח לבן
                binding.tvColorStatus.setText("✓");
                binding.tvColorStatus.setTextColor(Color.parseColor("green"));
                isColorDetected = true;
            } else {
                // ❌ המשטח לא מספיק לבן
                binding.tvColorStatus.setText("✕");
                binding.tvColorStatus.setTextColor(Color.parseColor("red"));
                isColorDetected = false;

                Toast.makeText(this, "The surface is not white enough ❌", Toast.LENGTH_SHORT).show();
            }

            updateLoginButtonState();
        }
    }
    private boolean isWhiteSurface(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int whitePixels = 0;
        int totalPixels = 0;

        for (int x = width / 4; x < 3 * width / 4; x += 5) {
            for (int y = height / 4; y < 3 * height / 4; y += 5) {
                int pixel = bitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                if (r > 220 && g > 220 && b > 220) {
                    whitePixels++;
                }
                totalPixels++;
            }
        }

        return ((float) whitePixels / totalPixels) > 0.6f;
    }

    private void checkStepsCondition() {
        // נבצע מימוש מלא בהמשך
        isStepsPassed = false;
        updateLoginButtonState();
    }

    private void checkBatteryCondition() {
        // מימוש זמני לפי התאמת סיסמה
        int batteryLevel = getBatteryPercentage();
        String input = binding.etPassword.getText().toString();

        if (!input.isEmpty() && Integer.toString(batteryLevel).equals(input)) {
            isBatteryMatched = true;
            binding.tvBatteryStatus.setText("✓");
            binding.tvBatteryStatus.setTextColor(Color.parseColor("green"));
        } else {
            isBatteryMatched = false;
            binding.tvBatteryStatus.setText("✕");
            binding.tvBatteryStatus.setTextColor(Color.parseColor("green"));
        }

        updateLoginButtonState();
    }

    private void checkWifiCondition() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI) {
            isWifiConnected = true;
            binding.tvWifiStatus.setText("✓");
            binding.tvWifiStatus.setTextColor(Color.parseColor("#388E3C"));
        } else {
            isWifiConnected = false;
            binding.tvWifiStatus.setText("✕");
            binding.tvWifiStatus.setTextColor(Color.parseColor("#B00020"));
        }

        updateLoginButtonState();
    }

    private int getBatteryPercentage() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
        return (int) ((level / (float) scale) * 100);
    }

    private void updateLoginButtonState() {
        boolean allConditions = isColorDetected && isStepsPassed && isBatteryMatched && isWifiConnected;
        binding.btnLogin.setEnabled(allConditions);
    }
}