package mobileapp.graduationproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TwoWayTranslation extends AppCompatActivity {

    private EditText etSttResult;
    private Button btnBack, btnRecordA, btnRecordB, btnPlayA, btnPlayB;
    private Spinner spnTranslateA, spnTranslateB;

    String[] translateLanguage = new String[] {"中譯英"}; // 下拉式選單的內容

    private MediaRecorder mediaRecorder;
    private String fileName; // 這個包含了錄音檔的儲存路徑
    private String uploadFileName = "recorded_audio.wav"; // 這個是上傳時的檔名
    private int selectedTransA = 1; // 選擇的翻譯方式，1 = 翻成英文  2 = 翻成中文 上面的spinner
    private int selectedTransB = 1; // 選擇的翻譯方式，1 = 翻成英文  2 = 翻成中文 下面的spinner

    APIService apiService;

    String sttResponse;

    // 權限相關
    private static final int REQUEST_MICROPHONE_PERMISSION = 200;
    private static final int REQUEST_READ_AUDIO_PERMISSION = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_two_way_translation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etSttResult = findViewById(R.id.et_result_bio);
        btnBack = findViewById(R.id.btn_twotranstomain);
        btnRecordA = findViewById(R.id.btn_record_bio_A);
        btnPlayA = findViewById(R.id.btn_playTrans_bio_A);
        btnRecordB = findViewById(R.id.btn_record_bio_B);
        btnPlayB = findViewById(R.id.btn_playTrans_bio_B);
        spnTranslateA = findViewById(R.id.spn_transSelectA);
        spnTranslateB = findViewById(R.id.spn_transSelectB);

        // 下拉式選單設定
        ArrayAdapter<String> adapterLanguage = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, translateLanguage);
        adapterLanguage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTranslateA.setAdapter(adapterLanguage);
        spnTranslateB.setAdapter(adapterLanguage);

        // 下拉式選單監聽
        AdapterView.OnItemSelectedListener spnListenerA = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTransA = (int)id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        AdapterView.OnItemSelectedListener spnListenerB = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTransB = (int)id + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        View.OnClickListener backListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(TwoWayTranslation.this, MainActivity.class);
                startActivity(intent);
            }
        };

        btnBack.setOnClickListener(backListener);
        spnTranslateA.setOnItemSelectedListener(spnListenerA);
        spnTranslateA.setSelection(0); // 預設選擇第一項(中譯英)
        spnTranslateB.setOnItemSelectedListener(spnListenerB);
        spnTranslateB.setSelection(0); // 預設選擇第一項(中譯英)
    }

    //確認錄音權限
    private void checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // 如果沒有錄音權限，請求權限
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE_PERMISSION);
        }
    }

    //確認存取權限
    private void checkReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_READ_AUDIO_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_MICROPHONE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ;
                } else {
                    // 權限被拒絕，提示使用者
                    Toast.makeText(this, "請開啟麥克風權限以開始錄音", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_READ_AUDIO_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ;
                } else {
                    // 權限被拒絕，提示使用者
                    Toast.makeText(this, "請開啟讀取音檔權限以播放音檔", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    private void startRecording() {
        File audioFile = new File(getExternalFilesDir(null), uploadFileName);
        fileName = audioFile.getAbsolutePath(); //設置錄音檔檔名&儲存路徑

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(fileName);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        // 獲取錄音文件的File對象
        File audioFile = new File(fileName);

        // 開始上傳音檔
        uploadFile(audioFile);
    }

    //stt上傳音檔
    private void uploadFile(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("audio_file", uploadFileName, requestFile);
        RequestBody language_flag = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedTransA));

        Call<ResponseBody> call = apiService.stt(body, language_flag);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TwoWayTranslation.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();

                    if (response.body() != null) {
                        // 解析伺服器回傳的JSON響應
                        String responseBody;
                        try {
                            responseBody = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseBody);
                            String stt_result = jsonObject.getString("stt_result");

                            // 儲存伺服器回傳的 message
                            sttResponse = stt_result;
                            etSttResult.setText(sttResponse);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(TwoWayTranslation.this, "Empty response", Toast.LENGTH_SHORT).show();
                        sttResponse = "Empty response";
                    }
                } else {
                    Toast.makeText(TwoWayTranslation.this, "Failed to upload file", Toast.LENGTH_SHORT).show();
                    try {
                        if (response.errorBody() != null) {
                            String errorResponse = response.errorBody().string();
                            JSONObject errorJson = new JSONObject(errorResponse);
                            String errorMessage = errorJson.optString("error", "Unknown error");

                            Log.e("API ERROR", errorMessage);
                        } else {
                            Log.e("API ERROR", "Unknown error");
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(TwoWayTranslation.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Upload error:", t.getMessage());
                sttResponse = t.getMessage();
                etSttResult.setText(sttResponse);
            }
        });
    }

    //tts播放音檔
    private void playAudio(String audioPath) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to play audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}