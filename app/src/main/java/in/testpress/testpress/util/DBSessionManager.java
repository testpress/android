package in.testpress.testpress.util;

import android.content.Context;

import java.util.Calendar;
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
        if(sessionDao.count() == 0) {
            return getNewSession();
        }
        return getLatestSession();
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

        //update Oldest post received time from the last post created time
        session.setOldestPostReceived(items.get(items.size() - 1).getCreated());

        Ln.e("Items size " + items.size());
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

    public DBSession getLatestSession() {
        return sessionDao.queryBuilder().orderDesc(DBSessionDao.Properties.Id).limit(1).list().get(0);
    }

    public DBSession getPreviousSession(DBSession currentSession) {
        if(sessionDao.queryBuilder().where(DBSessionDao.Properties.Id.lt(currentSession.getId())).count() > 0) {
            return sessionDao.queryBuilder().where(DBSessionDao.Properties.Id.lt(currentSession.getId())).orderDesc(DBSessionDao.Properties.Id).limit(1).list().get(0);
        } else {
            return null;
        }
    }
}
