package com.example.netpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.netpay.ui.components.AddFriendDialog
import com.example.netpay.ui.screens.*
import com.example.netpay.ui.theme.*
import com.example.netpay.ui.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

// ── NAVIGATION STATE ──
sealed class Screen {
    data object Home : Screen()
    data object Pay : Screen()
    data object History : Screen()
    data object Settings : Screen()
    data class FriendDetail(val friendId: String) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetPayApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetPayApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val homeViewModel: HomeViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val currentUser by rememberFirebaseAuthUser()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    NetpayTheme(darkTheme = true) {
        var showAddFriendDialog by remember { mutableStateOf(false) }
        val isSearching by homeViewModel.isSearching.collectAsState()

        if (showAddFriendDialog) {
            AddFriendDialog(
                isSearching = isSearching,
                onDismiss = { showAddFriendDialog = false },
                onAdd = { username ->
                    homeViewModel.addFriend(username) { success, message ->
                        if (success) {
                            showAddFriendDialog = false
                            scope.launch { snackbarHostState.showSnackbar(message ?: "Friend added") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar(message ?: "Failed to add friend") }
                        }
                    }
                }
            )
        }

        if (currentUser == null) {
            LoginScreen(onSuccess = { /* AuthState will update */ })
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = SoftBlack,
                        drawerTonalElevation = 0.dp
                    ) {
                        Spacer(Modifier.height(48.dp))
                        Text(
                            "NETPAY", 
                            modifier = Modifier.padding(24.dp),
                            color = MintGreen, 
                            fontSize = 24.sp, 
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        NavigationDrawerItem(
                            label = { Text("Dashboard") },
                            selected = currentScreen is Screen.Home,
                            onClick = { 
                                currentScreen = Screen.Home
                                scope.launch { drawerState.close() } 
                            },
                            icon = { Icon(Icons.Filled.Dashboard, null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = navigationDrawerItemColors()
                        )
                        NavigationDrawerItem(
                            label = { Text("Split Bill") },
                            selected = currentScreen is Screen.Pay,
                            onClick = { 
                                currentScreen = Screen.Pay
                                scope.launch { drawerState.close() } 
                            },
                            icon = { Icon(Icons.Filled.AddCircle, null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = navigationDrawerItemColors()
                        )
                        NavigationDrawerItem(
                            label = { Text("Activity Feed") },
                            selected = currentScreen is Screen.History,
                            onClick = { 
                                currentScreen = Screen.History
                                scope.launch { drawerState.close() } 
                            },
                            icon = { Icon(Icons.Filled.BarChart, null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = navigationDrawerItemColors()
                        )
                        
                        HorizontalDivider(Modifier.padding(vertical = 16.dp, horizontal = 24.dp), color = BorderGray)

                        NavigationDrawerItem(
                            label = { Text("Add Friend") },
                            selected = false,
                            onClick = { 
                                showAddFriendDialog = true
                                scope.launch { drawerState.close() } 
                            },
                            icon = { Icon(Icons.Filled.PersonAdd, null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = navigationDrawerItemColors()
                        )
                        NavigationDrawerItem(
                            label = { Text("Account Settings") },
                            selected = currentScreen is Screen.Settings,
                            onClick = { 
                                currentScreen = Screen.Settings
                                scope.launch { drawerState.close() } 
                            },
                            icon = { Icon(Icons.Filled.Settings, null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = navigationDrawerItemColors()
                        )
                        
                        Spacer(Modifier.weight(1f))
                        TextButton(
                            onClick = { FirebaseAuth.getInstance().signOut() },
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text("Logout", color = MutedWhite)
                        }
                    }
                }
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = DeepBlack,
                    topBar = {
                        TopAppBar(
                            title = { Text("NetPay", fontWeight = FontWeight.Black) },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, "Menu", tint = PureWhite)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = DeepBlack,
                                titleContentColor = PureWhite
                            )
                        )
                    }
                ) { paddingValues ->
                    Box(Modifier.padding(paddingValues)) {
                        val friends by homeViewModel.balances.collectAsState()
                        val totalBalance by homeViewModel.totalBalance.collectAsState()
                        val transactions by homeViewModel.transactions.collectAsState()

                        when (val screen = currentScreen) {
                            is Screen.Home -> HomeScreen(
                                userName = currentUser?.displayName ?: "User",
                                friends = friends,
                                totalBalance = totalBalance,
                                onFriendClick = { 
                                    homeViewModel.loadTransactions(it.friendId)
                                    currentScreen = Screen.FriendDetail(it.friendId) 
                                },
                                onAddFriend = { showAddFriendDialog = true },
                                onAddTransaction = { currentScreen = Screen.Pay },
                                onLogout = { FirebaseAuth.getInstance().signOut() },
                                onResetProfile = { currentScreen = Screen.Settings }
                            )
                            is Screen.Pay -> AddTransactionScreen(
                                friends = friends,
                                onBack = { currentScreen = Screen.Home },
                                onSubmit = { payloads ->
                                    payloads.forEach { p ->
                                        homeViewModel.addTransaction(p.friendId, p.amount, p.iOweThem, p.note)
                                    }
                                    currentScreen = Screen.Home
                                }
                            )
                            is Screen.History -> HistoryPlaceholder()
                            is Screen.Settings -> SettingsScreen(
                                currentUserEmail = currentUser?.email ?: "",
                                onLogout = { FirebaseAuth.getInstance().signOut() },
                                onDeleteAccount = { email, password ->
                                    homeViewModel.deleteAccount(email, password) { success, message ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(message ?: "Process finished")
                                        }
                                    }
                                }
                            )
                            is Screen.FriendDetail -> {
                                val friend = friends.find { it.friendId == screen.friendId }
                                if (friend != null) {
                                    FriendDetailScreen(
                                        friend = friend,
                                        transactions = transactions,
                                        onBack = { currentScreen = Screen.Home },
                                        onSettleUp = { homeViewModel.settleUp(friend.friendId) },
                                        onRemoveFriend = {
                                            if (friend.netBalance == 0.0) {
                                                homeViewModel.removeFriend(friend.friendId) { success, _ ->
                                                    if (success) currentScreen = Screen.Home
                                                }
                                            }
                                        },
                                        onAddTransaction = { currentScreen = Screen.Pay }
                                    )
                                } else {
                                    currentScreen = Screen.Home
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun navigationDrawerItemColors() = NavigationDrawerItemDefaults.colors(
    selectedContainerColor = MintGreen.copy(alpha = 0.1f),
    selectedTextColor = MintGreen,
    selectedIconColor = MintGreen,
    unselectedContainerColor = Color.Transparent,
    unselectedTextColor = MutedWhite,
    unselectedIconColor = MutedWhite
)

@Composable
fun HistoryPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.BarChart, null, tint = TextLow, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Activity Feed coming soon", color = TextLow)
        }
    }
}

@Composable
fun SettingsScreen(
    currentUserEmail: String,
    onLogout: () -> Unit,
    onDeleteAccount: (email: String, password: String) -> Unit
) {
    // State for the re-auth / delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    var deletePasswordVisible by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf("") }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deletePassword = ""
                deleteError = ""
            },
            containerColor = SoftBlack,
            titleContentColor = PureWhite,
            textContentColor = MutedWhite,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Warning,
                        null,
                        tint = SalmonRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Delete Account?", fontWeight = FontWeight.Black)
                }
            },
            text = {
                Column {
                    Text(
                        "This will permanently remove your profile and all data. This cannot be undone.",
                        color = MutedWhite,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Confirm your password to continue:",
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = {
                            deletePassword = it
                            deleteError = ""
                        },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (deletePasswordVisible)
                            androidx.compose.ui.text.input.VisualTransformation.None
                        else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { deletePasswordVisible = !deletePasswordVisible }) {
                                Icon(
                                    if (deletePasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    null,
                                    tint = MutedWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = DeepBlack,
                            focusedContainerColor = DeepBlack,
                            unfocusedBorderColor = BorderGray,
                            focusedBorderColor = SalmonRed,
                            unfocusedTextColor = PureWhite,
                            focusedTextColor = PureWhite,
                            unfocusedLabelColor = MutedWhite,
                            focusedLabelColor = SalmonRed
                        )
                    )
                    if (deleteError.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(deleteError, color = SalmonRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deletePassword.isBlank()) {
                            deleteError = "Password cannot be empty"
                        } else {
                            showDeleteDialog = false
                            onDeleteAccount(currentUserEmail, deletePassword)
                            deletePassword = ""
                        }
                    },
                    enabled = deletePassword.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SalmonRed,
                        contentColor = PureWhite,
                        disabledContainerColor = DarkGray
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete Permanently", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deletePassword = ""
                    deleteError = ""
                }) {
                    Text("Cancel", color = MutedWhite)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(DeepBlack)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(32.dp))

        // ── Profile Card ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = SoftBlack
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MintGreen.copy(alpha = 0.15f))
                        .border(2.dp, MintGreen.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        currentUserEmail.take(1).uppercase(),
                        color = MintGreen,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        currentUserEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        currentUserEmail,
                        color = MutedWhite,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // ── Section Label ──
        Text(
            "ACCOUNT",
            color = MutedWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )

        // ── Settings Card ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = SoftBlack
        ) {
            Column {
                SettingsRow(
                    icon = Icons.Filled.Logout,
                    iconTint = MutedWhite,
                    label = "Logout from Device",
                    sublabel = "You'll need to sign in again",
                    onClick = onLogout
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = BorderGray
                )
                SettingsRow(
                    icon = Icons.Filled.DeleteForever,
                    iconTint = SalmonRed,
                    label = "Delete Account",
                    sublabel = "Permanently removes all your data",
                    labelColor = SalmonRed,
                    onClick = { showDeleteDialog = true }
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // ── App Info ──
        Text(
            "ABOUT",
            color = MutedWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = SoftBlack
        ) {
            Column {
                SettingsRow(
                    icon = Icons.Filled.Info,
                    iconTint = MutedWhite,
                    label = "Version",
                    sublabel = "1.0.0",
                    onClick = {}
                )
            }
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    sublabel: String,
    labelColor: Color = PureWhite,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = labelColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(sublabel, color = MutedWhite, fontSize = 12.sp)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = BorderGray, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun rememberFirebaseAuthUser(): State<FirebaseUser?> {
    val firebaseAuth = FirebaseAuth.getInstance()
    val authState = remember { mutableStateOf(firebaseAuth.currentUser) }
    DisposableEffect(firebaseAuth) {
        val listener = FirebaseAuth.AuthStateListener { auth -> authState.value = auth.currentUser }
        firebaseAuth.addAuthStateListener(listener)
        onDispose { firebaseAuth.removeAuthStateListener(listener) }
    }
    return authState
}

//test