package com.example.sos

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PowerManager
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sos.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.concurrent.TimeUnit


@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private val IGNORE_BATTERY_OPTIMIZATION_REQUEST = 1002
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : ActivityLoginBinding
        lateinit var verificationID : String
        lateinit var context: LoginActivity
    }
    private lateinit var getPrefValue : String
    private lateinit var mAuth : FirebaseAuth
    private lateinit var timer: CountDownTimer
    private lateinit var phone : String
    @SuppressLint("MissingInflatedId", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this@LoginActivity

        val sharedPreference =  getSharedPreferences("SOS", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()

        // check for runtime permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_DENIED
            ) {
                requestPermissions(
                    arrayOf<String>(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_CONTACTS
                    ), 100
                )
            }
        }

        // check for BatteryOptimization,
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                askIgnoreOptimization()
            }
        }

        // below line is for getting instance
        // of our FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();

        binding.btn.setBackgroundColor(Color.TRANSPARENT)
        binding.verifyBtn.setBackgroundColor(Color.TRANSPARENT)
        binding.sendLayout.visibility = View.VISIBLE

        getPrefValue = "send"

        binding.btn.setOnClickListener {
            if(binding.name.length() == 0){
                binding.name.error = "Name is required!"
            }

            if(binding.phone.length() < 10){
                binding.phone.error = "Phone number must be 10 digits"
            }

            if(binding.phone.length() == 10 && binding.name.length() > 0) {
                binding.sendLayout.visibility = View.INVISIBLE
                binding.verifyLayout.visibility = View.VISIBLE
            }

            getPrefValue = "verify"
            
            startTimer()

            phone = "+91"+binding.phone.text.toString()

            editor.putString("name", binding.name.text.toString())
            editor.apply()

            otpSend(phone)

            requestSMSPermission()
        }

        binding.verifyBtn.setOnClickListener {

            if(binding.t1.text.isNotEmpty() && binding.t2.text.isNotEmpty() && binding.t3.text.isNotEmpty()
                && binding.t4.text.isNotEmpty() && binding.t5.text.isNotEmpty() && binding.t6.text.isNotEmpty()){

                if(verificationID.isNotEmpty()){
                    val code : String = binding.t1.text.toString() + binding.t2.text.toString() +
                            binding.t3.text.toString() + binding.t4.text.toString() +
                            binding.t5.text.toString() + binding.t6.text.toString()

                    val credential = PhoneAuthProvider.getCredential(verificationID, code)
                    signInWithCredential(credential)
                }

            }else{
                Toast.makeText(applicationContext, "OTP is not valid", Toast.LENGTH_SHORT).show()
            }
        }

        binding.reenter.setOnClickListener {
            binding.verifyLayout.visibility = View.INVISIBLE
            binding.sendLayout.visibility = View.VISIBLE
            getPrefValue = "send"
            timer.cancel()
        }

        binding.resend.setOnClickListener{
            getPrefValue = "verify"

            startTimer()

            otpSend(phone)

            requestSMSPermission()
        }

        autoFillText()
    }

    fun verifyCode(code: String) {
        // below line is used for getting
        // credentials from our verification id and code.
        val credential = PhoneAuthProvider.getCredential(verificationID, code)

        // after getting credential we are
        // calling sign in method.
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential){
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    context.startActivity(Intent(context, MainActivity::class.java))
                    val sharedPreference =  context.getSharedPreferences("SOS", Context.MODE_PRIVATE)
                    val editor = sharedPreference.edit()
                    editor.putBoolean("login", true)
                    editor.apply()
                    context.finish()
                    binding.progressLl.visibility = View.GONE
                }else{
                    Toast.makeText(applicationContext, "OTP is not valid", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun requestSMSPermission() {
        val permission = Manifest.permission.RECEIVE_SMS
        val grant = ContextCompat.checkSelfPermission(this, permission)
        if (grant != PackageManager.PERMISSION_GRANTED) {
            val permission_list = arrayOfNulls<String>(1)
            permission_list[0] = permission
            ActivityCompat.requestPermissions(this, permission_list, 1)
        }
    }

    private fun otpSend(phone : String) {

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phone) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(mCallBack) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    // callback method is called on Phone auth provider.
    private var   // initializing our callbacks for on
    // verification callback method.
            mCallBack: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            // below method is used when
            // OTP is sent from Firebase
            override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                // when we receive the OTP it
                // contains a unique id which
                // we are storing in our string
                // which we have already created.
                verificationID = s
                Log.d(TAG, "onCodeSent: "+s)

            }

            // this method is called when user
            // receive OTP from Firebase.
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {

            }

            // this method is called when firebase doesn't
            // sends our OTP code due to any error or issue.
            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_LONG).show()
                Log.d(TAG, "onVerificationFailed: "+e)
            }
        }

    private fun startTimer() {
        timer = object : CountDownTimer(30000, 1000) {

            // Callback function, fired on regular interval
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                binding.resend.text = "00 : "+(millisUntilFinished / 1000)
                binding.resend.isClickable = false
            }

            // Callback function, fired
            // when the time is up
            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                binding.resend.text = "RESEND"
                binding.resend.isClickable = true
            }
        }.start()
    }

    @Deprecated(message = "Deprecated in Java", replaceWith =
        ReplaceWith("super.onBackPressed()", "androidx.appcompat.app.AppCompatActivity")
    )
    override fun onBackPressed() {
        if(getPrefValue == "verify"){
            getPrefValue = "send"
            binding.verifyLayout.visibility = View.INVISIBLE
            binding.sendLayout.visibility = View.VISIBLE
            timer.cancel()
        }else{
            super.onBackPressed()
        }
    }

    private fun autoFillText(){
        binding.t1.addTextChangedListener(GenericTextWatcher(binding.t1, binding.t2))
        binding.t2.addTextChangedListener(GenericTextWatcher(binding.t2, binding.t3))
        binding.t3.addTextChangedListener(GenericTextWatcher(binding.t3, binding.t4))
        binding.t4.addTextChangedListener(GenericTextWatcher(binding.t4, binding.t5))
        binding.t5.addTextChangedListener(GenericTextWatcher(binding.t5, binding.t6))
        binding.t6.addTextChangedListener(GenericTextWatcher(binding.t6, null))



        binding.t1.setOnKeyListener(GenericKeyEvent(binding.t1, null))
        binding.t2.setOnKeyListener(GenericKeyEvent(binding.t2, binding.t1))
        binding.t3.setOnKeyListener(GenericKeyEvent(binding.t3, binding.t2))
        binding.t4.setOnKeyListener(GenericKeyEvent(binding.t4, binding.t3))
        binding.t5.setOnKeyListener(GenericKeyEvent(binding.t5, binding.t4))
        binding.t6.setOnKeyListener(GenericKeyEvent(binding.t6, binding.t5))
    }

    class GenericKeyEvent internal constructor(private val currentView: EditText, private val previousView: EditText?) : View.OnKeyListener{
        override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
            if(event!!.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.id != R.id.t1 && currentView.text.isEmpty()) {
                //If current is empty then previous EditText's number will also be deleted
                previousView!!.text = null
                previousView.requestFocus()
                return true
            }
            return false
        }


    }

    class GenericTextWatcher internal constructor(private val currentView: View, private val nextView: View?) :
        TextWatcher {
        override fun afterTextChanged(editable: Editable) { // TODO Auto-generated method stub
            val text = editable.toString()
            when (currentView.id) {
                R.id.t1 -> if (text.length == 1) nextView!!.requestFocus()
                R.id.t2 -> if (text.length == 1) nextView!!.requestFocus()
                R.id.t3 -> if (text.length == 1) nextView!!.requestFocus()
                R.id.t4 -> if (text.length == 1) nextView!!.requestFocus()
                R.id.t5 -> if (text.length == 1) nextView!!.requestFocus()
                //You can use EditText4 same as above to hide the keyboard
            }
        }

        override fun beforeTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) { // TODO Auto-generated method stub
        }

        override fun onTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) { // TODO Auto-generated method stub
        }
    }

    // this method prompts the user to remove any
    // battery optimisation constraints from the App
    private fun askIgnoreOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            @SuppressLint("BatteryLife") val intent =
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, IGNORE_BATTERY_OPTIMIZATION_REQUEST)
        }
    }
}