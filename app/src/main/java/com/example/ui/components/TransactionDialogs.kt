package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AccountEntity
import com.example.ui.theme.CashInGreen
import com.example.ui.theme.CashOutRed
import com.example.ui.theme.FeeGold
import com.example.ui.theme.SlateSecondary

// 1. CASH OUT DIALOG
// Customer sent digital money to Account -> Shop gives cash out of drawer
// User rule: "cash out ka aik button hu us me value negative dalo"
@Composable
fun CashOutDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        accountId: Long,
        amount: Double,
        fee: Double,
        customerName: String,
        customerPhone: String,
        note: String,
        referenceId: String
    ) -> Unit
) {
    val digitalAccounts = accounts.filter { !it.isCashDrawer }.ifEmpty { accounts }
    var selectedAccount by remember { mutableStateOf(digitalAccounts.firstOrNull()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    var amountText by remember { mutableStateOf("") }
    var feeText by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var referenceId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amountVal = amountText.toDoubleOrNull() ?: 0.0
    val feeVal = feeText.toDoubleOrNull() ?: 0.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CashOutRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = CashOutRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Cash Out (کیش ادائیگی)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Customer took Cash | Account Credited",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Account Selector
                Text(
                    text = "Select Account Receiving Digital Money:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedAccount?.name ?: "Select Account",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDropdownExpanded = true }
                            .testTag("cash_out_account_dropdown"),
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { isDropdownExpanded = true }
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        digitalAccounts.forEach { acc ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(acc.name, fontWeight = FontWeight.Bold)
                                        if (acc.accountNumber.isNotBlank()) {
                                            Text(acc.accountNumber, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                },
                                onClick = {
                                    selectedAccount = acc
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Amount (رقم)") },
                    placeholder = { Text("e.g. 5000") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cash_out_amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Dedicated Fees / Commission Column
                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text("Fee / Commission (فیس / منافع)") },
                    placeholder = { Text("e.g. 50") },
                    prefix = { Text("Rs. ") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = FeeGold,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cash_out_fee_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Live Preview of Impacts (User Requirement: negative cash in drawer)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Ledger Balance Impacts:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Physical Cash Drawer:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "-Rs. ${if (amountVal > 0) amountVal else 0.0} (Negative)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = CashOutRed
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${selectedAccount?.name ?: "Account"}:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "+Rs. ${if (amountVal > 0) amountVal else 0.0} (Positive)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = CashInGreen
                            )
                        }
                        if (feeVal > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Fee Profit Earned:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "+Rs. $feeVal",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = FeeGold
                                )
                            }
                        }
                    }
                }

                // Optional Customer Details
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name (گاہک کا نام) - Optional") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = referenceId,
                        onValueChange = { referenceId = it },
                        label = { Text("Trx ID / Ref") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (selectedAccount == null) {
                                errorMessage = "Please select an account"
                                return@Button
                            }
                            if (amountVal <= 0.0) {
                                errorMessage = "Please enter a valid amount"
                                return@Button
                            }
                            onConfirm(
                                selectedAccount!!.id,
                                amountVal,
                                feeVal,
                                customerName,
                                customerPhone,
                                note,
                                referenceId
                            )
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("confirm_cash_out_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CashOutRed)
                    ) {
                        Text("Save Cash Out (-)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 2. CASH IN DIALOG
// Customer gives physical cash to shop -> Shop sends money from one of the accounts
// User rule: "r agr acount me se kisi aik account se paise send hun tu cash out/in me positive value dalon"
@Composable
fun CashInDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        accountId: Long,
        amount: Double,
        fee: Double,
        customerName: String,
        customerPhone: String,
        note: String,
        referenceId: String
    ) -> Unit
) {
    val digitalAccounts = accounts.filter { !it.isCashDrawer }.ifEmpty { accounts }
    var selectedAccount by remember { mutableStateOf(digitalAccounts.firstOrNull()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    var amountText by remember { mutableStateOf("") }
    var feeText by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var referenceId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amountVal = amountText.toDoubleOrNull() ?: 0.0
    val feeVal = feeText.toDoubleOrNull() ?: 0.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CashInGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = CashInGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Cash In / Send (کیش وصولی)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Shop Received Cash | Sent From Account",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Account Selector
                Text(
                    text = "Select Account Sending Money to Customer:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedAccount?.name ?: "Select Account",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDropdownExpanded = true }
                            .testTag("cash_in_account_dropdown"),
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { isDropdownExpanded = true }
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        digitalAccounts.forEach { acc ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(acc.name, fontWeight = FontWeight.Bold)
                                        if (acc.accountNumber.isNotBlank()) {
                                            Text(acc.accountNumber, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                },
                                onClick = {
                                    selectedAccount = acc
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Amount (رقم)") },
                    placeholder = { Text("e.g. 3000") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cash_in_amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Dedicated Fees / Commission Column
                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text("Fee / Commission (فیس / کمیشن)") },
                    placeholder = { Text("e.g. 40") },
                    prefix = { Text("Rs. ") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = FeeGold,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cash_in_fee_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Live Preview of Impacts (User Requirement: positive cash in drawer)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Ledger Balance Impacts:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Physical Cash Drawer:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "+Rs. ${if (amountVal > 0) amountVal else 0.0} (Positive)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = CashInGreen
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${selectedAccount?.name ?: "Account"}:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "-Rs. ${if (amountVal > 0) amountVal else 0.0} (Deducted)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = CashOutRed
                            )
                        }
                        if (feeVal > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Fee Profit Earned:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "+Rs. $feeVal",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = FeeGold
                                )
                            }
                        }
                    }
                }

                // Optional Customer Details
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name / Receiver - Optional") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = referenceId,
                        onValueChange = { referenceId = it },
                        label = { Text("Trx ID / Ref") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (selectedAccount == null) {
                                errorMessage = "Please select an account"
                                return@Button
                            }
                            if (amountVal <= 0.0) {
                                errorMessage = "Please enter a valid amount"
                                return@Button
                            }
                            onConfirm(
                                selectedAccount!!.id,
                                amountVal,
                                feeVal,
                                customerName,
                                customerPhone,
                                note,
                                referenceId
                            )
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("confirm_cash_in_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CashInGreen)
                    ) {
                        Text("Save Cash In (+)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 3. INCOMING TRANSACTION DIALOG (Direct Account Deposit)
@Composable
fun IncomingDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        accountId: Long,
        amount: Double,
        fee: Double,
        customerName: String,
        note: String,
        referenceId: String
    ) -> Unit
) {
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var feeText by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var referenceId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Incoming Transaction (آمدنی)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedAccount?.name ?: "Select Account",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDropdownExpanded = true },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { isDropdownExpanded = true }
                            )
                        },
                        label = { Text("Account Receiving Money") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    selectedAccount = acc
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Incoming Amount") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text("Fee / Commission (Optional)") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Sender / Customer") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (selectedAccount == null || amt <= 0.0) {
                                errorMessage = "Please enter valid account and amount"
                                return@Button
                            }
                            onConfirm(
                                selectedAccount!!.id,
                                amt,
                                feeText.toDoubleOrNull() ?: 0.0,
                                customerName,
                                note,
                                referenceId
                            )
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Incoming (+)")
                    }
                }
            }
        }
    }
}

