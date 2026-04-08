package com.example.netpay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpay.data.model.FriendBalanceItem
import com.example.netpay.ui.theme.BorderGray
import com.example.netpay.ui.theme.DarkGray
import com.example.netpay.ui.theme.DeepBlack
import com.example.netpay.ui.theme.MintGreen
import com.example.netpay.ui.theme.MutedWhite
import com.example.netpay.ui.theme.PureWhite
import com.example.netpay.ui.theme.SalmonRed
import com.example.netpay.ui.theme.SoftBlack
import com.example.netpay.ui.theme.TextLow
import kotlin.math.round

private const val MAX_AMOUNT_DIGITS = 5

data class TransactionPayload(val friendId: String, val amount: Double, val iOweThem: Boolean, val note: String)

private enum class SplitMode {
    Manual,
    Equal
}

@Composable
fun AddTransactionScreen(
    friends: List<FriendBalanceItem>,
    isSubmitting: Boolean = false,
    onBack: () -> Unit,
    onSubmit: (List<TransactionPayload>) -> Unit
) {
    val selectedFriends = remember { mutableStateListOf<FriendBalanceItem>() }
    var friendDropdown by remember { mutableStateOf(false) }
    val amountsMap = remember { mutableStateMapOf<String, String>() }
    val oweMap = remember { mutableStateMapOf<String, Boolean>() }
    var splitMode by remember { mutableStateOf(SplitMode.Manual) }
    var equalTotalAmount by remember { mutableStateOf("") }
    var equalIOweThem by remember { mutableStateOf(false) }

    val manualTotalAmount = amountsMap.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val equalTotalValue = equalTotalAmount.toDoubleOrNull() ?: 0.0
    val equalSplitAmounts = remember(selectedFriends.toList(), equalTotalValue) {
        distributeEqualAmounts(equalTotalValue, selectedFriends.size)
    }
    val totalAmount = if (splitMode == SplitMode.Equal) equalTotalValue else manualTotalAmount

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
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
                            friends.forEach { friend ->
                                DropdownMenuItem(
                                    text = { Text(friend.friendName, color = PureWhite) },
                                    onClick = {
                                        if (!selectedFriends.any { it.friendId == friend.friendId }) {
                                            selectedFriends.add(friend)
                                        }
                                        friendDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    if (selectedFriends.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            selectedFriends.forEach { friend ->
                                FriendTag(name = friend.friendName) {
                                    selectedFriends.remove(friend)
                                    amountsMap.remove(friend.friendId)
                                    oweMap.remove(friend.friendId)
                                }
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(32.dp))
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        "SPLIT MODE",
                        color = MutedWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SplitModeButton(
                            label = "Manual",
                            selected = splitMode == SplitMode.Manual,
                            onClick = { splitMode = SplitMode.Manual },
                            modifier = Modifier.weight(1f)
                        )
                        SplitModeButton(
                            label = "Split Equally",
                            selected = splitMode == SplitMode.Equal,
                            onClick = { splitMode = SplitMode.Equal },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(40.dp))
                Text(
                    if (splitMode == SplitMode.Equal) "EQUAL SPLIT" else "AMOUNTS",
                    color = MutedWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            if (selectedFriends.isNotEmpty() && splitMode == SplitMode.Equal) {
                item {
                    EqualSplitCard(
                        totalAmount = equalTotalAmount,
                        iOweThem = equalIOweThem,
                        friendCount = selectedFriends.size,
                        onToggleIOweThem = { equalIOweThem = !equalIOweThem },
                        onAmountChange = { input ->
                            if (isValidAmountInput(input)) {
                                equalTotalAmount = input
                            }
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                }

                items(selectedFriends.zip(equalSplitAmounts)) { (friend, amount) ->
                    EqualSplitPreviewCard(
                        name = friend.friendName,
                        amount = amount,
                        iOweThem = equalIOweThem
                    )
                    Spacer(Modifier.height(16.dp))
                }
            } else if (selectedFriends.isNotEmpty()) {
                items(selectedFriends) { friend ->
                    val amount = amountsMap[friend.friendId] ?: ""
                    val iOweThem = oweMap[friend.friendId] ?: false

                    SplitAmountCard(
                        name = friend.friendName,
                        amountText = amount,
                        iOweThem = iOweThem,
                        onToggleIOweThem = { oweMap[friend.friendId] = !iOweThem },
                        onAmountChange = { newValue -> amountsMap[friend.friendId] = newValue }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Add a friend to set the amount.", color = TextLow)
                    }
                }
            }

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
                                if (totalAmount > 0) formatAmount(totalAmount) else "0.00",
                                color = PureWhite,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

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
                                val payloads = if (splitMode == SplitMode.Equal) {
                                    selectedFriends.zip(equalSplitAmounts).mapNotNull { (friend, amount) ->
                                        if (amount > 0) {
                                            TransactionPayload(friend.friendId, amount, equalIOweThem, "")
                                        } else {
                                            null
                                        }
                                    }
                                } else {
                                    selectedFriends.mapNotNull { friend ->
                                        val amount = (amountsMap[friend.friendId] ?: "").toDoubleOrNull() ?: 0.0
                                        if (amount > 0) {
                                            TransactionPayload(friend.friendId, amount, oweMap[friend.friendId] ?: false, "")
                                        } else {
                                            null
                                        }
                                    }
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
private fun SplitModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MintGreen else SoftBlack,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MintGreen else BorderGray
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (selected) DeepBlack else PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
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
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text(name.take(1).uppercase(), color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Text(name, color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Filled.Close,
                null,
                tint = MutedWhite,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() }
            )
        }
    }
}

@Composable
private fun EqualSplitCard(
    totalAmount: String,
    iOweThem: Boolean,
    friendCount: Int,
    onToggleIOweThem: () -> Unit,
    onAmountChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        color = SoftBlack
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$friendCount selected",
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                AssistChip(
                    onClick = onToggleIOweThem,
                    label = { Text(if (iOweThem) "I owe them" else "They owe me") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (iOweThem) SalmonRed.copy(alpha = 0.15f) else MintGreen.copy(alpha = 0.15f),
                        labelColor = if (iOweThem) SalmonRed else MintGreen
                    )
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "TOTAL AMOUNT",
                color = MutedWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = totalAmount,
                onValueChange = onAmountChange,
                placeholder = {
                    Text("0.00", color = TextLow, fontSize = 22.sp, fontWeight = FontWeight.Black)
                },
                prefix = {
                    Text("₹", color = MutedWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                },
                textStyle = TextStyle(
                    color = PureWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DeepBlack,
                    unfocusedContainerColor = DeepBlack,
                    focusedBorderColor = MintGreen,
                    unfocusedBorderColor = BorderGray,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                )
            )
        }
    }
}

@Composable
private fun EqualSplitPreviewCard(
    name: String,
    amount: Double,
    iOweThem: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        color = SoftBlack
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MintGreen)
            )
            Spacer(Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text(name.take(1).uppercase(), color = MutedWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (iOweThem) "I OWE THEM" else "THEY OWE ME",
                    color = if (iOweThem) SalmonRed else MintGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "₹ ${formatAmount(amount)}",
                color = PureWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
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
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MintGreen)
            )
            Spacer(Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text(name.take(1).uppercase(), color = MutedWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleIOweThem() }
                    .padding(vertical = 8.dp)
            ) {
                Text(name, color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (iOweThem) "I OWE THEM (TAP)" else "THEY OWE ME (TAP)",
                    color = if (iOweThem) SalmonRed else MintGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

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
                        Text(
                            "0.00",
                            color = TextLow,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.End
                        )
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

private fun distributeEqualAmounts(totalAmount: Double, peopleCount: Int): List<Double> {
    if (peopleCount <= 0) return emptyList()
    if (totalAmount <= 0.0) return List(peopleCount) { 0.0 }

    val totalCents = round(totalAmount * 100).toInt()
    val baseShare = totalCents / peopleCount
    val remainder = totalCents % peopleCount

    return List(peopleCount) { index ->
        (baseShare + if (index < remainder) 1 else 0) / 100.0
    }
}

private fun formatAmount(amount: Double): String = String.format("%.2f", amount)

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
