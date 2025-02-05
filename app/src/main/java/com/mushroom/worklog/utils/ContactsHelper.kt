package com.mushroom.worklog.utils

import android.content.Context
import android.provider.ContactsContract
import com.mushroom.worklog.model.Worker

class ContactsHelper(private val context: Context) {
    fun getContacts(): List<Worker> {
        val contacts = mutableListOf<Worker>()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            
            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex).replace("[^0-9]".toRegex(), "")
                if (name.isNotBlank() && number.isNotBlank()) {
                    contacts.add(Worker(name = name, phoneNumber = number))
                }
            }
        }
        
        return contacts
    }
} 