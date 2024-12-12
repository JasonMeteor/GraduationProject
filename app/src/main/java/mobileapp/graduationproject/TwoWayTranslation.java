package mobileapp.graduationproject;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

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

public class TwoWayTranslation extends AppCompatActivity {

    private EditText etSttResult;
    private Button btnBack, btnRecordA, btnRecordB, btnPlayA, btnPlayB;
    private Spinner spnFromA, spnToA, spnFromB, spnToB;
    private LottieAnimationView lottieLoading;

    String[] translateFrom = new String[] {"中文", "英文", "日文", "粵語", "馬來文"};
    String[] translateTo = new String[] {"轉中文", "轉英文", "轉中文(冠霖)", "轉中文(Wing)"}; // 下拉式選單的內容

    private MediaRecorder mediaRecorder;
    private String fileName; // 這個包含了錄音檔的儲存路徑
    private String uploadFileName = "recorded_audio.wav"; // 這個是上傳時的檔名
    private int fromLanguageA = 1, toLanguageA = 2;
    private int fromLanguageB = 1, toLanguageB = 2;
    private boolean isRecording = false;

    APIService apiService;

    String sttResponse;
    String fileFlag = ""; // stt回傳的檔案特徵

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

        apiService = RetrofitManager.getInstance().getAPI();

        etSttResult = findViewById(R.id.et_result_bio);
        btnBack = findViewById(R.id.btn_twotranstomain);
        btnRecordA = findViewById(R.id.btn_record_bio_A);
        btnPlayA = findViewById(R.id.btn_playTrans_bio_A);
        btnRecordB = findViewById(R.id.btn_record_bio_B);
        btnPlayB = findViewById(R.id.btn_playTrans_bio_B);
        spnFromA = findViewById(R.id.spn_from_language_A);
        spnToA = findViewById(R.id.spn_to_language_A);
        spnFromB = findViewById(R.id.spn_from_language_B);
        spnToB = findViewById(R.id.spn_to_language_B);
        lottieLoading = findViewById(R.id.ani_loading);

