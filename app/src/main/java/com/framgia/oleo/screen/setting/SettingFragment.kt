package com.framgia.oleo.screen.setting

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.framgia.oleo.R
import com.framgia.oleo.base.BaseFragment
import com.framgia.oleo.databinding.FragmentSettingBinding
import com.framgia.oleo.screen.login.LoginFragment
import com.framgia.oleo.utils.extension.isCheckMultiClick
import com.framgia.oleo.utils.extension.replaceFragment
import com.framgia.oleo.utils.extension.showSnackBar
import com.framgia.oleo.utils.extension.showToast
import com.framgia.oleo.utils.liveData.autoCleared
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_setting.*
import java.text.SimpleDateFormat
import java.util.*

class SettingFragment : BaseFragment(), View.OnClickListener, LocationListener {

    private lateinit var viewModel: SettingViewModel
    private var binding by autoCleared<FragmentSettingBinding>()
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var locationManager: LocationManager
    private var hasGps = false
    private var locationGPS: Location? = null
    private lateinit var address: Address

    private var permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = SettingViewModel.create(this, viewModelFactory)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun setUpView() {
        disableView()
        initCheckPermission()
        textViewLogOut.setOnClickListener(this)
    }

    override fun bindView() {
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.textViewLogOut -> if (isCheckMultiClick()) logOut()
            R.id.textViewWatchList -> if (isCheckMultiClick()) getLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        view!!.showSnackBar(GET_LOCATION)
        locationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (hasGps) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                this
            )
            val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (localGpsLocation != null) {
                locationGPS = localGpsLocation

                // Hiển thị vĩ độ lần đầu tiên của vị trí người dùng
                Log.d(LATITUDE_FIRST, locationGPS!!.latitude.toString())

                // Hiển thị kinh độ lần đầu tiên của vị trí người dùng
                Log.d(LONGITUDE_FIRST, locationGPS!!.longitude.toString())
            }
        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    override fun onLocationChanged(location: Location?) {
        if (hasGps) {
            if (location != null) {
                locationGPS = location

                // Hiển thị vĩ độ của vị trí người dùng theo thời gian
                Log.d(LATITUDE, locationGPS!!.latitude.toString())

                // Hiển thị kinh độ của vị trí người dùng theo thời gian
                Log.d(LONGITUDE, locationGPS!!.longitude.toString())

                val formatter = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = locationGPS!!.time

                // Hiển thị thời điểm lấy vị trí người dùng
                Log.d(TIME, formatter.format(calendar.time))

                val geocode = Geocoder(context, Locale.getDefault())
                val listAddress: MutableList<Address> =
                    geocode.getFromLocation(locationGPS!!.latitude, locationGPS!!.longitude, 1)
                address = listAddress[0]
                val currentAddress = address.getAddressLine(0)

                // Hiển thị tên vị trí người dùng
                Log.d(ADDRESS, currentAddress.toString())
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String?) {}
    override fun onProviderDisabled(provider: String?) {}

    private fun initCheckPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(permissions)) {
                enableView()
            } else {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        } else {
            enableView()
        }
    }

    private fun enableView() {
        textViewWatchList.isEnabled = true
        textViewWatchList.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorWhite))
        textViewWatchList.setOnClickListener(this)
    }

    private fun disableView() {
        textViewWatchList.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorGrey100))
        textViewWatchList.isEnabled = false
    }

    private fun checkPermission(permissions: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissions.indices) {
            if (context!!.checkCallingOrSelfPermission(permissions[i]) == PackageManager.PERMISSION_DENIED) {
                allSuccess = false
            }
        }
        return allSuccess
    }

    private fun logOut() {
        val builder = AlertDialog.Builder(activity!!, R.style.alertDialog)
        builder.setMessage(getString(R.string.validate_log_out)).setTitle(getString(R.string.log_out))
        builder.setPositiveButton(getString(R.string.ok)) { dialog, id ->
            signOutFacebook()
            signOutGoogle()
            viewModel.deleteUser()
            replaceFragment(R.id.containerMain, LoginFragment.newInstance(), false)
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun signOutFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return //already logged out
        }
        GraphRequest(AccessToken.getCurrentAccessToken(),
            "/me/permissions/",
            null,
            HttpMethod.DELETE,
            GraphRequest.Callback { LoginManager.getInstance().logOut() }).executeAsync()
    }

    private fun signOutGoogle() {
        googleSignInClient = GoogleSignIn.getClient(activity!!, viewModel.getGoogleSignInOptions())
        if (FirebaseAuth.getInstance() != null && googleSignInClient != null) {
            FirebaseAuth.getInstance().signOut()
            googleSignInClient.signOut()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allSuccess = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                    val requestAgain =
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                            permissions[i]
                        )
                    if (requestAgain) {
                        context!!.showToast(PERMISSION_DENIED)
                    } else {
                        context!!.showToast(ENABLE_PERMISSION)
                    }
                }
            }
            if (allSuccess) {
                enableView()
            }
        }
    }

    companion object {
        private const val PERMISSION_DENIED = "Permission denied"
        private const val ENABLE_PERMISSION = "Go to settings and enable the permission"
        private const val DATE_TIME_FORMAT = "HH:mm-dd/MM/yyyy"
        private const val LATITUDE = "latitude"
        private const val LONGITUDE = "longitude"
        private const val LATITUDE_FIRST = "latitude fist time"
        private const val LONGITUDE_FIRST = "longitude fist time"
        private const val ADDRESS = "Address"
        private const val TIME = "Time"
        private const val GET_LOCATION = "get location"
        private const val PERMISSION_REQUEST = 10
        // The minimum distance to change Updates in meters
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 0F
        // The minimum time between updates in milliseconds
        private const val MIN_TIME_BW_UPDATES: Long = 1000 * 5 * 1

        fun newInstance() = SettingFragment().apply {
            val bundle = Bundle()
            arguments = bundle
        }
    }
}
