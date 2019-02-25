package com.framgia.oleo.screen.setting

import android.location.Location
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.framgia.oleo.base.BaseViewModel
import com.framgia.oleo.data.source.UserRepository
import com.framgia.oleo.data.source.model.Place
import com.framgia.oleo.utils.Constant.PATH_STRING_USER
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject

class SettingViewModel @Inject constructor(
    private val googleSignInOptions: GoogleSignInOptions,
    private val userRepository: UserRepository,
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
) : BaseViewModel() {

    fun getGoogleSignInOptions(): GoogleSignInOptions = googleSignInOptions

    fun deleteUser() {
        userRepository.deleteUser()
    }

    fun pushUserLocation(location: Location, address: String, time: String) {
        val place = Place()
        var id = firebaseDatabase.getReference(PATH_STRING_USER).push().key.toString()
        place.id = id
        place.latitude = location.latitude.toString()
        place.longitude = location.longitude.toString()
        place.address = address
        place.time = time
        userRepository.pushUserLocation(userRepository.getUser()!!.id, id, place)
    }

    companion object {
        fun create(fragment: Fragment, factory: ViewModelProvider.Factory): SettingViewModel =
            ViewModelProvider(fragment, factory).get(SettingViewModel::class.java)
    }
}
