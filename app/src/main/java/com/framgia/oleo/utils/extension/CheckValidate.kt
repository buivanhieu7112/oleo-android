package com.framgia.oleo.utils.extension

import android.content.Context
import com.framgia.oleo.R
import com.google.android.material.textfield.TextInputLayout

fun validInputPhoneNumber(context: Context, phoneNumber: String, textInputLayout: TextInputLayout): Boolean {
    if (!phoneNumber.checkPhonePattern()) {
        textInputLayout.error = context.getString(R.string.validPhoneNumber)
        return false
    }
    textInputLayout.error = null
    return true
}

fun validInputPassword(context: Context, password: String, textInputLayout: TextInputLayout): Boolean {
    if (!password.checkPasswordPattern()) {
        textInputLayout.error = context.getString(R.string.validPassword)
        return false
    }
    textInputLayout.error = null
    return true
}

fun validInputEmail(context: Context, email: String, textInputLayout: TextInputLayout): Boolean {
    if (!email.checkMailPattern()) {
        textInputLayout.error = context.getString(R.string.validEmail)
        return false
    }
    return true
}

fun validInputUserName(context: Context, userName: String, textInputLayout: TextInputLayout): Boolean {
    if (!userName.checkUserNamePattern()) {
        textInputLayout.error = context.getString(R.string.validUserName)
        return false
    }
    return true
}

fun validInputConfirmPassword(
    context: Context,
    password: String,
    confirmPassword: String,
    textInputLayout: TextInputLayout
): Boolean {
    if (!(password.checkPasswordPattern() && confirmPassword == password)) {
        textInputLayout.error = context.getString(R.string.validConfirmPassword)
        return false
    }
    return true
}

fun String.checkPasswordPattern(): Boolean {
    this.let { return Regex(PASSWORD_PATTERN).find(this) != null }
}

fun String.checkMailPattern(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.checkPhonePattern(): Boolean {
    this.let { return Regex(PHONE_PATTERN).find(this) != null && this.length > 9 && this.length < 12 }
}

fun String.checkUserNamePattern(): Boolean {
    this.let { return Regex(USER_NAME_PATTERN).find(this) != null }
}

const val PASSWORD_PATTERN = "^[a-zA-Z0-9]*$"
const val PHONE_PATTERN = "(09|01|03|07|08|05[2|3|4|5|6|7|8|9|0|1])([0-9]{8})\\b"
const val USER_NAME_PATTERN = "^[a-zA-Z0-9]([._](?![._])|[a-zA-Z0-9]){1,18}[a-zA-Z0-9]\$"
