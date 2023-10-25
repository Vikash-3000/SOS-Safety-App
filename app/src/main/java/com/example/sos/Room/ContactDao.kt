package com.example.sos.Room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact : ContactEntity)

    @Delete
    suspend fun delete(contact: ContactEntity)

    @Query("Select * from contact order by id ASC")
    fun getAllContacts() : LiveData<List<ContactEntity>>
}