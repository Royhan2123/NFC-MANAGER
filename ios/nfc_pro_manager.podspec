Pod::Spec.new do |s|
  s.name             = 'nfc_pro_manager'
  s.version          = '1.0.3'
  s.summary          = 'A professional NFC library for Flutter.'
  s.description      = <<-DESC
A professional NFC library for Flutter. Supports HCE, Card Cloning, and NDEF.
                       DESC
  s.homepage         = 'https://github.com/Royhan2123/NFC-MANAGER'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '12.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'
end