// 3.5 OUTGOING DIALOG (Direct Account Withdrawal / Expense)
@Composable
fun OutgoingDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        accountId: Long,
        amount: Double,
        fee: Double,
        note: String,
        referenceId: String
    ) -> Unit
) {
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var feeText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var referenceId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Outgoing Expense / Withdrawal (اخراجات)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedAccount?.name ?: "Select Account",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDropdownExpanded = true },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { isDropdownExpanded = true }
                            )
                        },
                        label = { Text("Account Debited") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    selectedAccount = acc
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Outgoing Amount") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text("Fee / Bank Charges (Optional)") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Reason / Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (selectedAccount == null || amt <= 0.0) {
                                errorMessage = "Please enter valid account and amount"
                                return@Button
                            }
                            onConfirm(
                                selectedAccount!!.id,
                                amt,
                                feeText.toDoubleOrNull() ?: 0.0,
                                note,
                                referenceId
                            )
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CashOutRed)
                    ) {
                        Text("Save Outgoing (-)")
                    }
                }
            }
        }
    }
}

// 4. TRANSFER DIALOG (Between accounts)
@Composable
fun TransferDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        fee: Double,
        note: String
    ) -> Unit
) {
    var fromAccount by remember { mutableStateOf(accounts.firstOrNull()) }
    var toAccount by remember { mutableStateOf(accounts.getOrNull(1) ?: accounts.firstOrNull()) }
    var isFromExpanded by remember { mutableStateOf(false) }
    var isToExpanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var feeText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Transfer Between Accounts",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                // From Account
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = fromAccount?.name ?: "From Account",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From Account") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isFromExpanded = true },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(expanded = isFromExpanded, onDismissRequest = { isFromExpanded = false }) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    fromAccount = acc
                                    isFromExpanded = false
                                }
                            )
                        }
                    }
                }

                // To Account
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = toAccount?.name ?: "To Account",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To Account") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isToExpanded = true },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(expanded = isToExpanded, onDismissRequest = { isToExpanded = false }) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    toAccount = acc
                                    isToExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Transfer Amount") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text("Transfer Fee / Charges (Optional)") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (fromAccount == null || toAccount == null || fromAccount!!.id == toAccount!!.id || amt <= 0.0) {
                                errorMessage = "Please select two different accounts and valid amount"
                                return@Button
                            }
                            onConfirm(
                                fromAccount!!.id,
                                toAccount!!.id,
                                amt,
                                feeText.toDoubleOrNull() ?: 0.0,
                                note
                            )
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SlateSecondary)
                    ) {
                        Text("Confirm Transfer")
                    }
                }
            }
        }
    }
}

