package mobileapp.graduationproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnTranslate;
    private Button btnTwoWayTrans;
    private Button btnSetting;
    private Button btnGoToAPITest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTranslate = findViewById(R.id.btn_translate);
        btnTwoWayTrans = findViewById(R.id.btn_twowaytrans);
        btnSetting = findViewById(R.id.btn_setting);
        btnGoToAPITest = findViewById(R.id.btn_gotoapitest);

        View.OnClickListener switchLayoutListener = new View.OnClickListener() {

            Intent intent = new Intent();
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if(id == R.id.btn_translate){
                    intent.setClass(MainActivity.this, Translation.class);
                }
                else if(id == R.id.btn_twowaytrans){
                    intent.setClass(MainActivity.this, TwoWayTranslation.class);
                }
                else if (id == R.id.btn_setting){
                    ;intent.setClass(MainActivity.this, Setting.class);
                }
                else if(id == R.id.btn_gotoapitest){
                    intent.setClass(MainActivity.this, ArtTest.class);
                }
                startActivity(intent);
            }
        };

        btnTranslate.setOnClickListener(switchLayoutListener);
        btnTwoWayTrans.setOnClickListener(switchLayoutListener);
        btnSetting.setOnClickListener(switchLayoutListener);
        btnGoToAPITest.setOnClickListener(switchLayoutListener);
    }
}
