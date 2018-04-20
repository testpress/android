package in.testpress.testpress.models;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class ProfileDetails {

    public static final String PROFILE_DETAILS_PREFERENCES = "profileDetails";
    public static final String PROFILE_DETAILS = "profileDetails";

    private Integer id;
    private String url;
    private String username;
    private String displayName;
    private String firstName;
    private String lastName;
    private String email;
    private String photo;
    private String largeImage;
    private String mediumImage;
    private String smallImage;
    private String xSmallImage;
    private String miniImage;
    private String birthDate;
    private String gender;
    private String address1;
    private String address2;
    private String city;
    private String zip;
    private String state;
    private String stateChoices;
    private String phone;

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
     * The url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     * The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return
     * The username
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @param username
     * The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @return
     * The displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     *
     * @param displayName
     * The display_name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     *
     * @return
     * The firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     *
     * @param firstName
     * The first_name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     *
     * @return
     * The lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     *
     * @param lastName
     * The last_name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     *
     * @return
     * The email
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @param email
     * The email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     * @return
     * The photo
     */
    public String getPhoto() {
        return photo;
    }

    /**
     *
     * @param photo
     * The photo
     */
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    /**
     *
     * @return
     * The largeImage
     */
    public String getLargeImage() {
        return largeImage;
    }

    /**
     *
     * @param largeImage
     * The large_image
     */
    public void setLargeImage(String largeImage) {
        this.largeImage = largeImage;
    }

    /**
     *
     * @return
     * The mediumImage
     */
    public String getMediumImage() {
        return mediumImage;
    }

    /**
     *
     * @param mediumImage
     * The medium_image
     */
    public void setMediumImage(String mediumImage) {
        this.mediumImage = mediumImage;
    }

    /**
     *
     * @return
     * The smallImage
     */
    public String getSmallImage() {
        return smallImage;
    }

    /**
     *
     * @param smallImage
     * The small_image
     */
    public void setSmallImage(String smallImage) {
        this.smallImage = smallImage;
    }

    /**
     *
     * @return
     * The xSmallImage
     */
    public String getXSmallImage() {
        return xSmallImage;
    }

    /**
     *
     * @param xSmallImage
     * The x_small_image
     */
    public void setXSmallImage(String xSmallImage) {
        this.xSmallImage = xSmallImage;
    }

    /**
     *
     * @return
     * The miniImage
     */
    public String getMiniImage() {
        return miniImage;
    }

    /**
     *
     * @param miniImage
     * The mini_image
     */
    public void setMiniImage(String miniImage) {
        this.miniImage = miniImage;
    }

    /**
     *
     * @return
     * The birthDate
     */
    public String getBirthDate() {
        return birthDate;
    }

    /**
     *
     * @param birthDate
     * The birth_date
     */
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    /**
     *
     * @return
     * The gender
     */
    public String getGender() {
        return gender;
    }

    /**
     *
     * @param gender
     * The gender
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     *
     * @return
     * The address1
     */
    public String getAddress1() {
        return address1;
    }

    /**
     *
     * @param address1
     * The address1
     */
    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    /**
     *
     * @return
     * The address2
     */
    public String getAddress2() {
        return address2;
    }

    /**
     *
     * @param address2
     * The address2
     */
    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    /**
     *
     * @return
     * The city
     */
    public String getCity() {
        return city;
    }

    /**
     *
     * @param city
     * The city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     *
     * @return
     * The zip
     */
    public String getZip() {
        return zip;
    }

    /**
     *
     * @param zip
     * The zip
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     *
     * @return
     * The state
     */
    public String getState() {
        return state;
    }

    /**
     *
     * @param state
     * The state
     */
    public void setState(String state) {
        this.state = state;
    }

    public String getStateChoices() {
        return stateChoices;
    }

    public void setStateChoices(String stateChoices) {
        this.stateChoices = stateChoices;
    }

    /**
     *
     * @return
     * The phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     *
     * @param phone
     * The phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    private static String serialize(ProfileDetails profileDetails) {
        Gson gson = new Gson();
        return gson.toJson(profileDetails);
    }

    private static ProfileDetails deserialize(String serializedProfileDetails) {
        if (serializedProfileDetails != null && !serializedProfileDetails.isEmpty()) {
            try {
                Gson gson = new Gson();
                return gson.fromJson(serializedProfileDetails, ProfileDetails.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static public ProfileDetails getProfileDetailsFromPreferences(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PROFILE_DETAILS_PREFERENCES, Context.MODE_PRIVATE);

        String profileDetailSerialized = prefs.getString(PROFILE_DETAILS, null);
        return deserialize(profileDetailSerialized);
    }

    static public void saveProfileDetailsInPreferences(Context context,
                                                       ProfileDetails profileDetails) {

        SharedPreferences.Editor editor =
                context.getSharedPreferences(PROFILE_DETAILS_PREFERENCES, Context.MODE_PRIVATE).edit();

        String profileDetailSerialized = serialize(profileDetails);
        editor.putString(PROFILE_DETAILS, profileDetailSerialized);
        editor.apply();
    }

}
