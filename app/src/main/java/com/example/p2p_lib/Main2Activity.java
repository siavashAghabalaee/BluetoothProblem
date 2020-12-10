package com.example.p2p_lib;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.p2p.BluetoothManager;
import com.example.p2p.P2PCallback;

public class Main2Activity extends AppCompatActivity implements P2PCallback {

    private BluetoothManager manager = null;
    private EditText tv;
    private TextView tv_status;
    private Button btn_connect;
    private Button btn_send;
    private Button btn_disconnect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        manager = new  BluetoothManager(this,this);
        tv = findViewById(R.id.tv);
        tv_status = findViewById(R.id.tv_status);
        btn_connect = findViewById(R.id.btn_connect);
        btn_send = findViewById(R.id.btn_send);
        btn_disconnect = findViewById(R.id.btn_disconnect);


        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for A10
//              manager.connectToDevice("E8:5A:8B:3F:5C:B1");
                manager.connectToDevice("FC:AA:B6:50:DA:77");
                //for redMi
            }
        });
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.sendMessage(tv.getText().toString().toString());
            }
        });
        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.stop();
                finish();
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.stop();
        onStop();
    }

    @Override
    public void receivedMessage(String message) {
        Log.i("checkConnection", "message : "+message);
        tv.setText(message);
    }

    @Override
    public void connected() {
        Log.i("isCall", "connected1");
        tv_status.setText("connected");
        Log.i("isCall", "connected2");
    }

    @Override
    public void disconnected() {
        tv_status.setText("disconnected");
    }
}
