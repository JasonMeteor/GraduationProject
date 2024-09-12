package mobileapp.graduationproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.widget.Spinner;
import android.widget.TextView;
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

    private TextView tvSttResult;
    private Button btnBack;
    private Button btnRecord, btnPlayTrans;
    private Spinner spnAtoB;

    String[] translateLanguage = new String[] {"中譯英"}; //下拉式選單的內容

    private MediaRecorder mediaRecorder;
    private String fileName; //這個包含了錄音檔的儲存路徑
    private String uploadFileName = "recorded_audio.wav"; //這個是上傳時的檔名
    private int selectedTrans = 0; //選擇的翻譯方式，0 = 中譯英

    APIService apiService;

    String sttResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation);

        apiService = RetrofitManager.getInstance().getAPI();

        tvSttResult = findViewById(R.id.tv_sttResult);
        btnBack = findViewById(R.id.btn_back);
        btnRecord = findViewById(R.id.btn_record);
        btnPlayTrans = findViewById(R.id.btn_playTrans);
        spnAtoB = findViewById(R.id.spn_AtoB);

        //下拉式選單設定
        ArrayAdapter<String> adapterLanguage = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, translateLanguage);
        adapterLanguage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnAtoB.setAdapter(adapterLanguage);

        //下拉式選單監聽
        AdapterView.OnItemSelectedListener spnListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTrans = (int)id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        //按住錄音
        View.OnTouchListener recordListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 開始錄音
                    startRecording();
                    tvSttResult.setText("Now Loading...");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // 停止錄音
                    stopRecording();
                    tvSttResult.setText(sttResponse);
                }
                return true;
            }
        };

        View.OnClickListener playTransListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uploadText = tvSttResult.getText().toString();

                TranslateTts translateTts = new TranslateTts(selectedTrans, uploadText);
                Call<ResponseBody> call = apiService.tts(translateTts);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            // 保存接收到的音頻檔案到本地
                            try {
                                File audioFile = new File(getExternalFilesDir(null), "tts_output.mp3");
                                FileOutputStream fos = new FileOutputStream(audioFile);
                                fos.write(response.body().bytes());
                                fos.close();

                                // 播放音頻文件
                                playAudio(audioFile.getAbsolutePath());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
            }
        };

        //回首頁
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
        spnAtoB.setSelection(0); //預設選擇第一項(中譯英)
    }

    //確認錄音權限
    /*private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_CODE);
        }
    }*/

    private void startRecording() {
        File file = new File(getExternalFilesDir(null), uploadFileName);
        fileName = file.getAbsolutePath(); //設置錄音檔檔名&儲存路徑

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

    private void uploadFile(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", uploadFileName, requestFile);
        RequestBody language_flag = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedTrans));;

        Call<ResponseBody> call = apiService.stt(body, language_flag);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Translation.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();

                    // 解析伺服器回傳的JSON響應
                    String responseBody;
                    try {
                        responseBody = response.body().string();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(responseBody);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    String message;
                    try {
                        message = jsonObject.getString("message");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    sttResponse = message;
                } else {
                    Toast.makeText(Translation.this, "Failed to upload file", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(Translation.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Upload error:", t.getMessage());
            }
        });
    }

    //播放音檔
    private void playAudio(String audioPath) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
