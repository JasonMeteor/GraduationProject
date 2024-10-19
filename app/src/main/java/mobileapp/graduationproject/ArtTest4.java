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

public class ArtTest4 extends AppCompatActivity {

    private Button btnRewind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_art_test4);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnRewind = findViewById(R.id.btn_rewind);

        // 回首頁
        View.OnClickListener switchLayoutListener = new View.OnClickListener() {
            Intent intent = new Intent();
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if(id == R.id.btn_rewind){
                    intent.setClass(ArtTest4.this, ArtTest3.class);
                }
                startActivity(intent);
            }
        };

        btnRewind.setOnClickListener(switchLayoutListener);
    }
}