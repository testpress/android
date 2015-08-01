package in.testpress.testpress.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.Display;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.util.DisplayMetrics;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import in.testpress.testpress.R;

import butterknife.InjectView;
import in.testpress.testpress.R.id;
import in.testpress.testpress.models.AttemptAnswer;
import in.testpress.testpress.models.AttemptItem;
import in.testpress.testpress.models.AttemptQuestion;
import in.testpress.testpress.util.Ln;

public class AttemptQuestionsFragment extends Fragment {
    AttemptItem attemptItem;
    Integer index;
    ArrayList<String> url = new ArrayList<>();
    HashMap<String, Drawable> images = new HashMap<>();
    ImageLoader imageLoader;
    HashMap<String, ArrayList<String>> answerImagesUrl = new HashMap<>();
    static HashMap<String,Drawable> answerImages= new HashMap<>();
    static int noOfImages=0,optionNo=0,holder=1;

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
//        Spanned htmlSpan = Html.fromHtml(attemptQuestion.getQuestionHtml(), new URLImageParser(questionsView, c), null);
//        questionsView.setText(trim(htmlSpan, 0, htmlSpan.length()));

        Spanned htmlSpan = Html.fromHtml(attemptQuestion.getQuestionHtml(), new ImageGetter(), null);
        questionsView.setText(trim(htmlSpan, 0, htmlSpan.length()));
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        imageLoader = ImageLoader.getInstance();
        for(int j = 0; j < url.size() ; j++) {
            imageLoader.loadImage(url.get(j), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    Drawable drawable = new BitmapDrawable(loadedImage);
                    images.put(imageUri, drawable);
                    if(images.size() == url.size()) {
                        Spanned htmlSpan = Html.fromHtml(attemptQuestion.getQuestionHtml(), new ImageSetter(), null);
                        questionsView.setText(clickableText(htmlSpan));
                        questionsView.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                }
            });
        }

        String type = attemptItem.getAttemptQuestion().getType();
        switch (type) {
            case "R": createRadioButtonView(attemptAnswers, attemptQuestion);
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
    }

    public class ClickableImageSpan extends ClickableSpan {
        Drawable drawable;
        ClickableImageSpan(Drawable drawable){
            this.drawable= drawable;
        }

        @Override
        public void onClick(View textView) {
            final ImageDialog dialog = new ImageDialog(getActivity(),drawable);
            dialog.show();
        }
    }

   static public class ImageDialog extends Dialog {
        Drawable drawable;
        public ImageDialog(Context context,Drawable drawable) {
            super(context, R.style.ActivityDialog);
            this.drawable = drawable;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.image_view_layout);
            TouchImageView image=(TouchImageView)findViewById(R.id.image);
            image.setImageDrawable(drawable);
        }
    }

    private class ImageGetter implements Html.ImageGetter {
        Drawable drawable = null;
        public Drawable getDrawable(String source) {
            if(source != null) {
                url.add(source);
            }
            return drawable;
        }
    }

    private class ImageSetter implements Html.ImageGetter {
        Drawable drawable = null;
        public Drawable getDrawable(String source) {
            if(source != null) {
                if(images != null) {
                    try {
                        drawable = images.get(source);
                        Display display = getActivity().getWindowManager().getDefaultDisplay();
                        if(drawable.getIntrinsicWidth()> display.getWidth())
                            drawable.setBounds(0, 0, display.getWidth(), (display.getWidth()*drawable.getIntrinsicHeight())/drawable.getIntrinsicWidth());
                        else
                            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                    }
                    catch (Exception e) {}
                }
            }
            return drawable;
        }
    }

