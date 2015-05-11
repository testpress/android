package in.testpress.testpress.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Update {

    private Integer id;
    private Boolean active;
    private Long versionCode;
    private Date updateBefore;
    private String releaseNotes;
    private Date date;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
     * The active
     */
    public Boolean getActive() {
        return active;
    }

    /**
     *
     * @param active
     * The active
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     *
     * @return
     * The versionCode
     */
    public Long getVersionCode() {
        return versionCode;
    }

    /**
     *
     * @param versionCode
     * The version_code
     */
    public void setVersionCode(Long versionCode) {
        this.versionCode = versionCode;
    }

    /**
     *
     * @return
     * The updateBefore
     */
    public Date getUpdateBefore() {
        return updateBefore;
    }

    /**
     *
     * @param updateBefore
     * The update_before
     */
    public void setUpdateBefore(Date updateBefore) {
        this.updateBefore = updateBefore;
    }

    /**
     *
     * @return
     * The releaseNotes
     */
    public String getReleaseNotes() {
        return releaseNotes;
    }

    /**
     *
     * @param releaseNotes
     * The release_notes
     */
    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    /**
     *
     * @return
     * The date
     */
    public Date getDate() {
        return date;
    }

    /**
     *
     * @param date
     * The date
     */
    public void setDate(Date date) {
        this.date = date;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}