// 5. DIRECT SHEET ROW ENTRY DIALOG (Direct Google Sheet format entry)
@Composable
fun SheetRowEntryDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        accountId: Long,
        type: String,
        cashImpact: Double,
        accountImpact: Double,
        amount: Double,
        fee: Double,
        customerName: String,
        note: String,
        referenceId: String
    ) -> Unit
) {
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()) }
    var isAccExpanded by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf("CASH_OUT") }
    var cashImpactText by remember { mutableStateOf("-") }
    var accountImpactText by remember { mutableStateOf("") }
    var feeText by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var referenceId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Direct Sheet Row Entry (شیٹ اندراج)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Select Account
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedAccount?.name ?: "Account",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Account") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAccExpanded = true },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(expanded = isAccExpanded, onDismissRequest = { isAccExpanded = false }) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(text = { Text(acc.name) }, onClick = {
                                selectedAccount = acc
                                isAccExpanded = false
                            })
                        }
                    }
                }

                // Quick Mode Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val types = listOf(
                        "CASH_OUT" to "Cash Out (-)",
                        "CASH_IN" to "Cash In (+)",
                        "INCOMING" to "Incoming"
                    )
                    types.forEach { (tKey, tLabel) ->
                        val isSel = selectedType == tKey
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedType = tKey
                                    if (tKey == "CASH_OUT" && !cashImpactText.startsWith("-")) {
                                        cashImpactText = "-$cashImpactText"
                                    } else if (tKey == "CASH_IN" && cashImpactText.startsWith("-")) {
                                        cashImpactText = cashImpactText.removePrefix("-")
                                    }
                                },
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = tLabel,
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Cash In / Out Column (Allows exact +/- values like Google Sheet)
                OutlinedTextField(
                    value = cashImpactText,
                    onValueChange = { cashImpactText = it },
                    label = { Text("Cash In/Out Value (+ or -)") },
                    placeholder = { Text("-5000 or 3000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Dedicated Fee Column
                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text("Fee / Commission Column (فیس)") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Customer / Note
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer / Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = referenceId,
                    onValueChange = { referenceId = it },
                    label = { Text("Reference ID / Sheet Row") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val cashImpact = cashImpactText.toDoubleOrNull() ?: 0.0
                            val fee = feeText.toDoubleOrNull() ?: 0.0
                            val amt = Math.abs(cashImpact)

                            val accImpact = when (selectedType) {
                                "CASH_OUT" -> +amt
                                "CASH_IN" -> -amt
                                else -> +amt
                            }

                            if (selectedAccount == null || amt == 0.0) {
                                errorMessage = "Please provide valid account and cash value"
                                return@Button
                            }

                            onConfirm(
                                selectedAccount!!.id,
                                selectedType,
                                cashImpact,
                                accImpact,
                                amt,
                                fee,
                                customerName,
                                note,
                                referenceId
                            )
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Insert Row")
                    }
                }
            }
        }
    }
}

