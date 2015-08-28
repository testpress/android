package in.testpress.testpress.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class Product {

    private String productUrl;
    private String title;
    private String slug;
    private String description;
    private String descriptionHtml;
    private String created;
    private String institute;
    private Boolean isActive;
    private String image;
    private String cover;
    private String coverHtml;
    private String paymentLink;
    private String marketingSnippet;
    private String startDate;
    private String endDate;
    private Boolean isPublished;
    private String price;
    private List<Exam> exams = new ArrayList<Exam>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();



    /**
     *
     * @return
     * The productUrl
     */
    public String getProductUrl() {
        return productUrl;
    }

    /**
     *
     * @param productUrl
     * The product_url
     */
    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    /**
     *
     * @return
     * The title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     * The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     * The slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     *
     * @param slug
     * The slug
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     *
     * @return
     * The description
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     * The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     * The descriptionHtml
     */
    public String getDescriptionHtml() {
        return descriptionHtml;
    }

    /**
     *
     * @param descriptionHtml
     * The description_html
     */
    public void setDescriptionHtml(String descriptionHtml) {
        this.descriptionHtml = descriptionHtml;
    }

    /**
     *
     * @return
     * The created
     */
    public String getCreated() {
        return created;
    }

    /**
     *
     * @param created
     * The created
     */
    public void setCreated(String created) {
        this.created = created;
    }

    /**
     *
     * @return
     * The institute
     */
    public String getInstitute() {
        return institute;
    }

    /**
     *
     * @param institute
     * The institute
     */
    public void setInstitute(String institute) {
        this.institute = institute;
    }

    /**
     *
     * @return
     * The isActive
     */
    public Boolean getIsActive() {
        return isActive;
    }

    /**
     *
     * @param isActive
     * The is_active
     */
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    /**
     *
     * @return
     * The image
     */
    public String getImage() {
        return image;
    }

    /**
     *
     * @param image
     * The image
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     *
     * @return
     * The cover
     */
    public String getCover() {
        return cover;
    }

    /**
     *
     * @param cover
     * The cover
     */
    public void setCover(String cover) {
        this.cover = cover;
    }

    /**
     *
     * @return
     * The coverHtml
     */
    public String getCoverHtml() {
        return coverHtml;
    }

    /**
     *
     * @param coverHtml
     * The cover_html
     */
    public void setCoverHtml(String coverHtml) {
        this.coverHtml = coverHtml;
    }

    /**
     *
     * @return
     * The paymentLink
     */
    public String getPaymentLink() {
        return paymentLink;
    }

    /**
     *
     * @param paymentLink
     * The payment_link
     */
    public void setPaymentLink(String paymentLink) {
        this.paymentLink = paymentLink;
    }

    /**
     *
     * @return
     * The marketingSnippet
     */
    public String getMarketingSnippet() {
        return marketingSnippet;
    }

    /**
     *
     * @param marketingSnippet
     * The marketing_snippet
     */
    public void setMarketingSnippet(String marketingSnippet) {
        this.marketingSnippet = marketingSnippet;
    }

    /**
     *
     * @return
     * The startDate
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     *
     * @param startDate
     * The start_date
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     *
     * @return
     * The endDate
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     *
     * @param endDate
     * The end_date
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     *
     * @return
     * The isPublished
     */
    public Boolean getIsPublished() {
        return isPublished;
    }

    /**
     *
     * @param isPublished
     * The is_published
     */
    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    /**
     *
     * @return
     * The price
     */
    public String getPrice() {
        return "₹ " + price;
    }

    /**
     *
     * @param price
     * The price
     */
    public void setPrice(String price) {
        this.price = price;
    }

    /**
     *
     * @return
     * The exams
     */
    public List<Exam> getExams() {
        return exams;
    }

    /**
     *
     * @param exams
     * The exams
     */
    public void setExams(List<Exam> exams) {
        this.exams = exams;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String formatDate(String inputString) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            if(inputString != null && !inputString.isEmpty()) {
                date = simpleDateFormat.parse(inputString);
                DateFormat dateformat = DateFormat.getDateInstance();
                return dateformat.format(date);
            }
        } catch (ParseException e) {
        }
        return null;
    }

    public String getDate(){
        return formatDate(getStartDate()) + " to " + formatDate(getEndDate());
    }

}