package com.example.hw1_mobilesecurity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hw1_mobilesecurity.databinding.ActivitySuccessBinding;

public class SuccessActivity extends AppCompatActivity {

    private ActivitySuccessBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvSuccess.setText("Login Successful! ✅");
    }
}