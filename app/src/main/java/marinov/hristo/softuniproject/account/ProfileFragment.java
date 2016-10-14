package marinov.hristo.softuniproject.account;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import marinov.hristo.softuniproject.R;
import marinov.hristo.softuniproject.models.User;
import marinov.hristo.softuniproject.utils.GPSTracker;
import marinov.hristo.softuniproject.utils.RoundedImageView;

/**
 * @author Hristo Marinov (christo_marinov@abv.bg).
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

    long userId;
    User user;
    Editor editorLogin;
    SharedPreferences prefLogin;
    String mCurrentPhotoPath;
    String[] optionsArray;
    private GPSTracker mGps;
    private RoundedImageView mRoundImage;
    private static final int USER_PHOTO = 35;
    private static final int REQUEST_PHOTO_CAPTURE = 1;

    public interface IProfile {
        public void onProfile(int action);
    }

    IProfile callback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        callback = (IProfile) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        mRoundImage = (RoundedImageView) view.findViewById(R.id.roundImageView);
        EditText mUsername = (EditText) view.findViewById(R.id.username_profile);
        EditText mFirstName = (EditText) view.findViewById(R.id.fname_profile);
        EditText mLastName = (EditText) view.findViewById(R.id.lname_profile);
        Button mChangePhotoBtn = (Button) view.findViewById(R.id.change_photo);
        Button mLogoutBtn = (Button) view.findViewById(R.id.logout_profile);

        mChangePhotoBtn.setOnClickListener(this);
        mLogoutBtn.setOnClickListener(this);

        mGps = new GPSTracker(getActivity());

        prefLogin = getActivity().getSharedPreferences("activity_login", Context.MODE_PRIVATE);
        editorLogin = prefLogin.edit();

        userId = prefLogin.getLong("userId", -1);

        // Get user data if exist
        user = User.findById(User.class, userId);
        if (user != null) {
            mUsername.setText(user.getUsername());
            mFirstName.setText(user.getFirstName());
            mLastName.setText(user.getLastName());

            // Get image file
            File saveFile = new File(user.getImagePath());
            Uri path = Uri.fromFile(saveFile);

            try {
                mRoundImage.setImageBitmap(getBitmapFromUri(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_photo:
                createDialog();

                break;
            case R.id.logout_profile:
                editorLogin.putLong("userId", -1);
                editorLogin.commit();

                if (callback != null)
                    callback.onProfile(R.string.login);

                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mGps.stopUsingGPS();
    }

    /**
     * Update user imagePath with selected image path
     *
     * @param photoPath file path
     */
    private void updateUserPhoto(String photoPath) {
        user = User.findById(User.class, userId);
        if (user != null) {
            user.setImagePath(photoPath);
            user.save();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == USER_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                try {
                    Uri selectedImageUri = data.getData();
                    String photoPath = getPathFromURI(selectedImageUri);

                    Bitmap profileBitmap = getBitmapFromUri(selectedImageUri);
                    mRoundImage.setImageBitmap(profileBitmap);

                    updateUserPhoto(photoPath);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_PHOTO_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    File saveFile = new File(mCurrentPhotoPath);
                    Uri path = Uri.fromFile(saveFile);

                    Bitmap profileBitmap = getBitmapFromUri(path);
                    mRoundImage.setImageBitmap(profileBitmap);

                    updateUserPhoto(mCurrentPhotoPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Delete the empty file
                File file = new File(mCurrentPhotoPath);
                file.delete();
                if (file.exists()) {
                    try {
                        file.getCanonicalFile().delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (file.exists()) {
                        getActivity().getApplicationContext().deleteFile(file.getName());
                    }
                }
            }
        }
    }

    private void createDialog() {
        if (checkCameraHardware(getActivity())) {
            optionsArray = getResources().getStringArray(R.array.options_array);
        } else {
            optionsArray = getResources().getStringArray(R.array.options_array_short);
        }

        // , android.R.style.Theme_Holo_Light_Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.add_photo)
                .setItems(optionsArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        switch (position) {
                            case 0:
                                getImage();

                                break;
                            case 1:
                                dispatchTakePictureIntent();

                                break;
                        }
                    }
                });
        builder.create();
        builder.show();
    }

    /**
     * Check the device have camera
     *
     * @param context Context
     * @return true if the device have camera, otherwise false
     */
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * Get image from Gallery
     */
    public void getImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, USER_PHOTO);
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getApplicationContext().getContentResolver().query(contentUri, proj, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                res = cursor.getString(column_index);
            }
            cursor.close();
        }

        return res;
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        Bitmap image = null;
        ParcelFileDescriptor parcelFileDescriptor = getActivity().getApplicationContext().getContentResolver().openFileDescriptor(uri, "r");
        if (parcelFileDescriptor != null) {
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
        }
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getApplicationContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity().getApplicationContext(), "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_PHOTO_CAPTURE);
            }
        }
    }

    /**
     * Create Image file in your memory
     *
     * @return image
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        String city = getString(R.string.unknown);

        // check if GPS enabled
        if (mGps.canGetLocation()) {
            double latitude = mGps.getLatitude();
            double longitude = mGps.getLongitude();

            try {
                city = getAddress(latitude, longitude);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mGps.showSettingsAlert();
        }

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(new Date());
        String imageFileName = String.format("%s_%s_%s", "IMG", city, timeStamp);
        File storageDir = getActivity().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file path
        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

    private String getAddress(double lat, double lng) throws IOException {
        Geocoder gcd = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
        List<Address> addresses = gcd.getFromLocation(lat, lng, 1);
        if (addresses.size() > 0)
            return addresses.get(0).getLocality();

        return getString(R.string.unknown);
    }
}
