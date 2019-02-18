package com.framgia.oleo.screen.signup

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.framgia.oleo.R
import com.framgia.oleo.data.source.model.User
import com.framgia.oleo.databinding.FragmentSignupBinding
import com.framgia.oleo.utils.Constant.MIN_CHARACTER_INPUT_PASSWORD
import com.framgia.oleo.utils.di.Injectable
import com.framgia.oleo.utils.extension.createDialog
import com.framgia.oleo.utils.extension.isCheckClickableImageButtonClick
import com.framgia.oleo.utils.extension.isCheckMultiClick
import com.framgia.oleo.utils.extension.validInputConfirmPassword
import com.framgia.oleo.utils.extension.validInputEmail
import com.framgia.oleo.utils.extension.validInputPassword
import com.framgia.oleo.utils.extension.validInputPhoneNumber
import com.framgia.oleo.utils.extension.validInputUserName
import com.framgia.oleo.utils.liveData.autoCleared
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_signup.buttonClose
import kotlinx.android.synthetic.main.fragment_signup.buttonSignUp
import kotlinx.android.synthetic.main.fragment_signup.textInputConfirmPassword
import kotlinx.android.synthetic.main.fragment_signup.textInputEmail
import kotlinx.android.synthetic.main.fragment_signup.textInputPassword
import kotlinx.android.synthetic.main.fragment_signup.textInputPhoneNumber
import kotlinx.android.synthetic.main.fragment_signup.textInputUserName
import kotlinx.android.synthetic.main.fragment_signup.textLayoutConfirmPassword
import kotlinx.android.synthetic.main.fragment_signup.textLayoutEmail
import kotlinx.android.synthetic.main.fragment_signup.textLayoutPassword
import kotlinx.android.synthetic.main.fragment_signup.textLayoutPhoneNumber
import kotlinx.android.synthetic.main.fragment_signup.textLayoutUserName
import kotlinx.android.synthetic.main.fragment_signup.textMessageSignUp
import javax.inject.Inject

class SignUpFragment : DialogFragment(), Injectable, View.OnClickListener {

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: SignUpViewModel
    private lateinit var fireBase: FirebaseAuth

    private var binding by autoCleared<FragmentSignupBinding>()

    var onResultWhenLoginSuccess: ((message: String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater: LayoutInflater = activity!!.layoutInflater
        viewModel = SignUpViewModel.create(this, viewModelFactory)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup, null, false)
        binding.viewModel = viewModel
        binding.setLifecycleOwner(this)

        val dialog = createDialog(context!!, binding.root)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
    }

    private fun setUpView() {
        onCheckTextChanged()
        buttonClose.setOnClickListener(this)
        buttonSignUp.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.buttonClose -> if (isCheckMultiClick()) dismiss() // Todo dismiss diaLog Fragment
            R.id.buttonSignUp -> if (isCheckMultiClick()) {
                textMessageSignUp.text = ""
                onCheckValidateFormAndSignUp()
            }
        }
    }

    private fun onCheckValidateFormAndSignUp() {
        if (validInputUserName(context!!, textInputUserName.text.toString(), textLayoutUserName) && validInputEmail(
                context!!, textInputEmail.text.toString(), textLayoutEmail
            ) && validInputPhoneNumber(
                context!!, textInputPhoneNumber.text.toString(), textLayoutPhoneNumber
            ) && validInputPassword(
                context!!, textInputPassword.text.toString(), textLayoutPassword
            ) && validInputConfirmPassword(
                context!!,
                textInputPassword.text.toString(),
                textInputConfirmPassword.text.toString(),
                textLayoutConfirmPassword
            )
        ) {
            onSignUp()
        }
        isCheckClickableImageButtonClick(buttonSignUp)
    }

    private fun onSignUp() {
        fireBase = FirebaseAuth.getInstance()
        val email = textInputEmail.text.toString()
        val password = textInputPassword.text.toString()
        val progressDialog = ProgressDialog(context)

        fireBase.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
            activity!!
        ) { task ->
            if (task.isSuccessful) {
                progressDialog.show()
                val user = User()
                user.email = email
                user.userName = textInputUserName.text.toString()
                user.phoneNumber = textInputPhoneNumber.text.toString()

                FirebaseDatabase.getInstance().getReference(USER_TABLE_FIRE_BASE)
                    .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(user)
                    .addOnCompleteListener(activity!!) { result ->
                        progressDialog.dismiss()
                        if (result.isSuccessful) {
                            onResultWhenLoginSuccess!!.invoke(textInputPhoneNumber.text.toString())
                            dismiss()
                        } else textMessageSignUp.text = getString(R.string.sign_up_fail)
                    }
            } else textMessageSignUp.text = task.exception!!.message.toString()
        }
    }

    private fun onCheckTextChanged() {
        onCheckTextChangedSignUp(textLayoutUserName, textInputUserName)
        onCheckTextChangedSignUp(textLayoutEmail, textInputEmail)
        onCheckTextChangedSignUp(textLayoutPhoneNumber, textInputPhoneNumber)
        onCheckTextChangedSignUp(textLayoutPassword, textInputPassword)
        onCheckTextChangedSignUp(textLayoutConfirmPassword, textInputConfirmPassword)
    }

    private fun onCheckTextChangedSignUp(textInputLayout: TextInputLayout, textInputEditText: TextInputEditText) {

        textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayout.error = ""
                onSetEnableButtonLogin(
                    textInputUserName.text.toString(),
                    textInputEmail.text.toString(),
                    textInputPhoneNumber.text.toString(),
                    textInputPassword.text.toString(),
                    textInputConfirmPassword.text.toString()
                )
            }
        })
    }

    private fun onSetEnableButtonLogin(
        textUserName: String,
        textEmail: String,
        textPhoneNumber: String,
        textPassword: String,
        textConfirmPassword: String
    ) {
        if (textUserName.isNotBlank() && textEmail.isNotBlank() && textPhoneNumber.isNotBlank() && textPassword.length >= MIN_CHARACTER_INPUT_PASSWORD && textConfirmPassword.length >= MIN_CHARACTER_INPUT_PASSWORD) {
            buttonSignUp.isEnabled = true
            buttonSignUp.setBackgroundResource(R.drawable.button_sign_up_bg)
        } else {
            buttonSignUp.isEnabled = false
            buttonSignUp.setBackgroundResource(R.drawable.button_sign_up_disable_bg)
        }
    }

    companion object {

        const val USER_TABLE_FIRE_BASE = "User"

        fun newInstance() = SignUpFragment().apply {
            val bundle = Bundle()
            arguments = bundle
        }
    }
}
