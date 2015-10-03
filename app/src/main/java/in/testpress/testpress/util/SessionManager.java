package in.testpress.testpress.util;

import android.content.Context;

import java.util.Calendar;
import java.util.List;

import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.core.PostsPager;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.Session;
import in.testpress.testpress.models.SessionDao;

public class SessionManager {
    SessionDao sessionDao;

    public SessionManager(Context context) {
        sessionDao = ((TestpressApplication) context.getApplicationContext()).getDaoSession().getSessionDao();
    }

    public Session getNewSession() {
        Calendar rightNow = Calendar.getInstance();
        Session session = new Session();
        session.setCreated(rightNow.getTimeInMillis());
        if(sessionDao.count() > 0) {
            session.setLast_synced_date(getLatestSession().getLatestPostReceived());
        }
        return session;
    }

    public Session updateSession(Session session, PostsPager pager, List<Post> items) {

        //assign Latest post received from the first post created time @ first time only
        if(session.getLatestPostReceived() == null) {
            session.setLatestPostReceived(items.get(0).getCreated());
        }

        //update Oldest post received time from the last post created time
        session.setOldestPostReceived(items.get(items.size() - 1).getCreated());

        //update the state
        if(pager.hasMore()) {  //has next page means partial else completed

            session.setState("partial");

            //insertOrReplace to DB
            sessionDao.insertOrReplace(session);

        } else {
            session.setState("completed");

            //insertOrReplace to DB
            sessionDao.insertOrReplace(session);
        }
        return session;
    }

    public void merge(Session currentSession) {

        if(currentSession.getState().equals("completed")) {
            if (getPreviousSession(currentSession) != null) {     //check whether previous session available
                Session previousSession = getPreviousSession(currentSession);
                currentSession.setOldestPostReceived(previousSession.getOldestPostReceived());
                currentSession.setLast_synced_date(previousSession.getLast_synced_date());
                currentSession.setState(previousSession.getState());
                sessionDao.insertOrReplace(currentSession);
                sessionDao.deleteByKey(previousSession.getId());
            }
        }
    }

    public Session getLatestSession() {
        return sessionDao.queryBuilder().orderDesc(SessionDao.Properties.Id).limit(1).list().get(0);
    }

    public Session getPreviousSession(Session currentSession) {
        if(sessionDao.queryBuilder().where(SessionDao.Properties.Id.lt(currentSession.getId())).count() > 0) {
            return sessionDao.queryBuilder().where(SessionDao.Properties.Id.lt(currentSession.getId())).orderDesc(SessionDao.Properties.Id).limit(1).list().get(0);
        } else {
            return null;
        }
    }
}
