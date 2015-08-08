package in.testpress.testpress.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrationErrorDetails {

    private List<String> username = new ArrayList<String>();
    private List<String> email = new ArrayList<String>();
    private List<String> password = new ArrayList<String>();
    private List<String> phone = new ArrayList<String>();
    private List<String> nonFieldErrors = new ArrayList<String>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    /**
     *
     * @return
     * The username
     */
    public List<String> getUsername() {
        return username;
    }

    /**
     *
     * @param username
     * The username
     */
    public void setUsername(List<String> username) {
        this.username = username;
    }

    /**
     *
     * @return
     * The email
     */
    public List<String> getEmail() {
        return email;
    }

    /**
     *
     * @param email
     * The email
     */
    public void setEmail(List<String> email) {
        this.email = email;
    }

    /**
     *
     * @return
     * The password
     */
    public List<String> getPassword() {
        return password;
    }

    /**
     *
     * @param password
     * The password
     */
    public void setPassword(List<String> password) {
        this.password = password;
    }

    /**
     *
     * @return
     * The phone
     */
    public List<String> getPhone() {
        return phone;
    }

    /**
     *
     * @param phone
     * The phone
     */
    public void setPhone(List<String> phone) {
        this.phone = phone;
    }

    /**
     *
     * @return
     * The nonFieldErrors
     */
    public List<String> getNonFieldErrors() {
        return nonFieldErrors;
    }

    /**
     *
     * @param nonFieldErrors
     * The non_field_errors
     */
    public void setNonFieldErrors(List<String> nonFieldErrors) {
        this.nonFieldErrors = nonFieldErrors;
    }


    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
