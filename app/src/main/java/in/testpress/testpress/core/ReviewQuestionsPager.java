package in.testpress.testpress.core;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.ReviewAnswer;
import in.testpress.testpress.models.ReviewItem;
import in.testpress.testpress.models.ReviewQuestion;
import in.testpress.testpress.models.SelectedAnswer;
import in.testpress.testpress.models.TestpressApiResponse;


public class ReviewQuestionsPager extends ResourcePager<ReviewItem> {
    Attempt attempt;
    String filter;
    TestpressApiResponse<ReviewItem> response;

    public ReviewQuestionsPager(Attempt attempt, String filter, TestpressService service) {
        super(service);
        this.attempt = attempt;
        this.filter = filter;
    }

    @Override
    public ResourcePager<ReviewItem> clear() {
        response = null;
        return super.clear();
    }

    @Override
    protected Object getId(ReviewItem resource) {
        return resource.getItemId();
    }

    @Override
    public List<ReviewItem> getItems(int page, int size) {
        String url = null;
        if (response == null) {
            url = attempt.getReviewFrag();
            if (filter.equals("all") == false) {
                url = url + "?state=" + filter;
            }
        } else {
            try {
                URL full = new URL(response.getNext());
                url = full.getFile().substring(1);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                url = null;
            }
        }
        if (url != null) {
            try {
                response = service.getReviewItems(url);
                List<ReviewItem> reviewItems = response.getResults();
                ActiveAndroid.beginTransaction();
                try {
                    for (ReviewItem reviewItem : reviewItems) {
                        reviewItem.attemptId = attempt.getAttemptId();
                        reviewItem.filter = filter;
                        reviewItem.save();
                        ReviewQuestion question = reviewItem.getReviewQuestion();
                        question.reviewItem = reviewItem;
                        question.filter = filter;
                        question.save();
                        SelectedAnswer selectedAnswer = new SelectedAnswer();
                        for(Integer answer: reviewItem.getSelectedAnswers()) {
                            selectedAnswer.selectedAnswer = answer;
                            selectedAnswer.reviewItem = reviewItem;
                            selectedAnswer.filter = filter;
                            selectedAnswer.save();
                        }
                        for(ReviewAnswer answer : reviewItem.getReviewQuestion().getAnswers()) {
                            answer.reviewItem = reviewItem;
                            answer.filter = filter;
                            answer.save();
                        }
                    }
                    ActiveAndroid.setTransactionSuccessful();
                }
                finally {
                    ActiveAndroid.endTransaction();
                }
                return getAll();
            } catch (Exception e) {
                try {
                    List<ReviewItem> items = getAll();
                    return items;
                } catch (Exception exception) {
                    return null;
                }
            }
        }
        return Collections.emptyList();
    }


    public List<ReviewItem> getAll() {
        return new Select().from(ReviewItem.class)
                .where("attemptId = ?", attempt.getAttemptId()).where("filter = ?", filter)
                .orderBy("attemptId DESC")
                .execute();
    }

    @Override
    public boolean hasNext() {
        if (response == null) {
            return true;
        }

        if (response.getNext() != null) {
            return true;
        }

        return false;
    }
}
