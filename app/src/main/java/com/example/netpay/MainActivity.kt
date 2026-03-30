package com.example.netpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpay.ui.theme.*

// ─────────────── Data Models ───────────────

data class Friend(
    val id: String,
    val name: String,
    val initials: String,
    val netBalance: Double,          // +ve = they owe me, -ve = I owe them
    val avatarColor: Color = NetPayBlue,
    val lastActivity: String = ""
)

data class Transaction(
    val id: String,
    val description: String,
    val amount: Double,
    val iOwedThem: Boolean,         // true = I paid / they owe me
    val date: String
)

// ─────────────── Sample Data ───────────────

val sampleFriends = listOf(
    Friend("1", "Priya Sharma",  "PS", 1200.0,  Color(0xFF7C3AED), "Dinner last night"),
    Friend("2", "Rahul Mehta",   "RM", -850.0,  Color(0xFFDB2777), "Movie tickets"),
    Friend("3", "Aarav Joshi",   "AJ", 3500.0,  Color(0xFF059669), "Trip to Goa"),
    Friend("4", "Sneha Patel",   "SP", -250.0,  Color(0xFFD97706), "Coffee & snacks"),
    Friend("5", "Vikrant Singh", "VS", 0.0,     Color(0xFF0891B2), "All settled up")
)

val sampleTransactions = mapOf(
    "1" to listOf(
        Transaction("t1", "Dinner at Spice Garden", 800.0,  false, "28 Mar"),
        Transaction("t2", "Uber ride share",        400.0,  false, "27 Mar"),
        Transaction("t3", "Groceries split",        600.0,  true,  "25 Mar"),
        Transaction("t4", "Movie – Oppenheimer",    300.0,  true,  "20 Mar")
    ),
    "2" to listOf(
        Transaction("t5", "Movie tickets",          850.0,  true,  "26 Mar"),
        Transaction("t6", "Ice cream",              150.0,  false, "22 Mar")
    )
)

// ─────────────── Navigation ───────────────

sealed class Screen {
    object Home : Screen()
    data class FriendDetail(val friendId: String) : Screen()
    object AddTransaction : Screen()
}

// ─────────────── Main ───────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetpayTheme {
                NetPayApp()
            }
        }
    }
}

@Composable
fun NetPayApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var friends by remember { mutableStateOf(sampleFriends) }

    when (val screen = currentScreen) {
        is Screen.Home -> HomeScreen(
            friends = friends,
            onFriendClick = { currentScreen = Screen.FriendDetail(it.id) },
            onAddTransaction = { currentScreen = Screen.AddTransaction }
        )
        is Screen.FriendDetail -> {
            val friend = friends.first { it.id == screen.friendId }
            val txList  = sampleTransactions[screen.friendId] ?: emptyList()
            FriendDetailScreen(
                friend = friend,
                transactions = txList,
                onBack = { currentScreen = Screen.Home },
                onSettleUp = {
                    friends = friends.map {
                        if (it.id == friend.id) it.copy(netBalance = 0.0) else it
                    }
                    currentScreen = Screen.Home
                },
                onAddTransaction = { currentScreen = Screen.AddTransaction }
            )
        }
        is Screen.AddTransaction -> AddTransactionScreen(
            friends = friends,
            onBack = { currentScreen = Screen.Home },
            onSubmit = { friendId, amount, iOweThem, _ ->
                val delta = if (iOweThem) -amount else amount
                friends = friends.map {
                    if (it.id == friendId) it.copy(netBalance = it.netBalance + delta) else it
                }
                currentScreen = Screen.Home
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  SCREEN 1 — HOME
// ═══════════════════════════════════════════════════════════

@Composable
fun HomeScreen(
    friends: List<Friend>,
    onFriendClick: (Friend) -> Unit,
    onAddTransaction: () -> Unit
) {
    val totalBalance = friends.sumOf { it.netBalance }
    val totalOwedToMe = friends.filter { it.netBalance > 0 }.sumOf { it.netBalance }
    val totalIOwe     = friends.filter { it.netBalance < 0 }.sumOf { -it.netBalance }

    Box(modifier = Modifier.fillMaxSize().background(NetPayBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Header gradient card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(NetPayBlueDark, NetPayBlue, NetPayBlueLight)
                            )
                        )
                        .padding(top = 56.dp, start = 24.dp, end = 24.dp, bottom = 36.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Good evening,", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                                Text("Kedar 👋", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(Modifier.height(28.dp))

                        Text(
                            text = "Net Balance",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = formatAmount(totalBalance),
                            color = Color.White,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (totalBalance >= 0) "overall you are owed" else "overall you owe",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 13.sp
                        )

                        Spacer(Modifier.height(24.dp))

                        // Summary pills
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryPill(
                                label = "Owed to me",
                                amount = totalOwedToMe,
                                isPositive = true
                            )
                            SummaryPill(
                                label = "I owe",
                                amount = totalIOwe,
                                isPositive = false
                            )
                        }
                    }
                }
            }

            // ── Section label
            item {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Friends",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NetPayTextPrimary
                    )
                    Text(
                        "${friends.size} people",
                        fontSize = 13.sp,
                        color = NetPayTextSecondary
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Friend list
            items(friends) { friend ->
                FriendCard(friend = friend, onClick = { onFriendClick(friend) })
                Spacer(Modifier.height(8.dp))
            }
        }

        // ── FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = NetPayBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.shadow(12.dp, RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Add", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }

        // ── Bottom gradient fade
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, NetPayBackground))
                )
        )
    }
}

