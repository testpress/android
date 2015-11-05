package in.testpress.testpress.ui;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.edmodo.cropper.CropImageView;

import in.testpress.testpress.R;

public class CropImageActivity extends TestpressFragmentActivity {
    CropImageView cropImageView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_image_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        cropImageView = (CropImageView)findViewById(R.id.CropImageView);
        cropImageView.setImageBitmap(BitmapFactory.decodeFile(getIntent().getStringExtra("picturePath")));
        ((TextView) findViewById(R.id.done)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the cropped coordinates
                RectF rect = cropImageView.getActualCropRect();
                //details to crop in server
                int[] croppedImageDetails = new int[]{(int) rect.left, (int) rect.top, (int) rect.width(), (int) rect.height()};
                Intent intent = new Intent();
                intent.putExtra("croppedImageDetails", croppedImageDetails);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        ((TextView) findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return false;
    }
}
