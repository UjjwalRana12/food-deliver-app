package com.android.fooddelivery.viewModel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthViewModel:ViewModel() {

    private val auth:FirebaseAuth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private var verificationId: String? = null

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(exception: FirebaseException) {
            _uiState.value = AuthUiState.Error(exception.message)
            Log.w(TAG, "onVerificationFailed", exception)

            if (exception is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (exception is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            } else if (exception is FirebaseAuthMissingActivityForRecaptchaException) {
                // reCAPTCHA verification attempted with null Activity
            }
        }

        override fun onCodeSent(verId: String, token: PhoneAuthProvider.ForceResendingToken) {
            verificationId = verId
            _uiState.value = AuthUiState.CodeSent
        }
    }

    fun startPhoneNumberVerification(phoneNumber: String, activity: ComponentActivity) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode(code: String) {
        verificationId?.let {
            val credential = PhoneAuthProvider.getCredential(it, code)
            signInWithPhoneAuthCredential(credential)
        }
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
            viewModelScope.launch {
                try {
                    auth.signInWithCredential(credential).await()
                    _uiState.value = AuthUiState.Success
                }catch (e:Exception){
                    _uiState.value = AuthUiState.Error(e.message)
                }
            }
    }

    sealed class AuthUiState {
        object Idle : AuthUiState()
        object CodeSent : AuthUiState()
        object Success : AuthUiState()
        data class Error(val message: String?) : AuthUiState()
    }

}