package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ContentType {
    @Id
    private Long id;
    private String model;
    private String appLabel;
    @Generated(hash = 1962633954)
    public ContentType(Long id, String model, String appLabel) {
        this.id = id;
        this.model = model;
        this.appLabel = appLabel;
    }
    @Generated(hash = 163280486)
    public ContentType() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getModel() {
        return this.model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public String getAppLabel() {
        return this.appLabel;
    }
    public void setAppLabel(String appLabel) {
        this.appLabel = appLabel;
    }
}
