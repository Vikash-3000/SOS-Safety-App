package com.example.sos.Room

import androidx.lifecycle.LiveData
import com.example.sos.Room.ContactDao
import com.example.sos.Room.ContactEntity

class ContactRepository(private val contactDao : ContactDao) {

    val allContacts : LiveData<List<ContactEntity>> = contactDao.getAllContacts()

    suspend fun insert(contact : ContactEntity){
        contactDao.insert(contact)
    }

    suspend fun delete(contact: ContactEntity){
        contactDao.delete(contact)
    }
}