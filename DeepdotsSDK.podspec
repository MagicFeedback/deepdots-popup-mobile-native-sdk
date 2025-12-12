Pod::Spec.new do |s|
  s.name         = 'DeepdotsSDK'
  s.version      = '0.1.2'
  s.summary      = 'Deepdots Popup SDK - iOS framework (Kotlin Multiplatform).'
  s.homepage     = 'https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk'
  s.license      = { :type => 'Apache 2.0', :file => 'LICENSE' }
  s.author       = { 'Deepdots' => 'sdk@deepdots.com' }
  s.source       = { :git => 'https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk.git', :tag => s.version }
  s.platform     = :ios, '13.0'
  s.swift_version = '5.7'
  # Build only iOS frameworks (XCFramework) to avoid requiring Android SDK
  s.prepare_command = './gradlew :shared:assembleSharedReleaseXCFramework'
  s.vendored_frameworks = 'shared/build/XCFrameworks/release/shared.xcframework'
end