@file:Suppress("DEPRECATION")

package com.example.sos

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sos.Room.ContactEntity
import com.example.sos.Room.ContactViewModel
import com.example.sos.ShakeServices.ReactivateService
import com.example.sos.ShakeServices.SensorService
import com.example.sos.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MainActivity : AppCompatActivity(), Contact {

    private lateinit var viewModel: ContactViewModel
    private lateinit var binding : ActivityMainBinding

    private val PICK_CONTACT = 1

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context : Context
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val sharedPreference =  getSharedPreferences("SOS",Context.MODE_PRIVATE)

        // this is a special permission required only by devices using
        // Android Q and above. The Access Background Permission is responsible
        // for populating the dialog with "ALLOW ALL THE TIME" option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 100)
        }

        context = this
        binding.btn.setBackgroundColor(Color.TRANSPARENT)

        binding.user.text = "Hey, ${sharedPreference.getString("name", "User")}!!!"

        // start the service
        val sensorService = SensorService()

        val intent = Intent(this, sensorService.javaClass)
        if (!isMyServiceRunning(sensorService.javaClass)) {
            startService(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        var adapter = CAdapter(this, this)
        binding.recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(ContactViewModel::class.java)
        viewModel.allContacts.observe(this, Observer {list ->
            list?.let {
                adapter.updateList(it)
            }
        })

    }

    override fun onLongClickListener(contactEntity: ContactEntity): Boolean {
        //viewModel.deleteNode(contactEntity)
        //Toast.makeText(this, contactEntity.name+" Deleted...", Toast.LENGTH_SHORT).show()
        // generate an MaterialAlertDialog Box
        MaterialAlertDialogBuilder(this)
            .setTitle("Remove Contact")
            .setMessage("Are you sure want to remove this contact?")
            .setPositiveButton("YES") { dialogInterface, i -> // delete the specified contact from the database
                viewModel.deleteNode(contactEntity)
                Toast.makeText(this, "Contact removed!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(
                "NO"
            ) { dialogInterface, i -> }
            .show()
        return false
    }

    fun submitData(view : View){
        if (CAdapter.allContacts.size < 5) {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            Toast.makeText(this@MainActivity, "clicked....", Toast.LENGTH_SHORT)
                .show()
            startActivityForResult(intent, PICK_CONTACT)
        } else {
            Toast.makeText(this@MainActivity, "Can't Add more than 5 Contacts", Toast.LENGTH_SHORT)
                .show()
        }



//        val name = binding.name.text.toString()
//        val ph = binding.phone.text.toString()
//
//        if(name.isNotEmpty() && ph.isNotEmpty()){
//            viewModel.insertContact(ContactEntity(ph, name))
//            Toast.makeText(this, "$name Added...", Toast.LENGTH_SHORT).show()
//        }
    }

    // method to check if the service is running
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("Service status", "Running")
                return true
            }
        }
        Log.i("Service status", "Not running")
        return false
    }

    override fun onDestroy() {
        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, ReactivateService::class.java)
        this.sendBroadcast(broadcastIntent)
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permissions Denied!\n Can't use the App!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint(/* ...value = */ "Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_CONTACT -> if (resultCode == RESULT_OK) {
                val contactData = data?.data
                val c = managedQuery(contactData, null, null, null, null)
                if (c.moveToFirst()) {

                    val id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    val phone: String?
                    if (hasPhone.equals("1")) {
                        val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null)
                        phones!!.moveToFirst()
                        phone = phones.getString(phones.getColumnIndex("data1"))

                        val name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                        viewModel.insertContact(ContactEntity(phone, name))
                    }

                }
            }
        }
    }

}