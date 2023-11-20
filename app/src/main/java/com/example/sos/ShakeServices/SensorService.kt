@file:Suppress("DEPRECATION")

package com.example.sos.ShakeServices

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SmsManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.sos.CAdapter
import com.example.sos.MainActivity
import com.example.sos.R
import com.example.sos.ShakeServices.ShakeDetector.OnShakeListener
import com.example.sos.SplashActivity
import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnTokenCanceledListener


@Suppress("DEPRECATION")
class SensorService : Service(){

    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mShakeDetector: ShakeDetector? = null

    override fun onBind(intent: Intent?): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        // start the foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground()
        else startForeground(
            1,
            Notification()
        )

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mShakeDetector = ShakeDetector()
        mShakeDetector!!.setOnShakeListener(object : OnShakeListener {
            @RequiresApi(Build.VERSION_CODES.M)
            @SuppressLint("MissingPermission", "SuspiciousIndentation")
            override fun onShake(count: Int) {
                // check if the user has shacked
                // the phone for 3 time in a row
                if (count == 3) {

                    // vibrate the phone
                    vibrate()

                    // create FusedLocationProviderClient to get the user location
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(
                        applicationContext
                    )

                    // use the PRIORITY_BALANCED_POWER_ACCURACY
                    // so that the service doesn't use unnecessary power via GPS
                    // it will only use GPS at this very moment
                    fusedLocationClient.getCurrentLocation(
                        PRIORITY_BALANCED_POWER_ACCURACY,
                        object : CancellationToken() {
                            override fun isCancellationRequested(): Boolean {
                                return false
                            }

                            override fun onCanceledRequested(onTokenCanceledListener: OnTokenCanceledListener): CancellationToken {
                                return this
                            }
                        }).addOnSuccessListener(OnSuccessListener<Location?> { location ->
                        // check if location is null
                        // for both the cases we will
                        // create different messages

                        Log.d("HIiiiiiii    ", "AddOnSuccesssssss"+location.toString())
                        if (location != null) {
                            // get the SMSManager
                            val smsManager: SmsManager = SmsManager.getDefault()
                            // send SMS to each contact

                            for (c in CAdapter.allContacts) {

                                Log.d("HIiiiiiii    ", "AddOn......... list...check"+c.name+" phone.... "+c.phone)

                                val message = """Hey, ${c.name} I am in DANGER, i need help. Please urgently reach me out. Here are my coordinates.
 http://maps.google.com/?q=${location.latitude},${location.longitude}"""
                                    smsManager.sendTextMessage(
                                        c.phone,
                                        null,
                                        message,
                                        null,
                                        null
                                    )
                            }

                        } else {
                            val message = """
                        I am in DANGER, i need help. Please urgently reach me out.
                        GPS was turned off.Couldn't find location. Call your nearest Police Station.
                        """.trimIndent()
                            val smsManager: SmsManager = SmsManager.getDefault()
                            for (c in CAdapter.allContacts) {
                                Log.d("HIiiiiiii    ", "AddOn......... list...check"+c.name+" phone.... "+c.phone)
                                smsManager.sendTextMessage(
                                    c.phone,
                                    null,
                                    message,
                                    null,
                                    null
                                )
                            }
                        }
                    }).addOnFailureListener(OnFailureListener {
                        Log.d("Check: ", "OnFailure")
                        val message = """
                    I am in DANGER, i need help. Please urgently reach me out.
                    GPS was turned off.Couldn't find location. Call your nearest Police Station.
                    """.trimIndent()
                        val smsManager: SmsManager = SmsManager.getDefault()
                        for (c in CAdapter.allContacts) {
                            Log.d("HIiiiiiii    ", "AddOn......... list...check"+c.name+" phone.... "+c.phone)
                            smsManager.sendTextMessage(c.phone, null, message, null, null)
                        }
                    })
                }
            }
        })

        // register the listener
        mSensorManager!!.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI)

    }

    private fun call(no : String) {
        if(ContextCompat.checkSelfPermission(MainActivity.activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.activity, arrayOf(Manifest.permission.CALL_PHONE), 100)
        }

        val dialIntent = Intent(Intent.ACTION_CALL)
        dialIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        dialIntent.data = Uri.parse("tel:${no}")
        MainActivity.activity.startActivity(dialIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "example.permanence"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_MIN
        )
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("You are protected.")
            .setContentText("We are there for you") // this is important, otherwise the notification will show the way
            // you want i.e. it will show some default notification
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }

    // method to vibrate the phone
    fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val vibEff: VibrationEffect

        // Android Q and above have some predefined vibrating patterns
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibEff = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
            vibrator.cancel()
            vibrator.vibrate(vibEff)
        } else {
            vibrator.vibrate(500)
        }
    }

    override fun onDestroy() {
        // create an Intent to call the Broadcast receiver
        val broadcastIntent = Intent()
        broadcastIntent.setClass(this, ReactivateService::class.java)
        this.sendBroadcast(broadcastIntent)
        super.onDestroy()
    }
}