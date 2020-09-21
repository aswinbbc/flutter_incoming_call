package com.github.alezhka.flutter_incoming_call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CallBroadcastReceiver: BroadcastReceiver() {

    companion object {
        private const val ACTION_STARTED = "ACTION_STARTED"
        private const val ACTION_DISMISS = "ACTION_DISMISS"
        private const val ACTION_ACCEPT = "ACTION_ACCEPT"
        private const val ACTION_TIMEOUT = "ACTION_TIMEOUT"

        private const val EXTRA_CALL_DATA = "EXTRA_CALL_DATA"

        private const val EVENT_CALL_STARTED = "call_started"
        private const val EVENT_CALL_ACCEPT = "call_accept"
        private const val EVENT_CALL_DECLINE = "call_decline"
        private const val EVENT_CALL_MISSED = "call_missed"

        fun startedIntent(context: Context, callData: CallData) =
                Intent(context, CallBroadcastReceiver::class.java).apply {
                    action = ACTION_STARTED
                    putExtra(EXTRA_CALL_DATA, callData)
                }
        
        fun acceptIntent(context: Context, callData: CallData) =
                Intent(context, CallBroadcastReceiver::class.java).apply {
                    action = ACTION_ACCEPT
                    putExtra(EXTRA_CALL_DATA, callData)
                }
        
        fun declineIntent(context: Context, callData: CallData) =
                Intent(context, CallBroadcastReceiver::class.java).apply {
                    action = ACTION_DISMISS
                    putExtra(EXTRA_CALL_DATA, callData)
                }

        fun timeoutIntent(context: Context, callData: CallData) =
                Intent(context, CallBroadcastReceiver::class.java).apply {
                    action = ACTION_TIMEOUT
                    putExtra(EXTRA_CALL_DATA, callData)
                }

    }

    override fun onReceive(context: Context, intent: Intent) {
        val callNotification = CallNotification(context)
        val callPlayer = CallPlayer.getInstance(context)
        val action = intent.action ?: return

        val callData = intent.getParcelableExtra<CallData>(EXTRA_CALL_DATA) ?: return
        when(action) {
            ACTION_STARTED -> {
                sendEvent(EVENT_CALL_STARTED, callData)
            }
            ACTION_ACCEPT -> {
                callPlayer.stop()
                callNotification.clearNotification(callData.notificationId)
                
                Utils.backToForeground(context, FlutterIncomingCallPlugin.activity)
                sendEvent(EVENT_CALL_ACCEPT, callData)
            }
            ACTION_DISMISS -> {
                callPlayer.stop()
                callNotification.clearNotification(callData.notificationId)
                sendEvent(EVENT_CALL_DECLINE, callData)
            }
            ACTION_TIMEOUT -> {
                // rnVoipNotificationHelper.showMissCallNotification(intent.getStringExtra("missedCallTitle"), intent.getStringExtra("missedCallBody"), intent.getStringExtra("callerId"));
                sendEvent(EVENT_CALL_MISSED, callData)
            }
        }
    }

    private fun sendEvent(event: String, callData: CallData) {
        val actionData = mapOf(
                "uuid" to callData.uuid,
                "name" to callData.name,
                "number" to callData.handle,
                "avatar" to callData.avatar
        )
        FlutterIncomingCallPlugin.eventHandler.send(event, actionData)
    }
    
}