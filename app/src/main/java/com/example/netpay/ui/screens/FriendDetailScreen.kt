package com.example.netpay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpay.data.model.FriendBalanceItem
import com.example.netpay.data.model.Transaction
import com.example.netpay.ui.components.formatAmount
import com.example.netpay.ui.theme.*

@Composable
fun FriendDetailScreen(
    friend: FriendBalanceItem,
    transactions: List<Transaction>,
    onBack: () -> Unit,
    onSettleUp: () -> Unit,
    onRemoveFriend: () -> Unit,
    onAddTransaction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, null, tint = PureWhite)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("NetPay", color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { /* Search */ }) {
                            Icon(Icons.Filled.Search, null, tint = MutedWhite)
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SoftBlack),
                            contentAlignment = Alignment.Center
                        ) {
                             Icon(Icons.Filled.Person, null, tint = MutedWhite, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Profile Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Large Avatar
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SoftBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            friend.friendName.take(1).uppercase(),
                            color = MutedWhite,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "TRANSACTION WITH",
                        color = MutedWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        friend.friendName,
                        color = PureWhite,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Balance Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = SoftBlack
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Total Balance",
                            color = MutedWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            formatAmount(kotlin.math.abs(friend.netBalance)),
                            color = if (friend.netBalance >= 0) MintGreen else SalmonRed,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Black
                        )
                        
                        if (friend.netBalance != 0.0) {
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = onSettleUp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MintGreen,
                                    contentColor = DeepBlack
                                )
                            ) {
                                Text("Settle Up", fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }

            // Recent Activity
            item {
                Spacer(Modifier.height(48.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "RECENT ACTIVITY",
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val dateLabel = remember(transactions) {
                        transactions.maxOfOrNull { it.timestamp }?.let {
                            java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(it).uppercase()
                        } ?: "ALL TIME"
                    }
                    Text(
                        dateLabel,
                        color = MutedWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            if (transactions.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No recent activity.", color = TextLow)
                    }
                }
            } else {
                items(transactions) { tx ->
                    DetailTransactionItem(tx)
                }
            }
            
            // Delete Friend option
            item {
                Spacer(Modifier.height(40.dp))
                TextButton(
                    onClick = onRemoveFriend,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                ) {
                    Text("Delete Friend from Ledger", color = SalmonRed, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        // Add Transaction FAB
        FloatingActionButton(
            onClick = onAddTransaction,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MintGreen,
            contentColor = DeepBlack,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Add, null)
        }
    }
}

@Composable
fun DetailTransactionItem(tx: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical indicator
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (tx.amount >= 0) MintGreen else SalmonRed)
        )
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (tx.description.isNotBlank()) tx.description else "General Expense",
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(tx.timestamp) + " • " + (if (tx.amount >= 0) "They paid" else "You paid"),
                color = MutedWhite,
                fontSize = 13.sp
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                formatAmount(tx.amount),
                color = if (tx.amount >= 0) MintGreen else SalmonRed,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "SETTLED", // Placeholder status
                color = TextLow,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
