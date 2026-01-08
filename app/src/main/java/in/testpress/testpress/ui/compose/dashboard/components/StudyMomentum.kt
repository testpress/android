package `in`.testpress.testpress.ui.compose.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.testpress.testpress.ui.compose.dashboard.StudyMomentum
import `in`.testpress.testpress.ui.compose.dashboard.WeekDayActivity
import androidx.compose.ui.res.painterResource
import `in`.testpress.testpress.R

@Composable
fun StudyMomentumSection(
    momentum: StudyMomentum
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text(
            text = "Study Momentum", // Title Case as per design
            fontSize = 16.sp, // Larger
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A), // Dark Slate
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)), // Border instead of shadow
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // No shadow
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                
                // 1. Circles Row (Momentum Heatmap)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    momentum.weekDays.forEach { day ->
                        DayCircle(activity = day)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 2. Latest Activity Section
                Text(
                    text = "Latest Activity",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B), // Slate 500
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            letterSpacing = (-0.5).sp
                        )) {
                            append(momentum.latestActivity.title)
                        }
                        withStyle(style = SpanStyle(
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )) {
                            append(" • ${momentum.latestActivity.timeAgo}")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 3. Footer Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Streak
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fire),
                            contentDescription = "Streak",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${momentum.currentStreak}-day momentum",
                            fontSize = 14.sp,
                            color = Color(0xFF475569), // Slate 600 (Lighter)
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Right: Hours
                    Text(
                        text = "${momentum.weeklyHours}h this week",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B), // Slate 500
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun DayCircle(activity: WeekDayActivity) {
    val minutes = activity.minutes
    
    // Gradient Logic
    // If active, use a vertical gradient from Slate 500 to Slate 800
    // If empty, use flat Slate 200
    
    val backgroundModifier = if (minutes > 0) {
        // Active Gradient
        Modifier.background(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF64748B), // Slate 500 (Top)
                    Color(0xFF334155)  // Slate 700 (Bottom)
                )
            )
        )
    } else {
        // Empty Flat
        Modifier.background(Color(0xFFE2E8F0))
    }
    
    // Intensity Opacity/Shade?
    // If minutes are low (< 60), maybe lighter gradient?
    // Let's use transparency or a lighter gradient set for 'Low' activity if desired.
    // For now, sticking to one 'Active' gradient to ensure visibility.
    // The design shows 'Medium' (Light Blue) and 'High' (Dark Blue).
    // I should probably support 2 gradients.
    
    val finalModifier = if (minutes == 0) {
         Modifier.background(Color(0xFFE2E8F0)) // Empty
    } else if (minutes < 30) {
         // Low Activity (Light Blue-Grey)
         Modifier.background(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFCBD5E1), // Slate 300
                    Color(0xFF94A3B8)  // Slate 400
                )
            )
        )
    } else if (minutes < 60) {
         // Medium Activity (Medium Blue-Grey)
         Modifier.background(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF94A3B8), // Slate 400
                    Color(0xFF64748B)  // Slate 500
                )
            )
        )
    } else {
         // High Activity (Dark Blue-Grey)
         Modifier.background(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF5A6B85), // Custom Blue-Grey (Lighter start)
                    Color(0xFF334155)  // Slate 700 (Lighter end than 800)
                )
            )
        )
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .then(finalModifier)
        )
        
        Text(
            text = activity.dayLabel,
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
    }
}
