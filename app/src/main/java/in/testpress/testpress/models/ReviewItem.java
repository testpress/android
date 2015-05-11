package in.testpress.testpress.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import android.app.Activity;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.util.SafeAsyncTask;

@Table(name = "ReviewItem")
public class ReviewItem extends Model implements Parcelable {

    @Column(name = "ItemId", onUpdate = Column.ForeignKeyAction.SET_NULL)
    private Integer id;
    @Column(name = "attemptId", onDelete = Column.ForeignKeyAction.CASCADE)
    public Integer attemptId;
    @Column(name = "url")
    private String url;
    @Column(name = "question")
    private ReviewQuestion question;
    @Column(name = "selectedAnswers")
    private List<Integer> selectedAnswers = new ArrayList<Integer>();
    @Column(name = "review")
    private Boolean review;
    @Column(name = "Attempts", onDelete = Column.ForeignKeyAction.CASCADE)
    public Attempt attempt;
    @Column(name = "filter")
    public String filter;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private HashMap<String, Bitmap> images = new HashMap<>();
    private ArrayList<String> imageUrl = new ArrayList<>();
//    ReviewItem() {
//        selectedAnswers = new ArrayList<Integer>();
//    }

    public ReviewItem() {
        super();
    }


    // Parcelling part
    public ReviewItem(Parcel parcel){
        id = parcel.readInt();
        question = (ReviewQuestion) parcel.readParcelable(ReviewQuestion.class.getClassLoader());
        url = parcel.readString();
        selectedAnswers = new ArrayList<Integer>();
        parcel.readList(selectedAnswers, List.class.getClassLoader());
        review = parcel.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeParcelable(question, i);
        parcel.writeString(url);
        parcel.writeList(selectedAnswers);
        if (review == null) {
            parcel.writeByte((byte) (0));
        } else {
            parcel.writeByte((byte) (review ? 1 : 0)); //if review == true, byte == 1
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ReviewItem createFromParcel(Parcel parcel) {
            return new ReviewItem(parcel);
        }

        public ReviewItem[] newArray(int size) {
            return new ReviewItem[size];
        }
    };

    public void setImages(String url, Bitmap image) {
        this.images.put(url, image);
    }

    public HashMap<String, Bitmap> getImages() {
        return images;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl.add(imageUrl);
    }

    public ArrayList<String> getImageUrl() {
        return imageUrl;
    }


    /**
     *
     * @return
     * The id
     */
    public Integer getItemId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     * The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return
     * The question
     */
    public ReviewQuestion getReviewQuestion() {
        return question;
    }

    public ReviewQuestion getReviewQuestionList() {
        return new Select().from(ReviewQuestion.class).where("ReviewItem = ?", this.getId()).where("filter = ?", this.filter).executeSingle();
    }

    /**
     *
     * @param reviewQuestion
     * The question
     */
    public void setReviewQuestion(ReviewQuestion reviewQuestion) {
        this.question = reviewQuestion;
    }

    /**
     *
     * @return
     * The selectedAnswers
     */
    public List<Integer> getSelectedAnswers() {
        return selectedAnswers;
    }

    public List<Integer> getSelectedAnswerList() {
        List<Integer> selected = new ArrayList<>();
        List<SelectedAnswer> answers = new ArrayList<>();
        try {
            answers = new Select().from(SelectedAnswer.class).where("ReviewItem = ?", this.getId()).where("filter = ?", this.filter).execute();
        } catch (Exception e){}
        for (SelectedAnswer ans : answers) {
            selected.add(ans.selectedAnswer);
        }
        return selected;
    }

    /**
     *
     * @param selectedAnswers
     * The selected_answers
     */
    public void setSelectedAnswers(List<Integer> selectedAnswers) {
        this.selectedAnswers = selectedAnswers;
    }

    /**
     *
     * @return
     * The review
     */
    public Boolean getReview() {
        return review;
    }

    /**
     *
     * @param review
     * The review
     */
    public void setReview(Boolean review) {
        this.review = review;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}