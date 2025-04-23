package com.mtl.My_Hack_X.ui.screens.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.mtl.My_Hack_X.MyHackXApp
import com.mtl.My_Hack_X.R
import com.mtl.My_Hack_X.navigation.Screen
import com.mtl.My_Hack_X.ui.components.ErrorMessage
import com.mtl.My_Hack_X.ui.components.LoadingSpinner

enum class AuthMode { Login, Register }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.loginState.collectAsState()
    var authMode by remember { mutableStateOf(AuthMode.Login) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetPasswordEmail by remember { mutableStateOf("") }
    var resetPasswordStatus by remember { mutableStateOf<String?>(null) }

    // Configure Google Sign In
    val gso = remember {
        try {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.firebase_web_api_key))
                .requestEmail()
                .build()
        } catch (e: Exception) {
            // Fallback for test mode
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        }
    }
    
    val googleSignInClient = remember {
        try {
            GoogleSignIn.getClient(context, gso)
        } catch (e: Exception) {
            null
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    account?.idToken?.let { token ->
                        viewModel.signInWithGoogle(token)
                    }
                } catch (e: ApiException) {
                    errorText = "Google sign-in failed: ${e.message}"
                }
            } catch (e: Exception) {
                errorText = "Error processing sign-in: ${e.message}"
            }
        }
    }

    LaunchedEffect(state) {
        when (state) {
            is LoginState.Success -> {
                try {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } catch (e: Exception) {
                    errorText = "Navigation error: ${e.message}"
                }
            }
            is LoginState.Error -> {
                errorText = (state as LoginState.Error).message
            }
            else -> {}
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { 
                showForgotPasswordDialog = false 
                resetPasswordStatus = null
                resetPasswordEmail = ""
            },
            title = { Text("Password Reset") },
            text = {
                Column {
                    if (resetPasswordStatus != null) {
                        Text(
                            text = resetPasswordStatus!!,
                            color = if (resetPasswordStatus!!.startsWith("Error")) 
                                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    } else {
                        Text(
                            text = "Enter your email address and we'll send you a link to reset your password.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    OutlinedTextField(
                        value = resetPasswordEmail,
                        onValueChange = { resetPasswordEmail = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetPasswordEmail.isBlank()) {
                            resetPasswordStatus = "Error: Email cannot be empty"
                            return@Button
                        }
                        
                        viewModel.resetPassword(resetPasswordEmail) { success, message ->
                            resetPasswordStatus = if (success) {
                                "Password reset email sent. Check your inbox."
                            } else {
                                "Error: $message"
                            }
                        }
                    }
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showForgotPasswordDialog = false
                        resetPasswordStatus = null
                        resetPasswordEmail = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = if (authMode == AuthMode.Login) "Login" else "Sign Up",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is LoginState.Loading -> LoadingSpinner()
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // App Logo
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(120.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Tab Row for Login/Register
                        TabRow(
                            selectedTabIndex = authMode.ordinal,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Tab(
                                selected = authMode == AuthMode.Login,
                                onClick = { authMode = AuthMode.Login },
                                text = { Text("Login") }
                            )
                            Tab(
                                selected = authMode == AuthMode.Register,
                                onClick = { authMode = AuthMode.Register },
                                text = { Text("Sign Up") }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Test Mode Indicator
                        if (MyHackXApp.isTestMode) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ) {
                                Text(
                                    text = "App is running in Test Mode\nUse email: test@example.com\nPassword: password",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Error Text
                        if (errorText != null) {
                            Text(
                                text = errorText!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorText = null },
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorText = null },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (passwordVisible) R.drawable.ic_visibility 
                                                else R.drawable.ic_visibility_off
                                        ),
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (authMode == AuthMode.Register) ImeAction.Next else ImeAction.Done
                            )
                        )
                        
                        // Confirm Password Field (only in Register mode)
                        if (authMode == AuthMode.Register) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it; errorText = null },
                                label = { Text("Confirm Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            painter = painterResource(
                                                id = if (passwordVisible) R.drawable.ic_visibility 
                                                    else R.drawable.ic_visibility_off
                                            ),
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Login/Register Button
                        Button(
                            onClick = {
                                errorText = null
                                if (email.isBlank() || password.isBlank()) {
                                    errorText = "Email and password cannot be empty"
                                    return@Button
                                }
                                
                                if (authMode == AuthMode.Register) {
                                    if (password != confirmPassword) {
                                        errorText = "Passwords don't match"
                                        return@Button
                                    }
                                    viewModel.registerWithEmail(email, password)
                                } else {
                                    viewModel.signInWithEmail(email, password)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (authMode == AuthMode.Login) "Login" else "Sign Up")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Divider with OR text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Divider(modifier = Modifier.weight(1f))
                            Text(
                                text = " OR ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Divider(modifier = Modifier.weight(1f))
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Google Sign-In Button
                        OutlinedButton(
                            onClick = { 
                                if (googleSignInClient != null) {
                                    signInWithGoogle(googleSignInClient, launcher)
                                } else {
                                    // Simulate Google sign-in in test mode
                                    viewModel.signInWithGoogle("test_token")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_google),
                                    contentDescription = "Google Icon",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Continue with Google")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Forgot Password (only in Login mode)
                        if (authMode == AuthMode.Login) {
                            TextButton(
                                onClick = { showForgotPasswordDialog = true },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Forgot Password?")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun signInWithGoogle(
    googleSignInClient: GoogleSignInClient,
    launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>
) {
    try {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    } catch (e: Exception) {
        // Handle any errors silently
    }
} 