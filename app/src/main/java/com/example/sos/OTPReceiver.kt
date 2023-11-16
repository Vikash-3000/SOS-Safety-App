package com.example.sos

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.Telephony
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider


class OTPReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver", "ResourceAsColor")
    override fun onReceive(context: Context?, intent: Intent?) {
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

        for (sms in messages) {
            val message = sms.messageBody
            Log.d("tag", "onReceive: otp ....."+message[0]+"...."+message[1]+"......."+message.substring(0,6))
            LoginActivity.binding.t1.setText(message[0].toString())
            LoginActivity.binding.t2.setText(message[1].toString())
            LoginActivity.binding.t3.setText(message[2].toString())
            LoginActivity.binding.t4.setText(message[3].toString())
            LoginActivity.binding.t5.setText(message[4].toString())
            LoginActivity.binding.t6.setText(message[5].toString())

            LoginActivity.binding.progressLl.visibility = View.VISIBLE

            LoginActivity().verifyCode(message.substring(0, 6))
        }

    }
}