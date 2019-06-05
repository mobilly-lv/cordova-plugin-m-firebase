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


    @objc(setUserId:)
    func setUserId(command: CDVInvokedUrlCommand) {
        let myUserId = String(command.argument(at: 0) as! String)
        print("MLOG USERID : "+myUserId)
        Analytics.setUserID(myUserId)
    }


    @objc(logEvent:)
    func logEvent(command: CDVInvokedUrlCommand) {
        let event = String(command.argument(at: 0) as! String)
        let data : Dictionary<String, Any> = command.argument(at: 1) as! Dictionary<String, Any>
        print("MLOG EVENT : "+event)
        print("MLOG data : ",data)

        Analytics.logEvent(event, parameters:data)
    }


}
