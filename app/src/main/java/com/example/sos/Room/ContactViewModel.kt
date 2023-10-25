package com.example.sos.Room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactViewModel(application: Application) : AndroidViewModel(application){

    val allContacts : LiveData<List<ContactEntity>>
    private val repository : ContactRepository

    init {
        val dao = ContactDatabase.getDatabase(application).getContactDao()
        repository = ContactRepository(dao)
        allContacts = repository.allContacts
    }

    fun deleteNode(contactEntity: ContactEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(contactEntity)
    }

    fun insertContact(contactEntity: ContactEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(contactEntity)
    }
}