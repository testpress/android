package `in`.testpress.testpress.ui.compose.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import `in`.testpress.testpress.ui.compose.dashboard.components.TopUtilityBar
import `in`.testpress.testpress.ui.compose.dashboard.components.HeroBannerCarousel
import `in`.testpress.testpress.ui.compose.dashboard.components.ContextualHeroCard
import `in`.testpress.testpress.ui.compose.dashboard.components.TodaysSnapshot
import `in`.testpress.testpress.ui.compose.dashboard.components.StudyMomentumSection
import `in`.testpress.testpress.ui.compose.dashboard.components.PromotionalBannerSection
import `in`.testpress.testpress.ui.compose.dashboard.components.QuickAccessShortcuts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.Alignment

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewDashboardScreen(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {}
) {
    val heroBanners: List<HeroBanner> = remember { DashboardSampleData.sampleHeroBanners }
    val contextualActions = remember { DashboardSampleData.enrolledActions }
    val todayClasses = remember { DashboardSampleData.todayClasses }
    val assignments = remember { DashboardSampleData.assignments }
    val tests = remember { DashboardSampleData.tests }
    val studyMomentum = remember { DashboardSampleData.studyMomentum }
    val promotionalBanners = remember { DashboardSampleData.promotionalBanners }
    val shortcuts = remember { DashboardSampleData.quickAccessShortcuts }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        TopUtilityBar(
            onMenuClick = onMenuClick
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            HeroBannerCarousel(
                banners = heroBanners,
                onBannerClick = { banner ->
                    // TODO: Handle banner click
                }
            )
            
            // Contextual Hero Carousel
            val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { contextualActions.size })
            
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 0.dp),
                pageSpacing = 0.dp,
                verticalAlignment = Alignment.Top
            ) { page ->
                ContextualHeroCard(
                    action = contextualActions[page],
                    onActionClick = {
                        // TODO: Handle action click
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TodaysSnapshot(
                classes = todayClasses,
                assignments = assignments,
                tests = tests,
                onViewAllClick = {
                    // TODO: Handle view all
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            StudyMomentumSection(
                momentum = studyMomentum
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            PromotionalBannerSection(
                banners = promotionalBanners
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            QuickAccessShortcuts(
                shortcuts = shortcuts
            )
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun NewDashboardScreenPreview() {
    MaterialTheme {
        NewDashboardScreen()
    }
}
