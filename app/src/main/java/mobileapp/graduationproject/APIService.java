package mobileapp.graduationproject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface APIService {
    @POST("/subtract") //API location
    Call<MinusResponse> minusNumbers(@Body MinusRequest request); //減法器測試

    @Multipart
    @POST("/upload")
    Call<ResponseBody> fileUploadTest(@Part MultipartBody.Part file); //上傳測試

    @GET("/download/{filename}")
    Call<ResponseBody> fileDownloadTest(@Path("filename") String filename); //下載測試

    @Multipart
    @POST("/api/stt")
    Call<ResponseBody> stt(
            @Part MultipartBody.Part file,
            @Part("param") RequestBody language_flag
    ); //單向翻譯stt

    @POST("/api/translate")
    Call<ResponseBody> tts(@Body TranslateTts ttsData); //單向翻譯tts
}

// "/api/bidirectional_stt" 雙向翻譯stt接口
// "api/bidirectional_translate" 雙向翻譯tts接口