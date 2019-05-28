import Firebase

@objc(MobillyFirebase) class MobillyFirebase : CDVPlugin {
    static var pushManager:PushNotificationManager? = nil
    static var notificationCallback:CDVInvokedUrlCommand? = nil

    @objc(initialize:)
    func initialize(command: CDVInvokedUrlCommand) {

        MobillyFirebase.pushManager = PushNotificationManager(userID:"currently_logged_in_user_id",mFirebase: self)
        MobillyFirebase.pushManager?.registerForPushNotifications()

        FirebaseApp.configure()

        let token = Messaging.messaging().fcmToken
        print("MY TOKEN : ",token as Any)

        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK,
            messageAs: token
        )

        pluginResult?.setKeepCallbackAs(true)

        self.commandDelegate!.send(
          pluginResult,
          callbackId: command.callbackId
        )
    }

    @objc(onNotification:)
    func onNotification(command: CDVInvokedUrlCommand) {
        MobillyFirebase.notificationCallback = command
    }

}
