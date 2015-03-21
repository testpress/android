package in.testpress.testpress.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import in.testpress.testpress.R;
import in.testpress.testpress.core.PauseTimerEvent;
import in.testpress.testpress.core.ResumeTimerEvent;
import in.testpress.testpress.core.StopTimerEvent;
import in.testpress.testpress.core.TimerPausedEvent;
import in.testpress.testpress.core.TimerService;
import in.testpress.testpress.core.TimerTickEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.InjectView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class BootstrapTimerActivity extends BootstrapFragmentActivity implements View.OnClickListener {

    @Inject Bus eventBus;

    @InjectView(R.id.chronometer) protected TextView chronometer;
    @InjectView(R.id.start) protected Button start;
    @InjectView(R.id.stop) protected Button stop;
    @InjectView(R.id.pause) protected Button pause;
    @InjectView(R.id.resume) protected Button resume;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bootstrap_timer);

        setTitle(R.string.title_timer);

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        pause.setOnClickListener(this);
        resume.setOnClickListener(this);

    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.start:
                startTimer();
                break;
            case R.id.stop:
                produceStopEvent();
                break;
            case R.id.pause:
                producePauseEvent();
                break;
            case R.id.resume:
                produceResumeEvent();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            // Source:
            // http://developer.android.com/training/implementing-navigation/ancestral.html
            // This is the home button in the top left corner of the screen.
            case android.R.id.home:
                final Intent upIntent = NavUtils.getParentActivityIntent(this);
                // If parent is not properly defined in AndroidManifest.xml upIntent will be null
                // TODO hanlde upIntent == null
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                                    // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
                default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Starts the timer service
     */
    private void startTimer() {
        if (!isTimerServiceRunning()) {
            final Intent i = new Intent(this, TimerService.class);
            startService(i);

            setButtonVisibility(GONE, VISIBLE, GONE, VISIBLE);
        }
    }

    /**
     * Posts a {@link StopTimerEvent} message to the {@link Bus}
     */
    private void produceStopEvent() {
        eventBus.post(new StopTimerEvent());
    }

    /**
     * Posts a {@link PauseTimerEvent} message to the {@link Bus}
     */
    private void producePauseEvent() {
        eventBus.post(new PauseTimerEvent());
    }

    /**
     * Posts a {@link ResumeTimerEvent} message to the {@link Bus}
     */
    private void produceResumeEvent() {
        eventBus.post(new ResumeTimerEvent());
    }

    @Subscribe
    public void onTimerPausedEvent(final TimerPausedEvent event) {
        if (event.isTimerIsPaused()) {
            setButtonVisibility(GONE, VISIBLE, VISIBLE, GONE);
        } else if (isTimerServiceRunning()) {
            setButtonVisibility(GONE, VISIBLE, GONE, VISIBLE);
        }
    }

    /**
     * Called by {@link Bus} when a tick event occurs.
     *
     * @param event The event
     */
    @Subscribe
    public void onTickEvent(final TimerTickEvent event) {
        setFormattedTime(event.getMillis());
    }

    /**
     * Called by {@link Bus} when a tick event occurs.
     *
     * @param event The event
     */
    @Subscribe
    public void onPauseEvent(final PauseTimerEvent event) {
        setButtonVisibility(GONE, VISIBLE, VISIBLE, GONE);
    }

    /**
     * Called by {@link Bus} when a tick event occurs.
     *
     * @param event The event
     */
    @Subscribe
    public void onResumeEvent(final ResumeTimerEvent event) {
        setButtonVisibility(GONE, VISIBLE, GONE, VISIBLE);
    }

    /**
     * Called by {@link Bus} when a tick event occurs.
     *
     * @param event The event
     */
    @Subscribe
    public void onStopEvent(final StopTimerEvent event) {
        setButtonVisibility(VISIBLE, GONE, GONE, GONE);
        setFormattedTime(0); // Since its stopped, zero out the timer.
    }

    /**
     * Checks to see if the timer service is running or not.
     *
     * @return true if the service is running otherwise false.
     */
    private boolean isTimerServiceRunning() {
        final ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TimerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void setButtonVisibility(final int start, final int stop,
                                     final int resume, final int pause) {
        this.start.setVisibility(start);
        this.stop.setVisibility(stop);
        this.resume.setVisibility(resume);
        this.pause.setVisibility(pause);
    }

    /**
     * Sets the formatted time
     *
     * @param millis the elapsed time
     */
    private void setFormattedTime(long millis) {
        final String formattedTime = formatTime(millis);
        chronometer.setText(formattedTime);
    }

    /**
     * Formats the time to look like "HH:MM:SS"
     *
     * @param millis The number of elapsed milliseconds
     * @return A formatted time value
     */
    public static String formatTime(final long millis) {
        //TODO does not support hour>=100 (4.1 days)
        return String.format("%02d:%02d:%02d",
                millis / (1000 * 60 * 60),
                (millis / (1000 * 60)) % 60,
                (millis / 1000) % 60
        );
    }

}
