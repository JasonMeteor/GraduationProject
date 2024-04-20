package mobileapp.graduationproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnTranslate;
    private Button btnTraining;
    private Button btnSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTranslate = findViewById(R.id.btn_translate);
        btnTraining = findViewById(R.id.btn_training);
        btnSetting = findViewById(R.id.btn_setting);

        View.OnClickListener switchLayoutListener = new View.OnClickListener() {

            Intent intent = new Intent();
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if(id == R.id.btn_translate){
                    intent.setClass(MainActivity.this, Translation.class);
                }
                else if(id == R.id.btn_training){
                    intent.setClass(MainActivity.this, Training1.class);
                }
                else if(id == R.id.btn_setting){
                    intent.setClass(MainActivity.this, Setting.class);
                }
                startActivity(intent);
            }
        };

        btnTranslate.setOnClickListener(switchLayoutListener);
        btnTraining.setOnClickListener(switchLayoutListener);
        btnSetting.setOnClickListener(switchLayoutListener);
    }
}
