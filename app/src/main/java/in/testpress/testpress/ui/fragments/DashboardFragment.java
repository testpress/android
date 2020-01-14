package in.testpress.testpress.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.core.TestpressSdk;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.ui.adapters.DashboardAdapter;

public class DashboardFragment extends Fragment {
    private ArrayList<String> sections = new ArrayList<>();
    @InjectView(R.id.recycler_View)
    RecyclerView recyclerView;
    @InjectView(R.id.welcome_text)
    TextView welcomeText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Injector.inject(this);
        getActivity().invalidateOptionsMenu();
        return inflater.inflate(R.layout.dashboard_view, null);

    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        welcomeText.setTypeface(TestpressSdk.getRubikMediumFont(getContext()));
        DashboardAdapter adapter = new DashboardAdapter(getContext(), getObject());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private ArrayList<String> getObject() {
        sections.add("STATS_CHART");
        sections.add("CONTENT_CAROUSEL");
        sections.add("COURSE_CAROUSEL");
        sections.add("POST_CAROUSEL");
        sections.add("OFFERS_CAROUSEL");
        sections.add("CONTENT_CAROUSEL");
        sections.add("LEADERBOARD_LIST");
        return sections;
    }

}
