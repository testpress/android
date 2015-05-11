package in.testpress.testpress.core;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.models.TestpressApiResponse;


public class AttemptsPager extends ResourcePager<Attempt> {
    Exam exam;
    TestpressApiResponse<Attempt> response;

    public AttemptsPager(Exam exam, TestpressService service) {
        super(service);
        this.exam = exam;
    }

    @Override
    public ResourcePager<Attempt> clear() {
        response = null;
        return super.clear();
    }

    @Override
    protected Object getId(Attempt resource) {
        return resource.getAttemptId();
    }

    @Override
    public List<Attempt> getItems(int page, int size) {
        String url = null;
        if (response == null) {
            url = exam.getAttemptsFrag();
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
                List<Attempt> attempts;
                response = service.getAttempts(url);
                attempts = response.getResults();
                ActiveAndroid.beginTransaction();
                try {
                    for(Attempt attempt : attempts) {
                        attempt.examId = exam.getExamId();
                        attempt.save();
                    }
                    ActiveAndroid.setTransactionSuccessful();
                }
                finally {
                    ActiveAndroid.endTransaction();
                }
                return getAll();
            } catch (Exception e) {
                try {
                    List<Attempt> attempts;
                    attempts = getAll();
                    return attempts;
                } catch (Exception exception) {
                    return null;
                }
            }
        }
        return Collections.emptyList();
    }

    public List<Attempt> getAll() {
        return new Select()
                .from(Attempt.class)
                .where("examId = ?", exam.getExamId())
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
