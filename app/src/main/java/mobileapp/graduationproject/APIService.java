package mobileapp.graduationproject;

import okhttp3.MultipartBody;
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
    Call<MinusResponse> minusNumbers(@Body MinusRequest request);

    @Multipart
    @POST("/upload")
    Call<ResponseBody> fileUploadTest(@Part MultipartBody.Part file);

    @GET("/download/{filename}")
    Call<ResponseBody> fileDownloadTest(@Path("filename") String filename);
}
