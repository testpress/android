package `in`.testpress.testpress.ui.adapters

import `in`.testpress.testpress.R
import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.models.pojo.DashboardResponse
import `in`.testpress.testpress.models.pojo.DashboardSection
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class BannerCarouselAdapter(
        val response: DashboardResponse,
        val section: DashboardSection,
        val context: Context, val serviceProvider: TestpressServiceProvider
) : OffersCarouselAdapter(response, section, context, serviceProvider) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.image_only_carousel_item, parent, false)
        return MyViewHolder(view)
    }
}