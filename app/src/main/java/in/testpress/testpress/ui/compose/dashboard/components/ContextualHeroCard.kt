package `in`.testpress.testpress.ui.compose.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.testpress.testpress.R
import `in`.testpress.testpress.ui.compose.dashboard.ContextualAction
import `in`.testpress.testpress.ui.compose.dashboard.ActionType

@Composable
fun ContextualHeroCard(
    action: ContextualAction,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = getGradientBrush(action.type),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .shadow(
                                    elevation = 2.dp,
                                    shape = CircleShape,
                                    clip = false
                                )
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // Use PNG for JOIN_CLASS, WATCH_RECORDING, TAKE_TEST, PREPARE_TEST, and CONTINUE_STUDY
                            if (action.type == ActionType.JOIN_CLASS || action.type == ActionType.WATCH_RECORDING || action.type == ActionType.TAKE_TEST || action.type == ActionType.PREPARE_TEST || action.type == ActionType.CONTINUE_STUDY) {
                                val iconRes = when (action.type) {
                                    ActionType.JOIN_CLASS -> R.drawable.ic_live_class
                                    ActionType.TAKE_TEST -> R.drawable.ic_articles
                                    ActionType.PREPARE_TEST -> R.drawable.ic_schedule_class
                                    ActionType.CONTINUE_STUDY -> R.drawable.ic_test
                                    else -> R.drawable.ic_videos
                                }
                                Icon(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = null,
                                    tint = if (action.type == ActionType.TAKE_TEST) Color(0xFF475569) else Color(0xFF475569),
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = getActionIcon(action.type),
                                    contentDescription = null,
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = getStatusLabel(action.type),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569), // Slate 600 (Darker)
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    if (action.isLocked) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = action.title,
                        fontSize = 20.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                    
                    Text(
                        text = action.subject,
                        fontSize = 16.sp,
                        color = Color(0xFF334155)
                    )
                }
                
                // Metadata with Wrapping support
                val metadataParts = mutableListOf<Pair<String, Boolean>>() // Text, Emphasized
                if (action.metadata.isNotEmpty()) metadataParts.add(action.metadata to false)
                action.timeInfo?.let { if (it.isNotEmpty()) metadataParts.add(it to false) }
                action.countdown?.let { if (it.isNotEmpty()) metadataParts.add(it to true) }
                action.testDuration?.let { if (it.isNotEmpty()) metadataParts.add(it to false) }

                if (metadataParts.isNotEmpty()) {
                    Text(
                        text = buildAnnotatedString {
                             metadataParts.forEachIndexed { index, (text, isEmphasized) ->
                                 if (index > 0) {
                                     withStyle(SpanStyle(color = Color(0xFF94A3B8), fontSize = 14.sp)) { 
                                         append(" •\u00A0") 
                                     }
                                 }
                                 withStyle(SpanStyle(
                                     color = if (isEmphasized) Color(0xFF334155) else Color(0xFF475569),
                                     fontWeight = if (isEmphasized) FontWeight.Medium else FontWeight.Normal,
                                     fontSize = 14.sp
                                 )) {
                                     // Use non-breaking space to keep units together with values
                                     append(text.replace(" ", "\u00A0"))
                                 }
                             }
                        },
                        lineHeight = 32.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                action.progress?.let { progress ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progress",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "$progress% Complete",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        
                        LinearProgressIndicator(
                            progress = progress / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp)), // Rounded edges
                            color = getAccentColor(action.type),
                            trackColor = Color(0xFFCBD5E1) // Grey track
                        )
                    }
                }
                
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getAccentColor(action.type)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = getButtonText(action.type),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataItem(
    text: String,
    emphasized: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(Color(0xFF94A3B8), CircleShape)
        )
        
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (emphasized) Color(0xFF334155) else Color(0xFF475569), // Slate 600 for normal text
            fontWeight = if (emphasized) FontWeight.Medium else FontWeight.Normal
        )
    }
}

private fun getActionIcon(type: ActionType): ImageVector {
    return when (type) {
        ActionType.JOIN_CLASS -> Icons.Filled.PlayArrow // Fallback
        ActionType.WATCH_RECORDING -> Icons.Filled.PlayArrow
        ActionType.TAKE_TEST -> Icons.Filled.List
        ActionType.PREPARE_TEST -> Icons.Filled.DateRange
        ActionType.CONTINUE_STUDY -> Icons.Filled.Star
        ActionType.EXPLORE_COURSES -> Icons.Filled.Star
        ActionType.START_TRIAL -> Icons.Filled.Star
        ActionType.UPGRADE_PROMPT -> Icons.Filled.Lock
    }
}

private fun getStatusLabel(type: ActionType): String {
    return when (type) {
        ActionType.JOIN_CLASS -> "LIVE NOW"
        ActionType.TAKE_TEST -> "AVAILABLE NOW"
        ActionType.PREPARE_TEST -> "UPCOMING"
        ActionType.EXPLORE_COURSES, ActionType.START_TRIAL -> "FREE ACCESS"
        ActionType.UPGRADE_PROMPT -> "PREMIUM"
        ActionType.WATCH_RECORDING, ActionType.CONTINUE_STUDY -> "RECOMMENDED"
        else -> "RECOMMENDED"
    }
}

private fun getButtonText(type: ActionType): String {
    return when (type) {
        ActionType.JOIN_CLASS -> "Join Now"
        ActionType.WATCH_RECORDING -> "Watch Recording"
        ActionType.TAKE_TEST -> "Start Test"
        ActionType.PREPARE_TEST -> "Prepare for Test"
        ActionType.CONTINUE_STUDY -> "Continue Learning"
        ActionType.EXPLORE_COURSES -> "Explore Courses"
        ActionType.START_TRIAL -> "Start Free Trial"
        ActionType.UPGRADE_PROMPT -> "Upgrade Now"
    }
}

private fun getAccentColor(type: ActionType): Color {
    return when (type) {
        ActionType.JOIN_CLASS -> Color(0xFF16A34A) // Green
        ActionType.TAKE_TEST -> Color(0xFFDC2626) // Red
        ActionType.EXPLORE_COURSES, ActionType.START_TRIAL -> Color(0xFF2563EB) // Blue
        ActionType.UPGRADE_PROMPT -> Color(0xFFD97706) // Orange
        else -> Color(0xFF0F172A) // Black for others (Watch, Prepare, Continue)
    }
}

private fun getGradientBrush(type: ActionType): Brush {
    return when (type) {
        ActionType.EXPLORE_COURSES, ActionType.START_TRIAL -> {
            Brush.verticalGradient(
                colors = listOf(Color(0xFFEFF6FF), Color(0xFFE0E7FF))
            )
        }
        ActionType.UPGRADE_PROMPT -> {
            Brush.verticalGradient(
                colors = listOf(Color(0xFFFEF3C7), Color(0xFFFED7AA))
            )
        }
        else -> {
            Brush.verticalGradient(
                colors = listOf(Color(0xFFF8FAFC), Color(0xFFEFF6FF))
            )
        }
    }
}
