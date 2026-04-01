package com.example.netpay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpay.data.model.FriendBalanceItem
import com.example.netpay.ui.theme.*

@Composable
fun AddTransactionScreen(
    friends: List<FriendBalanceItem>,
    onBack: () -> Unit,
    onSubmit: (friendId: String, amount: Double, iOweThem: Boolean, note: String) -> Unit
) {
    var amountText     by remember { mutableStateOf("") }
    var selectedFriend by remember { mutableStateOf<FriendBalanceItem?>(null) }
    var friendDropdown by remember { mutableStateOf(false) }

    val totalAmount = amountText.toDoubleOrNull() ?: 0.0

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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, null, tint = PureWhite, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Text("NetPay", color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Filled.Search, null, tint = MutedWhite)
                    }
                }
            }

            // Header
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                    Text(
                        "Split Bill",
                        color = PureWhite,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "MULTI-FRIEND TRANSACTION",
                        color = MutedWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Add Friends Section
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        "ADD FRIENDS",
                        color = MintGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    // Selector Box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clickable { friendDropdown = true },
                        shape = RoundedCornerShape(12.dp),
                        color = SoftBlack
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.GroupAdd, null, tint = MintGreen)
                            Spacer(Modifier.width(16.dp))
                            Text(
                                if (selectedFriend != null) selectedFriend!!.friendName else "Select people from contacts...",
                                color = if (selectedFriend != null) PureWhite else TextLow,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Filled.ExpandMore, null, tint = MutedWhite)
                        }
                        
                        DropdownMenu(
                            expanded = friendDropdown,
                            onDismissRequest = { friendDropdown = false },
                            modifier = Modifier.background(DarkGray)
                        ) {
                            friends.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f.friendName, color = PureWhite) },
                                    onClick = { selectedFriend = f; friendDropdown = false }
                                )
                            }
                        }
                    }
                    
                    if (selectedFriend != null) {
                        Spacer(Modifier.height(16.dp))
                        Row {
                            FriendTag(name = selectedFriend!!.friendName) { selectedFriend = null }
                        }
                    }
                }
            }

            // Amounts Section
            item {
                Spacer(Modifier.height(40.dp))
                Text(
                    "AMOUNTS",
                    color = MutedWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            if (selectedFriend != null) {
                item {
                    SplitAmountCard(name = selectedFriend!!.friendName, amountText = amountText) {
                        amountText = it
                    }
                }
            } else {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("Add a friend to set the amount.", color = TextLow)
                    }
                }
            }

            // Summary Card
            item {
                Spacer(Modifier.height(40.dp))
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
                            "TOTAL AMOUNT REQUESTED",
                            color = MutedWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "₹",
                                color = MintGreen,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                amountText.ifEmpty { "0.00" },
                                color = PureWhite,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Bottom Action Item (Scrolled)
            item {
                Spacer(Modifier.height(48.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { if (selectedFriend != null && totalAmount > 0) onSubmit(selectedFriend!!.friendId, totalAmount, false, "") },
                        enabled = selectedFriend != null && totalAmount > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintGreen,
                            contentColor = DeepBlack,
                            disabledContainerColor = DarkGray
                        )
                    ) {
                        Text("CONFIRM TRANSACTION", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Spacer(Modifier.width(16.dp))
                        Icon(Icons.Filled.Send, null)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "FUNDS WILL BE REQUESTED IMMEDIATELY",
                        color = TextLow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun FriendTag(name: String, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SoftBlack,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(DarkGray), contentAlignment = Alignment.Center) {
                Text(name.take(1).uppercase(), color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Text(name, color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Filled.Close,
                null,
                tint = MutedWhite,
                modifier = Modifier.size(16.dp).clickable { onRemove() }
            )
        }
    }
}

@Composable
fun SplitAmountCard(name: String, amountText: String, onAmountChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(88.dp),
        shape = RoundedCornerShape(12.dp),
        color = SoftBlack
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vertical bar
            Box(Modifier.width(4.dp).height(32.dp).clip(RoundedCornerShape(2.dp)).background(MintGreen))
            Spacer(Modifier.width(16.dp))
            
            // Avatar
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(DarkGray), contentAlignment = Alignment.Center) {
                Text(name.take(1).uppercase(), color = MutedWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(16.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("THEY OWE ME", color = MutedWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            
            // Amount Input
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("₹", color = MutedWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) onAmountChange(it) },
                    textStyle = TextStyle(
                        color = PureWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.End
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(100.dp),
                    decorationBox = { inner ->
                        if (amountText.isEmpty()) Text("0.00", color = TextLow, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End)
                        inner()
                    }
                )
            }
        }
    }
}
