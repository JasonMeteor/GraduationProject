package mobileapp.graduationproject;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {
    private static RetrofitManager mInstance = new RetrofitManager();
    private APIService APIService;
    private RetrofitManager() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)  // 調整讀取超時設置
                .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))  // 調整連接池設置
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://140.134.24.83:5963/") //server IP address
                .client(okHttpClient)
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
