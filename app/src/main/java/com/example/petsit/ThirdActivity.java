package com.example.petsit; // Replace with your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class ThirdActivity extends AppCompatActivity {

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.third); // Make sure this matches the second.xml layout

        LinearLayout containerBtn = findViewById(R.id.container_btn);
        ImageView imageVector = findViewById(R.id.img_vector1);

        imageVector.setOnClickListener(view -> {
            Intent intent = new Intent(ThirdActivity.this, SecondActivity.class);
            startActivity(intent);
        });

        containerBtn.setOnClickListener(view -> {
            Intent intent = new Intent(ThirdActivity.this, RoleActivity.class);
            startActivity(intent);
        });
        // Swipe gesture detection (BOTH DIRECTIONS)
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e1.getX() - e2.getX();
                float diffY = e1.getY() - e2.getY();

                // Right-to-left swipe (go to ThirdActivity)
                if (Math.abs(diffX) > Math.abs(diffY) && diffX > 100 && Math.abs(velocityX) > 100) {
                    startActivity(new Intent(ThirdActivity.this, RoleActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;
                }
                // Left-to-right swipe (go back to FirstActivity)
                else if (Math.abs(diffX) > Math.abs(diffY) && diffX < -100 && Math.abs(velocityX) > 100) {
                    startActivity(new Intent(ThirdActivity.this, SecondActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}