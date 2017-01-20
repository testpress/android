package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Subject implements Parcelable {

    private Integer id;
    private String name;
    private Integer total;
    private Integer correct;
    private Integer unanswered;
    private Integer incorrect;
    private Float correctPercentage;
    private Float unansweredPercentage;
    private Float incorrectPercentage;
    private Integer parent;
    private boolean leaf;

    protected Subject(Parcel in) {
        id = in.readInt();
        name = in.readString();
        total = in.readInt();
        correct = in.readInt();
        unanswered = in.readInt();
        incorrect = in.readInt();
        correctPercentage = in.readFloat();
        unansweredPercentage = in.readFloat();
        incorrectPercentage = in.readFloat();
        leaf = in.readByte() != 0;
        parent = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(total);
        dest.writeInt(correct);
        dest.writeInt(unanswered);
        dest.writeInt(incorrect);
        dest.writeFloat(correctPercentage);
        dest.writeFloat(unansweredPercentage);
        dest.writeFloat(incorrectPercentage);
        dest.writeByte((byte) (leaf ? 1 : 0));
        if (parent != null) {
            dest.writeInt(parent);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Subject> CREATOR = new Creator<Subject>() {
        @Override
        public Subject createFromParcel(Parcel in) {
            return new Subject(in);
        }

        @Override
        public Subject[] newArray(int size) {
            return new Subject[size];
        }
    };

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The total
     */
    public Integer getTotal() {
        return total;
    }

    /**
     *
     * @param total
     * The total
     */
    public void setTotal(Integer total) {
        this.total = total;
    }

    /**
     *
     * @return
     * The correct
     */
    public Integer getCorrect() {
        return correct;
    }

    /**
     *
     * @param correct
     * The correct
     */
    public void setCorrect(Integer correct) {
        this.correct = correct;
    }

    /**
     *
     * @return
     * The unanswered
     */
    public Integer getUnanswered() {
        return unanswered;
    }

    /**
     *
     * @param unanswered
     * The unanswered
     */
    public void setUnanswered(Integer unanswered) {
        this.unanswered = unanswered;
    }

    /**
     *
     * @return
     * The incorrect
     */
    public Integer getIncorrect() {
        return incorrect;
    }

    /**
     *
     * @param incorrect
     * The incorrect
     */
    public void setIncorrect(Integer incorrect) {
        this.incorrect = incorrect;
    }

    /**
     *
     * @return
     * The parent
     */
    public Integer getParent() {
        return parent;
    }

    /**
     *
     * @param parent
     * The parent
     */
    public void setParent(Integer parent) {
        this.parent = parent;
    }

    /**
     *
     * @return
     * The leaf
     */
    public Boolean isLeaf() {
        return leaf;
    }

    /**
     *
     * @param leaf
     * The leaf
     */
    public void setLeaf(Boolean leaf) {
        this.leaf = leaf;
    }

    public Float getCorrectPercentage() {
        return correctPercentage;
    }

    public void setCorrectPercentage(Float correctPercentage) {
        this.correctPercentage = correctPercentage;
    }

    public Float getUnansweredPercentage() {
        return unansweredPercentage;
    }

    public void setUnansweredPercentage(Float unansweredPercentage) {
        this.unansweredPercentage = unansweredPercentage;
    }

    public Float getIncorrectPercentage() {
        return incorrectPercentage;
    }

    public void setIncorrectPercentage(Float incorrectPercentage) {
        this.incorrectPercentage = incorrectPercentage;
    }

    public static void sortSubjects(List<Subject> subjects) {
        Collections.sort(subjects, new Comparator<Subject>() {
            @Override
            public int compare(Subject item1, Subject item2) {
                return item1.getName().compareTo(item2.getName());
            }
        });
    }
}
