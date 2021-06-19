package com.rain2002kr.android_tinderapp

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MatchedUserActivity :AppCompatActivity(){
    private var auth = Firebase.auth
    private lateinit var userDB: DatabaseReference
    private val adapter = MatchedUserAdapter()
    private val cardItem = mutableListOf<CardItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match)

        userDB = Firebase.database.reference.child("Users")

        initMatchedUserRecyclerView()
        getMatchUsers()

    }

    private fun getMatchUsers() {
        val matchedDB = userDB.child(getCurrentUserID()).child("likeBy").child("match")

        matchedDB.addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.key?.isNotEmpty() == true){
                    getUserByKey(snapshot.key.orEmpty())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun getUserByKey(userId : String){
        userDB.child(userId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                cardItem.add(CardItem(userId, snapshot.child("name").value.toString()))
                adapter.submitList(cardItem)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun initMatchedUserRecyclerView() {
        val recyclerview = findViewById<RecyclerView>(R.id.matchedUserRecyclerView)

        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = adapter

    }

    private fun getCurrentUserID(): String {
        if (auth.currentUser == null) {
            Toast.makeText(this, "로그인이 되어있지 않습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
        return auth.currentUser?.uid.orEmpty()
    }
}