@Composable
fun SummaryPill(label: String, amount: Double, isPositive: Boolean) {
    val bg    = if (isPositive) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.10f)
    val color = Color.White
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val icon = if (isPositive) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Column {
            Text(label, color = color.copy(alpha = 0.7f), fontSize = 11.sp)
            Text("₹${amount.toInt()}", color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FriendCard(friend: Friend, onClick: () -> Unit) {
    val balanceColor = when {
        friend.netBalance > 0 -> NetPayGreen
        friend.netBalance < 0 -> NetPayRed
        else                  -> NetPayTextSecondary
    }
    val balanceBg = when {
        friend.netBalance > 0 -> NetPayGreenLight
        friend.netBalance < 0 -> NetPayRedLight
        else                  -> NetPayBlueSurface
    }
    val balanceText = when {
        friend.netBalance > 0 -> "owes you ₹${friend.netBalance.toInt()}"
        friend.netBalance < 0 -> "you owe ₹${(-friend.netBalance).toInt()}"
        else                  -> "all settled"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NetPaySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(friend.avatarColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.initials,
                    color = friend.avatarColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(14.dp))

            // Name + last activity
            Column(modifier = Modifier.weight(1f)) {
                Text(friend.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NetPayTextPrimary)
                if (friend.lastActivity.isNotEmpty()) {
                    Spacer(Modifier.height(3.dp))
                    Text(friend.lastActivity, fontSize = 12.sp, color = NetPayTextSecondary, maxLines = 1)
                }
            }

            // Balance chip
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(balanceBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (friend.netBalance == 0.0) "Settled" else "₹${Math.abs(friend.netBalance).toInt()}",
                        color = balanceColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(balanceText, fontSize = 11.sp, color = balanceColor)
            }

            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = NetPayTextHint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  SCREEN 2 — FRIEND DETAIL
// ═══════════════════════════════════════════════════════════

@Composable
fun FriendDetailScreen(
    friend: Friend,
    transactions: List<Transaction>,
    onBack: () -> Unit,
    onSettleUp: () -> Unit,
    onAddTransaction: () -> Unit
) {
    val balanceColor = when {
        friend.netBalance > 0 -> NetPayGreen
        friend.netBalance < 0 -> NetPayRed
        else                  -> NetPayTextSecondary
    }
    val balanceLabel = when {
        friend.netBalance > 0 -> "${friend.name.split(" ").first()} owes you"
        friend.netBalance < 0 -> "You owe ${friend.name.split(" ").first()}"
        else                  -> "All settled up!"
    }

    Box(modifier = Modifier.fillMaxSize().background(NetPayBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // ── Hero header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(NetPayBlueDark, NetPayBlue))
                        )
                        .padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 40.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        // Back btn
                        Row(Modifier.fillMaxWidth()) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        }

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(friend.initials, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(14.dp))
                        Text(friend.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(balanceLabel, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Spacer(Modifier.height(10.dp))

                        // Big balance number
                        Text(
                            text = formatAmount(Math.abs(friend.netBalance)),
                            fontSize = 52.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                friend.netBalance > 0 -> Color(0xFF6EE7B7)  // light green on dark bg
                                friend.netBalance < 0 -> Color(0xFFFCA5A5)  // light red on dark bg
                                else                  -> Color.White
                            }
                        )
                    }
                }
            }

            // ── Action buttons
            item {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionButton(
                        label = "They owe me",
                        icon = Icons.Filled.ArrowDownward,
                        containerColor = NetPayGreenLight,
                        contentColor = NetPayGreen,
                        modifier = Modifier.weight(1f)
                    ) { onAddTransaction() }

                    ActionButton(
                        label = "I owe them",
                        icon = Icons.Filled.ArrowUpward,
                        containerColor = NetPayRedLight,
                        contentColor = NetPayRed,
                        modifier = Modifier.weight(1f)
                    ) { onAddTransaction() }

                    ActionButton(
                        label = "Settle up",
                        icon = Icons.Filled.CheckCircle,
                        containerColor = NetPayBlueSurface,
                        contentColor = NetPayBlue,
                        modifier = Modifier.weight(1f)
                    ) { onSettleUp() }
                }
                Spacer(Modifier.height(24.dp))
            }

            // ── Transaction history header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NetPayTextPrimary)
                    Text("${transactions.size} entries", fontSize = 13.sp, color = NetPayTextSecondary)
                }
                Spacer(Modifier.height(10.dp))
            }

            // ── Transactions
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.ReceiptLong, null, tint = NetPayTextHint, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No transactions yet", color = NetPayTextSecondary, fontSize = 15.sp)
                        }
                    }
                }
            } else {
                items(transactions) { tx ->
                    TransactionRow(tx)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // ── Bottom "Add Transaction" persistent button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, NetPayBackground, NetPayBackground))
                )
                .padding(16.dp)
        ) {
            Button(
                onClick = onAddTransaction,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NetPayBlue),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Transaction", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ActionButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(contentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
fun TransactionRow(tx: Transaction) {
    val color  = if (!tx.iOwedThem) NetPayGreen else NetPayRed
    val bg     = if (!tx.iOwedThem) NetPayGreenLight else NetPayRedLight
    val sign   = if (!tx.iOwedThem) "+" else "-"
    val iconVec = if (!tx.iOwedThem) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NetPaySurface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconVec, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.description, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = NetPayTextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(tx.date, fontSize = 12.sp, color = NetPayTextSecondary)
            }
            Text(
                "$sign₹${tx.amount.toInt()}",
                color = color,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  SCREEN 3 — ADD TRANSACTION
// ═══════════════════════════════════════════════════════════

@Composable
fun AddTransactionScreen(
    friends: List<Friend>,
    onBack: () -> Unit,
    onSubmit: (friendId: String, amount: Double, iOweThem: Boolean, note: String) -> Unit
) {
    var amountText     by remember { mutableStateOf("") }
    var iOweThem       by remember { mutableStateOf(false) }
    var note           by remember { mutableStateOf("") }
    var selectedFriend by remember { mutableStateOf<Friend?>(null) }
    var friendDropdown by remember { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val canSubmit = selectedFriend != null && amount > 0

    Box(
        modifier = Modifier.fillMaxSize().background(NetPayBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(NetPayBlueDark, NetPayBlue)))
                    .padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 30.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("New Transaction", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(20.dp))

                    // ── Big amount input
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Enter amount", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "₹",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                            BasicTextField(
                                value = amountText,
                                onValueChange = { v -> if (v.all { it.isDigit() || it == '.' }) amountText = v },
                                textStyle = TextStyle(
                                    fontSize = 52.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    textAlign = TextAlign.Start
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                decorationBox = { inner ->
                                    if (amountText.isEmpty()) {
                                        Text("0", color = Color.White.copy(alpha = 0.3f), fontSize = 52.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                    inner()
                                }
                            )
                        }
                    }
                }
            }

            // ── Form body
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Toggle: They owe me / I owe them
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = NetPaySurface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ToggleChip(
                                label = "They owe me",
                                icon = Icons.Filled.ArrowDownward,
                                selected = !iOweThem,
                                selectedColor = NetPayGreen,
                                modifier = Modifier.weight(1f)
                            ) { iOweThem = false }

                            ToggleChip(
                                label = "I owe them",
                                icon = Icons.Filled.ArrowUpward,
                                selected = iOweThem,
                                selectedColor = NetPayRed,
                                modifier = Modifier.weight(1f)
                            ) { iOweThem = true }
                        }
                    }
                }

                // ── Friend picker
                item {
                    FormLabel("With friend")
                    Spacer(Modifier.height(6.dp))
                    Box {
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { friendDropdown = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = NetPaySurface),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (selectedFriend != null) {
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(selectedFriend!!.avatarColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(selectedFriend!!.initials, color = selectedFriend!!.avatarColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(selectedFriend!!.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = NetPayTextPrimary)
                                } else {
                                    Icon(Icons.Filled.PersonSearch, null, tint = NetPayTextHint, modifier = Modifier.size(22.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text("Select a friend", color = NetPayTextHint, fontSize = 15.sp)
                                }
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Filled.ExpandMore, null, tint = NetPayTextSecondary)
                            }
                        }
                        DropdownMenu(
                            expanded = friendDropdown,
                            onDismissRequest = { friendDropdown = false }
                        ) {
                            friends.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f.name) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier.size(30.dp).clip(CircleShape).background(f.avatarColor.copy(0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) { Text(f.initials, color = f.avatarColor, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                    },
                                    onClick = { selectedFriend = f; friendDropdown = false }
                                )
                            }
                        }
                    }
                }

                // ── Note field
                item {
                    FormLabel("Note (optional)")
                    Spacer(Modifier.height(6.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NetPaySurface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Filled.Edit, null, tint = NetPayTextHint, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            BasicTextField(
                                value = note,
                                onValueChange = { note = it },
                                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 60.dp),
                                textStyle = TextStyle(fontSize = 15.sp, color = NetPayTextPrimary),
                                decorationBox = { inner ->
                                    if (note.isEmpty()) Text("e.g. Dinner, Petrol, Movie…", color = NetPayTextHint, fontSize = 15.sp)
                                    inner()
                                }
                            )
                        }
                    }
                }

                // ── Preview chip
                item {
                    if (selectedFriend != null && amount > 0) {
                        val previewText = if (!iOweThem)
                            "${selectedFriend!!.name.split(" ").first()} owes you ₹${amount.toInt()}"
                        else
                            "You owe ${selectedFriend!!.name.split(" ").first()} ₹${amount.toInt()}"
                        val previewColor = if (!iOweThem) NetPayGreen else NetPayRed
                        val previewBg    = if (!iOweThem) NetPayGreenLight else NetPayRedLight

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(previewBg)
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (!iOweThem) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = previewColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(previewText, color = previewColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // ── Submit button
                item {
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = {
                            if (canSubmit) onSubmit(selectedFriend!!.id, amount, iOweThem, note)
                        },
                        enabled = canSubmit,
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NetPayBlue,
                            disabledContainerColor = NetPayBlueSurface,
                            disabledContentColor = NetPayBlue.copy(alpha = 0.4f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(if (canSubmit) 8.dp else 0.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Record Transaction", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) selectedColor.copy(alpha = 0.12f) else Color.Transparent
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) selectedColor else NetPayTextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            color = if (selected) selectedColor else NetPayTextSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun FormLabel(text: String) {
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NetPayTextSecondary)
}

// ─────────────── Helpers ───────────────

fun formatAmount(value: Double): String {
    return if (value == 0.0) "₹0"
    else "₹${String.format("%,.0f", value)}"
}