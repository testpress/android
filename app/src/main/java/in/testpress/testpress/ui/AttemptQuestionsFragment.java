package in.testpress.testpress.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import in.testpress.testpress.R;

import butterknife.InjectView;
import in.testpress.testpress.R.id;
import in.testpress.testpress.models.Answer;
import in.testpress.testpress.models.AttemptQuestion;
import in.testpress.testpress.models.Question;

public class AttemptQuestionsFragment extends Fragment {
    AttemptQuestion attemptQuestion;
    String url;
    Bitmap bmp = null;

    @InjectView(id.question) TextView questionsView;
    @InjectView(id.answers) RadioGroup answersView;
    @InjectView(id.review) CheckBox review;
    @InjectView(id.answers_checkbox) ViewGroup answersCheckboxView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.attemptQuestion = getArguments().getParcelable("question");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        List<Answer> answers = attemptQuestion.getQuestion().getAnswers();
        final Question question = attemptQuestion.getQuestion();

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_user_exam_questions, container, false);
        ButterKnife.inject(this,view);

        Spanned htmlSpan = Html.fromHtml(question.getQuestionHtml(), new ImageGetter(), null);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.loadImage(url, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                bmp = loadedImage;
                questionsView.setText(Html.fromHtml(question.getQuestionHtml(), new ImageGetter(), null));
            }
        });
        String type = attemptQuestion.getQuestion().getType();
        switch (type) {
            case "R": creareRadioButtonView(answers, question);
                break;
            case "C": createCheckBoxView(answers, question);
                break;
            default:break;

        }
        return view;
    }

    @OnCheckedChanged(id.review) void onChecked(boolean checked) {
        if(checked)questionsView.setBackgroundColor(Color.LTGRAY);
        else questionsView.setBackgroundColor(android.R.color.transparent);
    }

    private class ImageGetter implements Html.ImageGetter {
        Drawable d = null;
        public Drawable getDrawable(String source) {
            if(source != null) {
                url = source;
                if(bmp != null) {
                    d = new BitmapDrawable(bmp);
                    d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
                }
            }
            return d;
        }
    }

    public void createCheckBoxView(List<Answer> answers, Question question) {
        final List<Integer> savedAnswers = new ArrayList<Integer>();
        for(int i = 0 ; i < question.getAnswers().size() ; i++) {

            final CheckBox option = new CheckBox(getActivity());
            option.setId(i);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            option.setLayoutParams(layoutParams);
            option.setText(Html.fromHtml(answers.get(i).getTextHtml()));
            option.setButtonDrawable(android.R.color.transparent);
            option.setPadding(25, 10, 0, 0);
            List<Integer> selectedAnswers = attemptQuestion.getSelectedAnswers();

            if(!selectedAnswers.isEmpty()) {
                if (selectedAnswers.get(0).equals(answers.get(i).getId())) {
                    option.setChecked(true);
                    option.setBackgroundColor(Color.parseColor("#66FF99"));
                }
            }
            answersCheckboxView.addView(option);
            option.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    List<Answer> answers = attemptQuestion.getQuestion().getAnswers();
                    if(checked) {
                        compoundButton.setBackgroundColor(Color.parseColor("#66FF99"));
                        savedAnswers.add(answers.get(compoundButton.getId()).getId());

                    }
                    else {compoundButton.setBackgroundColor(android.R.color.transparent);
                        savedAnswers.remove(answers.get(compoundButton.getId()).getId());}
                    attemptQuestion.saveAnswers(savedAnswers);
                }
            });
        }
    }

    public void creareRadioButtonView(List<Answer> answers, Question question) {
        for(int i = 0 ; i < question.getAnswers().size() ; i++) {
            final RadioButton option = new RadioButton(getActivity());
            option.setId(i);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            option.setLayoutParams(layoutParams);
            option.setText(Html.fromHtml(answers.get(i).getTextHtml()));
            option.setButtonDrawable(android.R.color.transparent);
            option.setPadding(25, 10, 0, 0);
            List<Integer> selectedAnswers = attemptQuestion.getSelectedAnswers();

            if(!selectedAnswers.isEmpty()) {
                if (selectedAnswers.get(0).equals(answers.get(i).getId())) {
                    option.setChecked(true);
                    option.setBackgroundColor(Color.parseColor("#66FF99"));
                }
            }
            answersView.addView(option);
            option.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    List<Answer> answers = attemptQuestion.getQuestion().getAnswers();
                    if(checked) {
                        compoundButton.setBackgroundColor(Color.parseColor("#66FF99"));
                        List<Integer> savedAnswers = new ArrayList<Integer>();
                        savedAnswers.add(answers.get(compoundButton.getId()).getId());
                        attemptQuestion.saveAnswers(savedAnswers);
                    }
                    else compoundButton.setBackgroundColor(android.R.color.transparent);
                }
            });
        }
    }

}
