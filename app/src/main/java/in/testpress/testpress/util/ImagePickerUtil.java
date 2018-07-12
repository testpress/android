package in.testpress.testpress.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.theartofdev.edmodo.cropper.CropImage;

import in.testpress.testpress.R;

import static android.app.Activity.RESULT_OK;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE;

public class ImagePickerUtil {

    private View rootLayout;
    private Activity activity;

    public ImagePickerUtil(View rootLayout, Activity activity) {
        this.rootLayout = rootLayout;
        this.activity = activity;
    }

    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data,
                                 ImagePickerActivityResultHandler handler) {

        if (requestCode == PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(activity, data);
            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(activity, imageUri)) {
                // Request permission
                handler.onStoragePermissionsRequired(imageUri);
                ActivityCompat.requestPermissions(
                        activity,
                        new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                        PICK_IMAGE_PERMISSIONS_REQUEST_CODE
                );
            } else {
                // No permissions required or already grunted
                startCropImageActivity(imageUri);
            }
        } else if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                handler.onSuccessfullyImageCropped(result.getUri().getPath());
            } else if (resultCode == CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //noinspection ThrowableResultOfMethodCallIgnored
                Exception exception = result.getError();
                Snackbar.make(rootLayout, exception.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults,
                                           Uri selectedImageUri) {

        if (requestCode == PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (selectedImageUri == null ||
                    (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                // Permission granted show image picker
                startCropImageActivity(selectedImageUri);
            } else {
                Snackbar.make(rootLayout, R.string.testpress_action_cant_done_without_permission,
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    protected void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setAllowFlipping(false)
                .start(activity);
    }

    public interface ImagePickerActivityResultHandler {
        void onStoragePermissionsRequired(Uri selectedImageUri);
        void onSuccessfullyImageCropped(String imagePath);
    }

}
