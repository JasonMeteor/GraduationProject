package mobileapp.graduationproject;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {
    private static RetrofitManager mInstance = new RetrofitManager();
    private APIService APIService;
    private RetrofitManager() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("") //wait for url or server location
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        APIService = retrofit.create(APIService.class);
    }
    public static RetrofitManager getInstance() {
        return mInstance;
    }
    public APIService getAPI() {
        return APIService;
    }
}
