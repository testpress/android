package `in`.testpress.testpress.ui.compose.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.testpress.testpress.R
import `in`.testpress.testpress.ui.compose.dashboard.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodaysSnapshot(
    classes: List<TodayClass>,
    assignments: List<Assignment>,
    tests: List<Test>,
    modifier: Modifier = Modifier,
    onViewAllClick: () -> Unit = {}
) {
    // Smart grouping logic
    val liveClasses = remember(classes) { classes.filter { it.status == ClassStatus.LIVE } }
    val upcomingClasses = remember(classes) { classes.filter { it.status == ClassStatus.UPCOMING } }
    val completedClasses = remember(classes) { classes.filter { it.status == ClassStatus.COMPLETED } }

    val nextClass = upcomingClasses.firstOrNull()
    val laterClasses = if (upcomingClasses.isNotEmpty()) upcomingClasses.drop(1) else emptyList()

    val overdueAssignments = remember(assignments) { assignments.filter { it.status == AssignmentStatus.OVERDUE } }
    val pendingAssignments = remember(assignments) { assignments.filter { it.status == AssignmentStatus.PENDING } }
    
    // Check if sections should be shown
    val nowNextItems = remember(liveClasses, nextClass) { 
        liveClasses + (if (nextClass != null) listOf(nextClass) else emptyList()) 
    }
    
    val deadlineItems = remember(overdueAssignments, pendingAssignments) {
        overdueAssignments + pendingAssignments
    }
    
    val laterTodayItems = remember(laterClasses, completedClasses) {
        laterClasses + completedClasses
    }
    
    val hasNowAndNext = nowNextItems.isNotEmpty()
    val hasDeadlines = deadlineItems.isNotEmpty()
    val hasTests = tests.isNotEmpty()
    val hasLaterToday = laterTodayItems.isNotEmpty()

    if (!hasNowAndNext && !hasDeadlines && !hasTests && !hasLaterToday) return

    Column(modifier = modifier) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Schedule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "View all",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2563EB),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onViewAllClick() }
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // NOW & NEXT
            if (hasNowAndNext) {
                SnapshotSection(
                    title = "Now & Next",
                    items = nowNextItems
                ) { item ->
                    SnapshotClassCard(classItem = item)
                }
            }

            // DEADLINES
            if (hasDeadlines) {
                SnapshotSection(
                    title = "Deadlines",
                    items = deadlineItems
                ) { item ->
                    SnapshotAssignmentCard(assignment = item)
                }
            }

            // UPCOMING TESTS
            if (hasTests) {
                SnapshotSection(
                    title = "Upcoming Tests",
                    items = tests
                ) { item ->
                    SnapshotTestCard(test = item)
                }
            }

            // LATER TODAY
            if (hasLaterToday) {
                SnapshotSection(
                    title = "Later Today",
                    items = laterTodayItems
                ) { item ->
                    SnapshotClassCard(classItem = item)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> SnapshotSection(
    title: String,
    items: List<T>,
    content: @Composable (T) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp, // Increased to 12sp
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF64748B), 
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        if (items.size > 1) {
            val pagerState = rememberPagerState(pageCount = { items.size })
            
            Box(modifier = Modifier.fillMaxWidth()) {
                // 1. Invisible "Ghost" items to calculate the natural Max Height of the section
                // The Box will size itself to the tallest item (since they are stacked).
                Box(
                     modifier = Modifier
                         .padding(horizontal = 20.dp) // Match Pager padding
                         .alpha(0f) // Invisible
                ) {
                    items.forEach { item ->
                        content(item)
                    }
                }
                
                // 2. The Actual Pager
                // It sits on top and fills the height determined by the Ghost items
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 20.dp), // Use contentPadding
                    pageSpacing = 16.dp, // Standard spacing
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .matchParentSize() // Fill the calculated height
                        .graphicsLayer { clip = false } // Allow bouncing effect over padding
                ) { page ->
                    content(items[page])
                }
            }
                
                // Simple Dots
                if (items.size <= 5) {
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(items.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration) Color(0xFF94A3B8) else Color(0xFFCBD5E1)
                            val width = if (pagerState.currentPage == iteration) 24.dp else 6.dp
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(width = width, height = 6.dp)
                            )
                        }
                    }
                }
        } else {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                content(items.first())
            }
        }
    }
}