// 6. EDIT OR ADD ACCOUNT DIALOG
@Composable
fun AccountEditDialog(
    account: AccountEntity?, // null if creating new
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit,
    onDelete: ((AccountEntity) -> Unit)? = null
) {
    var name by remember { mutableStateOf(account?.name ?: "") }
    var accountType by remember { mutableStateOf(account?.accountType ?: "JAZZCASH") }
    var accountNumber by remember { mutableStateOf(account?.accountNumber ?: "") }
    var initialBalanceText by remember { mutableStateOf(account?.initialBalance?.let { String.format(java.util.Locale.US, "%.0f", it) } ?: "0") }
    var isCashDrawer by remember { mutableStateOf(account?.isCashDrawer ?: false) }
    var selectedColor by remember { mutableStateOf(account?.colorHex ?: "#059669") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presetColors = listOf(
        "#16A34A" to "Green",
        "#DC2626" to "Red",
        "#EA580C" to "Orange",
        "#059669" to "Emerald",
        "#0D9488" to "Teal",
        "#0284C7" to "Blue",
        "#4F46E5" to "Indigo",
        "#9333EA" to "Purple"
    )

    val accountTypes = listOf(
        "CASH" to "Cash Drawer",
        "JAZZCASH" to "JazzCash",
        "EASYPAISA" to "EasyPaisa",
        "SADAPAY" to "SadaPay",
        "NAYAPAY" to "NayaPay",
        "BANK" to "Bank Account",
        "OTHER" to "Other Account"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (account == null) "Add Account (نیا اکاؤنٹ)" else "Edit Account (اکاؤنٹ درست کریں)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name (e.g. JazzCash 1)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Account / Phone / IBAN (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = { initialBalanceText = it },
                    label = { Text("Initial Starting Balance") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text("Account Color Theme:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetColors.forEach { (hex, _) ->
                        val col = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .clickable { selectedColor = hex }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == hex) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (account != null && onDelete != null && !account.isCashDrawer) {
                        IconButton(
                            onClick = { onDelete(account) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Please enter an account name"
                                return@Button
                            }
                            val initBal = initialBalanceText.toDoubleOrNull() ?: 0.0
                            val updated = account?.copy(
                                name = name.trim(),
                                accountNumber = accountNumber.trim(),
                                initialBalance = initBal,
                                colorHex = selectedColor,
                                accountType = accountType
                            ) ?: AccountEntity(
                                name = name.trim(),
                                accountType = accountType,
                                accountNumber = accountNumber.trim(),
                                initialBalance = initBal,
                                colorHex = selectedColor,
                                isCashDrawer = isCashDrawer
                            )
                            onSave(updated)
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
