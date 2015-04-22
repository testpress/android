package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

import android.app.Activity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.util.SafeAsyncTask;


public class AttemptItem implements Parcelable {
    private String url;
    private AttemptQuestion question;
    private List<Integer> selectedAnswers = new ArrayList<Integer>();
    private Boolean review;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private List<Integer> savedAnswers = new ArrayList<Integer>();

    AttemptItem() {
        selectedAnswers = new ArrayList<Integer>();
        savedAnswers = new ArrayList<Integer>();
    }

    // Parcelling part
    public AttemptItem(Parcel parcel){
        question = (AttemptQuestion) parcel.readParcelable(AttemptQuestion.class.getClassLoader());
        url = parcel.readString();
        selectedAnswers = new ArrayList<Integer>();
        parcel.readList(selectedAnswers, List.class.getClassLoader());
        savedAnswers = new ArrayList<Integer>();
        parcel.readList(savedAnswers, List.class.getClassLoader());
        review = parcel.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(question, i);
        parcel.writeString(url);
        parcel.writeList(selectedAnswers);
        parcel.writeList(savedAnswers);
        if (review == null) {
            parcel.writeByte((byte) (0));
        } else {
            parcel.writeByte((byte) (review ? 1 : 0)); //if review == true, byte == 1
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public AttemptItem createFromParcel(Parcel parcel) {
            return new AttemptItem(parcel);
        }

        public AttemptItem[] newArray(int size) {
            return new AttemptItem[size];
        }
    };

    public void saveAnswers(List<Integer> savedAnswers) {
        this.savedAnswers = savedAnswers;
    }

    public List<Integer> getSavedAnswers() {
        return savedAnswers;
    }

    public String saveResult(final Activity activity, final TestpressServiceProvider serviceProvider) throws Exception {

        SafeAsyncTask<AttemptItem> postResult = new SafeAsyncTask<AttemptItem>() {
            String fragment = null;

            @Override
            public AttemptItem call() throws Exception {
                return serviceProvider.getService(activity).postAnswer(fragment, savedAnswers);
            }

            @Override
            protected void onPreExecute() {
                try {
                    URL urlfrag = new URL(url);
                    fragment = urlfrag.getFile();
                }
                catch (MalformedURLException e) {

                }
            }

            @Override
            protected void onSuccess(AttemptItem result) {
            }

            @Override
            protected void onException(Exception e) {

            }

            @Override
            protected void onFinally() {
            }
        };
        postResult.execute();
        return null;
    }

    public Boolean hasChanged() {
        if(!savedAnswers.equals(selectedAnswers) && !savedAnswers.isEmpty()) {
            return true;
        }
        else return false;
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
    public AttemptQuestion getAttemptQuestion() {
        return question;
    }

    /**
     *
     * @param attemptQuestion
     * The question
     */
    public void setAttemptQuestion(AttemptQuestion attemptQuestion) {
        this.question = attemptQuestion;
    }

    /**
     *
     * @return
     * The selectedAnswers
     */
    public List<Integer> getSelectedAnswers() {
        return selectedAnswers;
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