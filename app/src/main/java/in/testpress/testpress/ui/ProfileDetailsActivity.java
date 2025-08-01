package in.testpress.testpress.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.kevinsawicki.wishlist.Toaster;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import in.testpress.exam.util.ImageUtils;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.InstituteSettingsDao;
import in.testpress.testpress.models.ProfileDetails;
import in.testpress.testpress.models.SsoUrl;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.FormatDate;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.Strings;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static in.testpress.testpress.BuildConfig.BASE_URL;
import static in.testpress.testpress.BuildConfig.WHITE_LABELED_HOST_URL;

public class ProfileDetailsActivity extends BaseAuthenticatedActivity
        implements LoaderManager.LoaderCallbacks<ProfileDetails> {

    @Inject TestpressServiceProvider serviceProvider;
    @Inject TestpressService testpressService;
    ImageView profilePhoto;
    ImageView imageEditButton;
    TextView displayName;
    CollapsingToolbarLayout collapsingToolbar;
    EditText firstName;
    EditText lastName;
    AppCompatTextView email;
    AppCompatTextView username;
    Spinner gender;
    AppCompatTextView address;
    AppCompatTextView phone;
    EditText dateOfBirth;
    ImageButton datePicker;
    EditText city;
    Spinner state;
    EditText pinCode;
    TableRow firstNameRow;
    TableRow lastNameRow;
    LinearLayout mailIdRow;
    LinearLayout usernameContainer;
    TableRow genderRow;
    LinearLayout addressRow;
    TableRow mobileNoRow;
    TableRow cityRow;
    TableRow dobRow;
    TableRow stateRow;
    TableRow pinCodeRow;
    TextView emptyView;
    ImageView editButton;
    Button saveButton;
    RelativeLayout profileDetailsView;
    ProgressBar horizontalProgressBar;
    ProgressBar progressBar;
    ProfileDetails profileDetails;
    ArrayAdapter<String> genderSpinnerAdapter;
    ArrayAdapter<String> stateSpinnerAdapter;
    ImageUtils imagePickerUtils;
    String[] datePickerDate;
    ImageLoader imageLoader;
    DisplayImageOptions options;
    Menu menu;
    Button deleteAccountButton;

    Button editProfileButton;
    static final private int SELECT_IMAGE = 100;
    public String ssoUrl;
    private final HashMap<Integer, Runnable> menuActions = new HashMap<>();


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!CommonUtils.isUserAuthenticated(this)) {
            return;
        }
        setContentView(R.layout.profile_detail_layout);
        TestpressApplication.getAppComponent().inject(this);
        bindViews();
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        profileDetailsView.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);
        genderSpinnerAdapter = getDropdownListAdapter(Constants.genderChoices.keySet());
        gender.setAdapter(genderSpinnerAdapter);
        stateSpinnerAdapter = getDropdownListAdapter(Constants.stateChoices.keySet());
        state.setAdapter(stateSpinnerAdapter);
        pinCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(final TextView v, final int actionId,
                                          final KeyEvent event) {
                if (actionId == IME_ACTION_DONE) {
                    saveDetails();
                    return true;
                }
                return false;
            }
        });
        imagePickerUtils = new ImageUtils(profileDetailsView, this);
        imagePickerUtils.setAspectRatio(1, 1);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.drawable.profile_image_sample)
                .showImageOnFail(R.drawable.profile_image_sample)
                .showImageOnLoading(R.drawable.profile_image_sample).build();
        getSupportLoaderManager().initLoader(0, null, this);
        fetchSsoLink();
        initializeDeleteAccountButton();
        initializeEditProfileButton();
        setupMenuActions();
    }

    private void bindViews() {
        profilePhoto = findViewById(R.id.profile_photo);
        imageEditButton = findViewById(R.id.edit_profile_photo);
        displayName = findViewById(R.id.display_name);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        gender = findViewById(R.id.gender);
        address = findViewById(R.id.address);
        phone = findViewById(R.id.phone);
        dateOfBirth = findViewById(R.id.date_of_birth);
        datePicker = findViewById(R.id.datepicker);
        city = findViewById(R.id.city);
        state = findViewById(R.id.state);
        pinCode = findViewById(R.id.pin_code);
        firstNameRow = findViewById(R.id.first_name_container);
        lastNameRow = findViewById(R.id.last_name_container);
        mailIdRow = findViewById(R.id.email_container);
        usernameContainer = findViewById(R.id.username_container);
        genderRow = findViewById(R.id.gender_container);
        addressRow = findViewById(R.id.address_container);
        mobileNoRow = findViewById(R.id.mobile_container);
        cityRow = findViewById(R.id.city_container);
        dobRow = findViewById(R.id.date_of_birth_container);
        stateRow = findViewById(R.id.state_container);
        pinCodeRow = findViewById(R.id.pincode_container);

        emptyView = findViewById(R.id.empty);
        editButton = findViewById(R.id.edit);
        saveButton = findViewById(R.id.save);
        profileDetailsView = findViewById(R.id.profile_details);
        horizontalProgressBar = findViewById(R.id.horizontal_progress_bar);

        profilePhoto.setOnClickListener(v -> displayProfilePhoto());
        imageEditButton.setOnClickListener(v -> selectImageFromMobile());
        datePicker.setOnClickListener(v -> pickDate());
        editButton.setOnClickListener(v -> editProfileDetails());
        saveButton.setOnClickListener(v -> saveDetails());
    }

    private void initializeDeleteAccountButton() {
        deleteAccountButton = findViewById(R.id.delete_account);
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileDetailsActivity.this.startActivity(
                        AccountDeleteActivity.Companion.createIntent(
                                ProfileDetailsActivity.this,
                                "Delete Account",
                                WHITE_LABELED_HOST_URL + "/settings/account/delete/",
                                true,
                                false,
                                AccountDeleteActivity.class
                        )
                );
            }
        });
    }

    private void initializeEditProfileButton() {
        editProfileButton = findViewById(R.id.edit_profile);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fetchInstituteSetting().getAllow_profile_edit() && !Strings.toString(profileDetails.getUsername()).isEmpty()) {

                    if (!Strings.toString(ssoUrl).isEmpty()) {
                        Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                        intent.putExtra(WebViewActivity.ACTIVITY_TITLE, "Edit Profile");
                        intent.putExtra(WebViewActivity.URL_TO_OPEN, BASE_URL + ssoUrl+"&next=/settings/profile/mobile/");
                        startActivity(intent);
                    } else {
                        Toaster.showLong(ProfileDetailsActivity.this, R.string.edit_profile_error);
                    }
                }
            }
        });
    }

    private void setupMenuActions() {
        menuActions.put(R.id.refresh, () -> {
            progressBar.setVisibility(View.VISIBLE);
            getSupportLoaderManager().restartLoader(0, null, this);
        });
        menuActions.put(R.id.tick, this::saveDetails);
        menuActions.put(R.id.cancel, () -> displayProfileDetails(profileDetails));
    }

    @Override
    public Loader<ProfileDetails> onCreateLoader(int id, Bundle bundle) {
        return new ThrowableLoader<ProfileDetails>(this, profileDetails) {

            @Override
            public ProfileDetails loadData() throws Exception {
                try {
                    return serviceProvider.getService(ProfileDetailsActivity.this).getProfileDetails();
                } catch (Exception exception) {
                    throw exception;
                }
            }
        };
    }

    public void onLoadFinished(final Loader<ProfileDetails> loader, final ProfileDetails profileDetails) {
        progressBar.setVisibility(View.GONE);
        getSupportLoaderManager().destroyLoader(loader.getId());
        if (profileDetails == null) { //loading failed
            Exception exception = ((ThrowableLoader<ProfileDetails>) loader).clearException();
            if(exception.getCause() instanceof UnknownHostException) {
                emptyView.setText(R.string.no_internet);
                emptyView.setVisibility(View.VISIBLE);
                Toaster.showLong(ProfileDetailsActivity.this, R.string.no_internet);
            } else {
                Toaster.showLong(ProfileDetailsActivity.this, exception.getMessage());
            }
            return;
        } else {
            emptyView.setVisibility(View.GONE);
            this.profileDetails = profileDetails;
        }
        profileDetailsView.setVisibility(View.VISIBLE);
        displayProfileDetails(this.profileDetails);
    }

    void displayProfileDetails(ProfileDetails profileDetails) {
        //download and display image from url
        imageLoader.displayImage(profileDetails.getLargeImage(), profilePhoto, options);
//        menu.setGroupVisible(R.id.editMode, false);
//        menu.setGroupVisible(R.id.viewMode, false);
        setVisibility(View.VISIBLE, new View[]{displayName, editButton});
        setVisibility(View.GONE, new View[]{firstNameRow, lastNameRow, imageEditButton, datePicker});
        displayName.setText(profileDetails.getFirstName() + " " + profileDetails.getLastName());
        collapsingToolbar.setTitle(profileDetails.getDisplayName());
        handleDetail(email, mailIdRow, profileDetails.getEmail());
        handleDetail(username, usernameContainer, profileDetails.getUsername());
        handleDetail(phone, mobileNoRow, profileDetails.getPhone());
        handleDetail(gender, genderRow, profileDetails.getGender());
        handleDetail(dateOfBirth, dobRow, profileDetails.getBirthDate());
//        String fullAddress = profileDetails.getAddress1() + "\n" + profileDetails.getAddress2()
//                + "\n" + profileDetails.getCity() + " - " + profileDetails.getZip();
//        handleDetail(address, addressRow, fullAddress);
        handleDetail(city, cityRow, profileDetails.getCity());
        handleDetail(state, stateRow, profileDetails.getState());
        handleDetail(pinCode, pinCodeRow, profileDetails.getZip());
        saveButton.setVisibility(View.GONE);
        setEnabled(false, new View[]{email, phone, gender, dateOfBirth, address, city, state, pinCode});
        showOrHideDeleteAccountButton();
    }

    private void showOrHideDeleteAccountButton() {
        Boolean allowSignUp = TestpressApplication.getInstituteSettings().getAllowSignup();
        if (Boolean.TRUE.equals(allowSignUp)) {
            deleteAccountButton.setVisibility(View.VISIBLE);
        }
    }

    private void handleDetail(View widget, View viewRow, String detail) {
        //check whether the detail is null & set the visibility
        if (detail != null) {
            viewRow.setVisibility(View.VISIBLE);
            switch (widget.getClass().getName()) {
                case "androidx.appcompat.widget.AppCompatEditText":
                    ((EditText)widget).setText(detail);
                    break;
                case "androidx.appcompat.widget.AppCompatSpinner":
                    if(widget == gender) {
                        ((Spinner) widget).setSelection(genderSpinnerAdapter.getPosition(detail));
                    } else if(widget == state){
                        ((Spinner) widget).setSelection(stateSpinnerAdapter.getPosition(detail));
                    }
                    break;
                default:
                    ((TextView)widget).setText(detail);
            }
        } else {
            viewRow.setVisibility(View.GONE);
        }
    }

    //method used to enable or disable views
    private void setEnabled(boolean enable, View[] views) {
        for(View view : views) {
            view.setEnabled(enable);
        }
    }

    //method used to set the visibility of views
    private void setVisibility(int visibility, View[] views){
        for(View view : views) {
            view.setVisibility(visibility);
        }
    }

    private void editProfileDetails() {
        menu.setGroupVisible(R.id.editMode, true);
        menu.setGroupVisible(R.id.viewMode, false);
        editButton.setVisibility(View.GONE);
        setVisibility(View.VISIBLE, new View[]{imageEditButton, mailIdRow, firstNameRow, lastNameRow, mobileNoRow, genderRow, dobRow, datePicker, addressRow, cityRow, stateRow, pinCodeRow, saveButton});
        setEnabled(true, new View[]{email, phone, gender, dateOfBirth, address, city, state, pinCode});
        firstName.setText(profileDetails.getFirstName());
        lastName.setText(profileDetails.getLastName());
        if (profileDetails.getGender() == null) {
            gender.setSelection(genderSpinnerAdapter.getPosition("--select--"));
        }
        if (profileDetails.getState() == null) {
            state.setSelection(stateSpinnerAdapter.getPosition("--select--"));
        }
    }

    private void displayProfilePhoto() {
        if (profileDetails != null && fetchInstituteSetting().getAllow_profile_edit()) {
            Intent intent = new Intent(this, ProfilePhotoActivity.class);
            intent.putExtra("profilePhoto", profileDetails.getPhoto());
            startActivityForResult(intent, SELECT_IMAGE);
        }
    }

    private void selectImageFromMobile() {
        CropImage.startPickImageActivity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECT_IMAGE) {
            // To handle the edit option in ProfilePhotoActivity
            selectImageFromMobile();
        } else {
            imagePickerUtils.onActivityResult(requestCode, resultCode, data,
                    new ImageUtils.ImagePickerResultHandler() {
                        @Override
                        public void onSuccessfullyImageCropped(CropImage.ActivityResult result) {
                            onImageCropped(result);
                        }
                    });
        }
    }

    void onImageCropped(CropImage.ActivityResult result) {
        Uri selectedImageUri;
        int[] croppedImageDetails = null;
        int rotatedDegree = result.getRotation();
        if (rotatedDegree != 0) {
            selectedImageUri = result.getUri();
        } else {
            selectedImageUri = result.getOriginalUri();
            Rect rect = result.getCropRect();
            // Details to crop in server
            croppedImageDetails = new int[]{ rect.left, rect.top, rect.width(), rect.height()};
        }
        Bitmap selectedImage;
        try {
            selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
        } catch (IOException e) {
            Snackbar.make(profileDetailsView, R.string.file_path_not_suitable, Snackbar.LENGTH_SHORT)
                    .show();

            return;
        }
        if(rotatedDegree != 0) {
            croppedImageDetails =
                    new int[] { 0, 0, selectedImage.getWidth(), selectedImage.getHeight() };
        }
        horizontalProgressBar.setVisibility(View.VISIBLE);
        // Encode the image as string
        ByteArrayOutputStream baostream = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.JPEG, 50, baostream);
        byte[] byteImage = baostream.toByteArray();
        // Converting Image byte array into Base64 String
        String encodedImage = Base64.encodeToString(byteImage, Base64.DEFAULT);
        saveProfilePhoto(croppedImageDetails, encodedImage);
    }

    private void saveDetails() {
        if(validate()) {
            final MaterialDialog progressDialog = new MaterialDialog.Builder(this)
                    .title(R.string.loading)
                    .content(R.string.please_wait)
                    .widgetColorRes(R.color.primary)
                    .progress(true, 0)
                    .show();
            new SafeAsyncTask<ProfileDetails>() {
                public ProfileDetails call() throws Exception {
                    return serviceProvider.getService(ProfileDetailsActivity.this)
                            .updateUserDetails(
                                    profileDetails.getUrl().replace(BASE_URL + "/", ""),
                                    email.getText().toString(),
                                    firstName.getText().toString(),
                                    lastName.getText().toString(),
                                    phone.getText().toString(),
                                    Constants.genderChoices.get(gender.getSelectedItem().toString()),
                                    dateOfBirth.getText().toString(),
                                    address.getText().toString(),
                                    city.getText().toString(),
                                    Constants.stateChoices.get(state.getSelectedItem().toString()),
                                    pinCode.getText().toString()
                            );
                }

                @Override
                protected void onException(final Exception e) throws RuntimeException {
                    progressDialog.dismiss();
                    Toaster.showLong(ProfileDetailsActivity.this, R.string.no_internet);
                }

                @Override
                public void onSuccess(final ProfileDetails profileDetails) {
                    //set the integer value into corresponding text
                    if (profileDetails.getGender() != null) {
                        for (Map.Entry<String, Integer> entry : Constants.genderChoices.entrySet()) {
                            if (entry.getValue().equals(Integer.parseInt(profileDetails.getGender()))) {
                                profileDetails.setGender(entry.getKey());
                                break;
                            }
                        }
                    }
                    if (profileDetails.getStateChoices() != null) {
                        for (Map.Entry<String, Integer> entry : Constants.stateChoices.entrySet()) {
                            if (entry.getValue().equals(Integer.parseInt(profileDetails.getStateChoices()))) {
                                profileDetails.setState(entry.getKey());
                                break;
                            }
                        }
                    }
                    //display the new profileDetails
                    profileDetails.setUrl(ProfileDetailsActivity.this.profileDetails.getUrl());
                    displayProfileDetails(profileDetails);
                    progressDialog.dismiss();
                    ProfileDetailsActivity.this.profileDetails = profileDetails;
                    Toaster.showLong(ProfileDetailsActivity.this, "Updated Successfully");
                }
            }.execute();
        }
    }

    public void saveProfilePhoto(final int[] croppedImageDetails, final String encodedImage) {
        new SafeAsyncTask<ProfileDetails>() {
            public ProfileDetails call() throws Exception {
                return serviceProvider.getService(ProfileDetailsActivity.this).updateProfileImage(
                        profileDetails.getUrl().replace(BASE_URL + "/", ""),
                        encodedImage,
                        croppedImageDetails
                );
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                horizontalProgressBar.setVisibility(View.GONE);
                Toaster.showLong(ProfileDetailsActivity.this, "Profile Photo uploading failed try again");
            }

            @Override
            public void onSuccess(final ProfileDetails profileDetails) {
                //display the new image
                horizontalProgressBar.setVisibility(View.GONE);
                imageLoader.displayImage(profileDetails.getLargeImage(), profilePhoto, options);
                ProfileDetailsActivity.this.profileDetails.setPhoto(profileDetails.getPhoto());
                ProfileDetailsActivity.this.profileDetails.setLargeImage(profileDetails.getLargeImage());
                Toaster.showLong(ProfileDetailsActivity.this, "Profile Photo Updated Successfully");
            }
        }.execute();
    }

    private boolean validate() {
        //Email Validation
        if (email.getText().toString().trim().length() == 0) {
            email.setError("This is a required field");
            email.requestFocus();
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()) {
            email.setError("Please enter a valid Email address");
            email.requestFocus();
            return false;
        }
        //Phone number Validation
        if (phone.getText().toString().trim().length() == 0) {
            phone.setError("This is a required field");
            phone.requestFocus();
            return false;
        } else {
            Pattern phoneNumberPattern = Pattern.compile("\\d{10}");
            Matcher phoneNumberMatcher = phoneNumberPattern.matcher(phone.getText().toString().trim());
            if (!phoneNumberMatcher.matches()) {
                phone.setError("This field may contain only 10 digit valid Mobile Numbers");
                phone.requestFocus();
                return false;
            }
        }
        //Date Of Birth Validation
        if(dateOfBirth.getText().toString().trim().length() != 0) {
            FormatDate formatDate = new FormatDate();
            if(!formatDate.isDateValid(dateOfBirth.getText().toString().trim())) {
                    dateOfBirth.setError("Date is invalid or wrong format. Use YYYY-MM-DD");
                    dateOfBirth.requestFocus();
                    return false;
            }
        }
        //pin code verification
        if (dateOfBirth.getText().toString().trim().length() != 0 && pinCode.getText().toString().trim().length() != 6) {
            pinCode.setError("Enter 6 digit valid pin number");
            pinCode.requestFocus();
            return false;
        }
        return true;
    }

    private void pickDate() {
        if(datePickerDate == null) {
            //if birthdate is null then set current date
            if (profileDetails.getBirthDate() != null) {
                datePickerDate = profileDetails.getBirthDate().split("-");
            } else {
                final Calendar calendar = Calendar.getInstance();
                datePickerDate = new String[]{Integer.toString(calendar.get(Calendar.YEAR)), Integer.toString(calendar.get(Calendar.MONTH) + 1), Integer.toString(calendar.get(Calendar.DAY_OF_MONTH))};
            }
        }
        DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileDetailsActivity.this,
            new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    // Display Selected date in textbox
                    //add 0 if month r day less than 10 to make it in format 2015-01-01
                    String month;
                    if((monthOfYear + 1) < 10) { // Month is 0 based so add 1
                        month = "0" + (monthOfYear + 1);
                    } else {
                        month = Integer.toString(monthOfYear + 1);
                    }
                    String day;
                    if((dayOfMonth) < 10) {
                        day = "0" + (dayOfMonth);
                    } else {
                        day = Integer.toString(dayOfMonth);
                    }
                    dateOfBirth.setText(year + "-" + month + "-" + day );
                    dateOfBirth.setError(null);//clear the error message if already exist
                    datePickerDate = new String[] { Integer.toString(year), month, day}; //when user click next time this date will be set in picker
                }
            }, Integer.parseInt(datePickerDate[0]), Integer.parseInt(datePickerDate[1]) - 1, Integer.parseInt(datePickerDate[2]));
        datePickerDialog.show();
    }

    private void cancelEditing() {
        //discard the changes & display existing details
        new MaterialDialog.Builder(this)
                .title("Do you really want to discard the changes ?")
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .positiveColorRes(R.color.primary)
                .negativeColorRes(R.color.primary)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        displayProfileDetails(profileDetails);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private ArrayAdapter<String> getDropdownListAdapter(Collection<String> collection) {
        List<String> Choices = new ArrayList<String>(collection);
        Collections.sort(Choices, new Comparator<String>() {
            @Override
            public int compare(String item1, String item2) {
                return item1.compareTo(item2);
            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, Choices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tick_cancel_refresh, menu);
        this.menu = menu;
        menu.setGroupVisible(R.id.editMode, false);
        menu.setGroupVisible(R.id.viewMode, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return super.onOptionsItemSelected(item);
        }

        Runnable action = menuActions.get(item.getItemId());
        if (action != null) {
            action.run();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        imagePickerUtils.permissionsUtils.onRequestPermissionsResult(requestCode, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (imagePickerUtils != null) {
            imagePickerUtils.permissionsUtils.onResume();
        }
    }

    @Override
    public void onBackPressed(){
        //if backpress from edit mode then display the existing profile detail
        if(firstNameRow.getVisibility() == View.VISIBLE) {
            cancelEditing();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onLoaderReset(final Loader<ProfileDetails> loader) {
        // Intentionally left blank
    }

    public InstituteSettings fetchInstituteSetting () {
        DaoSession daoSession = ((TestpressApplication) getApplicationContext()).getDaoSession();
        InstituteSettingsDao instituteSettingsDao = daoSession.getInstituteSettingsDao();
        List<InstituteSettings> instituteSettingsList = instituteSettingsDao.queryBuilder()
                .where(InstituteSettingsDao.Properties.BaseUrl.eq(BASE_URL))
                .list();
        if (instituteSettingsList.size() != 0) {
            return instituteSettingsList.get(0);
        } else {
            finish();
        }

        return null;
    }

    public void fetchSsoLink() {
        new SafeAsyncTask<SsoUrl>() {
            @Override
            public SsoUrl call() throws Exception {
                return serviceProvider.getService(ProfileDetailsActivity.this).getSsoUrl();
            }

            @Override
            protected void onException(final Exception exception) throws RuntimeException {
                super.onException(exception);

                if (exception.getCause() instanceof UnknownHostException) {
                    Toaster.showLong(ProfileDetailsActivity.this, R.string.no_internet);
                }
            }

            @Override
            protected void onSuccess(final SsoUrl ssoLink) throws Exception {
                ssoUrl = ssoLink.getSsoUrl();
            }
        }.execute();
    }
}
