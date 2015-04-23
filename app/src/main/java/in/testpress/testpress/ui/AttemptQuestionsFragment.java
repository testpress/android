package in.testpress.testpress.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import in.testpress.testpress.R;

import butterknife.InjectView;
import in.testpress.testpress.R.id;
import in.testpress.testpress.models.AttemptAnswer;
import in.testpress.testpress.models.AttemptItem;
import in.testpress.testpress.models.AttemptQuestion;

public class AttemptQuestionsFragment extends Fragment {
    AttemptItem attemptItem;
    Integer index;
    String url;
    Bitmap bmp = null;

    @InjectView(id.question) TextView questionsView;
    @InjectView(id.question_index) TextView questionIndex;
    @InjectView(id.answers) RadioGroup answersView;
    @InjectView(id.review) CheckBox review;
    @InjectView(id.answers_checkbox) ViewGroup answersCheckboxView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.attemptItem = getArguments().getParcelable("attemptItem");
        this.index = getArguments().getInt("question_index");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        List<AttemptAnswer> attemptAnswers = attemptItem.getAttemptQuestion().getAttemptAnswers();
        final AttemptQuestion attemptQuestion = attemptItem.getAttemptQuestion();

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.attempt_question_fragment, container, false);
        ButterKnife.inject(this,view);

        Context c = getActivity().getApplicationContext();

        questionIndex.setText(index + ".");
        Spanned htmlSpan = Html.fromHtml(attemptQuestion.getQuestionHtml(), new URLImageParser(questionsView, c), null);
        questionsView.setText(trim(htmlSpan, 0, htmlSpan.length()));

//        Spanned htmlSpan = Html.fromHtml(attemptQuestion.getQuestionHtml(), new ImageGetter(), null);

