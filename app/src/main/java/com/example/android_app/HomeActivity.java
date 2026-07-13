package com.example.android_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        final Button zaehlerButton = findViewById(R.id.zaehler);
        final TextView zaehlerText = findViewById(R.id.textView);
        Globals g = Globals.getInstance();
        int counter = g.getCounter();
        String counterText = Integer.toString(counter);
        zaehlerText.setText(counterText);

        zaehlerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals g = Globals.getInstance();
                g.increase();
                int counter = g.getCounter();
                String counterText = Integer.toString(counter);
                zaehlerText.setText(counterText);

            }
        });
    }
}