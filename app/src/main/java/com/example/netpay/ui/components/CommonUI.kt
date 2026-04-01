package com.example.netpay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpay.data.model.FriendBalanceItem
import com.example.netpay.data.model.Transaction
import com.example.netpay.ui.theme.*

@Composable
fun FriendCard(friend: FriendBalanceItem, onClick: () -> Unit) {
    val balanceColor = when {
        friend.netBalance > 0 -> NeonMint
        friend.netBalance < 0 -> NeonError
        else                  -> TextLow
    }
    val balanceText = when {
        friend.netBalance > 0 -> "owes you ${formatAmount(friend.netBalance)}"
        friend.netBalance < 0 -> "you owe ${formatAmount(-friend.netBalance)}"
        else                  -> "all settled"
    }
    val initials = friend.friendName.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = SlateSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateOutline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NeonViolet.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = NeonViolet,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(friend.friendName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextHigh)
                Text(balanceText, fontSize = 12.sp, color = balanceColor)
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextLow,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TransactionRow(tx: Transaction) {
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    val iPaid = tx.fromId == uid
    
    val color = if (tx.amount > 0) NeonMint else NeonError
    val icon  = if (tx.amount > 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown
    
    val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
    val date = sdf.format(java.util.Date(tx.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (tx.description.isNotEmpty()) tx.description else "Expense",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextHigh
            )
            Text(date, fontSize = 11.sp, color = TextLow)
        }
        
        Text(
            formatAmount(kotlin.math.abs(tx.amount)),
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

@Composable
fun SummaryPill(label: String, amount: Double, isPositive: Boolean) {
    val color = if (isPositive) NeonMint else NeonError
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Column {
            Text(label, color = TextLow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(formatAmount(amount), color = color, fontSize = 14.sp, fontWeight = FontWeight.Black)
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
fun ToggleChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) selectedColor.copy(alpha = 0.15f) else Color.Transparent
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
            tint = if (selected) selectedColor else TextLow,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            color = if (selected) selectedColor else TextLow,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun FormLabel(text: String) {
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMed)
}

fun formatAmount(value: Double): String {
    return "₹${String.format("%,.0f", value)}"
}
