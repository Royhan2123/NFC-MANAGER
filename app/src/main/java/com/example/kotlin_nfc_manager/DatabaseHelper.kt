package com.example.kotlin_nfc_manager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "nfc_manager.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, uid TEXT UNIQUE)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun registerUser(name: String, uid: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("uid", uid)
        }
        return db.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun getUserNameByUid(uid: String): String? {
        val db = this.readableDatabase
        val cursor = db.query("users", arrayOf("name"), "uid=?", arrayOf(uid), null, null, null)
        var name: String? = null
        if (cursor.moveToFirst()) name = cursor.getString(0)
        cursor.close()
        return name
    }
    
    fun getAllUsers(): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT name, uid FROM users", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Pair(cursor.getString(0), cursor.getString(1)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}
