//
//  PushNotificationManager.swift
//  FirebaseStarterKit
//
//  Created by Florian Marcu on 1/28/19.
//  Copyright © 2019 Instamobile. All rights reserved.
//

import Firebase
import FirebaseFirestore
import FirebaseMessaging
import UIKit
import UserNotifications

class PushNotificationManager: NSObject, MessagingDelegate, UNUserNotificationCenterDelegate {
    let userID: String
    let mFirebase:CDVPlugin
    init(userID: String,mFirebase:CDVPlugin) {
        self.userID = userID
        self.mFirebase = mFirebase
        super.init()
    }

    func registerForPushNotifications() {
        if #available(iOS 10.0, *) {
            // For iOS 10 display notification (sent via APNS)
            UNUserNotificationCenter.current().delegate = self
            let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
            UNUserNotificationCenter.current().requestAuthorization(
                options: authOptions,
                completionHandler: {_, _ in })
            // For iOS 10 data message (sent via FCM)
            Messaging.messaging().delegate = self
        } else {
            let settings: UIUserNotificationSettings =
                UIUserNotificationSettings(types: [.alert, .badge, .sound], categories: nil)
            UIApplication.shared.registerUserNotificationSettings(settings)
        }

        UIApplication.shared.registerForRemoteNotifications()
        updateFirestorePushTokenIfNeeded()
    }

    func updateFirestorePushTokenIfNeeded() {
        if let token = Messaging.messaging().fcmToken {
            print("MY FCM TOKEN : ",token)
            let usersRef = Firestore.firestore().collection("users_table").document(userID)
            usersRef.setData(["fcmToken": token], merge: true)
        }
    }

    func messaging(_ messaging: Messaging, didReceive remoteMessage: MessagingRemoteMessage) {

    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String) {
        updateFirestorePushTokenIfNeeded()
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void)  {

            //let json = try JSONSerialization.data(withJSONObject: notification.request.content.userInfo).base64EncodedData()
            //let jsonData = String(data: json, encoding: String.Encoding.utf8) ?? "{}"
            print("Recieved message with response ",notification.request.content.userInfo)
            let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: notification.request.content.userInfo
            )
            pluginResult?.setKeepCallbackAs(true)
            mFirebase.commandDelegate!.send(
                pluginResult,
                callbackId: MobillyFirebase.notificationCallback?.callbackId
            )
            completionHandler([.alert, .badge, .sound])
    }
}
