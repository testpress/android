package in.testpress.testpress.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.kevinsawicki.wishlist.Toaster;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.ProfileDetails;
import in.testpress.testpress.util.FormatDate;
import in.testpress.testpress.util.ImageUtils;
import in.testpress.testpress.util.SafeAsyncTask;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

public class ProfileDetailsActivity extends TestpressFragmentActivity implements LoaderManager.LoaderCallbacks<ProfileDetails> {

    @Inject TestpressServiceProvider serviceProvider;
    @InjectView(R.id.profilePhoto) ImageView profilePhoto;
    @InjectView(R.id.editProfilePhoto) ImageView imageEditButton;
    @InjectView(R.id.displayName) TextView displayName;
    @InjectView(R.id.firstName) EditText firstName;
    @InjectView(R.id.lastName) EditText lastName;
    @InjectView(R.id.mailId) EditText mailId;
    @InjectView(R.id.gender) Spinner gender;
    @InjectView(R.id.address) EditText address;
    @InjectView(R.id.mobileNo) EditText mobileNo;
    @InjectView(R.id.dateOfBirth) EditText dateOfBirth;
    @InjectView(R.id.datepicker) ImageButton datePicker;
    @InjectView(R.id.city) EditText city;
    @InjectView(R.id.state) Spinner state;
    @InjectView(R.id.pinCode) EditText pinCode;
    @InjectView(R.id.firstNameRow) TableRow firstNameRow;
    @InjectView(R.id.lastNameRow) TableRow lastNameRow;
    @InjectView(R.id.mailIdRow) TableRow mailIdRow;
    @InjectView(R.id.genderRow) TableRow genderRow;
    @InjectView(R.id.addressRow) TableRow addressRow;
    @InjectView(R.id.mobileNoRow) TableRow mobileNoRow;
    @InjectView(R.id.cityRow) TableRow cityRow;
    @InjectView(R.id.dobRow) TableRow dobRow;
    @InjectView(R.id.stateRow) TableRow stateRow;
    @InjectView(R.id.pinCodeRow) TableRow pinCodeRow;
    @InjectView(R.id.empty) TextView emptyView;
    @InjectView(R.id.editButton) ImageView editButton;
    @InjectView(R.id.saveDetails) Button saveButton;
    @InjectView(R.id.profileDetails) RelativeLayout profileDetailsView;
    @InjectView(R.id.horizontalProgressBar) ProgressBar horizontalProgressBar;
    ProgressBar progressBar;
    ProfileDetails profileDetails;
    ArrayAdapter<String> genderSpinnerAdapter;
    ArrayAdapter<String> stateSpinnerAdapter;
    Bitmap selectedImage;
    String encodedImage = "";
    String[] datePickerDate;
    ImageLoader imageLoader;
    DisplayImageOptions options;
    Menu menu;
    static final private int SELECT_IMAGE = 100;
    static final private int FETCH_AND_CROP_IMAGE = 500;
    static final private int SAVE_CROPPED_IMAGE = 999;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_detail_layout);
        Injector.inject(this);
        ButterKnife.inject(this);
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
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.drawable.profile_image_sample)
                .showImageOnFail(R.drawable.profile_image_sample)
                .showImageOnLoading(R.drawable.profile_image_sample).build();
        getSupportLoaderManager().initLoader(0, null, this);
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
        if (profileDetails == null) { //loading failed
            Exception exception = ((ThrowableLoader<ProfileDetails>) loader).clearException();
            if(exception.getCause() instanceof UnknownHostException) {
                emptyView.setText(R.string.no_internet);
                emptyView.setVisibility(View.VISIBLE);
                Toaster.showLong(ProfileDetailsActivity.this, R.string.no_internet);
                return;
            } else {
                Toaster.showLong(ProfileDetailsActivity.this, exception.getMessage());
            }
        } else {
            emptyView.setVisibility(View.GONE);
            this.profileDetails = profileDetails;
        }
        profileDetailsView.setVisibility(View.VISIBLE);
        displayProfileDetails(this.profileDetails);
    }

    void displayProfileDetails(ProfileDetails profileDetails) {
        //download and display image from url
        imageLoader.displayImage(profileDetails.getMediumImage(), profilePhoto, options);
        menu.setGroupVisible(R.id.editMode, false);
        menu.setGroupVisible(R.id.viewMode, true);
        setVisibility(View.VISIBLE, new View[]{displayName, editButton});
        setVisibility(View.GONE, new View[]{firstNameRow, lastNameRow, imageEditButton, datePicker});
        displayName.setText(profileDetails.getFirstName() + " " + profileDetails.getLastName());
        handleDetail(mailId, mailIdRow, profileDetails.getEmail());
        handleDetail(mobileNo, mobileNoRow, profileDetails.getPhone());
        handleDetail(gender, genderRow, profileDetails.getGender());
        handleDetail(dateOfBirth, dobRow, profileDetails.getBirthDate());
        handleDetail(address, addressRow, profileDetails.getAddress1());
        handleDetail(city, cityRow, profileDetails.getCity());
        handleDetail(state, stateRow, profileDetails.getState());
        handleDetail(pinCode, pinCodeRow, profileDetails.getZip());
        saveButton.setVisibility(View.GONE);
        setEnabled(false, new View[]{mailId, mobileNo, gender, dateOfBirth, address, city, state, pinCode});
    }

    private void handleDetail(View widget, View viewRow, String detail) {
        //check whether the detail is null & set the visibility
        if (detail != null) {
            viewRow.setVisibility(View.VISIBLE);
            switch (widget.getClass().getName()) {
                case "android.support.v7.widget.AppCompatEditText":
                    ((EditText)widget).setText(detail);
                    break;
                case "android.support.v7.widget.AppCompatSpinner":
                    if(widget == gender) {
                        ((Spinner) widget).setSelection(genderSpinnerAdapter.getPosition(detail));
                    } else if(widget == state){
                        ((Spinner) widget).setSelection(stateSpinnerAdapter.getPosition(detail));
                    }
                    break;
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

    @OnClick(R.id.editButton)
    public void editProfileDetails() {
        menu.setGroupVisible(R.id.editMode, true);
        menu.setGroupVisible(R.id.viewMode, false);
        editButton.setVisibility(View.GONE);
        setVisibility(View.VISIBLE, new View[]{imageEditButton, mailIdRow, firstNameRow, lastNameRow, mobileNoRow, genderRow, dobRow, datePicker, addressRow, cityRow, stateRow, pinCodeRow, saveButton});
        setEnabled(true, new View[]{mailId, mobileNo, gender, dateOfBirth, address, city, state, pinCode});
        firstName.setText(profileDetails.getFirstName());
        lastName.setText(profileDetails.getLastName());
        if (profileDetails.getGender() == null) {
            gender.setSelection(genderSpinnerAdapter.getPosition("--select--"));
        }
        if (profileDetails.getState() == null) {
            state.setSelection(stateSpinnerAdapter.getPosition("--select--"));
        }
    }

    @OnClick(R.id.profilePhoto)
    public void displayProfilePhoto() {
        Intent intent = new Intent(this, ProfilePhotoActivity.class);
        intent.putExtra("profilePhoto", profileDetails.getPhoto());
        startActivityForResult(intent, SELECT_IMAGE);
    }

    @OnClick(R.id.editProfilePhoto)
    public void selectImageFromMobile() {
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, FETCH_AND_CROP_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                //to handle the edit option in ProfilePhotoActivity
                case SELECT_IMAGE:
                    selectImageFromMobile();
                    break;

                //result handling of selected image from gallery
                case FETCH_AND_CROP_IMAGE:
                    if (null != data) {
                        Uri selectedImageUri = data.getData();
                        //get the list of images filepath
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        //using selectedImageUri set the cursor on filepath
                        Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        //get the filepath from cursor
                        String picturePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                        cursor.close();
                        selectedImage = ImageUtils.decodeImage(picturePath, 500, 500);
                        if (selectedImage == null) {
                            new MaterialDialog.Builder(this)
                                    .title("Sorry, this file path is not suitable.\nPlease try another folder")
                                    .positiveText(R.string.ok)
                                    .positiveColorRes(R.color.primary)
                                    .buttonsGravity(GravityEnum.CENTER)
                                    .show();
                            return;
                        }
                        //crop the image
                        Intent intent = new Intent(this, CropImageActivity.class);
                        intent.putExtra("picturePath", picturePath);
                        startActivityForResult(intent, SAVE_CROPPED_IMAGE);
                    }
                    break;

                //handling result of cropped image
                case SAVE_CROPPED_IMAGE:
                    horizontalProgressBar.setVisibility(View.VISIBLE);
                    int rotatedDegree = data.getIntExtra("rotatedDegree", 0);
                    if(rotatedDegree != 0) {
                        Matrix matrix = new Matrix();
                        matrix.setRotate(rotatedDegree);
                        selectedImage = Bitmap.createBitmap(selectedImage, 0, 0, selectedImage.getWidth(), selectedImage.getHeight(), matrix, true);
                    }
                    //encode the image as string
                    ByteArrayOutputStream baostream = new ByteArrayOutputStream();
                    selectedImage.compress(Bitmap.CompressFormat.JPEG, 80, baostream);
                    byte[] byteImage = baostream.toByteArray();
                    // Converting Image byte array into Base64 String
                    encodedImage = Base64.encodeToString(byteImage, Base64.DEFAULT);
                    saveProfilePhoto(data.getIntArrayExtra("croppedImageDetails"));
                    break;
            }
        }
    }

    @OnClick(R.id.saveDetails)
    public void saveDetails() {
        if(validate()) {
            final MaterialDialog progressDialog = new MaterialDialog.Builder(this)
                    .title(R.string.loading)
                    .content(R.string.please_wait)
                    .widgetColorRes(R.color.primary)
                    .progress(true, 0)
                    .show();
            new SafeAsyncTask<ProfileDetails>() {
                public ProfileDetails call() throws Exception {
                    return serviceProvider.getService(ProfileDetailsActivity.this).updateUserDetails(profileDetails.getUrl().replace(Constants.Http.URL_BASE + "/", ""), mailId.getText().toString(),
                            firstName.getText().toString(), lastName.getText().toString(), mobileNo.getText().toString(), Constants.genderChoices.get(gender.getSelectedItem().toString()),
                            dateOfBirth.getText().toString(), address.getText().toString(), city.getText().toString(), Constants.stateChoices.get(state.getSelectedItem().toString()), pinCode.getText().toString());
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

    public void saveProfilePhoto(final int[] croppedImageDetails) {
        new SafeAsyncTask<ProfileDetails>() {
            public ProfileDetails call() throws Exception {
                return serviceProvider.getService(ProfileDetailsActivity.this).updateProfileImage(profileDetails.getUrl().replace(Constants.Http.URL_BASE + "/", ""), encodedImage, croppedImageDetails);
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
                profilePhoto.setImageBitmap(Bitmap.createBitmap(selectedImage, croppedImageDetails[0], croppedImageDetails[1], croppedImageDetails[2], croppedImageDetails[3]));
                ProfileDetailsActivity.this.profileDetails.setPhoto(profileDetails.getPhoto());
                ProfileDetailsActivity.this.profileDetails.setMediumImage(profileDetails.getMediumImage());
                Toaster.showLong(ProfileDetailsActivity.this, "Profile Photo Updated Successfully");
            }
        }.execute();
    }

    private boolean validate() {
        //Email Validation
        if (mailId.getText().toString().trim().length() == 0) {
            mailId.setError("This is a required field");
            mailId.requestFocus();
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mailId.getText().toString().trim()).matches()) {
            mailId.setError("Please enter a valid Email address");
            mailId.requestFocus();
            return false;
        }
        //Phone number Validation
        if (mobileNo.getText().toString().trim().length() == 0) {
            mobileNo.setError("This is a required field");
            mobileNo.requestFocus();
            return false;
        } else {
            Pattern phoneNumberPattern = Pattern.compile("\\d{10}");
            Matcher phoneNumberMatcher = phoneNumberPattern.matcher(mobileNo.getText().toString().trim());
            if (!phoneNumberMatcher.matches()) {
                mobileNo.setError("This field may contain only 10 digit valid Mobile Numbers");
                mobileNo.requestFocus();
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

    @OnClick(R.id.datepicker) public void pickDate() {
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.refresh:
                progressBar.setVisibility(View.VISIBLE);
                getSupportLoaderManager().restartLoader(0, null, this);
                return true;
            case R.id.tick:
                saveDetails();
                return true;
            case R.id.cancel:
                displayProfileDetails(profileDetails);
                return true;
            default:
                return false;
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
}