//    public class URLImageParser implements Html.ImageGetter {
//        Context c;
//        View container;
//
//        /***
//         * Construct the URLImageParser which will execute AsyncTask and refresh the container
//         * @param t
//         * @param c
//         */
//        public URLImageParser(View t, Context c) {
//            this.c = c;
//            this.container = t;
//        }
//        public class URLDrawable extends BitmapDrawable {
//            // the drawable that you need to set, you could set the initial drawing
//            // with the loading image if you need to
//            protected Drawable drawable;
//
//            @Override
//            public void draw(Canvas canvas) {
//                // override the draw to facilitate refresh function later
//                if(drawable != null) {
//                    drawable.draw(canvas);
//                }
//            }
//        }
//
//        public Drawable getDrawable(String source) {
//            URLDrawable urlDrawable = new URLDrawable();
//
//            // get the actual source
//            ImageGetterAsyncTask asyncTask =
//                    new ImageGetterAsyncTask( urlDrawable);
//
//            asyncTask.execute(source);
//
//            // return reference to URLDrawable where I will change with actual image from
//            // the src tag
//            return urlDrawable;
//        }
//
//        public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
//            URLDrawable urlDrawable;
//
//            public ImageGetterAsyncTask(URLDrawable d) {
//                this.urlDrawable = d;
//            }
//
//            @Override
//            protected Drawable doInBackground(String... params) {
//                String source = params[0];
//                return fetchDrawable(source);
//            }
//
//            @Override
//            protected void onPostExecute(Drawable result) {
//                // set the correct bound according to the result from HTTP call
//                urlDrawable.setBounds(0, 0, 0 + result.getIntrinsicWidth(), 0
//                        + result.getIntrinsicHeight());
//
//                // change the reference of the current drawable to the result
//                // from the HTTP call
//                urlDrawable.drawable = result;
//
//                // redraw the image by invalidating the container
//                URLImageParser.this.container.invalidate();
//            }
//
//            /***
//             * Get the Drawable from URL
//             * @param urlString
//             * @return
//             */
//            public Drawable fetchDrawable(String urlString) {
//                try {
//                    InputStream is = fetch(urlString);
//                    Drawable drawable = Drawable.createFromStream(is, "src");
//                    drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(), 0
//                            + drawable.getIntrinsicHeight());
//                    return drawable;
//                } catch (Exception e) {
//                    return null;
//                }
//            }
//
//            private InputStream fetch(String urlString) throws MalformedURLException, IOException {
//                DefaultHttpClient httpClient = new DefaultHttpClient();
//                HttpGet request = new HttpGet(urlString);
//                HttpResponse response = httpClient.execute(request);
//                return response.getEntity().getContent();
//            }
//        }
//    }

    public void createCheckBoxView(final List<AttemptAnswer> attemptAnswers, AttemptQuestion attemptQuestion) {
        final int questionNo=index;
        final List<Integer> savedAnswers = new ArrayList<Integer>();
        for(int i = 0 ; i < attemptQuestion.getAttemptAnswers().size() ; i++) {
            final String answer  = attemptQuestion.getAttemptAnswers().get(i).getTextHtml();
            final CheckBox option = new CheckBox(getActivity());
            option.setId(i);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            option.setLayoutParams(layoutParams);
            Spanned html = Html.fromHtml(answer, new AnswerImageGetter(i), null);
            option.setText(trim(html, 0, html.length()));
            for(int j = 0; j < answerImagesUrl.get(i+"").size() ; j++) {
                if (answerImages.containsKey(answerImagesUrl.get(i + "").get(j))) {
                    ;
                    if (questionNo != holder) {
                        optionNo = 0;
                        holder = questionNo;
                    }
                    noOfImages++;
                    if (answerImagesUrl.get(optionNo + "").size() == noOfImages) {
                        html = Html.fromHtml(answer, new AnswerImageSetter(), null);
                        option.setText(clickableText(html));
                        option.setMovementMethod(LinkMovementMethod.getInstance());
                        optionNo++;
                        noOfImages = 0;
                    }
                } else {
                    imageLoader.loadImage(answerImagesUrl.get(i + "").get(j), new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            Drawable drawable = new BitmapDrawable(loadedImage);
                            answerImages.put(imageUri, drawable);
                            if (questionNo != holder) {
                                optionNo = 0;
                                holder = questionNo;
                            }
                            noOfImages++;
                            if (answerImagesUrl.get(optionNo + "").size() == noOfImages) {
                                Spanned html = Html.fromHtml(answer, new AnswerImageSetter(), null);
                                option.setText(clickableText(html));
                                option.setMovementMethod(LinkMovementMethod.getInstance());
                                optionNo++;
                                noOfImages = 0;
                            }
                        }
                    });
                }
            }
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

    private SpannableString clickableText(Spanned html){

        SpannableString span = new SpannableString(trim(html, 0, html.length()));
        ImageSpan[] spans = span.getSpans(0, span.length(), ImageSpan.class);
        for(int i = 0; i < spans.length; i++) {
            ClickableImageSpan clickableSpan = new ClickableImageSpan(spans[i].getDrawable());
             span.setSpan(clickableSpan, span.getSpanStart(spans[i]), span.getSpanStart(spans[i])+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    // http://stackoverflow.com/a/16745540/400236
    public static CharSequence trim(CharSequence s, int start, int end) {
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 2))) {
            end--;
        }

        return s.subSequence(start, end);
    }

    public void createRadioButtonView(List<AttemptAnswer> attemptAnswers, AttemptQuestion attemptQuestion) {
        final int questionNo=index;
        for(int i = 0 ; i < attemptQuestion.getAttemptAnswers().size() ; i++) {
            LayoutInflater inflater;
            final String answer  = attemptQuestion.getAttemptAnswers().get(i).getTextHtml();
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final RadioButton option = (RadioButton) inflater.inflate(R.layout.attempt_radio_button_fragment ,
                    null);
            //final RadioButton option = new RadioButton(getActivity());
            option.setId(i);
            //LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            //option.setLayoutParams(layoutParams);
            //option.setHt
            Spanned html = Html.fromHtml(attemptAnswers.get(i).getTextHtml(), new AnswerImageGetter(i), null);
            option.setText(trim(html, 0, html.length()));
            for(int j = 0; j < answerImagesUrl.get(i+"").size() ; j++) {
                if(answerImages.containsKey(answerImagesUrl.get(i+"").get(j))){;
                    if(questionNo!=holder) {
                        optionNo = 0;
                        holder = questionNo;
                    }
                    noOfImages++;
                    if(answerImagesUrl.get(optionNo+"").size()==noOfImages){
                        html = Html.fromHtml(answer, new AnswerImageSetter(), null);
                        option.setText(clickableText(html));
                        option.setMovementMethod(LinkMovementMethod.getInstance());
                        optionNo++;
                        noOfImages=0;
                    }
                }else{
                    imageLoader.loadImage(answerImagesUrl.get(i + "").get(j), new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            Drawable drawable = new BitmapDrawable(loadedImage);
                            answerImages.put(imageUri, drawable);
                            if (questionNo != holder) {
                                optionNo = 0;
                                holder = questionNo;
                            }
                            noOfImages++;
                            if (answerImagesUrl.get(optionNo + "").size() == noOfImages) {
                                Spanned html = Html.fromHtml(answer, new AnswerImageSetter(), null);
                                option.setText(clickableText(html));
                                option.setMovementMethod(LinkMovementMethod.getInstance());
                                optionNo++;
                                noOfImages = 0;
                            }
                        }
                    });
                }
            }
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

    private class AnswerImageGetter implements Html.ImageGetter {
        Drawable drawable = null;
        int i;
        ArrayList<String> temp=new ArrayList<>();
        AnswerImageGetter(int i){
            this.i=i;
            answerImagesUrl.put(i+"",temp);
        }
        public Drawable getDrawable(String source) {
            if(source != null) {
                temp.add(source);
            }
            answerImagesUrl.put(i+"",temp);
            return drawable;
        }
    }

    private class AnswerImageSetter implements Html.ImageGetter {
        Drawable drawable = null;
        public Drawable getDrawable(String source) {
            if(source != null) {
                if(answerImages != null) {
                    try {
                        drawable = answerImages.get(source);
                        Display display = getActivity().getWindowManager().getDefaultDisplay();
                        if(drawable.getIntrinsicWidth()> display.getWidth())
                            drawable.setBounds(0, 0, display.getWidth(),(display.getWidth()*drawable.getIntrinsicHeight())/drawable.getIntrinsicWidth());
                        else
                            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    }
                    catch (Exception e) {}
                }
            }
            return drawable;
        }
    }

}
