package com.example.netpay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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

private const val MAX_AMOUNT_DIGITS = 5

data class TransactionPayload(val friendId: String, val amount: Double, val iOweThem: Boolean, val note: String)

@Composable
fun AddTransactionScreen(
    friends: List<FriendBalanceItem>,
    isSubmitting: Boolean = false,
    onBack: () -> Unit,
    onSubmit: (List<TransactionPayload>) -> Unit
) {
    val selectedFriends = remember { mutableStateListOf<FriendBalanceItem>() }
    var friendDropdown  by remember { mutableStateOf(false) }
    val amountsMap      = remember { mutableStateMapOf<String, String>() }
    val oweMap          = remember { mutableStateMapOf<String, Boolean>() }

    val totalAmount = amountsMap.values.sumOf { it.toDoubleOrNull() ?: 0.0 }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            

            // Header
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                    Text(
                        "New Expense",
                        color = PureWhite,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "PERSON-TO-PERSON TRANSACTION",
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
                        "SELECT FRIEND",
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
                                if (selectedFriends.isNotEmpty()) "${selectedFriends.size} selected" else "Select people from contacts...",
                                color = if (selectedFriends.isNotEmpty()) PureWhite else TextLow,
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
                                    onClick = {  
                                        if (!selectedFriends.any { it.friendId == f.friendId }) selectedFriends.add(f)
                                        friendDropdown = false 
                                    }
                                )
                            }
                        }
                    }
                    
                    if (selectedFriends.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                            selectedFriends.forEach { f ->
                                FriendTag(name = f.friendName) { 
                                    selectedFriends.remove(f)
                                    amountsMap.remove(f.friendId)
                                    oweMap.remove(f.friendId)
                                }
                                Spacer(Modifier.width(8.dp))
                            }
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

            if (selectedFriends.isNotEmpty()) {
                items(selectedFriends) { friend ->
                    val amount = amountsMap[friend.friendId] ?: ""
                    val iOweThem = oweMap[friend.friendId] ?: false
                    
                    SplitAmountCard(
                        name = friend.friendName, 
                        amountText = amount,
                        iOweThem = iOweThem,
                        onToggleIOweThem = { oweMap[friend.friendId] = !iOweThem }
                    ) { newValue ->
                        amountsMap[friend.friendId] = newValue
                    }
                    Spacer(Modifier.height(16.dp))
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
                                if (totalAmount > 0) String.format("%.2f", totalAmount) else "0.00",
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
                        onClick = { 
                            if (selectedFriends.isNotEmpty() && totalAmount > 0) {
                                val payloads = selectedFriends.mapNotNull { f ->
                                    val amt = (amountsMap[f.friendId] ?: "").toDoubleOrNull() ?: 0.0
                                    if (amt > 0) {
                                        TransactionPayload(f.friendId, amt, oweMap[f.friendId] ?: false, "")
                                    } else null
                                }
                                if (payloads.isNotEmpty()) {
                                    onSubmit(payloads)
                                }
                            }
                        },
                        enabled = !isSubmitting && selectedFriends.isNotEmpty() && totalAmount > 0,
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
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = DeepBlack
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("SAVING...", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        } else {
                            Text("CONFIRM TRANSACTION", fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Spacer(Modifier.width(16.dp))
                            Icon(Icons.Filled.Send, null)
                        }
                    }
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
fun SplitAmountCard(
    name: String, 
    amountText: String, 
    iOweThem: Boolean,
    onToggleIOweThem: () -> Unit,
    onAmountChange: (String) -> Unit
) {
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
            
            // Info (Click to toggle owe direction)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleIOweThem() }
                    .padding(vertical = 8.dp) // Provide slightly larger tap area
            ) {
                Text(name, color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (iOweThem) "I OWE THEM (TAP)" else "THEY OWE ME (TAP)", 
                    color = if (iOweThem) SalmonRed else MintGreen, 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Amount Input
            Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                Text("₹", color = MutedWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (isValidAmountInput(input)) {
                            onAmountChange(input)
                        }
                    },
                    textStyle = TextStyle(
                        color = PureWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.End
                    ),
                    placeholder = { 
                        Text("0.00", color = TextLow, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End) 
                    },
                    modifier = Modifier.width(150.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
        }
    }
}

private fun isValidAmountInput(input: String): Boolean {
    if (input.isEmpty()) return true
    if (input.count { it == '.' } > 1) return false
    if (input.any { !it.isDigit() && it != '.' }) return false

    val parts = input.split('.', limit = 2)
    val wholePart = parts[0]
    val fractionalPart = parts.getOrNull(1).orEmpty()

    if (wholePart.length > MAX_AMOUNT_DIGITS) return false
    if (fractionalPart.length > 2) return false

    return true
}
