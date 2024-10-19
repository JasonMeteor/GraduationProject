package mobileapp.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ArtTest3 extends AppCompatActivity {

    private Button btnRewind;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_art_test3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnRewind = findViewById(R.id.btn_rewind);
        btnNext = findViewById(R.id.btn_next);

        // 回首頁
        View.OnClickListener switchLayoutListener = new View.OnClickListener() {
            Intent intent = new Intent();
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if(id == R.id.btn_rewind){
                    intent.setClass(ArtTest3.this, ArtTest2.class);
                }
                else if(id == R.id.btn_next){
                    intent.setClass(ArtTest3.this, ArtTest4.class);
                }
                startActivity(intent);
            }
        };

        btnRewind.setOnClickListener(switchLayoutListener);
        btnNext.setOnClickListener(switchLayoutListener);
    }
}