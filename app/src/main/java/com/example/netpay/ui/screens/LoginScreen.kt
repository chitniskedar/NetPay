package com.example.netpay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.netpay.ui.viewmodel.AuthState
import com.example.netpay.ui.viewmodel.AuthViewModel
import com.example.netpay.ui.theme.*

@Composable
fun LoginScreen(onSuccess: () -> Unit) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Stylized Logo/Header
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(listOf(MintGreen, MintGreen.copy(alpha = 0.6f)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "NP",
                    color = DeepBlack,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "NetPay",
                color = PureWhite,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "ELEGANT EXPENSE TRACKING",
                color = MutedWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(56.dp))

            if (!isLogin) {
                LoginTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name"
                )
                Spacer(Modifier.height(16.dp))
            }

            LoginTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address"
            )
            Spacer(Modifier.height(16.dp))
            LoginTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                isPassword = true
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = { authViewModel.authenticate(email, password, isLogin, name) },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = authState !is AuthState.Loading && email.isNotEmpty() && password.isNotEmpty() && (isLogin || name.isNotEmpty()),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintGreen,
                    contentColor = DeepBlack,
                    disabledContainerColor = DarkGray
                )
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = DeepBlack, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        if (isLogin) "SIGN IN" else "CREATE ACCOUNT", 
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            TextButton(
                onClick = { isLogin = !isLogin },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    if (isLogin) "New to NetPay? Sign Up" else "Already have an account? Sign In",
                    color = MutedWhite,
                    fontSize = 14.sp
                )
            }

            if (authState is AuthState.Error) {
                Spacer(Modifier.height(16.dp))
                Text(
                    (authState as AuthState.Error).message,
                    color = SalmonRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = SoftBlack,
            focusedContainerColor = SoftBlack,
            unfocusedBorderColor = BorderGray,
            focusedBorderColor = MintGreen,
            unfocusedTextColor = PureWhite,
            focusedTextColor = PureWhite,
            unfocusedLabelColor = MutedWhite,
            focusedLabelColor = MintGreen
        ),
        singleLine = true
    )
}
