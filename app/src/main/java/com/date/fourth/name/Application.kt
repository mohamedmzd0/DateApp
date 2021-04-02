package com.date.fourth.name

import android.app.Application
import com.onesignal.OSNotificationReceivedEvent
import com.onesignal.OneSignal
import org.json.JSONObject

private const val LOG_TAG = "AFApplication"

class Application : Application() {

    override fun onCreate() {
        super.onCreate()


        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(Constants.ONESIGNAL_APP_ID);
        OneSignal.setNotificationWillShowInForegroundHandler(object :
            OneSignal.OSNotificationWillShowInForegroundHandler {
            override fun notificationWillShowInForeground(notificationReceivedEvent: OSNotificationReceivedEvent?) {
                val data: JSONObject? =
                    notificationReceivedEvent?.notification?.getAdditionalData();

            }
        });
    }

}