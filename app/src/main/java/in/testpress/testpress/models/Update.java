package in.testpress.testpress.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Update {

    private Boolean updateRequired;
    private Boolean force;
    private String message;
    private Integer days;

    /**
     *
     * @return
     * The updateRequired
     */
    public Boolean getUpdateRequired() {
        return updateRequired;
    }

    /**
     *
     * @param updateRequired
     * The update_required
     */
    public void setUpdateRequired(Boolean updateRequired) {
        this.updateRequired = updateRequired;
    }

    /**
     *
     * @return
     * The force
     */
    public Boolean getForce() {
        return force;
    }

    /**
     *
     * @param force
     * The force
     */
    public void setForce(Boolean force) {
        this.force = force;
    }

    /**
     *
     * @return
     * The message
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     * @param message
     * The message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }
}