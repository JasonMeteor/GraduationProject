package mobileapp.graduationproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Setting extends AppCompatActivity {

    private Button btnBack, btnSave;
    private Spinner spnSoloVoice, spnDuelVoiceA, spnDuelVoiceB;

    String[] dummySoundDatabase = new String[] {"聲庫1", "聲庫2", "聲庫3"}; // 下拉式選單的內容

    private int selectedSoloVoice, selectedDuelVoiceA, selectedDuelVoiceB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack = findViewById(R.id.btn_fromSettingToMain);
        btnSave = findViewById(R.id.btn_save_setting);
        spnSoloVoice = findViewById(R.id.spn_single_trans);
        spnDuelVoiceA = findViewById(R.id.spn_double_trans_A);
        spnDuelVoiceB = findViewById(R.id.spn_double_trans_B);

        loadSetting();

        // 下拉式選單設定
        ArrayAdapter<String> adapterVoice = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dummySoundDatabase);
        adapterVoice.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSoloVoice.setAdapter(adapterVoice);
        spnDuelVoiceA.setAdapter(adapterVoice);
        spnDuelVoiceB.setAdapter(adapterVoice);

        // 下拉式選單監聽
        AdapterView.OnItemSelectedListener spnSoloListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSoloVoice = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        // 下拉式選單監聽
        AdapterView.OnItemSelectedListener spnDuelAListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDuelVoiceA = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        // 下拉式選單監聽
        AdapterView.OnItemSelectedListener spnDuelBListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDuelVoiceB = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        View.OnClickListener btnListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.btn_fromSettingToMain) {
                    Intent intent = new Intent();
                    intent.setClass(Setting.this, MainActivity.class);
                    startActivity(intent);
                } else if (id == R.id.btn_save_setting) {
                    saveSetting();
                }
            }
        };

        btnBack.setOnClickListener(btnListener);
        btnSave.setOnClickListener(btnListener);
        spnSoloVoice.setOnItemSelectedListener(spnSoloListener);
        spnSoloVoice.setSelection(selectedSoloVoice); //預設選擇
        spnDuelVoiceA.setOnItemSelectedListener(spnDuelAListener);
        spnDuelVoiceA.setSelection(selectedDuelVoiceA); //預設選擇
        spnDuelVoiceB.setOnItemSelectedListener(spnDuelBListener);
        spnDuelVoiceB.setSelection(selectedDuelVoiceB); //預設選擇
    }

    private void saveSetting() {
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE); // 保存設置的物件
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("SoloVoiceBank", selectedSoloVoice);
        editor.putInt("DuelVoiceBankA", selectedDuelVoiceA);
        editor.putInt("DuelVoiceBankB", selectedDuelVoiceB);
        editor.apply();

        Log.d("CheckSave", "Solo: " + selectedSoloVoice);
        Log.d("CheckSave", "DuelA: " + selectedDuelVoiceA);
        Log.d("CheckSave", "DuelB: " + selectedDuelVoiceB);
    }

    private void loadSetting() {
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE); // 讀取設置的物件
        selectedSoloVoice = sharedPreferences.getInt("SoloVoiceBank", 0);
        selectedDuelVoiceA = sharedPreferences.getInt("DuelVoiceBankA", 0);
        selectedDuelVoiceB = sharedPreferences.getInt("DuelVoiceBankB", 0);

        Log.d("CheckLoad", "Solo: " + selectedSoloVoice);
        Log.d("CheckLoad", "DuelA: " + selectedDuelVoiceA);
        Log.d("CheckLoad", "DuelB: " + selectedDuelVoiceB);
    }
}