import Flutter
import UIKit

public class NfcProPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "com.nfcpro/methods", binaryMessenger: registrar.messenger())
    let instance = NfcProPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "startScan":
      result(true)
    case "stopScan":
      result(nil)
    default:
      result(FlutterMethodNotImplemented)
    }
  }
}
