package com.example.p2p_lib

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.p2p.BluetoothManager
import com.example.p2p.P2PCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() ,P2PCallback{
    lateinit var manager : BluetoothManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        manager = BluetoothManager(this@MainActivity,this)


        tv.movementMethod = ScrollingMovementMethod()
        tv.setOnClickListener{
            val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", tv.text.toString())
            clipboard.setPrimaryClip(clip)
        }


        btn_connect.setOnClickListener {
            //for A10
            manager = BluetoothManager(this@MainActivity,this)
//            manager.connectToDevice("E8:5A:8B:3F:5C:B1")
            manager.connectToDevice("FC:AA:B6:50:DA:77")
            //for redMi
        }

        btn_send.setOnClickListener {
            manager.sendMessage(tv.text.toString())
        }

        btn_disconnect.setOnClickListener {
            manager.stop()
            finish()
        }


    }

    override fun onResume() {
        super.onResume()
        manager.onResume()
    }

    override fun onPause() {
        super.onPause()
        manager.stop()
        finish()
    }

    override fun connected() {
        //Log.i("isCall", "connected1")
        //tv_status.setText("connected")
        //Log.i("isCall", "connected2")

    }

    override fun disconnected() {
        tv_status.setText("disconnect")
        Log.i("isCall", "disconnected")
    }

    override fun receivedMessage(message: String?) {
        setMessage(message)

    }

    public fun setMessage(message :String?){
        Handler().postDelayed(Runnable {
            Log.i("checkConnection", "message : "+message)
            Log.i("checkConnection", Thread.currentThread().name)
            tv.setText(message)
        },2000)
    }

}
