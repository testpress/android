package in.testpress.testpress.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.testpress.mikephil.charting.charts.PieChart;
import com.github.testpress.mikephil.charting.data.PieData;
import com.github.testpress.mikephil.charting.data.PieDataSet;
import com.github.testpress.mikephil.charting.data.PieEntry;
import com.github.testpress.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.models.Subject;

class IndividualSubjectAnalyticsGraphAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Subject> subjects = new ArrayList<>();
    private Context context;

    IndividualSubjectAnalyticsGraphAdapter(Context context, List<Subject> subjects) {
        this.context = context;
        this.subjects = subjects;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        PieChart chart;
        TextView subjectName;
        TextView correct;
        TextView incorrect;
        TextView unanswered;
        TextView total;
        TextView correctPercentage;
        TextView incorrectPercentage;
        TextView unansweredPercentage;
        View divider;

        ViewHolder(View convertView) {
            super(convertView);
            divider = convertView.findViewById(R.id.divider);
            chart = (PieChart) convertView.findViewById(R.id.chart);
            subjectName = ((TextView) convertView.findViewById(R.id.subject_name));
            correct = ((TextView) convertView.findViewById(R.id.correct));
            incorrect = ((TextView) convertView.findViewById(R.id.incorrect));
            unanswered = ((TextView) convertView.findViewById(R.id.unanswered));
            total = ((TextView) convertView.findViewById(R.id.total));
            correctPercentage = ((TextView) convertView.findViewById(R.id.correct_percentage));
            incorrectPercentage = ((TextView) convertView.findViewById(R.id.incorrect_percentage));
            unansweredPercentage = ((TextView) convertView.findViewById(R.id.unanswered_percentage));
        }
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.individual_subject_analytics_graph_list_item, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ViewHolder) {
            final ViewHolder holder = (ViewHolder) viewHolder;
            Subject subject = subjects.get(position);
            holder.subjectName.setText(subject.getName());
            holder.correct.setText(subject.getCorrect().toString());
            holder.incorrect.setText(subject.getIncorrect().toString());
            holder.unanswered.setText(subject.getUnanswered().toString());
            holder.total.setText(subject.getTotal().toString());
            holder.correctPercentage
                    .setText(String.format("%.2f%s", subject.getCorrectPercentage(), "%"));
            holder.incorrectPercentage
                    .setText(String.format("%.2f%s", subject.getIncorrectPercentage(), "%"));
            holder.unansweredPercentage
                    .setText(String.format("%.2f%s", subject.getUnansweredPercentage(), "%"));
            if (position == 0) {
                holder.divider.setVisibility(View.GONE);
            }
            PieData data = getBarData(subject);
            holder.chart.setData(data);
            holder.chart.setDescription("");
            holder.chart.setClickable(false);
            holder.chart.setTouchEnabled(false);
            holder.chart.setUsePercentValues(true);
            holder.chart.setHoleRadius(48f);
            holder.chart.setTransparentCircleRadius(51f);
            holder.chart.setExtraOffsets(0, 0, 0, 0);
            holder.chart.setTransparentCircleColor(Color.WHITE);
            holder.chart.setTransparentCircleAlpha(110);
            holder.chart.getLegend().setEnabled(false);
        }
    }

    private PieData getBarData(Subject subject) {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        entries.add(new PieEntry(subject.getCorrectPercentage(), 0));
        entries.add(new PieEntry(subject.getIncorrectPercentage(), 1));
        entries.add(new PieEntry(subject.getUnansweredPercentage(), 2));

        PieDataSet dataSet = new PieDataSet(entries, subject.getName());
        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(ContextCompat.getColor(context, R.color.green_correct));
        colors.add(ContextCompat.getColor(context, R.color.red_incorrect));
        colors.add(ContextCompat.getColor(context, R.color.yellow));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(14);
        data.setValueFormatter(new PercentFormatter());
        data.setDrawValues(false);
        return data;
    }

}
