package com.dss.ipcontrol

import DataAdapter
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dss.ipcontrol.data.DataManager
import com.dss.ipcontrol.databinding.ActivityMainBinding
import com.dss.ipcontrol.socket.ICommProvider
import com.dss.ipcontrol.socket.ICommProvider.IResultListener
import com.dss.ipcontrol.socket.SocketProvider
import com.dss.ipcontrol.socket.SocketServer.EVENT_SERVER_FAIL
import com.dss.ipcontrol.socket.SocketServer.EVENT_SERVER_START
import com.dss.ipcontrol.socket.SocketServer.EVENT_SERVER_SUCCESS
import com.dss.ipcontrol.utils.NetworkUtil

class MainActivity : AppCompatActivity() {
    companion object {
        const val DEFAULT_PORT = "5000"
        const val SP_KEY_PORT = "sp_key_port"
        const val TAG = "IPControl"
        const val WHAT_INIT_SERVICE = 0
        const val WHAT_RESTART_SERVICE = 1
        const val WHAT_STOP_SERVICE = 2
        const val IP_NONE = "0.0.0.0"
    }

    private lateinit var mainBinding: ActivityMainBinding
    private var sp: SharedPreferences? = null
    private lateinit var mSocketProvider: SocketProvider
    private lateinit var mHandlerThread: HandlerThread
    private lateinit var mTaskHandler: Handler
    private val dataAdapter: DataAdapter by lazy { DataAdapter(DataManager.getDataList()) }

    private var commProviderListener: ICommProvider.ICommProviderListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        sp = getSharedPreferences(TAG, Context.MODE_PRIVATE)
        DataManager.clearData()
        initView()
        val intent = Intent()
        intent.setComponent(ComponentName("com.syxs.hicx", "com.syxs.hicx.server.IPControlService"))
    }

    override fun onResume() {
        super.onResume()
        mainBinding.ipAddressTv.text = NetworkUtil.getIpAddress(this)
    }

    private fun initView() {
        mainBinding.editTv.setText(sp?.getString(SP_KEY_PORT, DEFAULT_PORT))
        mainBinding.editTv.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val input = mainBinding.editTv.text.toString()
                if (input.isNotEmpty()) {
                    val value = input.toIntOrNull() ?: return@setOnEditorActionListener false
                    if (value < 5000) {
                        mainBinding.tipTv.text = "Port not less than 5000"
                        mainBinding.buttonTv.isEnabled = false
                    } else if (value > 12000) {
                        mainBinding.tipTv.text = "Port not more than 12000"
                        mainBinding.buttonTv.isEnabled = false
                    } else {
                        mainBinding.buttonTv.isEnabled = true
                        mainBinding.tipTv.text = ""
                    }
                }
                hideKeyboard()
                mainBinding.buttonTv.requestFocus()
                false
            } else {
                false
            }
        }
        mainBinding.buttonTv.setOnClickListener {
            Log.d(TAG, "initData: setOnClickListener")
            if (mainBinding.buttonTv.text == getString(R.string.start)) {
                validatePort()
            } else {
                stopService()
                mainBinding.buttonTv.text = getString(R.string.start)
                mainBinding.editTv.isEnabled = true
            }
        }
        mainBinding.buttonTv.requestFocus()
        mainBinding.dataListRv.layoutManager = LinearLayoutManager(this)
        mainBinding.dataListRv.adapter = dataAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun validatePort() {
        mainBinding.tipTv.text = ""
        sp?.edit()?.putString(SP_KEY_PORT, mainBinding.editTv.text.toString())?.apply()
        mainBinding.ipAddressTv.text = NetworkUtil.getIpAddress(this)
        if (mainBinding.ipAddressTv.text != IP_NONE) {
            initIpControlService()
        } else {
            Toast.makeText(this, "Please connect the network first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initIpControlService() {
        mHandlerThread = HandlerThread(TAG)
        mHandlerThread.start()
        mTaskHandler = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                Log.d(TAG, "handleMessage: what:" + msg.what)
                when (msg.what) {
                    WHAT_INIT_SERVICE -> {
                        mSocketProvider = SocketProvider.getInstance()
                        mSocketProvider.init(this@MainActivity)
                        commProviderListener = object : ICommProvider.ICommProviderListener {
                            override fun onRecvEvent(event: String?, msg: String?) {
                                onReceiveEvent(event, msg)
                            }

                            override fun onRecvData(
                                module: String?, data: ByteArray?, meta: Bundle?
                            ) {
                                if (meta?.getString("host") != null) {
                                    onReceiveData(data, meta.getString("host")!!)
                                } else {
                                    onReceiveData(data)
                                }
                            }
                        }
                        reStartService(null)
                    }

                    WHAT_RESTART_SERVICE -> reStartService(null)
                    WHAT_STOP_SERVICE -> stopService()
                }
            }
        }
        mTaskHandler.sendEmptyMessage(WHAT_INIT_SERVICE)
    }

    private fun reStartService(listener: ICommProvider.IResultListener?) {
        try {
            DataManager.clearData()
            mSocketProvider.setEventListener(this, commProviderListener)
            val b = Bundle()
            b.putInt("port", mainBinding.editTv.text.toString().toInt())
            mSocketProvider.restart(this, b, listener)
        } catch (e: Exception) {
            Log.e(TAG, "reStartSICPNet: ", e)
        }
    }

    private fun stopService() {
        mSocketProvider.setEventListener(this, null)
        mSocketProvider.stop(this)
        DataManager.clearData()
        dataAdapter.updateDataList(DataManager.getDataList())
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        mTaskHandler.removeCallbacksAndMessages(null)
        stopService()
        commProviderListener = null
    }

    private fun onReceiveEvent(event: String?, msg: String?) {
        Log.d(TAG, "onRecvEvent: event:$event msg:$msg")
        when (event) {
            EVENT_SERVER_START -> {
                runOnUiThread {
                    mainBinding.buttonTv.isEnabled = false
                }
            }

            EVENT_SERVER_SUCCESS -> {
                runOnUiThread {
                    Toast.makeText(this, "Start Service Success", Toast.LENGTH_SHORT).show()
                    mainBinding.buttonTv.text = getString(R.string.stop)
                    mainBinding.editTv.isEnabled = false
                    mainBinding.buttonTv.isEnabled = true
                    mainBinding.buttonTv.requestFocus()
                }
            }

            EVENT_SERVER_FAIL -> {
                runOnUiThread {
                    mainBinding.tipTv.text = msg
                    mainBinding.buttonTv.text = getString(R.string.start)
                    mainBinding.editTv.isEnabled = true
                    mainBinding.buttonTv.isEnabled = true
                    mainBinding.buttonTv.requestFocus()
                }
            }
        }
    }

    private fun onReceiveData(data: ByteArray?, host: String = "") {
        if (data == null) {
            return
        }
        Log.d(TAG, "onRecvData: data:$data")
        runOnUiThread {
            addNewData(data, host, false)
        }
    }

    private fun addNewData(data: ByteArray, host: String, isServer: Boolean) {
        val text = (if (isServer) "Server" else "Client") +"($host):"+ data.toString(Charsets.UTF_8)
        DataManager.addData(text)
        dataAdapter.updateDataList(DataManager.getDataList())
        mainBinding.dataListRv.post {
            if (dataAdapter.itemCount - 1 > 5) {
                // 自动滚动到末尾
                (mainBinding.dataListRv.layoutManager as? LinearLayoutManager)?.smoothScrollToPosition(
                    mainBinding.dataListRv, null, dataAdapter.itemCount - 1
                )
            }
        }
    }

}