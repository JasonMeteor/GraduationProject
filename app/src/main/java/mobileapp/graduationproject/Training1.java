package mobileapp.graduationproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Training1 extends AppCompatActivity {

    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training1);

        btnBack = findViewById(R.id.btn_back);

        View.OnClickListener backHomeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Training1.this, MainActivity.class);
                startActivity(intent);
            }
        };

        btnBack.setOnClickListener(backHomeListener);
    }
}
