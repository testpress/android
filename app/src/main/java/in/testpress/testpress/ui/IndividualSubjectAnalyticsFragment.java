package in.testpress.testpress.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.testpress.R;
import in.testpress.testpress.models.Subject;

import static in.testpress.testpress.ui.AnalyticsFragment.SUBJECTS;

public class IndividualSubjectAnalyticsFragment extends Fragment {

    @InjectView(R.id.subjects_answer_count_list) RecyclerView listView;
    @InjectView(R.id.individual_subjects_list) RecyclerView individualList;
    private List<Subject> subjects = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            subjects = getArguments().getParcelableArrayList(SUBJECTS);
        }
        if (subjects == null || subjects.isEmpty()) {
            throw new IllegalStateException("Subjects must not be null or empty");
        }
        Subject.sortSubjects(subjects);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.individual_subject_analytics_fragment,
                container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setNestedScrollingEnabled(false);
        individualList.setNestedScrollingEnabled(false);
        individualList.setHasFixedSize(true);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        individualList.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setAdapter(new SubjectAnswersCountListAdapter(getActivity(), subjects));
        IndividualSubjectAnalyticsGraphAdapter listAdapter =
                new IndividualSubjectAnalyticsGraphAdapter(getActivity(), subjects);
        individualList.setAdapter(listAdapter);
    }

}
