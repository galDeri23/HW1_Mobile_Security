package com.example.hw1_mobilesecurity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.hw1_mobilesecurity.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final int CAMERA_REQUEST_CODE = 123;

    private boolean isColorDetected = false;
    private boolean isWifiConnected = false;
    private boolean isBluetoothOn = false;
    private boolean isCaptchaPassed = false;
    private boolean isBatteryMatched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();

        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.btnLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Login successful 🎉", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        binding.btnCheckColor.setOnClickListener(v -> checkColorCondition());
        binding.btnCheckWifi.setOnClickListener(v -> checkWifiCondition());
        binding.btnCheckBluetooth.setOnClickListener(v -> checkBluetoothCondition());
        binding.btnCheckCaptcha.setOnClickListener(v -> checkCaptchaCondition());
        binding.btnCheckBattery.setOnClickListener(v -> checkBatteryCondition());
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
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            if (isWhiteSurface(imageBitmap)) {
                isColorDetected = true;
                binding.btnCheckColor.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green));
            } else {
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


        for (int x = width / 6; x < 5 * width / 6; x += 3) {
            for (int y = height / 6; y < 5 * height / 6; y += 3) {
                int pixel = bitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                int brightness = (r + g + b) / 3;


                if (brightness > 180 && r > 160 && g > 160 && b > 160) {
                    whitePixels++;
                }

                totalPixels++;
            }
        }

        float ratio = (float) whitePixels / totalPixels;

        return ratio > 0.5f; // 50% במקום 60%
    }

    private void checkWifiCondition() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI) {
            isWifiConnected = true;
            binding.btnCheckWifi.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green));
        } else {
            isWifiConnected = false;
            Toast.makeText(this, "Not connected to Wi-Fi ❌", Toast.LENGTH_SHORT).show();
        }
        updateLoginButtonState();
    }

    private void checkBluetoothCondition() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device ❌", Toast.LENGTH_LONG).show();
            return;
        }

        if (bluetoothAdapter.isEnabled()) {
            isBluetoothOn = true;
            binding.btnCheckBluetooth.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green));
        } else {
            isBluetoothOn = false;
            Toast.makeText(this, "Bluetooth is OFF ❌", Toast.LENGTH_SHORT).show();
        }

        updateLoginButtonState();
    }

    private void checkCaptchaCondition() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Prove you're human 🧠");
        builder.setMessage("Which of the buttons is green?");

        builder.setPositiveButton("🟥", (dialog, which) -> {
            Toast.makeText(this, "Oops! That's not green ❌", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("🟦", (dialog, which) -> {
            Toast.makeText(this, "Nope! That's blue ❌", Toast.LENGTH_SHORT).show();
        });

        builder.setNeutralButton("🟩", (dialog, which) -> {
            isCaptchaPassed = true;
            binding.btnCheckCaptcha.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green));
            updateLoginButtonState();
            Toast.makeText(this, "Nice! You're definitely human ✅", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }


    private void checkBatteryCondition() {
        int batteryLevel = getBatteryPercentage();
        String input = binding.etPassword.getText().toString();

        if (!input.isEmpty() && Integer.toString(batteryLevel).equals(input)) {
            isBatteryMatched = true;
            binding.btnCheckBattery.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green));
        } else {
            isBatteryMatched = false;
            Toast.makeText(this, "Battery % doesn't match input ❌", Toast.LENGTH_SHORT).show();
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
        boolean allConditions = isColorDetected && isWifiConnected && isBluetoothOn && isCaptchaPassed && isBatteryMatched;
        binding.btnLogin.setEnabled(allConditions);
    }
}
