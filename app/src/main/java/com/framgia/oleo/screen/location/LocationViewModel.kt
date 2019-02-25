package com.framgia.oleo.screen.location

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.framgia.oleo.base.BaseViewModel
import com.framgia.oleo.data.source.UserRepository
import com.framgia.oleo.data.source.model.Place
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject

class LocationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
) : BaseViewModel() {
    private lateinit var locationAdapter: LocationAdapter

    fun setAdapter(locationAdapter: LocationAdapter) {
        this.locationAdapter = locationAdapter
    }

    fun getAdapter() = locationAdapter

    fun getListLocation() {
        val id = userRepository.getUser()!!.id
        userRepository.getFriendLocation(id, object : ChildEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {}

            override fun onChildMoved(dataSnapshot: DataSnapshot, id: String?) {}

            override fun onChildChanged(dataSnapshot: DataSnapshot, id: String?) {}

            override fun onChildAdded(dataSnapshot: DataSnapshot, id: String?) {
                locationAdapter.updateData(dataSnapshot.getValue(Place::class.java)!!)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        })
    }

    companion object {
        const val LIMIT_DATA = 5

        fun create(fragment: Fragment, factory: ViewModelProvider.Factory): LocationViewModel =
            ViewModelProvider(fragment, factory).get(LocationViewModel::class.java)
    }
}
