#import "ScreenCaptureRestrictionsPlugin.h"
#if __has_include(<screen_capture_restrictions/screen_capture_restrictions-Swift.h>)
#import <screen_capture_restrictions/screen_capture_restrictions-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "screen_capture_restrictions-Swift.h"
#endif

@implementation ScreenCaptureRestrictionsPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftScreenCaptureRestrictionsPlugin registerWithRegistrar:registrar];
}
@end
