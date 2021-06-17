package com.rain2002kr.android_tinderapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction

class LikeActivity : AppCompatActivity(), CardStackListener {

    private var auth = Firebase.auth
    private lateinit var userDB: DatabaseReference
    private val logoutButton: Button by lazy { findViewById(R.id.logoutButton) }
    private val adapter = CardItemAdpter()
    private val cardItem = mutableListOf<CardItem>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like )

        getCurrentUserDBstatus()
        userSignOut()
        initCardStackView()

    }

    private fun initCardStackView() {
        val stackView = findViewById<CardStackView>(R.id.cardStackView)
        stackView.layoutManager = CardStackLayoutManager(this)
        stackView.adapter = adapter

    }

    private fun getCurrentUserDBstatus(){
        userDB = Firebase.database.reference.child("Users")
        val currentUserDB = userDB.child(getCurrentUserID())
        // 값을 가져오는 방법은 리스너를 달아야한다.
        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 데이터가 있으면 값을 받아온다.
                if (snapshot.child("name").value == null) {
                    showNameInputPopup()
                    return
                }
                // todo 널이 아니라며, 유저 정보를 갱신하라
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun userSignOut() {

        logoutButton.setOnClickListener {
            Toast.makeText(this, "테스트", Toast.LENGTH_SHORT).show()
            if (auth.currentUser == null) {
                return@setOnClickListener
            } else {
                auth.signOut()
                finish()
            }

        }
    }

    private fun showNameInputPopup() {
        // edit text view 추가하기
        val editText = EditText(this)

        AlertDialog.Builder(this)
            .setTitle("이름을 입력해주세요.")
            .setView(editText)
            .setPositiveButton("저장") { _, _ ->
                if (editText.text.isEmpty()) {
                    showNameInputPopup()
                } else {
                    saveUserName(editText.text.toString())
                }
            }
            .setCancelable(false) // 취소 못하게
            .show()

    }

    private fun saveUserName(name: String) {

        val userId = getCurrentUserID()
        val currentUserDB = userDB.child(userId)
        val user = mutableMapOf<String, Any>()
        user["userId"] = userId
        user["name"] = name
        currentUserDB.updateChildren(user)

        getUnSelectedUsers()
        // todo 유저 정보를 가져와라


    }

    private fun getUnSelectedUsers() {

        userDB.addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // 현재 보고있는 유저아이디가, 내가 아니고 like 와 dislike 가 없다면 한번도 체크 하지 않은 유저
                if(snapshot.child("userId").value != getCurrentUserID()
                    && snapshot.child("likeBy").child("like").hasChild(getCurrentUserID()).not()
                    && snapshot.child("likeBy").child("dislike").hasChild(getCurrentUserID()).not()
                ){
                    val userId = snapshot.child("userId").value.toString()
                    var name = "undecided"
                    if (snapshot.child("name").value != null){
                        name = snapshot.child("name").value.toString()
                    }

                    cardItem.add(CardItem(userId, name))
                    adapter.submitList(cardItem)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                cardItem.find { it.userId == snapshot.key }?.let{
                    it.name = snapshot.child("name").value.toString()
                }
                adapter.submitList(cardItem)
                adapter.notifyDataSetChanged()

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getCurrentUserID(): String {
        if (auth.currentUser == null) {
            Toast.makeText(this, "로그인이 되어있지 않습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
        return auth.currentUser?.uid.orEmpty()
    }


    // todo CardStack implement method
    override fun onCardSwiped(direction: Direction?) {

    }

    override fun onCardDragging(direction: Direction?, ratio: Float) { }

    override fun onCardRewound() {}

    override fun onCardCanceled() {}

    override fun onCardAppeared(view: View?, position: Int) {}

    override fun onCardDisappeared(view: View?, position: Int) {}
}