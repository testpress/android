package in.testpress.testpress.models;

import java.util.ArrayList;
import java.util.List;

public class ProductDetails extends Product {

    private String description;
    private String institute;
    private Boolean requiresShipping;
    private List<Exam> exams = new ArrayList<Exam>();
    private List<Notes> notes = new ArrayList<Notes>();

    /**
     *
     * @return
     * The description
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     * The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     * The institute
     */
    public String getInstitute() {
        return institute;
    }

    /**
     *
     * @param institute
     * The institute
     */
    public void setInstitute(String institute) {
        this.institute = institute;
    }

    /**
     *
     * @return
     * The requiresShipping
     */
    public Boolean getRequiresShipping() {
        return requiresShipping;
    }

    /**
     *
     * @param requiresShipping
     * The requires_shipping
     */
    public void setRequiresShipping(Boolean requiresShipping) {
        this.requiresShipping = requiresShipping;
    }

    /**
     *
     * @return
     * The exams
     */
    public List<Exam> getExams() {
        return exams;
    }

    /**
     *
     * @param exams
     * The exams
     */
    public void setExams(List<Exam> exams) {
        this.exams = exams;
    }

    /**
     *
     * @return
     * The notes
     */
    public List<Notes> getNotes() {
        return notes;
    }

    /**
     *
     * @param notes
     * The notes
     */
    public void setNotes(List<Notes> notes) {
        this.notes = notes;
    }
}
