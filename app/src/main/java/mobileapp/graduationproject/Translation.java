package mobileapp.graduationproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Translation extends AppCompatActivity {

    private EditText etSttResult;
    private Button btnBack;
    private Button btnRecord, btnPlayTrans;
    private Spinner spnAtoB;

    String[] translateLanguage = new String[] {"中譯英", "英譯中"}; // 下拉式選單的內容

    private MediaRecorder mediaRecorder;
    private String fileName; // 這個包含了錄音檔的儲存路徑
    private String uploadFileName = "recorded_audio.wav"; // 這個是上傳時的檔名
    private int selectedTrans; // 選擇的翻譯方式，1 = 中譯英  2 = 英譯中

    APIService apiService;

    String sttResponse;
    String fileFlag; // stt回傳的檔案特徵

    // 權限相關
    private static final int REQUEST_MICROPHONE_PERMISSION = 200;
    private static final int REQUEST_READ_AUDIO_PERMISSION = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation);

        apiService = RetrofitManager.getInstance().getAPI();

        etSttResult = findViewById(R.id.et_result);
        btnBack = findViewById(R.id.btn_twotranstomain);
        btnRecord = findViewById(R.id.btn_record);
        btnPlayTrans = findViewById(R.id.btn_playTrans);
        spnAtoB = findViewById(R.id.spn_AtoB);

        // 下拉式選單設定
        ArrayAdapter<String> adapterLanguage = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, translateLanguage);
        adapterLanguage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnAtoB.setAdapter(adapterLanguage);

        // 下拉式選單監聽
        AdapterView.OnItemSelectedListener spnListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTrans = (int)id + 1;
                //Toast.makeText(Translation.this, "FLAG = " + selectedTrans, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        // 按住錄音
        View.OnTouchListener recordListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    checkMicrophonePermission();
                    if (ContextCompat.checkSelfPermission(Translation.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        ;
                    } else {
                        // 開始錄音
                        startRecording();
                        etSttResult.setText("Now Loading...");
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (ContextCompat.checkSelfPermission(Translation.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        ;
                    } else {
                        // 停止錄音
                        stopRecording();
                    }
                }
                return true;
            }
        };

        // 播放tts
        View.OnClickListener playTransListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkReadExternalStoragePermission(); // 檢查權限

                String uploadText = etSttResult.getText().toString();

                TranslateTts translateTts = new TranslateTts(uploadText, selectedTrans, fileFlag); // 打包成JSON
                Call<ResponseBody> call = apiService.tts(translateTts); // 呼叫對應接口
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        // 正常回應
                        if (response.isSuccessful()) {
                            if (response.body() != null){
                                byte[] audioData = null;
                                try {
                                    audioData = response.body().bytes();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                Log.d("Audio Data Length", "Length: " + audioData.length);
                                try {
                                    if (audioData.length > 0){
                                        File audioFile = new File(getExternalFilesDir(null), "tts_output.wav");
                                        FileOutputStream fos = new FileOutputStream(audioFile);
                                        fos.write(audioData);
                                        fos.close();

                                        //Toast.makeText(Translation.this, "Get audio successfully", Toast.LENGTH_SHORT).show();

                                        // 播放音頻文件
                                        playAudio(audioFile.getAbsolutePath());
                                    } else {
                                        Log.e("Audio Error", "Received empty audio data");
                                        Toast.makeText(Translation.this, "Received empty audio data", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Log.e("File Save Error", "Error saving the audio file: " + e.getMessage());
                                }
                            } else {
                                Log.e("API ERROR", "Response body is null");
                            }
                        }
                        // 回應錯誤
                        else {
                            Toast.makeText(Translation.this, "Failed to get audio", Toast.LENGTH_SHORT).show();
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

                    // 請求失敗
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(Translation.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Upload error:", t.getMessage());
                    }
                });
            }
        };

        // 回首頁
        View.OnClickListener backHomeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Translation.this, MainActivity.class);
                startActivity(intent);
            }
        };

        btnBack.setOnClickListener(backHomeListener);
        btnRecord.setOnTouchListener(recordListener);
        btnPlayTrans.setOnClickListener(playTransListener);
        spnAtoB.setOnItemSelectedListener(spnListener);
        spnAtoB.setSelection(0); //預設選擇
    }

    //確認錄音權限
    private void checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // 如果沒有錄音權限，請求權限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE_PERMISSION);
        }
    }

    //確認存取權限
    private void checkReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
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
        RequestBody language_flag = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedTrans));

        Call<ResponseBody> call = apiService.stt(body, language_flag);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    //Toast.makeText(Translation.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();

                    if (response.body() != null) {
                        // 解析伺服器回傳的JSON響應
                        String responseBody;
                        try {
                            responseBody = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseBody);
                            String stt_result = jsonObject.getString("stt_result");
                            fileFlag = jsonObject.getString("flag");
                            Log.d("FLAG CHECK", fileFlag);

                            // 儲存伺服器回傳的 message
                            sttResponse = stt_result;
                            // 處理英文句號
                            if (selectedTrans == 2) {
                                sttResponse = sttResponse.substring(0, sttResponse.length()-1);
                            }
                            etSttResult.setText(sttResponse);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Toast.makeText(Translation.this, "Empty response", Toast.LENGTH_SHORT).show();
                        sttResponse = "Empty response";
                        etSttResult.setText(sttResponse);
                    }
                } else {
                    //Toast.makeText(Translation.this, "Failed to upload file", Toast.LENGTH_SHORT).show();
                    sttResponse = "Failed to upload file";
                    etSttResult.setText(sttResponse);
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
                Toast.makeText(Translation.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
