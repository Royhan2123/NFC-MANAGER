package com.example.kotlin_nfc_manager

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/**
 * NfcProPlugin - Version 2.0 (Industry Grade)
 * Enhanced lifecycle management and robust error reporting.
 */
class NfcProPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    private lateinit var channel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private var eventSink: EventChannel.EventSink? = null
    
    private var activity: Activity? = null
    private var context: Context? = null
    private var nfcController: NfcController? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        
        // Setup Method Channel
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "com.nfcpro/methods")
        channel.setMethodCallHandler(this)
        
        // Setup Event Channel
        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "com.nfcpro/events")
        eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                eventSink = events
            }
            override fun onCancel(arguments: Any?) {
                eventSink = null
            }
        })
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        try {
            when (call.method) {
                "isAvailable" -> {
                    val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
                    result.success(nfcAdapter != null && nfcAdapter.isEnabled)
                }
                "supportsEmulation" -> {
                    val pm = context?.packageManager
                    val supportsHce = pm?.hasSystemFeature(android.content.pm.PackageManager.FEATURE_NFC_HOST_CARD_EMULATION) ?: false
                    result.success(supportsHce)
                }
                "startScan" -> {
                    nfcController?.startNfcSession()
                    result.success(true)
                }
                "stopScan" -> {
                    nfcController?.stopNfcSession()
                    result.success(true)
                }
                "writeTag" -> {
                    val data = call.argument<String>("data") ?: ""
                    val success = nfcController?.writeNdef(data) ?: false
                    result.success(success)
                }
                "transceive" -> {
                    val capdu = call.argument<String>("capdu") ?: ""
                    val rapdu = nfcController?.transceiveApdu(capdu)
                    if (rapdu != null) result.success(rapdu) 
                    else result.error("INVALID_APDU", "APDU transmission failed or timeout", null)
                }
                "setClonedId" -> {
                    val id = call.argument<String>("id") ?: ""
                    val prefs = context?.getSharedPreferences("NfcProPrefs", Context.MODE_PRIVATE)
                    prefs?.edit()?.putString("cloned_identity", id)?.apply()
                    result.success(true)
                }
                "getClonedId" -> {
                    val prefs = context?.getSharedPreferences("NfcProPrefs", Context.MODE_PRIVATE)
                    result.success(prefs?.getString("cloned_identity", ""))
                }
                else -> result.notImplemented()
            }
        } catch (e: Exception) {
            result.error("NATIVE_ERROR", e.message, e.stackTraceToString())
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    // --- Activity Lifecycle Management ---

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        nfcController = NfcController(activity!!, eventSink)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun ReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        nfcController?.stopNfcSession()
        activity = null
        nfcController = null
    }
}
