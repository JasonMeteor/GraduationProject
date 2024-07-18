package mobileapp.graduationproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Training1 extends AppCompatActivity {

    private Button btnBack;
    private Button btn_AdderCal;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training1);

        btnBack = findViewById(R.id.btn_back);

        View.OnClickListener backHomeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Training1.this, MainActivity.class);
                startActivity(intent);
            }
        };

        btnBack.setOnClickListener(backHomeListener);
    }

    public void adderCal(View view){
        // All the Operations can be done Here.
        final TextView text=findViewById(R.id.adderResult);
        final EditText valA = (EditText)findViewById(R.id.editTextNumberDecimalFirst);
        final EditText valB = (EditText)findViewById(R.id.editTextNumberDecimalSecond);

        if(valA.getText().toString().trim().length() > 0 &&
                valB.getText().toString().trim().length() > 0) {

            int numA = Integer.parseInt(valA.getText().toString());
            int numB = Integer.parseInt(valB.getText().toString());

            if(numA>0 && numA<100 && numB>0 && numB<100) {
                text.setText("result = " + (numA+numB));
            } else {
                text.setText("please input 1-99");
            }
        } else {
            text.setText("incorrect input");
        }

    }
}
