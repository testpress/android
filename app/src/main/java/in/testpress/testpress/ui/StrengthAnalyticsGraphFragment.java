package in.testpress.testpress.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.testpress.mikephil.charting.charts.HorizontalBarChart;
import com.github.testpress.mikephil.charting.components.XAxis;
import com.github.testpress.mikephil.charting.components.YAxis;
import com.github.testpress.mikephil.charting.data.BarData;
import com.github.testpress.mikephil.charting.data.BarDataSet;
import com.github.testpress.mikephil.charting.data.BarEntry;
import com.github.testpress.mikephil.charting.formatter.PercentFormatter;
import com.github.testpress.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.testpress.R;
import in.testpress.testpress.models.Subject;
import in.testpress.testpress.util.GraphAxisLabelFormatter;
import in.testpress.testpress.util.GraphAxisPercentValueFormatter;
import in.testpress.testpress.util.UIUtils;

import static in.testpress.testpress.ui.AnalyticsFragment.SUBJECTS;

public class StrengthAnalyticsGraphFragment extends Fragment {

    @InjectView(R.id.strength_label_layout) LinearLayout strengthLabelLayout;
    @InjectView(R.id.strength_label) TextView strengthLabel;
    @InjectView(R.id.weakness_label_layout) LinearLayout weaknessLabelLayout;
    @InjectView(R.id.weakness_label) TextView weaknessLabel;
    @InjectView(R.id.unanswered_label_layout) LinearLayout unansweredLabelLayout;
    @InjectView(R.id.chart) HorizontalBarChart chart;
    List<Subject> subjects;
    private ExploreSpinnerAdapter spinnerAdapter;
    String selectedCourseCategory = "";
    /**
     * When spinnerAdapter is set to spinner, the spinner itself select first item as default,
     * so onItemSelected callback will be called with position 0, we need to omit this callback
     */
    private Boolean spinnerDefaultCallback = true;
    private int selectedItemPosition = -1;
    ArrayList<IBarDataSet> dataForAllAnswers;
    ArrayList<IBarDataSet> dataForCorrectAnswers;
    ArrayList<IBarDataSet> dataForIncorrectAnswers;
    ArrayList<IBarDataSet> dataForUnansweredAnswers;

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
        spinnerAdapter = new ExploreSpinnerAdapter(getActivity().getLayoutInflater(),
                getActivity().getResources(), true);
        spinnerAdapter.hideSpinner(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater
                .inflate(R.layout.subject_strength_analytics_fragment, container, false);
        ButterKnife.inject(this, view);
        getActivity().invalidateOptionsMenu();
        setSelectedSpinnerItem(0);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.filter, menu);
        MenuItem filterMenu = menu.findItem(R.id.filter);
        View actionView = MenuItemCompat.getActionView(filterMenu);
        final View circle = actionView.findViewById(R.id.filter_applied_sticky_tick);
        Spinner spinner = (Spinner) actionView.findViewById(R.id.spinner);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((Spinner) spinner).setBackgroundTintList(ContextCompat.getColorStateList(
                    getActivity(), R.color.testpress_actionbar_text));
        } else {
            ViewCompat.setBackgroundTintList(spinner, ContextCompat.getColorStateList(
                    getActivity(), R.color.testpress_actionbar_text));
        }
        spinnerDefaultCallback = true;
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                if (position == 0) {
                    circle.setVisibility(View.GONE);
                } else {
                    circle.setVisibility(View.VISIBLE);
                }
                if (spinnerDefaultCallback) {
                    spinnerDefaultCallback = false;
                } else if ((selectedItemPosition != position)) {
                    // Omit callback if position is already selected position
                    selectedItemPosition = position;
                    selectedCourseCategory = spinnerAdapter.getTag(position);
                    setSelectedSpinnerItem(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spinnerAdapter.clear();
        spinnerAdapter.addItem(getString(R.string.all), getString(R.string.all), false, 0);
        spinnerAdapter.addItem(getString(R.string.correct), getString(R.string.correct), true, 0);
        spinnerAdapter.addItem(getString(R.string.incorrect), getString(R.string.incorrect), true, 0);
        spinnerAdapter.addItem(getString(R.string.unanswered), getString(R.string.unanswered), true, 0);
        if (selectedItemPosition == -1) {
            selectedItemPosition = 0;
            spinnerAdapter.notifyDataSetChanged();
        } else {
            spinnerAdapter.notifyDataSetChanged();
            spinner.setSelection(selectedItemPosition);
        }
        filterMenu.setVisible(true);
    }

    private ArrayList<IBarDataSet> getDataForAllAnswers() {
        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();
        for (int i = 1; i <= subjects.size(); i++) {
            Subject subject = subjects.get(i-1);
            entries.add(new BarEntry(i, new float[]{subject.getCorrectPercentage(),
                    subject.getIncorrectPercentage(), subject.getUnansweredPercentage()}));
        }
        BarDataSet barDataSet = new BarDataSet(entries, "");
        List<Integer> cols = Arrays.asList(
                ContextCompat.getColor(getContext(), R.color.green_correct),
                ContextCompat.getColor(getContext(), R.color.red_incorrect),
                ContextCompat.getColor(getContext(), R.color.yellow)
        );
        barDataSet.setColors(cols);
        ArrayList<IBarDataSet> sets = new ArrayList<IBarDataSet>();
        sets.add(barDataSet);
        return sets;
    }

    private ArrayList<IBarDataSet> getDataForCorrectAnswers() {
        ArrayList<BarEntry> correctPercentageEntries = new ArrayList<BarEntry>();
        for (int i = 1; i <= subjects.size(); i++) {
            Subject subject = subjects.get(i-1);
            correctPercentageEntries.add(new BarEntry(i, subject.getCorrectPercentage()));
        }
        ArrayList<IBarDataSet> sets = new ArrayList<IBarDataSet>();
        sets.add(getBarDataSet(correctPercentageEntries, R.color.green_correct));
        return sets;
    }

    private ArrayList<IBarDataSet> getDataForIncorrectAnswers() {
        ArrayList<BarEntry> incorrectPercentageEntries = new ArrayList<BarEntry>();
        for (int i = 1; i <= subjects.size(); i++) {
            Subject subject = subjects.get(i-1);
            incorrectPercentageEntries.add(new BarEntry(i, subject.getIncorrectPercentage()));
        }
        ArrayList<IBarDataSet> sets = new ArrayList<IBarDataSet>();
        sets.add(getBarDataSet(incorrectPercentageEntries, R.color.red_incorrect));
        return sets;
    }

    private ArrayList<IBarDataSet> getDataForUnansweredAnswers() {
        ArrayList<BarEntry> unansweredPercentageEntries = new ArrayList<BarEntry>();
        for (int i = 1; i <= subjects.size(); i++) {
            Subject subject = subjects.get(i-1);
            unansweredPercentageEntries.add(new BarEntry(i, subject.getUnansweredPercentage()));
        }
        ArrayList<IBarDataSet> sets = new ArrayList<IBarDataSet>();
        sets.add(getBarDataSet(unansweredPercentageEntries, R.color.yellow));
        return sets;
    }

    public void setSelectedSpinnerItem(int position) {
        switch (position) {
            case 0:
                strengthLabel.setText(getString(R.string.strength));
                weaknessLabel.setText(getString(R.string.weakness));
                UIUtils.setVisible(new View[] {strengthLabelLayout, weaknessLabelLayout,
                        unansweredLabelLayout});
                if (dataForAllAnswers == null) {
                    dataForAllAnswers = getDataForAllAnswers();
                }
                populateChart(chart, dataForAllAnswers, true);
                break;
            case 1:
                strengthLabel.setText(getString(R.string.strength_label));
                strengthLabelLayout.setVisibility(View.VISIBLE);
                UIUtils.setGone(new View[] {weaknessLabelLayout, unansweredLabelLayout});
                if (dataForCorrectAnswers == null) {
                    dataForCorrectAnswers = getDataForCorrectAnswers();
                }
                populateChart(chart, dataForCorrectAnswers, false);
                break;
            case 2:
                weaknessLabel.setText(getString(R.string.weakness_label));
                weaknessLabelLayout.setVisibility(View.VISIBLE);
                UIUtils.setGone(new View[] {strengthLabelLayout, unansweredLabelLayout});
                if (dataForIncorrectAnswers == null) {
                    dataForIncorrectAnswers = getDataForIncorrectAnswers();
                }
                populateChart(chart, dataForIncorrectAnswers, false);
                break;
            case 3:
                UIUtils.setGone(new View[] {strengthLabelLayout, weaknessLabelLayout});
                unansweredLabelLayout.setVisibility(View.VISIBLE);
                if (dataForUnansweredAnswers == null) {
                    dataForUnansweredAnswers = getDataForUnansweredAnswers();
                }
                populateChart(chart, dataForUnansweredAnswers, false);
                break;
        }
    }

    protected void populateChart(HorizontalBarChart chart, ArrayList<IBarDataSet> sets, boolean all) {
        BarData data = new BarData(sets);
        data.setValueTypeface(Typeface.DEFAULT_BOLD);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(subjects.size() + 2, true);
        xAxis.setDrawGridLines(false);
        ArrayList<String> labels = new ArrayList<String>();
        labels.add("");
        for (Subject subject : subjects) {
            labels.add(subject.getName());
        }
        labels.add("");
        xAxis.setTextSize(14);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setXOffset(10);
        xAxis.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
        xAxis.setValueFormatter(new GraphAxisLabelFormatter(labels, 1));
        xAxis.setAxisMinValue(0);
        xAxis.setAxisMaxValue(subjects.size() + 1);
        chart.setMinimumHeight(Math.max(200, subjects.size() * 100));
        data.setBarWidth(0.5f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setAxisMaxValue(100f);
        leftAxis.setSpaceTop(15f);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setAxisMinValue(0f);
        rightAxis.setAxisMaxValue(100f);

        if (all) {
            data.setValueTextSize(10);
            data.setValueFormatter(new GraphAxisPercentValueFormatter());
            xAxis.setDrawAxisLine(false);
            rightAxis.setDrawLabels(false);
            rightAxis.setDrawAxisLine(false);
            rightAxis.setDrawGridLines(false);
            chart.setDrawValueAboveBar(false);
            chart.setExtraOffsets(0, 0, 30, 0);
        } else {
            data.setValueTextSize(12);
            data.setValueFormatter(new PercentFormatter());
            xAxis.setDrawAxisLine(true);
            rightAxis.setLabelCount(5, false);
            rightAxis.setTextSize(10);
            rightAxis.setDrawLabels(true);
            rightAxis.setDrawAxisLine(true);
            rightAxis.setDrawGridLines(true);
            rightAxis.setTextColor(ContextCompat.getColor(getActivity(), R.color.graph_text));
            rightAxis.setGridColor(Color.parseColor("#cccccc"));
            chart.setDrawValueAboveBar(true);
            chart.setExtraOffsets(0, 0, 50, 0);
        }

        chart.setData(data);
        chart.setDescription("");
        chart.setFitBars(true);
        chart.setTouchEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateY(500);
        chart.invalidate();
    }

    public BarDataSet getBarDataSet(ArrayList<BarEntry> correctPercentageEntries,
                                    @ColorRes int colorRes) {
        BarDataSet barDataSet = new BarDataSet(correctPercentageEntries, "");
        barDataSet.setColor(ContextCompat.getColor(getContext(), colorRes));
        barDataSet.setValueTextColor(ContextCompat.getColor(getContext(), colorRes));
        return barDataSet;
    }

}