@Composable
fun SnapshotClassCard(classItem: TodayClass) {
    val isLive = classItem.status == ClassStatus.LIVE
    val isCompleted = classItem.status == ClassStatus.COMPLETED
    
    val borderColor = if (isLive) Color(0xFF16A34A) else Color(0xFFE2E8F0)
    
    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)), // Always Slate-200
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFFF8FAFC) else Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon
                Box(modifier = Modifier.padding(top = 3.dp)) {
                    if (isLive) {
                         Icon(
                             painter = painterResource(id = R.drawable.ic_live_class),
                             contentDescription = null,
                             tint = Color(0xFF16A34A),
                             modifier = Modifier.size(20.dp)
                         )
                    } else if (isCompleted) {
                         Icon(
                             painter = painterResource(id = R.drawable.ic_check_circle),
                             contentDescription = null,
                             tint = Color(0xFF16A34A),
                             modifier = Modifier.size(20.dp)
                         )
                    } else {
                         Icon(
                             painter = painterResource(id = R.drawable.ic_schedule_class),
                             contentDescription = null,
                             tint = Color(0xFF2563EB),
                             modifier = Modifier.size(20.dp)
                         )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = classItem.subject,
                            fontSize = 16.sp, // Match roughly font-medium base size
                            fontWeight = FontWeight.Medium,
                            color = if (isCompleted) Color(0xFF475569) else Color(0xFF0F172A),
                            maxLines = 1,
                            lineHeight = 20.sp, // leading-tight
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (isLive) {
                            Surface(
                                color = Color(0xFF16A34A),
                                shape = RoundedCornerShape(percent = 50), // rounded-full
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "LIVE",
                                    color = Color.White,
                                    fontSize = 10.sp, // text-xs small
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp) // px-3 py-0.5
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = classItem.topic,
                        fontSize = 14.sp, // text-sm
                        color = if (isCompleted) Color(0xFF64748B) else Color(0xFF475569), // Slate 600
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp) // mt-0.5 mb-3
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp) // gap-2
                    ) {
                        Text(
                            text = classItem.faculty,
                            fontSize = 12.sp, // text-xs
                            color = Color(0xFF64748B) // Slate 500
                        )
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = classItem.time,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                
                Box(modifier = Modifier.padding(top = 1.dp)) {
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SnapshotAssignmentCard(assignment: Assignment) {
    val isOverdue = assignment.status == AssignmentStatus.OVERDUE
    
    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon
                Box(modifier = Modifier.padding(top = 3.dp)) {
                    if (isOverdue) {
                         Icon(
                             painter = painterResource(id = R.drawable.ic_deadline),
                             contentDescription = null,
                             tint = Color(0xFFDC2626),
                             modifier = Modifier.size(20.dp)
                         )
                    } else {
                         Icon(
                             painter = painterResource(id = R.drawable.ic_assignment),
                             contentDescription = null,
                             tint = Color(0xFFD97706),
                             modifier = Modifier.size(20.dp)
                         )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    // Title Row (Title + Chevron)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = assignment.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            lineHeight = 24.sp,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        
                        Icon(
                            Icons.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp).padding(bottom = 2.dp) // Nudge up
                        )
                    }
                    
                    Text(
                        text = assignment.description,
                        fontSize = 14.sp,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Left: Subject • Due Time
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Text(
                                text = assignment.subject,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            
                            // Separator Dot
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .size(3.dp)
                                    .background(Color(0xFF64748B), CircleShape)
                            )
                            
                            Text(
                                text = "Due ${assignment.dueTime}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1
                            )
                        }
                        
                        // Right: Progress
                        assignment.progress?.let { progress ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Dashed Progress Bar (4 dashes)
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    val activeColor = Color(0xFFF59E0B)
                                    val inactiveColor = Color(0xFFE2E8F0)
                                    val totalDashes = 4
                                    val activeDashes = (progress / 100f * totalDashes).toInt().coerceIn(0, totalDashes)
                                    
                                    repeat(totalDashes) { index ->
                                        Box(
                                            modifier = Modifier
                                                .width(16.dp) 
                                                .height(4.dp) 
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(if (index < activeDashes) activeColor else inactiveColor)
                                        )
                                    }
                                }
                                
                                Text(
                                    text = "$progress%",
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SnapshotTestCard(test: Test) {
    val isImportant = test.isImportant
    
    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.padding(top = 3.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_test),
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = test.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            lineHeight = 20.sp,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (isImportant) {
                            Surface(
                                color = Color(0xFFDC2626),
                                shape = RoundedCornerShape(percent = 50),
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                            ) {
                                Text(
                                    text = "IMPORTANT",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = "${test.type} TEST", // Capitalized TEST
                        fontSize = 14.sp,
                        color = Color(0xFF475569),
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = test.time,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = test.duration,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                
                Box(modifier = Modifier.padding(top = 1.dp)) {
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
