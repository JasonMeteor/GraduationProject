package mobileapp.graduationproject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface APIService {
    @POST("/subtract") //API location
    Call<MinusResponse> minusNumbers(@Body MinusRequest request);
}
