package com.rain2002kr.android_tinderapp

import android.content.Intent
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
import com.rain2002kr.android_tinderapp.DBKey.Companion.DIS_LIKE
import com.rain2002kr.android_tinderapp.DBKey.Companion.EMAIL
import com.rain2002kr.android_tinderapp.DBKey.Companion.LIKE
import com.rain2002kr.android_tinderapp.DBKey.Companion.LIKED_BY
import com.rain2002kr.android_tinderapp.DBKey.Companion.MATCH
import com.rain2002kr.android_tinderapp.DBKey.Companion.NAME
import com.rain2002kr.android_tinderapp.DBKey.Companion.USERS
import com.rain2002kr.android_tinderapp.DBKey.Companion.USER_ID
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
    private val manager by lazy {
        CardStackLayoutManager(this, this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)

        getCurrentUserDBstatus()
        userSignOut()
        initCardStackView()
        initMatchListButton()


    }

    private fun initMatchListButton() {
        val matchListButton = findViewById<Button>(R.id.matchListButton)
        matchListButton.setOnClickListener {
            startActivity(Intent(this, MatchedUserActivity::class.java))
        }
    }

    private fun initCardStackView() {
        val stackView = findViewById<CardStackView>(R.id.cardStackView)
        stackView.layoutManager = manager
        stackView.adapter = adapter

    }

    private fun getCurrentUserDBstatus() {
        userDB = Firebase.database.reference.child(USERS)
        val currentUserDB = userDB.child(getCurrentUserID())
        // ?????? ???????????? ????????? ???????????? ???????????????.
        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // ???????????? ????????? ?????? ????????????.
                if (snapshot.child(NAME).value == null) {
                    showNameInputPopup()
                    return
                }

                getUnSelectedUsers()
                // todo ?????? ????????????, ?????? ????????? ????????????
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun userSignOut() {

        logoutButton.setOnClickListener {
            Toast.makeText(this, "${auth.currentUser} ???????????? ???????????????.", Toast.LENGTH_SHORT).show()
            if (auth.currentUser == null) {
                return@setOnClickListener
            } else {
                auth.signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

        }
    }

    private fun showNameInputPopup() {
        // edit text view ????????????
        val editText = EditText(this)

        AlertDialog.Builder(this)
                .setTitle(getString(R.string.write_name))
                .setView(editText)
                .setPositiveButton(R.string.save) { _, _ ->
                    if (editText.text.isEmpty()) {
                        showNameInputPopup()
                    } else {
                        saveUserName(editText.text.toString())
                    }
                }
                .setCancelable(false) // ?????? ?????????
                .show()

    }

    private fun saveUserName(name: String) {

        val userId = getCurrentUserID()
        val currentUserDB = userDB.child(userId)
        val userEmail = getCurrentUserEmail()
        val user = mutableMapOf<String, Any>()
        user[USER_ID] = userId
        user[NAME] = name
        user[EMAIL] = userEmail
        currentUserDB.updateChildren(user)

        getUnSelectedUsers()
        // todo ?????? ????????? ????????????


    }

    private fun getUnSelectedUsers() {

        userDB.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // ?????? ???????????? ??????????????????, ?????? ????????? like ??? dislike ??? ????????? ????????? ?????? ?????? ?????? ??????
                if (snapshot.child(USER_ID).value != getCurrentUserID()
                        && snapshot.child(LIKED_BY).child(LIKE).hasChild(getCurrentUserID()).not()
                        && snapshot.child(LIKED_BY).child(DIS_LIKE).hasChild(getCurrentUserID()).not()
                ) {
                    val userId = snapshot.child(USER_ID).value.toString()
                    var name = getString(R.string.not_decide_name)
                    if (snapshot.child(NAME).value != null) {
                        name = snapshot.child(NAME).value.toString()
                    }

                    cardItem.add(CardItem(userId, name))
                    adapter.submitList(cardItem)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                cardItem.find { it.userId == snapshot.key }?.let {
                    it.name = snapshot.child(NAME).value.toString()
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
            Toast.makeText(this, getString(R.string.notLogin), Toast.LENGTH_SHORT).show()
            finish()
        }
        return auth.currentUser?.uid.orEmpty()
    }

    private fun getCurrentUserEmail(): String {
        if (auth.currentUser == null) {
            Toast.makeText(this, getString(R.string.notLogin), Toast.LENGTH_SHORT).show()
            finish()
        }
        return auth.currentUser?.email.orEmpty()
    }



    private fun like() {
        val card = cardItem[manager.topPosition - 1]
        cardItem.removeFirst()
        // ?????? ??????,
        userDB.child(card.userId)
                .child(LIKED_BY)
                .child(LIKE)
                .child(getCurrentUserID())
                .setValue(true)

        // todo ????????? ??? ????????? ?????? ??????.
        Toast.makeText(this, "${card.name}?????? Like ???????????????.", Toast.LENGTH_LONG).show()
        saveMatchIfOtherLikeMe(card.userId)

    }

    private fun saveMatchIfOtherLikeMe(otherUserId: String) {

        val otherUserDB = userDB.child(getCurrentUserID()).child(LIKED_BY).child(LIKE).child(otherUserId)
        otherUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == true) {
                    userDB.child(getCurrentUserID())
                            .child(LIKED_BY)
                            .child(MATCH)
                            .child(otherUserId)
                            .setValue(true)


                    userDB.child(otherUserId)
                            .child(LIKED_BY)
                            .child(MATCH)
                            .child(getCurrentUserID())
                            .setValue(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun dislike() {
        val card = cardItem[manager.topPosition - 1]
        cardItem.removeFirst()
        // ?????? ??????,
        userDB.child(card.userId)
                .child(LIKED_BY)
                .child(DIS_LIKE)
                .child(getCurrentUserID())
                .setValue(true)

        // todo ????????? ??? ????????? ?????? ??????.
        Toast.makeText(this, "${card.name}?????? disLike ???????????????.", Toast.LENGTH_LONG).show()
    }

    // todo CardStack implement method
    override fun onCardSwiped(direction: Direction?) {
        when (direction) {
            Direction.Right -> like()
            Direction.Left -> dislike()
            else -> {

            }
        }
    }

    override fun onCardDragging(direction: Direction?, ratio: Float) {}

    override fun onCardRewound() {}

    override fun onCardCanceled() {}

    override fun onCardAppeared(view: View?, position: Int) {}

    override fun onCardDisappeared(view: View?, position: Int) {}
}