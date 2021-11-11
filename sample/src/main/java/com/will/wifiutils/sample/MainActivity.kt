package com.will.wifiutils.sample

import android.Manifest
import android.content.Context
import android.net.wifi.ScanResult
import android.os.Bundle
import android.text.method.KeyListener
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.will.wifiutils.Logger
import com.will.wifiutils.sample.R
import com.will.wifiutils.TypeEnum
import com.will.wifiutils.WifiUtils
import com.will.wifiutils.wifiConnect.ConnectionErrorCode
import com.will.wifiutils.wifiConnect.ConnectionSuccessListener
import com.will.wifiutils.wifiDisconnect.DisconnectionErrorCode
import com.will.wifiutils.wifiDisconnect.DisconnectionSuccessListener
import com.will.wifiutils.wifiRemove.RemoveErrorCode
import com.will.wifiutils.wifiRemove.RemoveSuccessListener
import com.will.wifiutils.wifiScan.ScanResultsListener
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var textview_ssid: TextView
    private lateinit var textview_password: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            555
        )
        WifiUtils.forwardLog(object :Logger{
            override fun log(priority: Int, tag: String?, message: String?) {
                Log.d(tag,message!!)
            }
        })
        WifiUtils.enableLog(true)
        findViewById<Button>(R.id.button_connect).setOnClickListener {
            connectWithWpa(
                applicationContext
            )
//            Log.d("withContext","scan start")
//            WifiUtils.withContext(this).scanWifi(object :ScanResultsListener{
//                override fun onScanResults(scanResults: List<ScanResult?>) {
//                    Log.d("withContext","scan start result size=${scanResults.size}")
//                }
//            }).start()
        }
        findViewById<Button>(R.id.button_prefix).setOnClickListener {
            connectWithPrefix(
                applicationContext
            )
        }
        findViewById<Button>(R.id.button_connect_hidden).setOnClickListener {
            connectHidden(
                applicationContext
            )
        }
        findViewById<Button>(R.id.button_disconnect).setOnClickListener {
            disconnect(
                applicationContext
            )
        }
        findViewById<Button>(R.id.button_remove).setOnClickListener { remove(applicationContext) }
        findViewById<Button>(R.id.button_check).setOnClickListener { check(applicationContext) }
        findViewById<Button>(R.id.button_check_internet_connection).setOnClickListener {
            checkInternetConnection(
                applicationContext
            )
        }
        textview_password = findViewById(R.id.textview_password)
        textview_ssid = findViewById(R.id.textview_ssid)
    }

    private fun connectWithPrefix(context: Context) {
        WifiUtils.withContext(context)
            .patternMatch()
            .connectWith(textview_ssid.text.toString())
            .setTimeout(15000)
            .onConnectionResult(object : ConnectionSuccessListener {
                override fun success() {
                    Toast.makeText(context, "SUCCESS!", Toast.LENGTH_SHORT).show()
                }

                override fun failed(errorCode: ConnectionErrorCode) {
                    Toast.makeText(context, "EPIC FAIL!$errorCode", Toast.LENGTH_SHORT).show()
                }
            })
            .start()
    }

    private fun connectWithWpa(context: Context) {
        WifiUtils.withContext(context)
            .connectWith(textview_ssid.text.toString(), textview_password.text.toString(),true)
            .setTimeout(15000)
            .onConnectionResult(object : ConnectionSuccessListener {
                override fun success() {
                    Toast.makeText(context, "SUCCESS!", Toast.LENGTH_SHORT).show()
                }

                override fun failed(errorCode: ConnectionErrorCode) {
                    Toast.makeText(context, "EPIC FAIL!$errorCode", Toast.LENGTH_SHORT).show()
                }
            })
            .start()
    }

    private fun connectHidden(context: Context) {
        WifiUtils.withContext(context)
            .connectWith(
                textview_ssid.text.toString(),
                textview_password.text.toString(),
                TypeEnum.PSK
            )?.onConnectionResult(object : ConnectionSuccessListener {
                override fun success() {
                    Toast.makeText(context, "SUCCESS!", Toast.LENGTH_SHORT).show()
                }

                override fun failed(errorCode: ConnectionErrorCode) {
                    connectWithWpa(context)
                    Toast.makeText(context, "EPIC FAIL!$errorCode", Toast.LENGTH_SHORT).show()
                }
            })?.start()
    }

    private fun disconnect(context: Context) {
        WifiUtils.withContext(context)
            .disconnect(object : DisconnectionSuccessListener {
                override fun success() {
                    Toast.makeText(context, "Disconnect success!", Toast.LENGTH_SHORT).show()
                }

                override fun failed(errorCode: DisconnectionErrorCode) {
                    Toast.makeText(context, "Failed to disconnect: $errorCode", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun remove(context: Context) {
        WifiUtils.withContext(context)
            .remove(textview_ssid.text.toString(), object : RemoveSuccessListener {
                override fun success() {
                    Toast.makeText(context, "Remove success!", Toast.LENGTH_SHORT).show()
                }

                override fun failed(errorCode: RemoveErrorCode) {
                    Toast.makeText(
                        context,
                        "Failed to disconnect and remove: $errorCode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun check(context: Context) {
        val result = WifiUtils.withContext(context).isWifiConnected(textview_ssid.text.toString())
        Toast.makeText(context, "Wifi Connect State: $result", Toast.LENGTH_SHORT).show()
    }


    private fun checkInternetConnection(context: Context) {
        val executorService: ExecutorService = Executors.newFixedThreadPool(4)
        executorService.execute {
            try {
                val timeoutMs = 1500
                val sock = Socket()
                val sockaddr: SocketAddress = InetSocketAddress("8.8.8.8", 53)
                sock.connect(sockaddr, timeoutMs)
                sock.close()
                runOnUiThread {
                    Toast.makeText(
                        context,
                        "Connecting to internet successfully",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(context, "Cant connect to internet", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}