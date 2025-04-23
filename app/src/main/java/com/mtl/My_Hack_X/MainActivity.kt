package com.mtl.My_Hack_X

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.mtl.My_Hack_X.navigation.NavGraph
import com.mtl.My_Hack_X.navigation.Screen
import com.mtl.My_Hack_X.ui.components.NotificationBadge
import com.mtl.My_Hack_X.ui.screens.notifications.NotificationsViewModel
import com.mtl.My_Hack_X.ui.theme.My_Hack_XTheme
import com.mtl.My_Hack_X.ui.navigation.AppNavHost
import kotlinx.coroutines.CoroutineExceptionHandler

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set up global error handler to prevent crashes
        setupErrorHandler()
        
        Log.d(TAG, "Using test mode: ${MyHackXApp.isTestMode}")
        
        setContent {
            My_Hack_XTheme {
                val errorState = remember { mutableStateOf<String?>(null) }
                
                // Error callback function - store the error in state
                val onError: (Exception) -> Unit = { e ->
                    Log.e(TAG, "Error in app", e)
                    errorState.value = e.message ?: "An unknown error occurred"
                }
                
                // Show either the main content or error UI based on state
                if (errorState.value != null) {
                    EmergencyFallbackUI(
                        errorMessage = errorState.value!!,
                        onRetry = {
                            // Reset error state on retry
                            errorState.value = null
                        }
                    )
                } else {
                    AppContent(onError = onError)
                }
            }
        }
    }
    
    private fun isFirebaseInitialized(): Boolean {
        return try {
            FirebaseApp.getInstance() != null
        } catch (e: Exception) {
            false
        }
    }
    
    private fun setupErrorHandler() {
        val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread $thread", throwable)
            
            // You could report to a crash reporting service here
            
            // Call the original handler if it exists
            defaultExceptionHandler?.uncaughtException(thread, throwable)
        }
    }
}

@Composable
fun AppContent(onError: (Exception) -> Unit) {
    // Use remember to cache errors that occur within this composition
    DisposableEffect(Unit) {
        val handler = CoroutineExceptionHandler { _, exception ->
            if (exception is Exception) {
                onError(exception)
            }
        }
        
        onDispose {
            // Clean up if needed
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val navController = rememberNavController()
        
        // We don't use try-catch here, instead we'll rely on the CoroutineExceptionHandler
        // and the DisposableEffect to report errors
        AppNavHost(navController = navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    navController: NavHostController,
    notificationsViewModel: NotificationsViewModel = viewModel()
) {
    val unreadCount by notificationsViewModel.unreadCount.collectAsState()
    
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            NotificationBadge(
                unreadCount = unreadCount,
                onClick = { navController.navigate(Screen.Notifications.route) }
            )
        }
    )
}

@Composable
fun EmergencyFallbackUI(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Try Again")
            }
        }
    }
} 