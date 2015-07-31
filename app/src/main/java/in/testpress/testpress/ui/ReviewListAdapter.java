package in.testpress.testpress.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import in.testpress.testpress.R;
import in.testpress.testpress.models.ReviewAnswer;
import in.testpress.testpress.models.ReviewItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReviewListAdapter extends SingleTypeAdapter<ReviewItem> {

    private LayoutInflater inflater;
    Activity activity;
    ImageLoader imageLoader;
    HashMap<String, ArrayList<String>> answerImagesUrl = new HashMap<>();
    static HashMap<String,Bitmap> answerImages= new HashMap<>();
    static int noOfImages=0,optionNo=0,holder=1;

    /**
     * @param inflater
     * @param items
     */
    public ReviewListAdapter(final int layoutId, final LayoutInflater inflater,
                             final List<ReviewItem> items, Activity activity) {
        super(inflater, layoutId);
        this.inflater = inflater;
        setItems(items);
        this.activity = activity;
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(activity)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.question, R.id.answer, R.id.explanation };
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

    @Override
    protected void update(final int position, final ReviewItem item) {
        Spanned html = Html.fromHtml(item.getReviewQuestion().getQuestionHtml().replaceAll("\n", ""), new ImageGetter(item), null);
        setText(0, trim(html, 0, html.length()));

        final String explanation = item.getReviewQuestion().getExplanationHtml().replaceAll("\n", "");
        if (explanation.equals("")) {
            updater.view.findViewById(R.id.explanation_heading).setVisibility(View.GONE);
            updater.view.findViewById(R.id.explanation).setVisibility(View.GONE);
        } else {
            html = Html.fromHtml(explanation, new ImageGetter(item), null);
            setText(2, trim(html, 0, html.length()));
        }

        if(item.getImages().size() > 0) {
            Spanned htmlQuestion = Html.fromHtml(item.getReviewQuestion().getQuestionHtml().replaceAll("\n", ""), new ImageSetter(item), null);
            Spanned htmlExplanation = Html.fromHtml(explanation, new ImageSetter(item), null);;
            setText(0, clickableText(htmlQuestion));
            setText(2, clickableText(htmlExplanation));
            TextView question = textView(0);
            TextView explanationView = textView(2);
            question.setMovementMethod(LinkMovementMethod.getInstance());
            explanationView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        else {
            for (int j = 0; j < item.getImageUrl().size(); j++) {
                imageLoader.loadImage(item.getImageUrl().get(j), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        item.setImages(imageUri, loadedImage);
                        if (item.getImages().size() == item.getImageUrl().size()) {
                            Spanned htmlQuestion = Html.fromHtml(item.getReviewQuestion().getQuestionHtml().replaceAll("\n", ""), new ImageSetter(item), null);
                            Spanned htmlExplanation = Html.fromHtml(explanation, new ImageSetter(item), null);
                            setText(0, clickableText(htmlQuestion));
                            setText(2, clickableText(htmlExplanation));
                            TextView question = textView(0);
                            TextView explanationView = textView(2);
                            question.setMovementMethod(LinkMovementMethod.getInstance());
                            explanationView.setMovementMethod(LinkMovementMethod.getInstance());
                        }
                    }
                });
            }
        }

//        String explanation = item.getReviewQuestion().getExplanationHtml().replaceAll("\n", "");
//        if (explanation.equals("")) {
//            updater.view.findViewById(R.id.explanation_heading).setVisibility(View.GONE);
//            updater.view.findViewById(R.id.explanation).setVisibility(View.GONE);
//        } else {
//            html = Html.fromHtml(explanation);
//            setText(2, trim(html, 0, html.length()));
//        }
        noOfImages=0;optionNo = 0;
        ((TextView)updater.view.findViewById(R.id.question_index)).setText((position + 1) + ".");
        LinearLayout correctAnswersView = (LinearLayout)updater.view.findViewById(R.id.correct_answer);
        //Clear all children first else it keeps appending old items
        correctAnswersView.removeAllViews();
        LinearLayout answersView = (LinearLayout)updater.view.findViewById(R.id.answer);
        //Clear all children first else it keeps appending old items
        answersView.removeAllViews();
        List<ReviewAnswer> answers = item.getReviewQuestion().getAnswers();
        for(int i = 0 ; i < answers.size() ; i++) {
            final int questionNo=position + 1;
            final ReviewAnswer answer = answers.get(i);
            View option = inflater.inflate(R.layout.review_answer, null);
            html = Html.fromHtml(answer.getTextHtml(), new AnswerImageGetter(i), null);
            final TextView answerText = (TextView) option.findViewById(R.id.answer_text);
            TextView optionText = (TextView) option.findViewById(R.id.option);
            optionText.setText("" + (char) (i + 97));
            answerText.setText(trim(html, 0, html.length()));
            for(int j = 0; j < answerImagesUrl.get(i+"").size() ; j++) {
                if(answerImages.containsKey(answerImagesUrl.get(i+"").get(j))){
                    if(questionNo!=holder) {
                        optionNo = 0;
                        holder = questionNo;
                    }
                    noOfImages++;
                    if(answerImagesUrl.get(optionNo+"").size()==noOfImages){
                        html = Html.fromHtml(answer.getTextHtml(), new AnswerImageSetter(), null);
                        answerText.setText(clickableText(html));
                        answerText.setMovementMethod(LinkMovementMethod.getInstance());
                        optionNo++;
                        noOfImages=0;
                    }
                }else{
                    imageLoader.loadImage(answerImagesUrl.get(i + "").get(j), new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            answerImages.put(imageUri, loadedImage);
                            if (questionNo != holder) {
                                optionNo = 0;
                                holder = questionNo;
                            }
                            noOfImages++;
                            if (answerImagesUrl.get(optionNo + "").size() == noOfImages) {
                                Spanned html = Html.fromHtml(answer.getTextHtml(), new AnswerImageSetter(), null);
                                answerText.setText(clickableText(html));
                                answerText.setMovementMethod(LinkMovementMethod.getInstance());
                                optionNo++;
                                noOfImages = 0;
                            }
                        }
                    });
                }
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 20);
            answersView.addView(option);
            option.setLayoutParams(params);
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (item.getSelectedAnswers().contains(answer.getId()) == true) {
                if (answer.getIsCorrect() == true) {
                    if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        optionText.setBackgroundResource(R.drawable.round_green_background);
                    } else {
                        optionText.setBackground(inflater.getContext().getResources().getDrawable(R.drawable.round_green_background));
                    }
                } else {
                    if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        optionText.setBackgroundResource(R.drawable.round_red_background);
                    } else {
                        optionText.setBackground(inflater.getContext().getResources().getDrawable(R.drawable.round_red_background));
                    }
                }
            }
            if (answer.getIsCorrect() == true) {
                TextView correctOption = new TextView(inflater.getContext());
                int hw = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, inflater.getContext().getResources().getDisplayMetrics());
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(hw, hw);
                textParams.setMargins(0, 0, 5, 0);
                correctOption.setLayoutParams(textParams);
                correctOption.setGravity(Gravity.CENTER);
                correctOption.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                correctOption.setTextColor(Color.parseColor("#FFFFFF"));
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    correctOption.setBackgroundResource(R.drawable.round_background);
                } else {
                    correctOption.setBackground(inflater.getContext().getResources().getDrawable(R.drawable.round_background));
                }
                correctOption.setTypeface(Typeface.DEFAULT_BOLD);
                correctOption.setText("" + (char) (i + 97));
                correctOption.setVisibility(View.VISIBLE);
                correctAnswersView.addView(correctOption);
            }
        }
    }

    private  SpannableString clickableText(Spanned spanned){

        SpannableString span = new SpannableString(trim(spanned, 0, spanned.length()));
        ImageSpan[] spans = span.getSpans(0, span.length(), ImageSpan.class);
        for(int i = 0; i < spans.length; i++) {
            MyClickableSpan clickableSpan = new MyClickableSpan(spans[i].getDrawable());
            span.setSpan(clickableSpan, span.getSpanStart(spans[i]), span.getSpanStart(spans[i])+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    public class MyClickableSpan extends ClickableSpan {
        Drawable drawable;
        MyClickableSpan(Drawable drawable){
            this.drawable= drawable;
        }

        @Override
        public void onClick(View textView) {
            final DialogAlert dialog = new DialogAlert(activity,drawable);
            dialog.show();
        }
    }

    static public class DialogAlert extends Dialog {
        Drawable drawable;
        public DialogAlert(Context context,Drawable drawable) {
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
        ReviewItem item;
        ImageGetter(ReviewItem item) {
            this.item = item;
        }
        public Drawable getDrawable(String source) {
            if(source != null) {
               item.setImageUrl(source);
            }
            return drawable;
        }
    }

    private class ImageSetter implements Html.ImageGetter {
        Drawable drawable = null;
        ReviewItem item;

        ImageSetter(ReviewItem item) {
            this.item = item;
        }
        public Drawable getDrawable(String source) {
            if(source != null) {
                if(item.getImages() != null) {
                    try {
                        drawable = new BitmapDrawable(item.getImages().get(source));
                        Display display = activity.getWindowManager().getDefaultDisplay();
                        if(drawable.getIntrinsicWidth()> display.getWidth())
                            drawable.setBounds(0, 0, drawable.getIntrinsicWidth()/2, drawable.getIntrinsicHeight()/2);
                        else
                            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    }
                    catch (Exception e) {}
                }
            }
            return drawable;
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
                        drawable = new BitmapDrawable(answerImages.get(source));
                        Display display = activity.getWindowManager().getDefaultDisplay();
                        if(drawable.getIntrinsicWidth()> display.getWidth())
                            drawable.setBounds(0, 0, drawable.getIntrinsicWidth()/2, drawable.getIntrinsicHeight()/2);
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
