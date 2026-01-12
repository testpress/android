package `in`.testpress.testpress.ui.compose.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.testpress.testpress.R
import `in`.testpress.testpress.ui.compose.dashboard.QuickAccessShortcut
import `in`.testpress.testpress.ui.compose.dashboard.ShortcutIcon

@Composable
fun QuickAccessShortcuts(
    shortcuts: List<QuickAccessShortcut>,
    onShortcutClick: (QuickAccessShortcut) -> Unit = {}
) {
    if (shortcuts.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Quick Access",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Custom Grid Implementation (3 columns)
        val chunkedShortcuts = shortcuts.chunked(3)
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            chunkedShortcuts.forEach { rowShortcuts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowShortcuts.forEach { shortcut ->
                        QuickAccessItem(
                            shortcut = shortcut,
                            modifier = Modifier.weight(1f),
                            onClick = { onShortcutClick(shortcut) }
                        )
                    }
                    // Add spacers if row is incomplete
                    if (rowShortcuts.size < 3) {
                        repeat(3 - rowShortcuts.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAccessItem(
    shortcut: QuickAccessShortcut,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)), // Slate 100
                contentAlignment = Alignment.Center
            ) {
                val (iconRes, iconVector) = getShortcutIcon(shortcut.icon)
                
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = Color(0xFF334155), // Slate 700
                        modifier = Modifier.size(20.dp)
                    )
                } else if (iconVector != null) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Text(
                text = shortcut.label,
                fontSize = 12.sp,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

private fun getShortcutIcon(type: ShortcutIcon): Pair<Int?, ImageVector?> {
    return when (type) {
        ShortcutIcon.VIDEO -> Pair(R.drawable.ic_live_class, null)
        ShortcutIcon.PRACTICE -> Pair(R.drawable.ic_articles, null)
        ShortcutIcon.TESTS -> Pair(R.drawable.ic_clipboard_check, null) // Distinguish from Notes
        ShortcutIcon.NOTES -> Pair(R.drawable.ic_test, null) // Book Open
        ShortcutIcon.DOUBTS -> Pair(R.drawable.ic_message_circle, null)
        ShortcutIcon.SCHEDULE -> Pair(R.drawable.ic_schedule, null)
    }
}
