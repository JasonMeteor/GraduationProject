package mobileapp.graduationproject;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadTestActivity extends AppCompatActivity {

    APIService apiService;

    private Button btnUpload;
    private Button btnDownload;
    private Button btnBack;

    private String originalFileName;

    private static final int PICK_FILE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiService = RetrofitManager.getInstance().getAPI();

        btnUpload = findViewById(R.id.button);
        btnDownload = findViewById(R.id.button2);
        btnBack = findViewById(R.id.button3);

        View.OnClickListener btnListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();

                if(id == R.id.button){ //upload
                    // 啟動檔案選擇器
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("audio/*"); // 限制音頻類型檔案
                    startActivityForResult(intent, PICK_FILE_REQUEST);
                } else if(id == R.id.button2){ //download
                    // 欲下載檔案之名稱
                    // 以後應該要改成特定格式，像這樣直接用檔名實在有點麻煩
                    String filename = "Test Voice.wav2030230493767763165_20240824.wav";

                    // 下载文件
                    downloadFile(filename);
                } else if(id == R.id.button3){
                    Intent intent = new Intent();
                    intent.setClass(UploadTestActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        };

        btnUpload.setOnClickListener(btnListener);
        btnDownload.setOnClickListener(btnListener);
        btnBack.setOnClickListener(btnListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            String filePath = getFilePathFromUri(fileUri);

            // 上传文件
            if (filePath != null) {
                Log.e("NameTest1", new File(filePath).getName());
                uploadFile(new File(filePath));
            } else {
                Toast.makeText(this, "Failed to get file path", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFilePathFromUri(Uri uri) {
        String filePath = null;

        try {
            // 獲取檔案類型
            String mimeType = getContentResolver().getType(uri);

            // 檢查檔案類型
            if (mimeType != null /*&& mimeType.equals("audio/wav")*/) {
                originalFileName = getFileName(uri);

                // 分离文件名和扩展名
                String name = originalFileName;
                String extension = "";

                int dotIndex = originalFileName.lastIndexOf('.');
                if (dotIndex != -1) {
                    name = originalFileName.substring(0, dotIndex);
                    extension = originalFileName.substring(dotIndex);
                }

                // 创建临时文件
                File tempFile = File.createTempFile(name, extension, getCacheDir());

                // 将文件内容复制到临时文件
                InputStream inputStream = getContentResolver().openInputStream(uri);
                FileOutputStream outputStream = new FileOutputStream(tempFile);

                byte[] buffer = new byte[1024];
                int length;

                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.close();
                inputStream.close();

                filePath = tempFile.getAbsolutePath();
            } else {
                Toast.makeText(this, "Invalid file type selected. Please select a wav file.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filePath;
    }

    private String getFileName(Uri uri) {
        String fileName = null;

        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                fileName = cursor.getString(nameIndex);
                cursor.close();
            }
        } else if (uri.getScheme().equals("file")) {
            fileName = new File(uri.getPath()).getName();
        }

        return fileName;
    }

    private void uploadFile(File file) {
        String oriFileName = originalFileName;
        Log.e("NameTest2", oriFileName);

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", oriFileName, requestFile);

        Call<ResponseBody> call = apiService.fileUploadTest(body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(UploadTestActivity.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UploadTestActivity.this, "Failed to upload file", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(UploadTestActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Upload error:", t.getMessage());
            }
        });
    }

    private void downloadFile(String filename) {
        Call<ResponseBody> call = apiService.fileDownloadTest(filename);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    boolean writtenToDisk = writeResponseBodyToDisk(response.body(), filename);
                    if (writtenToDisk) {
                        Toast.makeText(UploadTestActivity.this, "File downloaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UploadTestActivity.this, "Failed to save file", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UploadTestActivity.this, "Failed to download file", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(UploadTestActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Download error:", t.getMessage());
            }
        });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String filename) {
        try {
            // 创建文件路径
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);
                }

                outputStream.flush();
                return true;
            } catch (Exception e) {
                Log.e("File save error:", e.getMessage());
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (Exception e) {
            Log.e("File write error:", e.getMessage());
            return false;
        }
    }
}