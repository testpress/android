package `in`.testpress.testpress.ui.compose.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.ExperimentalFoundationApi
import `in`.testpress.testpress.ui.compose.dashboard.PromotionalBanner

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PromotionalBannerSection(
    banners: List<PromotionalBanner>
) {
    if (banners.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Updates & Announcements",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        val pagerState = rememberPagerState(pageCount = { banners.size })

        Box(modifier = Modifier.fillMaxWidth()) {
            // Ghost items to calculate max height (prevents flickering)
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .alpha(0f) // Invisible
            ) {
                banners.forEach { banner ->
                    BannerCard(banner = banner)
                }
            }
            
            // Actual Pager
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                contentPadding = PaddingValues(0.dp), // No content padding to prevent peek
                pageSpacing = 16.dp,
                modifier = Modifier
                    .matchParentSize() // Fill calculated height
                    .padding(horizontal = 20.dp) // Apply padding here instead
            ) { page ->
                BannerCard(banner = banners[page])
            }
        }
        
        // Pager Indicator
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                val color = if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)
                val width = if (isSelected) 24.dp else 6.dp
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(color)
                        .size(width = width, height = 6.dp)
                )
            }
        }
    }
}

@Composable
fun BannerCard(banner: PromotionalBanner) {
    val bgColor = remember(banner.bgColor) { safeParseColor(banner.bgColor, Color(0xFFEFF6FF)) }
    val textColor = remember(banner.textColor) { safeParseColor(banner.textColor, Color(0xFF1E3A8A)) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)), // Outline added as requested
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Main Content
            Column(
                modifier = Modifier.padding(end = 24.dp) // Reserve space for chevron
            ) {
                // Tag (Conditional)
                if (!banner.tag.isNullOrEmpty()) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = banner.tag,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp, // Explicitly smaller text for tag
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp) // Compact padding
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Text Content
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Text(
                            text = banner.icon,
                            fontSize = 18.sp,
                            color = Color.Unspecified,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = banner.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = banner.description,
                        fontSize = 13.sp, // Reduced to 13sp for hierarchy
                        color = textColor.copy(alpha = 0.9f),
                        maxLines = 3,
                        lineHeight = 18.sp,
                        letterSpacing = 0.sp
                    )
                }
            }
            
            // Chevron Icon (Always Top Right)
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

fun safeParseColor(hex: String, default: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        default
    }
}
