package `in`.testpress.testpress.ui.adapters

import `in`.testpress.testpress.R
import `in`.testpress.testpress.models.pojo.Banner
import `in`.testpress.testpress.models.pojo.DashboardResponse
import `in`.testpress.testpress.models.pojo.DashboardSection
import `in`.testpress.util.ImageUtils
import `in`.testpress.util.IntegerList
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import java.util.*

class ImageOnlyBannerCarouselAdapter(val response: DashboardResponse, val section: DashboardSection, val context: Context) : RecyclerView.Adapter<ImageOnlyBannerCarouselAdapter.ItemViewHolder>() {
    private var imageLoader: ImageLoader = ImageUtils.initImageLoader(context)
    private var options: DisplayImageOptions = ImageUtils.getPlaceholdersOption()
    private var banners: ArrayList<Banner> = ArrayList()

    init {
        imageLoader = ImageUtils.initImageLoader(context)
        options = `in`.testpress.testpress.util.ImageUtils.getPlaceholdersOption()
        populateBanners()
    }

    private fun populateBanners() {
        val items: IntegerList = section.items
        for (item in items) {
            response.bannerHashMap[java.lang.Long.valueOf(item.toLong())]?.let {
                banners.add(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.full_width_carousel_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        imageLoader.displayImage(banners[position].image, holder.image, options)
    }

    override fun getItemCount(): Int {
        return banners.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById<View>(R.id.image_view) as ImageView
    }
}