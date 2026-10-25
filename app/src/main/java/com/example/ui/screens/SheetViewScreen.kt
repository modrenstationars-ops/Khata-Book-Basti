package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TransactionWithDetails
import com.example.ui.TransactionDialogType
import com.example.ui.theme.CashInGreen
import com.example.ui.theme.CashOutRed
import com.example.ui.theme.FeeGold
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SheetViewScreen(
    transactions: List<TransactionWithDetails>,
    onShareCsv: (Context) -> Unit,
    onCopyCsv: (Context) -> Unit,
    onOpenDialog: (TransactionDialogType) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hScrollState = rememberScrollState()

    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    var totalCashIn = 0.0
    var totalCashOut = 0.0
    var totalFees = 0.0

    for (item in transactions) {
        val t = item.transaction
        totalFees += t.fee
        if (t.cashImpact > 0) totalCashIn += t.cashImpact
        if (t.cashImpact < 0) totalCashOut += -t.cashImpact
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 72.dp)
    ) {
        // Top Toolbar for Google Sheet Actions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            tint = CashInGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Google Sheets Table View",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Exact spreadsheet layout with +/- Cash & Fees",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Export & Copy Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onShareCsv(context) },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("btn_export_csv"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CashInGreen)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { onCopyCsv(context) },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("btn_copy_csv"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Sheet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { onOpenDialog(TransactionDialogType.SheetRow) },
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("btn_insert_sheet_row"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Row", fontSize = 12.sp)
                    }
                }
            }
        }

        // Spreadsheet Table
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Horizontal Scroll Container
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(hScrollState)
                ) {
                    // Frozen Header Row
                    SheetHeaderRow()

                    // Scrollable Data Rows
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        itemsIndexed(transactions, key = { _, item -> item.transaction.id }) { index, item ->
                            SheetDataRow(
                                rowIndex = transactions.size - index,
                                item = item,
                                formatter = formatter,
                                isEven = index % 2 == 0
                            )
                        }

                        // Summary Totals Row at end
                        item {
                            SheetTotalsRow(
                                totalCashIn = totalCashIn,
                                totalCashOut = totalCashOut,
                                totalFees = totalFees,
                                formatter = formatter
                            )
                        }
                    }
                }
            }
        }
    }
}

// Column widths for the sheet
private val W_INDEX = 40.dp
private val W_DATE = 80.dp
private val W_TIME = 60.dp
private val W_ACCOUNT = 120.dp
private val W_TYPE = 90.dp
private val W_CASH = 100.dp
private val W_ACC_IMPACT = 100.dp
private val W_FEE = 80.dp
private val W_ACC_BAL = 100.dp
private val W_CASH_BAL = 100.dp
private val W_NOTE = 140.dp
private val W_REF = 90.dp

@Composable
private fun SheetHeaderRow() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .height(40.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderCell("#", W_INDEX)
            HeaderCell("Date", W_DATE)
            HeaderCell("Time", W_TIME)
            HeaderCell("Account", W_ACCOUNT)
            HeaderCell("Type", W_TYPE)
            HeaderCell("Cash In/Out (+/-)", W_CASH, highlight = true)
            HeaderCell("Acc Impact", W_ACC_IMPACT)
            HeaderCell("Fee (فیس)", W_FEE, highlight = true)
            HeaderCell("Acc Balance", W_ACC_BAL)
            HeaderCell("Cash Balance", W_CASH_BAL)
            HeaderCell("Customer / Note", W_NOTE)
            HeaderCell("Ref ID", W_REF)
        }
    }
}

