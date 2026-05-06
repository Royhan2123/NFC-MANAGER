package com.example.kotlin_nfc_manager

import android.app.Activity
import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/**
 * NfcProPlugin - The bridge between Flutter and Native Android NFC.
 * Exposes all advanced features (HCE, Cloning, Reading, Writing) to Dart.
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
        
        // Setup Event Channel for real-time tag data
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
        when (call.method) {
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
            "setClonedId" -> {
                val id = call.argument<String>("id") ?: ""
                val prefs = context?.getSharedPreferences("nfc_cloner", Context.MODE_PRIVATE)
                prefs?.edit()?.putString("cloned_identity", id)?.apply()
                result.success(true)
            }
            "getClonedId" -> {
                val prefs = context?.getSharedPreferences("nfc_cloner", Context.MODE_PRIVATE)
                val id = prefs?.getString("cloned_identity", "")
                result.success(id)
            }
            "transceive" -> {
                val capdu = call.argument<String>("capdu") ?: ""
                val rapdu = nfcController?.transceiveApdu(capdu)
                result.success(rapdu)
            }
            else -> result.notImplemented()
        }
    }

    // --- ActivityAware Implementation ---

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        nfcController = NfcController(activity!!)
        
        // Forward tag events to Flutter
        nfcController?.onTagScanned = { uid, type, content ->
            val data = mapOf(
                "uid" to uid,
                "type" to type,
                "content" to content
            )
            activity?.runOnUiThread {
                eventSink?.success(data)
            }
        }
        
        nfcController?.onErrorEvent = { message ->
            activity?.runOnUiThread {
                eventSink?.error("NFC_ERROR", message, null)
            }
        }
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)
    }
}
