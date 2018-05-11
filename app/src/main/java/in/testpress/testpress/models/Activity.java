package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Activity {
    @Id
    private Long id;
    private int actorContentType;
    private String actorObjectId;
    private int targetContentType;
    private int targetObjectId;
    private int actionObjectContentType;
    private String actionObjectObjectId;
    private String timestamp;
    private String verb;

    @Generated(hash = 2081144785)
    public Activity(Long id, int actorContentType, String actorObjectId,
            int targetContentType, int targetObjectId, int actionObjectContentType,
            String actionObjectObjectId, String timestamp, String verb) {
        this.id = id;
        this.actorContentType = actorContentType;
        this.actorObjectId = actorObjectId;
        this.targetContentType = targetContentType;
        this.targetObjectId = targetObjectId;
        this.actionObjectContentType = actionObjectContentType;
        this.actionObjectObjectId = actionObjectObjectId;
        this.timestamp = timestamp;
        this.verb = verb;
    }

    @Generated(hash = 126967852)
    public Activity() {
    }

    public int getActorContentType() {
        return actorContentType;
    }

    public void setActorContentType(int actorContentType) {
        this.actorContentType = actorContentType;
    }

    public String getActorObjectId() {
        return actorObjectId;
    }

    public void setActorObjectId(String actorObjectId) {
        this.actorObjectId = actorObjectId;
    }

    public int getTargetContentType() {
        return targetContentType;
    }

    public void setTargetContentType(int targetContentType) {
        this.targetContentType = targetContentType;
    }

    public int getTargetObjectId() {
        return targetObjectId;
    }

    public void setTargetObjectId(int targetObjectId) {
        this.targetObjectId = targetObjectId;
    }

    public int getActionObjectContentType() {
        return actionObjectContentType;
    }

    public void setActionObjectContentType(int actionObjectContentType) {
        this.actionObjectContentType = actionObjectContentType;
    }

    public String getActionObjectObjectId() {
        return actionObjectObjectId;
    }

    public void setActionObjectObjectId(String actionObjectObjectId) {
        this.actionObjectObjectId = actionObjectObjectId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
