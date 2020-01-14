package in.testpress.testpress.ui.temp;


import android.os.Parcel;
import android.os.Parcelable;

import in.testpress.models.ProfileDetails;
import in.testpress.testpress.models.User;

public class Reputation {

    private Integer id;
    private User user;
    private Integer trophiesCount;
    private Integer rank;
    private Integer difference;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getTrophiesCount() {
        return trophiesCount;
    }

    public void setTrophiesCount(Integer trophiesCount) {
        this.trophiesCount = trophiesCount;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getDifference() {
        return difference;
    }

    public void setDifference(Integer difference) {
        this.difference = difference;
    }

}
