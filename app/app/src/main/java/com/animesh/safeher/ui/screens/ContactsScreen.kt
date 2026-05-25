package com.animesh.safeher.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.animesh.safeher.data.models.EmergencyContact
import com.animesh.safeher.ui.theme.BackgroundDark
import com.animesh.safeher.ui.theme.CardBg
import com.animesh.safeher.ui.theme.DangerRed
import com.animesh.safeher.ui.theme.OnSurface
import com.animesh.safeher.ui.theme.PinkLight
import com.animesh.safeher.ui.theme.PinkPrimary
import com.animesh.safeher.ui.theme.PurpleAccent
import com.animesh.safeher.ui.theme.SurfaceDark
import com.animesh.safeher.viewmodel.MainViewModel

@Composable
fun ContactsScreen(viewModel: MainViewModel) {

    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundDark,
                        Color(0xFF1A0030)
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // Header
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Emergency Contacts",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurface
                )

                Text(
                    text = "These contacts will be alerted via SMS when you trigger SOS",
                    fontSize = 13.sp,
                    color = PinkLight,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Empty State
            if (uiState.contacts.isEmpty()) {

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = PinkPrimary.copy(alpha = 0.4f),
                            modifier = Modifier.size(72.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No contacts added yet",
                            fontSize = 16.sp,
                            color = OnSurface.copy(alpha = 0.5f)
                        )

                        Text(
                            text = "Add contacts who should be alerted in emergency",
                            fontSize = 13.sp,
                            color = OnSurface.copy(alpha = 0.35f)
                        )
                    }
                }

            } else {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    items(
                        items = uiState.contacts,
                        key = { it.id }
                    ) { contact ->

                        ContactCard(
                            contact = contact,
                            onDelete = {
                                viewModel.deleteContact(contact.id)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = {
                showAddDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = PinkPrimary,
            shape = CircleShape
        ) {

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White
            )
        }
    }

    // Add Dialog
    if (showAddDialog) {

        AddContactDialog(
            onDismiss = {
                showAddDialog = false
            },
            onAdd = { name, phone, relation ->

                viewModel.addContact(
                    name = name,
                    phone = phone,
                    relation = relation
                )

                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ContactCard(
    contact: EmergencyContact,
    onDelete: () -> Unit
) {

    var showDeleteConfirm by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBg
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PinkPrimary,
                                PurpleAccent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = contact.name.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = contact.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface
                )

                Text(
                    text = contact.phone,
                    fontSize = 13.sp,
                    color = PinkLight
                )

                if (contact.relation.isNotBlank()) {

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PinkPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {

                        Text(
                            text = contact.relation,
                            fontSize = 11.sp,
                            color = PinkLight,
                            modifier = Modifier.padding(
                                horizontal = 6.dp,
                                vertical = 2.dp
                            )
                        )
                    }
                }
            }

            // Delete
            IconButton(
                onClick = {
                    showDeleteConfirm = true
                }
            ) {

                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = DangerRed.copy(alpha = 0.7f)
                )
            }
        }
    }

    // Confirm Dialog
    if (showDeleteConfirm) {

        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
            },
            title = {
                Text("Remove Contact?")
            },
            text = {
                Text("Remove ${contact.name} from emergency contacts?")
            },
            confirmButton = {

                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {

                    Text(
                        text = "Remove",
                        color = DangerRed
                    )
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                    }
                ) {

                    Text("Cancel")
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun AddContactDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }

    val relations = listOf(
        "Mother",
        "Father",
        "Sister",
        "Brother",
        "Friend",
        "Husband",
        "Other"
    )

    var selectedRelation by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,

        title = {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = PinkPrimary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Add Emergency Contact",
                    color = OnSurface
                )
            }
        },

        text = {

            Column {

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = {
                        Text("Full Name")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = dialogFieldColors(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                    },
                    label = {
                        Text("Phone (+91XXXXXXXXXX)")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    colors = dialogFieldColors(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Relation",
                    fontSize = 12.sp,
                    color = OnSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column {

                    relations.chunked(3).forEach { row ->

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {

                            row.forEach { rel ->

                                FilterChip(
                                    selected = selectedRelation == rel,

                                    onClick = {
                                        selectedRelation = rel
                                        relation = rel
                                    },

                                    label = {
                                        Text(
                                            text = rel,
                                            fontSize = 11.sp
                                        )
                                    },

                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PinkPrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = CardBg,
                                        labelColor = OnSurface
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        },

        confirmButton = {

            Button(
                onClick = {
                    onAdd(
                        name.trim(),
                        phone.trim(),
                        relation
                    )
                },

                enabled = name.isNotBlank() &&
                        phone.isNotBlank(),

                colors = ButtonDefaults.buttonColors(
                    containerColor = PinkPrimary
                ),

                shape = RoundedCornerShape(10.dp)
            ) {

                Text("Add Contact")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    text = "Cancel",
                    color = OnSurface.copy(alpha = 0.6f)
                )
            }
        }
    )
}

@Composable
private fun dialogFieldColors(): TextFieldColors {

    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PinkPrimary,
        unfocusedBorderColor = OnSurface.copy(alpha = 0.3f),
        focusedLabelColor = PinkPrimary,
        cursorColor = PinkPrimary,
        focusedTextColor = OnSurface,
        unfocusedTextColor = OnSurface,
        focusedContainerColor = CardBg,
        unfocusedContainerColor = CardBg
    )
}