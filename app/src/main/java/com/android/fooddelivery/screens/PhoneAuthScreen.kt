package com.android.fooddelivery.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.fooddelivery.viewModel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(viewModel: AuthViewModel, activity: ComponentActivity) {
    val uiState by viewModel.uiState.collectAsState()
    var phoneNumber by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    when (uiState) {
        is AuthViewModel.AuthUiState.Idle -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                TextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.startPhoneNumberVerification(phoneNumber, activity = activity) }) {
                    Text("Send Code")
                }
            }
        }
        is AuthViewModel.AuthUiState.CodeSent -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                TextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Verification Code") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.verifyCode(code) }) {
                    Text("Verify Code")
                }
            }
        }
        is AuthViewModel.AuthUiState.Success -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Authentication Successful!")
            }
        }
        is AuthViewModel.AuthUiState.Error -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Error: ${(uiState as AuthViewModel.AuthUiState.Error).message}")
            }
        }
    }
}

