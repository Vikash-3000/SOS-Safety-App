package com.example.sos.Room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Contact")
class ContactEntity(val phone: String, val name : String) {
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
        set(value) {
            field = value
        }
}