@Composable
private fun HeaderCell(title: String, width: androidx.compose.ui.unit.Dp, highlight: Boolean = false) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun SheetDataRow(
    rowIndex: Int,
    item: TransactionWithDetails,
    formatter: NumberFormat,
    isEven: Boolean
) {
    val t = item.transaction
    val bgColor = if (isEven) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    Surface(
        color = bgColor,
        modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .height(44.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index
            CellText(text = "$rowIndex", width = W_INDEX, isMuted = true)

            // Date
            CellText(text = t.dateString, width = W_DATE)

            // Time
            CellText(text = t.timeString, width = W_TIME, isMuted = true)

            // Account
            CellText(
                text = if (t.type == "TRANSFER" && item.toAccountName != null) {
                    "${item.accountName} → ${item.toAccountName}"
                } else {
                    item.accountName
                },
                width = W_ACCOUNT,
                isBold = true
            )

            // Type
            val typeShort = when (t.type) {
                "CASH_OUT" -> "Cash Out"
                "CASH_IN" -> "Cash In"
                "INCOMING" -> "Incoming"
                "OUTGOING" -> "Outgoing"
                "TRANSFER" -> "Transfer"
                else -> t.type
            }
            CellText(text = typeShort, width = W_TYPE)

            // Cash In/Out (+ or -) -> Key User Requirement!
            val cashText = when {
                t.cashImpact > 0 -> "+${formatter.format(t.cashImpact)}"
                t.cashImpact < 0 -> "-${formatter.format(Math.abs(t.cashImpact))}"
                else -> "0"
            }
            val cashCol = when {
                t.cashImpact > 0 -> CashInGreen
                t.cashImpact < 0 -> CashOutRed
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Box(
                modifier = Modifier
                    .width(W_CASH)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = cashText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = cashCol
                    ),
                    maxLines = 1
                )
            }

            // Account Impact
            val accText = when {
                t.accountImpact > 0 -> "+${formatter.format(t.accountImpact)}"
                t.accountImpact < 0 -> "-${formatter.format(Math.abs(t.accountImpact))}"
                else -> "${formatter.format(t.amount)}"
            }
            CellText(text = accText, width = W_ACC_IMPACT)

            // Fee / Commission Column
            Box(
                modifier = Modifier
                    .width(W_FEE)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Rs. ${formatter.format(t.fee)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = FeeGold
                    ),
                    maxLines = 1
                )
            }

            // Running Account Balance
            CellText(text = formatter.format(item.accountBalanceAfter), width = W_ACC_BAL, isMuted = true)

            // Running Cash Balance
            CellText(text = formatter.format(item.cashBalanceAfter), width = W_CASH_BAL, isMuted = true)

            // Customer / Note
            val noteCombined = listOfNotNull(
                t.customerName.takeIf { it.isNotBlank() },
                t.note.takeIf { it.isNotBlank() }
            ).joinToString(" - ")
            CellText(text = if (noteCombined.isNotBlank()) noteCombined else "-", width = W_NOTE)

            // Ref ID
            CellText(text = if (t.referenceId.isNotBlank()) t.referenceId else "-", width = W_REF, isMuted = true)
        }
    }
}

@Composable
private fun SheetTotalsRow(
    totalCashIn: Double,
    totalCashOut: Double,
    totalFees: Double,
    formatter: NumberFormat
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(W_INDEX + W_DATE + W_TIME + W_ACCOUNT + W_TYPE)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "TOTALS / سمری:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Cash Net Total
            val netCash = totalCashIn - totalCashOut
            val netCashText = if (netCash >= 0) "+Rs. ${formatter.format(netCash)}" else "-Rs. ${formatter.format(Math.abs(netCash))}"
            Box(
                modifier = Modifier
                    .width(W_CASH)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = netCashText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (netCash >= 0) CashInGreen else CashOutRed
                    )
                )
            }

            // Empty Acc Impact
            Box(modifier = Modifier.width(W_ACC_IMPACT))

            // Total Fees
            Box(
                modifier = Modifier
                    .width(W_FEE)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "Rs. ${formatter.format(totalFees)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = FeeGold
                    )
                )
            }

            // Remaining cells
            Box(modifier = Modifier.width(W_ACC_BAL + W_CASH_BAL + W_NOTE + W_REF))
        }
    }
}

@Composable
private fun CellText(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isBold: Boolean = false,
    isMuted: Boolean = false
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 11.sp,
                color = if (isMuted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            ),
            maxLines = 1
        )
    }
}
