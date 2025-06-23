package com.example.petsit;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class FirstActivity extends AppCompatActivity {

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first);

        // 1. Existing button click navigation
        LinearLayout containerBtn = findViewById(R.id.container_btn);
        containerBtn.setOnClickListener(view -> {
            startActivity(new Intent(FirstActivity.this, SecondActivity.class));
        });

        // 2. Swipe gesture detection (RIGHT-TO-LEFT)
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // Detect RIGHT-TO-LEFT swipe (e1 = start point, e2 = end point)
                if (e1.getX() - e2.getX() > 100 && Math.abs(velocityX) > 100) {
                    startActivity(new Intent(FirstActivity.this, SecondActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    // 3. Pass touch events to the gesture detector
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}