        // 下拉式選單設定
        ArrayAdapter<String> adapterFromLanguage = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, translateFrom);
        adapterFromLanguage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnFromA.setAdapter(adapterFromLanguage);
        spnFromB.setAdapter(adapterFromLanguage);
        ArrayAdapter<String> adapterToLanguage = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, translateTo);
        adapterToLanguage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnToA.setAdapter(adapterToLanguage);
        spnToB.setAdapter(adapterToLanguage);

        // 下拉式選單監聽
        AdapterView.OnItemSelectedListener spnListenerFromA = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageA = (int)id + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        AdapterView.OnItemSelectedListener spnListenerToA = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageA = (int)id + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        AdapterView.OnItemSelectedListener spnListenerFromB = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageB = (int)id + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        AdapterView.OnItemSelectedListener spnListenerToB = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageB = (int)id + 1;
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
                    if (ContextCompat.checkSelfPermission(TwoWayTranslation.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                    } else {
                        startRecording();
                        etSttResult.setText("Listening...");
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (ContextCompat.checkSelfPermission(TwoWayTranslation.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                    } else {
                        stopRecording(v.getId());
                    }
                }
                return true;
            }
        };

        // 播放tts
        View.OnClickListener playTransListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPlayA.setEnabled(false);
                btnPlayB.setEnabled(false);
                lottieLoading.setVisibility(View.VISIBLE);

                if (fileFlag.isEmpty()) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(TwoWayTranslation.this);
                    alertDialog.setTitle("錯誤");
                    alertDialog.setMessage("請先進行錄音");
                    alertDialog.setNeutralButton("好",(dialog, which) -> {
                        btnPlayA.setEnabled(true);
                        btnPlayB.setEnabled(true);
                        lottieLoading.setVisibility(View.INVISIBLE);
                    });
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                } else {
                    checkReadExternalStoragePermission(); // 檢查權限

                    String uploadText = etSttResult.getText().toString();

                    TranslateTts translateTts = new TranslateTts(uploadText, 0, 0, fileFlag); // 打包成JSON
                    int btnID = v.getId();
                    if (btnID == R.id.btn_playTrans_bio_A) {
                        translateTts.setLanguage_flag(fromLanguageA, toLanguageA);
                    } else if (btnID == R.id.btn_playTrans_bio_B) {
                        translateTts.setLanguage_flag(fromLanguageB, toLanguageB);
                    }

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

                                            //Toast.makeText(TwoWayTranslation.this, "Get audio successfully", Toast.LENGTH_SHORT).show();

                                            // 播放音頻文件
                                            playAudio(audioFile.getAbsolutePath());
                                            lottieLoading.setVisibility(View.INVISIBLE);
                                        } else {
                                            Log.e("Audio Error", "Received empty audio data");
                                            Toast.makeText(TwoWayTranslation.this, "Received empty audio data", Toast.LENGTH_SHORT).show();
                                            btnPlayA.setEnabled(true);
                                            btnPlayB.setEnabled(true);
                                            lottieLoading.setVisibility(View.INVISIBLE);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Log.e("File Save Error", "Error saving the audio file: " + e.getMessage());
                                        btnPlayA.setEnabled(true);
                                        btnPlayB.setEnabled(true);
                                        lottieLoading.setVisibility(View.INVISIBLE);
                                    }
                                } else {
                                    Log.e("API ERROR", "Response body is null");
                                    btnPlayA.setEnabled(true);
                                    btnPlayB.setEnabled(true);
                                    lottieLoading.setVisibility(View.INVISIBLE);
                                }
                            }
                            // 回應錯誤
                            else {
                                Toast.makeText(TwoWayTranslation.this, "Failed to get audio", Toast.LENGTH_SHORT).show();
                                btnPlayA.setEnabled(true);
                                btnPlayB.setEnabled(true);
                                lottieLoading.setVisibility(View.INVISIBLE);

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
                            Toast.makeText(TwoWayTranslation.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("Upload error:", t.getMessage());
                            btnPlayA.setEnabled(true);
                            btnPlayB.setEnabled(true);
                            lottieLoading.setVisibility(View.INVISIBLE);
                        }
                    });
                }
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
        btnRecordA.setOnTouchListener(recordListener);
        btnRecordB.setOnTouchListener(recordListener);
        btnPlayA.setOnClickListener(playTransListener);
        btnPlayB.setOnClickListener(playTransListener);
        spnFromA.setOnItemSelectedListener(spnListenerFromA);
        spnFromA.setSelection(0); // 預設選擇
        spnToA.setOnItemSelectedListener(spnListenerToA);
        spnToA.setSelection(1);
        spnFromB.setOnItemSelectedListener(spnListenerFromB);
        spnFromB.setSelection(1); // 預設選擇
        spnToB.setOnItemSelectedListener(spnListenerToB);
        spnToB.setSelection(0);
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
        if(isRecording) {
            return;
        }

        File audioFile = new File(getExternalFilesDir(null), uploadFileName);
        fileName = audioFile.getAbsolutePath(); //設置錄音檔檔名&儲存路徑

        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(fileName);

            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
        }
    }

    private void stopRecording(int btnID) {
        if(!isRecording) {
            return;
        }

        try{
            mediaRecorder.stop();

            // 獲取錄音文件的File對象
            File audioFile = new File(fileName);

            // 開始上傳音檔
            uploadFile(audioFile, btnID);
        } catch (RuntimeException e) {
            e.printStackTrace();
            etSttResult.setText(""); // 淨空文字欄，重新顯示hint
        } finally{
            releaseMediaRecorder();
        }
    }

    // 釋放錄音器資源
    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        }
    }

    //stt上傳音檔
    private void uploadFile(File file, int btnID) {
        lottieLoading.setVisibility(View.VISIBLE);

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("audio_file", uploadFileName, requestFile);

        int sendTransFlag = 0;
        if (btnID == R.id.btn_record_bio_A) {
            sendTransFlag = fromLanguageA;
        } else if(btnID == R.id.btn_record_bio_B) {
            sendTransFlag = fromLanguageB;
        }
        RequestBody language_flag = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(sendTransFlag));

        Call<ResponseBody> call = apiService.stt(body, language_flag);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    //Toast.makeText(TwoWayTranslation.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();

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
                            // 處理句號 英 日 馬來
                            if ((btnID == R.id.btn_record_bio_A && fromLanguageA == 2) || (btnID == R.id.btn_record_bio_B && fromLanguageB == 2) ||
                                (btnID == R.id.btn_record_bio_A && fromLanguageA == 3) || (btnID == R.id.btn_record_bio_B && fromLanguageB == 3) ||
                                (btnID == R.id.btn_record_bio_A && fromLanguageA == 5) || (btnID == R.id.btn_record_bio_B && fromLanguageB == 5)) {
                                sttResponse = sttResponse.substring(0, sttResponse.length()-1);
                            }
                            etSttResult.setText(sttResponse);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        } finally {
                            lottieLoading.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        //Toast.makeText(TwoWayTranslation.this, "Empty response", Toast.LENGTH_SHORT).show();
                        sttResponse = "Empty response";
                        etSttResult.setText(sttResponse);
                        lottieLoading.setVisibility(View.INVISIBLE);
                    }
                } else {
                    //Toast.makeText(TwoWayTranslation.this, "Failed to upload file", Toast.LENGTH_SHORT).show();
                    sttResponse = "Failed to upload file";
                    etSttResult.setText(sttResponse);
                    lottieLoading.setVisibility(View.INVISIBLE);

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
                sttResponse = "網路錯誤，無法連接到伺服器";
                etSttResult.setText(sttResponse);
                lottieLoading.setVisibility(View.INVISIBLE);
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

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    btnPlayA.setEnabled(true);
                    btnPlayB.setEnabled(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to play audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnPlayA.setEnabled(true);
            btnPlayB.setEnabled(true);
        }
    }
}