package in.testpress.testpress.ui;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.Toaster;

import org.w3c.dom.Text;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.Exam;

public class ReviewFragment extends Fragment {
    Attempt attempt;
    Exam exam;
    @InjectView(R.id.exam_title) TextView examTitle;
    @InjectView(R.id.total_correct) TextView totalCorrect;
    @InjectView(R.id.total_incorrect) TextView totalIncorrect;
    @InjectView(R.id.total_unanswered) TextView totalUnanswered;
    @InjectView(R.id.score) TextView score;
    @InjectView(R.id.rank) TextView rank;
    @InjectView(R.id.percentile) TextView percentile;
    @InjectView(R.id.sub_percentile) TextView subPercentile;
    @InjectView(R.id.sub_score) TextView subScore;

    @Inject
    protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(final Menu optionsMenu, final MenuInflater inflater) {
        inflater.inflate(R.menu.attempt_actions, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (getActivity() == null) {
            return false;
        }

        switch (item.getItemId()) {
            case R.id.retake:
                if (exam.getAllowRetake() == true) {
                    Intent intent = new Intent(getActivity(), ExamActivity.class);
                    intent.putExtra("exam", exam);
                    startActivity(intent);
                    return true;
                } else {
                    Toaster.showShort(getActivity(), "Retakes are not allowed for this exam.");
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.review_fragment, container, false);
        ButterKnife.inject(this, view);
        Injector.inject(this);
        this.exam = getArguments().getParcelable("exam");
        this.attempt = getArguments().getParcelable("attempt");
        examTitle.setText(exam.getTitle());
        totalCorrect.setText("" + attempt.getCorrectCount());
        totalIncorrect.setText("" + attempt.getIncorrectCount());
        Integer unanswered = attempt.getTotalQuestions() - (attempt.getCorrectCount() + attempt.getIncorrectCount());
        totalUnanswered.setText("" + unanswered);
        rank.setText(attempt.getScore());
        score.setText(attempt.getScore());
        percentile.setText(attempt.getPercentile());
        subScore.setText(attempt.getScore());
        subPercentile.setText(attempt.getPercentile());
        return view;
    }
}
