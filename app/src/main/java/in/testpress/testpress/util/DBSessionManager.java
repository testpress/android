package in.testpress.testpress.util;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.core.PostsPager;
import in.testpress.testpress.models.DBSession;
import in.testpress.testpress.models.DBSessionDao;
import in.testpress.testpress.models.Post;

public class DBSessionManager {
    DBSessionDao sessionDao;

    public DBSessionManager(Context context) {
        sessionDao = ((TestpressApplication) context.getApplicationContext()).getDaoSession().getDBSessionDao();
    }

    public DBSession getSession() {
        Ln.e("##### Session Stats #####");
        Ln.e("Total session rows: " + sessionDao.count());
        Ln.e("##### Session Stats #####");
        try {
            DBSession lastSession = getLatestSession();
            Calendar now = Calendar.getInstance();
            Ln.e("Time now = " + now.getTimeInMillis());
            Ln.e("Last session time = " + lastSession.getCreated());
            Date created = new Date(lastSession.getCreated());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            Ln.e("Time now = " + dateFormat.format(now.getTime()));
            Ln.e("Last session time = " + dateFormat.format(created));
            Ln.e("Time difference = " + (now.getTimeInMillis() - lastSession.getCreated()));
            Ln.e("Time difference in mins = " + ((now.getTimeInMillis() - lastSession.getCreated()) / (60 * 1000)));
            if(lastSession.getState().equals("partial") &&
                    (((now.getTimeInMillis() - lastSession.getCreated()) / (60 * 1000)) < 1)) {
                Ln.e("Returning last session");
                return lastSession;
            }
        } catch (IndexOutOfBoundsException e) {
            Ln.e("Empty Session!");
        }
        Ln.e("Returning new session");
        return getNewSession();
    }

    public DBSession getNewSession() {
        Calendar rightNow = Calendar.getInstance();
        DBSession session = new DBSession();
        session.setCreated(rightNow.getTimeInMillis());
        if(sessionDao.count() > 0) {
            session.setLastSyncedDate(getLatestSession().getLatestPostReceived());
        }
        return session;
    }

    public DBSession updateSession(DBSession session, PostsPager pager, List<Post> items) {

        //assign Latest post received from the first post created time @ first time only
        if(session.getLatestPostReceived() == null) {
            session.setLatestPostReceived(items.get(0).getCreated());
        }

        Ln.e("Items size " + items.size());

        //update Oldest post received time from the last post created time
        session.setOldestPostReceived(items.get(items.size() - 1).getCreated());

        Ln.e("Session oldest post received " + session.getOldestPostReceived());

        //update the state
        if(pager.hasMore()) {  //has next page means partial else completed
            session.setState("partial");
        } else {
            session.setState("completed");
        }
        sessionDao.insertOrReplace(session);

        return session;
    }

    public void merge(DBSession currentSession) {

        if(currentSession.getState().equals("completed")) {
            if (getPreviousSession(currentSession) != null) {     //check whether previous session available
                DBSession previousSession = getPreviousSession(currentSession);
                currentSession.setOldestPostReceived(previousSession.getOldestPostReceived());
                currentSession.setLastSyncedDate(previousSession.getLastSyncedDate());
                currentSession.setState(previousSession.getState());
                sessionDao.insertOrReplace(currentSession);
                sessionDao.deleteByKey(previousSession.getId());
            }
        }
    }

    public DBSession getLatestSession() throws IndexOutOfBoundsException {
        return sessionDao.queryBuilder().orderDesc(DBSessionDao.Properties.Id).limit(1).list().get(0);
    }

    public DBSession getPreviousSession(DBSession currentSession) {
        if(sessionDao.queryBuilder().where(DBSessionDao.Properties.Id.lt(currentSession.getId())).count() > 0) {
            return sessionDao.queryBuilder().where(DBSessionDao.Properties.Id.lt(currentSession.getId())).orderDesc(DBSessionDao.Properties.Id).limit(1).list().get(0);
        } else {
            return null;
        }
    }

    public void printSessionInfo(DBSession session) {
        Date created = new Date(session.getCreated());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Ln.e("##### Session Info #####");
        Ln.e("ID: " + session.getId());
        Ln.e("Created: " + dateFormat.format(created));
        Ln.e("State: " + session.getState());
        Ln.e("Latest post received: " + session.getLatestPostReceived());
        Ln.e("Oldest post received: " + session.getOldestPostReceived());
        Ln.e("Last synced date: " + session.getLastSyncedDate());
        Ln.e("##### Session Info #####");
    }

}
