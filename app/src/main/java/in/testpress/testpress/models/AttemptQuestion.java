package in.testpress.testpress.models;


import android.app.Activity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.util.SafeAsyncTask;


public class AttemptQuestion {
    private String url;
    private Question question;
    private List<Integer> selectedAnswers = new ArrayList<Integer>();
    private Boolean review;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private List<Integer> savedAnswers = new ArrayList<Integer>();

    public void saveAnswers(List<Integer> savedAnswers) {
        this.savedAnswers = savedAnswers;
    }

    public List<Integer> getSavedAnswers() {
        return savedAnswers;
    }

    public String saveResult(final Activity activity, final TestpressServiceProvider serviceProvider) throws Exception {

        SafeAsyncTask<AttemptQuestion> postResult = new SafeAsyncTask<AttemptQuestion>() {
            String fragment = null;

            @Override
            public AttemptQuestion call() throws Exception {
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
            protected void onSuccess(AttemptQuestion result) {
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

    public Boolean hasChanged() { return !savedAnswers.equals(selectedAnswers);}

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
    public Question getQuestion() {
        return question;
    }

    /**
     *
     * @param question
     * The question
     */
    public void setQuestion(Question question) {
        this.question = question;
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