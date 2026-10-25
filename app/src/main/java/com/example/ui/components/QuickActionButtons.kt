package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CashInGreen
import com.example.ui.theme.CashOutRed
import com.example.ui.theme.FeeGold
import com.example.ui.theme.SlateSecondary

@Composable
fun QuickActionButtons(
    onCashOutClick: () -> Unit,
    onCashInClick: () -> Unit,
    onIncomingClick: () -> Unit,
    onTransferClick: () -> Unit,
    onSheetRowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Actions (فوری اندراج)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "With Fees Tracking",
                    style = MaterialTheme.typography.labelSmall,
                    color = FeeGold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Two Buttons: Cash Out (-) & Cash In (+)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // CASH OUT BUTTON (Negative Cash, Positive Account)
                ActionButton(
                    modifier = Modifier.weight(1f),
                    title = "Cash Out",
                    subtitle = "- Cash / + Account",
                    icon = Icons.Default.ArrowDownward,
                    containerColor = CashOutRed,
                    contentColor = Color.White,
                    testTag = "btn_cash_out",
                    onClick = onCashOutClick
                )

                // CASH IN BUTTON (Positive Cash, Negative Account)
                ActionButton(
                    modifier = Modifier.weight(1f),
                    title = "Cash In",
                    subtitle = "+ Cash / - Account",
                    icon = Icons.Default.ArrowUpward,
                    containerColor = CashInGreen,
                    contentColor = Color.White,
                    testTag = "btn_cash_in",
                    onClick = onCashInClick
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary Buttons: Incoming (+), Transfer, Sheet Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SecondaryActionButton(
                    modifier = Modifier.weight(1f),
                    title = "+ Incoming",
                    subtitle = "Account In",
                    icon = Icons.Default.CallReceived,
                    color = MaterialTheme.colorScheme.primary,
                    testTag = "btn_incoming",
                    onClick = onIncomingClick
                )

                SecondaryActionButton(
                    modifier = Modifier.weight(1f),
                    title = "Transfer",
                    subtitle = "Acc to Acc",
                    icon = Icons.Default.SwapHoriz,
                    color = SlateSecondary,
                    testTag = "btn_transfer",
                    onClick = onTransferClick
                )

                SecondaryActionButton(
                    modifier = Modifier.weight(1f),
                    title = "Sheet Row",
                    subtitle = "Direct Entry",
                    icon = Icons.Default.TableChart,
                    color = FeeGold,
                    testTag = "btn_sheet_row",
                    onClick = onSheetRowClick
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(58.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SecondaryActionButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = color
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(14.dp),
                    tint = color
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = color,
                    maxLines = 1
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
    }
}
