package in.testpress.testpress.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.models.Subject;
import in.testpress.testpress.ui.view.MaterialRippleLayout;

public class SubjectAnswersCountListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    Activity activity;
    private List<Subject> subjects = new ArrayList<>();

    public SubjectAnswersCountListAdapter(Activity activity, final List<Subject> subjects) {
        this.activity = activity;
        this.subjects = subjects;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialRippleLayout answersCountListItem;
        TextView subjectName;
        TextView correct;
        TextView incorrect;
        TextView unanswered;
        ImageView rightDirectionArrow;

        public ViewHolder(View convertView) {
            super(convertView);
            answersCountListItem = ((MaterialRippleLayout) convertView.findViewById(R.id.ripple_layout));
            subjectName = ((TextView) convertView.findViewById(R.id.subject_name));
            correct = ((TextView) convertView.findViewById(R.id.correct));
            incorrect = ((TextView) convertView.findViewById(R.id.incorrect));
            unanswered = ((TextView) convertView.findViewById(R.id.unanswered));
            rightDirectionArrow = ((ImageView) convertView.findViewById(R.id.right_direction_arrow));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return subjects.size() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.subject_answers_count_list_header, parent, false);
            return new RecyclerView.ViewHolder(v) {};
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.subject_answers_count_list_item, parent, false);
            return new ViewHolder(v);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ViewHolder) {
            final ViewHolder holder = (ViewHolder) viewHolder;
            final Subject subject = subjects.get(position - 1);
            holder.subjectName.setText(subject.getName());
            holder.correct.setText(subject.getCorrect().toString());
            holder.incorrect.setText(subject.getIncorrect().toString());
            holder.unanswered.setText(subject.getUnanswered().toString());
            if (subject.isLeaf()) {
                holder.rightDirectionArrow.setVisibility(View.INVISIBLE);
                holder.answersCountListItem.setEnabled(false);
            } else {
                holder.rightDirectionArrow.setVisibility(View.VISIBLE);
                holder.answersCountListItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity, AnalyticsActivity.class);
                        intent.putExtra(AnalyticsFragment.SUBJECT, subject);
                        activity.startActivity(intent);
                    }
                });
            }
        }
    }

}