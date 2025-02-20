import Flutter
import UIKit

public class SwiftScreenCaptureRestrictionsPlugin: NSObject, FlutterPlugin {
  static var channel: FlutterMethodChannel?

  public static func register(with registrar: FlutterPluginRegistrar) {
    channel = FlutterMethodChannel(name: "screen_capture_restrictions", binaryMessenger: registrar.messenger())
    let instance: SwiftScreenCaptureRestrictionsPlugin = .init()
    registrar.addMethodCallDelegate(instance, channel: channel!)

    // 画面を黒くするためのTextField
    // TextField for blacking out the screen
    let textField = UITextField()
    instance.setupTextField(textField: textField)

    channel!.setMethodCallHandler { (call: FlutterMethodCall, result:@escaping FlutterResult) -> Void in
      switch call.method {
        case "observe":
          // 監視開始時点での画面収録状態を確認する
          // Check the screen recording status at the start of observation
          channel!.invokeMethod("didScreenRecord", arguments: UIScreen.main.isCaptured)

          NotificationCenter.default.addObserver(
            self,
            selector: #selector(onChangedScreenShot(_:)),
            name: UIApplication.userDidTakeScreenshotNotification,
            object: nil
          )
          NotificationCenter.default.addObserver(
            self,
            selector: #selector(onChangedScreenRecord(_:)),
            name: UIScreen.capturedDidChangeNotification,
            object: nil
          )
          result(nil)
          break

        case "dispose":
          NotificationCenter.default.removeObserver(
            self,
            name: UIApplication.userDidTakeScreenshotNotification,
            object: nil
          )
          NotificationCenter.default.removeObserver(
            self,
            name: UIScreen.capturedDidChangeNotification,
            object: nil
          )
          result(nil)
          break

        case "isRecording":
          result(UIScreen.main.isCaptured)
          break

        case "enableSecure":
          textField.isSecureTextEntry = true
          result(nil)
          break

        case "disableSecure":
          textField.isSecureTextEntry = false
          result(nil)
          break
          
        default:
          break
      }
    }
  }

  @objc static func onChangedScreenShot(_ isCaptured: Bool) {
    // iosはfileのpathを返せない
    // ios cannot return the path of the file
    channel!.invokeMethod("didScreenShot", arguments: "ios does not support file path.")
  }
  
  @objc static func onChangedScreenRecord(_ isCaptured: Bool) {
    channel!.invokeMethod("didScreenRecord", arguments: UIScreen.main.isCaptured)
  }

  deinit {
    NotificationCenter.default.removeObserver(
      self,
      name: UIApplication.userDidTakeScreenshotNotification,
      object: nil
    )
    NotificationCenter.default.removeObserver(
      self,
      name: UIScreen.capturedDidChangeNotification,
      object: nil
    )
  }

  private func setupTextField(textField: UITextField) {
    if let window = getWindow() {
      if(!window.subviews.contains(textField)) {
        window.addSubview(textField)
        textField.centerYAnchor.constraint(equalTo: window.centerYAnchor).isActive = true
        textField.centerXAnchor.constraint(equalTo: window.centerXAnchor).isActive = true
        window.layer.superlayer?.addSublayer(textField.layer)
        window.layer.superlayer?.backgroundColor = UIColor.white.cgColor
        textField.layer.sublayers?.last?.addSublayer(window.layer)
      }
    }
  }

  private func getWindow() -> UIWindow? {
    if #available(iOS 13.0, *) {
      return UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }.first?.windows.first
    } else {
      return UIApplication.shared.windows.first
    }
  }
}
