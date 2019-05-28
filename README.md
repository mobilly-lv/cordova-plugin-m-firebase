# cordova-plugin-m-firebase

Cordova plugin for firebase push notification iOS and Android

This is quick and dirty implimentation to support iOS cordova 9.
iOS part is in Swift and using CocoaPods

Resources are copied every time with `cordova prepare`
```xml
<m-firebase>
    <colors src="res/m-firebase-colors.xml"/>
    <strings src="res/m-firebase-strings.xml"/>
    <google-services-json src="google-services.json"/>
    <google-services-plist src="GoogleService-Info.plist"/>
    <ic-notification src="res/icons/android/ic_android_notification_mdpi.png" size="mdpi"/>
    <ic-notification src="res/icons/android/ic_android_notification_hdpi.png" size="hdpi"/>
    <ic-notification src="res/icons/android/ic_android_notification_xhdpi.png" size="xhdpi"/>
    <ic-notification src="res/icons/android/ic_android_notification_xxhdpi.png" size="xxhdpi"/>
    <ic-notification src="res/icons/android/ic_android_notification_xxxhdpi.png" size="xxxhdpi"/>
  </m-firebase>```
