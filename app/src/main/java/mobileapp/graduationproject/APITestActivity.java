package mobileapp.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class APITestActivity extends AppCompatActivity {

    APIService apiService;

    int num1, num2;

    EditText etNum1, etNum2;
    TextView tvResult;
    Button btnMinus, btnBackToMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_apitest);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiService = RetrofitManager.getInstance().getAPI();

        etNum1 = findViewById(R.id.et_num1);
        etNum2 = findViewById(R.id.et_num2);
        tvResult = findViewById(R.id.tv_minusresult);
        btnMinus = findViewById(R.id.btn_minus);
        btnBackToMain = findViewById(R.id.btn_backtomain);

        View.OnClickListener btnListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if(id == btnMinus.getId()){
                    num1 = Integer.parseInt(etNum1.getText().toString());
                    num2 = Integer.parseInt(etNum2.getText().toString());

                    MinusRequest minusRequest = new MinusRequest(num1, num2);
                    Call<MinusResponse> call = apiService.minusNumbers(minusRequest);
                    call.enqueue(new Callback<MinusResponse>() {
                        @Override
                        public void onResponse(Call<MinusResponse> call, Response<MinusResponse> response) {
                            Log.d("API", "package sent");
                            if(response.isSuccessful()){
                                MinusResponse minusResponse = response.body();
                                if(minusResponse != null){
                                    int result = minusResponse.getResult();
                                    String resultMessage = "結果: " + result;
                                    tvResult.setText(resultMessage);
                                }
                                else{
                                    tvResult.setText("結果: fail to get result.");
                                }
                            }
                            else{
                                Log.d("API", "fail to get response");
                            }
                        }

                        @Override
                        public void onFailure(Call<MinusResponse> call, Throwable t) {
                            Toast.makeText(APITestActivity.this, "ERROR: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("API", t.getMessage());
                        }
                    });
                }
                else if(id == btnBackToMain.getId()){
                    Intent intent = new Intent();
                    intent.setClass(APITestActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        };

        btnMinus.setOnClickListener(btnListener);
        btnBackToMain.setOnClickListener(btnListener);
    }
}