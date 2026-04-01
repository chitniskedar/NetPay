package com.example.netpay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpay.data.model.FriendBalanceItem
import com.example.netpay.ui.components.formatAmount
import com.example.netpay.ui.theme.*

@Composable
fun HomeScreen(
    userName: String,
    friends: List<FriendBalanceItem>,
    totalBalance: Double,
    onFriendClick: (FriendBalanceItem) -> Unit,
    onAddFriend: () -> Unit,
    onAddTransaction: () -> Unit,
    onLogout: () -> Unit,
    onResetProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Custom Top Bar logic
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
                            Icon(Icons.Filled.Person, "Profile", tint = PureWhite, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            "NetPay",
                            color = PureWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Filled.Search, "Search", tint = MutedWhite)
                    }
                }
            }

            // Ledger Overview Header
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                    Text(
                        "LEDGER OVERVIEW",
                        color = MutedWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Decorative Search box
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black // Solid black for the search box as in ref
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.FilterList, null, tint = MutedWhite, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("Search transactions", color = TextLow, fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }

            // Today Section
            item {
                SectionHeader("TODAY")
            }

            if (friends.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No recent activity.", color = TextLow)
                    }
                }
            } else {
                items(friends) { friend ->
                    ActivityItem(friend = friend, onClick = { onFriendClick(friend) })
                }
            }

            // Yesterday Section (Placeholder logic)
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader("YESTERDAY")
            }
            
            // Just for demo, showing same list or empty
            item {
                Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text("No activities yesterday.", color = TextLow, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = MutedWhite,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    )
}

@Composable
fun ActivityItem(friend: FriendBalanceItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical indicator bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (friend.netBalance >= 0) MintGreen else SalmonRed)
        )
        Spacer(Modifier.width(16.dp))
        
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SoftBlack),
            contentAlignment = Alignment.Center
        ) {
             Text(
                friend.friendName.take(1).uppercase(),
                color = MutedWhite, // Low contrast text for avatar placeholder
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(Modifier.width(16.dp))
        
        // Name & Description
        Column(modifier = Modifier.weight(1f)) {
            Text(
                friend.friendName,
                color = PureWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (friend.netBalance >= 0) "They owe you" else "You owe them",
                color = MutedWhite,
                fontSize = 13.sp
            )
        }
        
        // Amount & Time
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = (if (friend.netBalance >= 0) "+" else "-") + formatAmount(kotlin.math.abs(friend.netBalance)),
                color = if (friend.netBalance >= 0) MintGreen else SalmonRed,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "11:24 AM",
                color = TextLow,
                fontSize = 11.sp
            )
        }
    }
}
