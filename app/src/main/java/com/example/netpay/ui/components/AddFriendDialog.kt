package com.example.netpay.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpay.ui.theme.*

@Composable
fun AddFriendDialog(isSearching: Boolean, onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Friend", fontWeight = FontWeight.Black, color = PureWhite) },
        text = {
            Column {
                Text("Enter the name of the person you want to keep track of.", fontSize = 14.sp, color = MutedWhite)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Friend's Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSearching,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = DeepBlack,
                        focusedContainerColor = DeepBlack,
                        unfocusedBorderColor = BorderGray,
                        focusedBorderColor = MintGreen,
                        unfocusedTextColor = PureWhite,
                        focusedTextColor = PureWhite,
                        unfocusedLabelColor = MutedWhite,
                        focusedLabelColor = MintGreen
                    ),
                    singleLine = true
                )
                if (isSearching) {
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MintGreen, trackColor = BorderGray)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name) }, 
                shape = RoundedCornerShape(10.dp),
                enabled = !isSearching && name.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintGreen,
                    contentColor = DeepBlack,
                    disabledContainerColor = DarkGray
                )
            ) {
                Text("Add Friend", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MutedWhite)
            }
        },
        containerColor = SoftBlack,
        titleContentColor = PureWhite,
        textContentColor = MutedWhite
    )
}