//        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
//                .cacheOnDisc(true).cacheInMemory(true)
//                .imageScaleType(ImageScaleType.EXACTLY)
//                .displayer(new FadeInBitmapDisplayer(300)).build();
//
//        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity())
//                .defaultDisplayImageOptions(defaultOptions)
//                .memoryCache(new WeakMemoryCache())
//                .discCacheSize(100 * 1024 * 1024).build();
//
//        ImageLoader.getInstance().init(config);
//        ImageLoader imageLoader = ImageLoader.getInstance();
//
//        imageLoader.loadImage(url, new SimpleImageLoadingListener() {
//            @Override
//            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                bmp = loadedImage;
//                questionsView.setText(Html.fromHtml(attemptQuestion.getQuestionHtml(), new ImageGetter(), null));
//            }
//        });
        String type = attemptItem.getAttemptQuestion().getType();
        switch (type) {
            case "R": creareRadioButtonView(attemptAnswers, attemptQuestion);
                break;
            case "C": createCheckBoxView(attemptAnswers, attemptQuestion);
                break;
            default:break;

        }
        try {
            review.setChecked(attemptItem.getReview());
        }
        catch (Exception e) {
            attemptItem.setReview(false);
        }
        attemptItem.saveAnswers(attemptItem.getSelectedAnswers());
        attemptItem.setCurrentReview(attemptItem.getReview());
        return view;
    }

    @OnCheckedChanged(id.review) void onChecked(boolean checked) {
        attemptItem.setCurrentReview(checked);
        if(checked)questionsView.setBackgroundColor(Color.LTGRAY);
        else questionsView.setBackgroundColor(android.R.color.transparent);
    }

    public class URLImageParser implements Html.ImageGetter {
        Context c;
        View container;

        /***
         * Construct the URLImageParser which will execute AsyncTask and refresh the container
         * @param t
         * @param c
         */
        public URLImageParser(View t, Context c) {
            this.c = c;
            this.container = t;
        }
        public class URLDrawable extends BitmapDrawable {
            // the drawable that you need to set, you could set the initial drawing
            // with the loading image if you need to
            protected Drawable drawable;

            @Override
            public void draw(Canvas canvas) {
                // override the draw to facilitate refresh function later
                if(drawable != null) {
                    drawable.draw(canvas);
                }
            }
        }

        public Drawable getDrawable(String source) {
            URLDrawable urlDrawable = new URLDrawable();

            // get the actual source
            ImageGetterAsyncTask asyncTask =
                    new ImageGetterAsyncTask( urlDrawable);

            asyncTask.execute(source);

            // return reference to URLDrawable where I will change with actual image from
            // the src tag
            return urlDrawable;
        }

        public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
            URLDrawable urlDrawable;

            public ImageGetterAsyncTask(URLDrawable d) {
                this.urlDrawable = d;
            }

            @Override
            protected Drawable doInBackground(String... params) {
                String source = params[0];
                return fetchDrawable(source);
            }

            @Override
            protected void onPostExecute(Drawable result) {
                // set the correct bound according to the result from HTTP call
                urlDrawable.setBounds(0, 0, 0 + result.getIntrinsicWidth(), 0
                        + result.getIntrinsicHeight());

                // change the reference of the current drawable to the result
                // from the HTTP call
                urlDrawable.drawable = result;

                // redraw the image by invalidating the container
                URLImageParser.this.container.invalidate();
            }

            /***
             * Get the Drawable from URL
             * @param urlString
             * @return
             */
            public Drawable fetchDrawable(String urlString) {
                try {
                    InputStream is = fetch(urlString);
                    Drawable drawable = Drawable.createFromStream(is, "src");
                    drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(), 0
                            + drawable.getIntrinsicHeight());
                    return drawable;
                } catch (Exception e) {
                    return null;
                }
            }

            private InputStream fetch(String urlString) throws MalformedURLException, IOException {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet request = new HttpGet(urlString);
                HttpResponse response = httpClient.execute(request);
                return response.getEntity().getContent();
            }
        }
    }

    public void createCheckBoxView(List<AttemptAnswer> attemptAnswers, AttemptQuestion attemptQuestion) {
        final List<Integer> savedAnswers = new ArrayList<Integer>();
        for(int i = 0 ; i < attemptQuestion.getAttemptAnswers().size() ; i++) {

            final CheckBox option = new CheckBox(getActivity());
            option.setId(i);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            option.setLayoutParams(layoutParams);
            option.setText(Html.fromHtml(attemptAnswers.get(i).getTextHtml()));
            option.setButtonDrawable(android.R.color.transparent);
            option.setPadding(25, 10, 0, 0);
            List<Integer> selectedAnswers = attemptItem.getSelectedAnswers();

            if(!selectedAnswers.isEmpty()) {
                if (selectedAnswers.get(0).equals(attemptAnswers.get(i).getId())) {
                    option.setChecked(true);
                    option.setBackgroundColor(Color.parseColor("#66FF99"));
                }
            }
            answersCheckboxView.addView(option);
            option.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    List<AttemptAnswer> attemptAnswers = attemptItem.getAttemptQuestion().getAttemptAnswers();
                    if(checked) {
                        compoundButton.setBackgroundColor(Color.parseColor("#66FF99"));
                        savedAnswers.add(attemptAnswers.get(compoundButton.getId()).getId());

                    }
                    else {compoundButton.setBackgroundColor(android.R.color.transparent);
                        savedAnswers.remove(attemptAnswers.get(compoundButton.getId()).getId());}
                    attemptItem.saveAnswers(savedAnswers);
                }
            });
        }
    }

    // http://stackoverflow.com/a/16745540/400236
    public static CharSequence trim(CharSequence s, int start, int end) {
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end);
    }

    public void creareRadioButtonView(List<AttemptAnswer> attemptAnswers, AttemptQuestion attemptQuestion) {
        for(int i = 0 ; i < attemptQuestion.getAttemptAnswers().size() ; i++) {
            LayoutInflater inflater;
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            RadioButton option = (RadioButton) inflater.inflate(R.layout.attempt_radio_button_fragment ,
                    null);
            //final RadioButton option = new RadioButton(getActivity());
            option.setId(i);
            //LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            //option.setLayoutParams(layoutParams);
            //option.setHt
            Spanned html = Html.fromHtml(attemptAnswers.get(i).getTextHtml());
            option.setText(trim(html, 0, html.length()));
            //Log.e("AttemptQuestionsFragment", Html.fromHtml(attemptAnswers.get(i).getTextHtml()));
            //option.setButtonDrawable(android.R.color.transparent);
            //option.setPadding(25, 10, 0, 0);
            List<Integer> selectedAnswers = attemptItem.getSelectedAnswers();

            if(!selectedAnswers.isEmpty()) {
                if (selectedAnswers.get(0).equals(attemptAnswers.get(i).getId())) {
                    option.setChecked(true);
                    //option.setBackgroundColor(Color.parseColor("#66FF99"));
                }
            }
            answersView.addView(option);
            option.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    List<AttemptAnswer> attemptAnswers = attemptItem.getAttemptQuestion().getAttemptAnswers();
                    if(checked) {
                        //compoundButton.setBackgroundColor(Color.parseColor("#66FF99"));
                        List<Integer> savedAnswers = new ArrayList<Integer>();
                        savedAnswers.add(attemptAnswers.get(compoundButton.getId()).getId());
                        attemptItem.saveAnswers(savedAnswers);
                    }
                    //else compoundButton.setBackgroundColor(android.R.color.transparent);
                }
            });
        }
    }

}
