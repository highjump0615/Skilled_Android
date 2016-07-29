package com.iliayugai.skilled;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.widget.Emojicon.EmojiconEditText;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class EditProfileActivity extends Activity implements View.OnClickListener {

    private static final String TAG = EditProfileActivity.class.getSimpleName();

    private static final int GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE = 100;
    private static final int GALLERY_OPEN_FOR_BACKGROUND_REQUEST_CODE = 200;

    private static final int MAX_ABOUT_ME_LENGTH = 45;

    private View mLayoutContainer;
    private ParseImageView mImagePhoto;
    private EmojiconEditText mEditAboutMe;
    private TextView mTextAboutMeLength;
    private EmojiconEditText mEditFullName;
    private EditText mEditLocation;
    private TextView mTextUserName;
    private View mLayoutChangePassword;

    private Uri mFileUri;
    private Bitmap mNewBitmap = null;
    private Bitmap mBitmapPhoto = null;
    private Bitmap mBitmapBackground = null;
    private Uri mUrlVideoToPost = null;

    private boolean mUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);
        initTitleBar();
        initViews();
        initData();
    }

    @Override
    public void onBackPressed() {
        setResult(mUpdated ? RESULT_OK : RESULT_CANCELED);
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.image_trend:
                onBackPressed();
                break;

            case R.id.image_user_photo:
                openGallery(GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE);
                break;

            case R.id.btn_set_background:
                openGallery(GALLERY_OPEN_FOR_BACKGROUND_REQUEST_CODE);
                break;

            case R.id.btn_change_password:
                startActivity(new Intent(this, ChangePasswordActivity.class));
                overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                break;

            case R.id.btn_update_it:
                onUpdate();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE
                || requestCode == GALLERY_OPEN_FOR_BACKGROUND_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Video captured and saved to fileUri specified in the Intent
                //Toast.makeText(this, "Video saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();
                if (data == null) {
                    onBackPressed();
                } else {
                    Object object = data.getData();
                    if (object != null) {
                        mFileUri = (Uri) object;
                    }

                    String mimeType = null;
                    String realFilePath;
                    Log.d(TAG, "selected fileName = " + mFileUri.getPath());

                    // Crop image
                    /*if (requestCode == GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE) {
                        Intent viewMediaIntent = new Intent();
                        viewMediaIntent.setAction(android.content.Intent.ACTION_VIEW);
                        File file = new File(mFileUri.getPath());
                        viewMediaIntent.setDataAndType(Uri.fromFile(file), "image*//*");
                        viewMediaIntent.putExtra("crop", "true");
                        viewMediaIntent.putExtra("aspectX", 1);
                        viewMediaIntent.putExtra("aspectY", 1);
                        viewMediaIntent.putExtra("outputX", 140);
                        viewMediaIntent.putExtra("outputY", 140);
                        viewMediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        startActivityForResult(viewMediaIntent, CROP_REQUEST_CODE);
                        return;
                    }*/

                    if (mFileUri.getPath().contains("/external/")) {
                        mimeType = getMimeType(mFileUri);
                        realFilePath = CommonUtils.convertImageUriToFile(this, mFileUri);
                    } else {
                        realFilePath = mFileUri.getPath();
                        String extension = MimeTypeMap.getFileExtensionFromUrl(realFilePath);
                        if (extension != null) {
                            MimeTypeMap mime = MimeTypeMap.getSingleton();
                            mimeType = mime.getMimeTypeFromExtension(extension);
                        }
                    }

                    if (mimeType != null) {
                        if (mimeType.contains("image")) {
                            mNewBitmap = CommonUtils.getBitmapFromUri(Uri.parse(realFilePath));
                            refreshImageViews(requestCode);
                            return;
                        } else if (mimeType.contains("video")) {
                            mNewBitmap = ThumbnailUtils.createVideoThumbnail(realFilePath, MediaStore.Images.Thumbnails.MINI_KIND);
                            mUrlVideoToPost = Uri.parse(realFilePath);
                            refreshImageViews(requestCode);
                            return;
                        }
                    }

                    mNewBitmap = null;
                    mUrlVideoToPost = null;
                }
            }
        }
    }

    /**
     * Initialize TitleBar
     */
    private void initTitleBar() {
        final Resources res = getResources();

        int size = (int) (res.getDimension(R.dimen.navigation_layout_title_bar_height) * Config.mScaleFactor);
        //int margin = (int) (res.getDimension(R.dimen.title_bar_margin) * Config.mScaleFactor);
        int padding = (int) (res.getDimension(R.dimen.title_bar_button_padding) * Config.mScaleFactor);
        float textSize = res.getDimension(R.dimen.navigation_text_nav_title_font_size) * Config.mFontScaleFactor;

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_title_bar);
        ViewGroup.LayoutParams layoutParams = layout.getLayoutParams();
        layoutParams.height = size;
        layout.setLayoutParams(layoutParams);

        // Trend ImageView
        ImageView imageView = (ImageView) findViewById(R.id.image_trend);
        imageView.setOnClickListener(this);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        params.width = params.height = size;
        imageView.setLayoutParams(params);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setImageResource(R.drawable.btn_back_bg);

        // Search ImageView
        imageView = (ImageView) findViewById(R.id.image_search);
        imageView.setVisibility(View.INVISIBLE);

        // Title TextView
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-DemiBold.otf");
        TextView textTitle = (TextView) findViewById(R.id.text_nav_title);
        textTitle.setText(R.string.edit_profile);
        textTitle.setTypeface(typeFace);
        textTitle.setTextSize(textSize);

        View viewEdge = findViewById(R.id.view_h_line);
        params = (RelativeLayout.LayoutParams) viewEdge.getLayoutParams();
        params.height = (int) (res.getDimension(R.dimen.navigation_view_h_line_height) * Config.mScaleFactor);
        viewEdge.setLayoutParams(params);
    }

    private void initViews() {
        Resources resources = getResources();

        int cellHeight = (int) (resources.getDimension(R.dimen.post_title_edit_height) * Config.mScaleFactor);
        int margin = (int) (resources.getDimension(R.dimen.edit_profile_layout_padding) * Config.mScaleFactor);
        int paddingTop = (int) (resources.getDimension(R.dimen.post_layout_image_padding_top) * Config.mScaleFactor);
        int paddingLeft = (int) (resources.getDimension(R.dimen.post_category_text_padding_left) * Config.mScaleFactor);
        float fontSize = resources.getDimension(R.dimen.post_category_button_text_size) * Config.mFontScaleFactor;
        float aboutMeFontSize = resources.getDimension(R.dimen.edit_profile_edit_about_me_font_size) * Config.mFontScaleFactor;
        float smallFontSize = resources.getDimension(R.dimen.small_font_size) * Config.mFontScaleFactor;
        float scaledDensity = resources.getDisplayMetrics().scaledDensity;
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Light.otf");

        /// Photo and about me layout
        mLayoutContainer = findViewById(R.id.layout_profile);
        mLayoutContainer.setPadding(0, margin, 0, 0);

        mLayoutContainer = findViewById(R.id.layout_photo_and_about);
        mLayoutContainer.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop);

        // User Photo ImageView
        mImagePhoto = (ParseImageView) findViewById(R.id.image_user_photo);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mImagePhoto.getLayoutParams();
        params.width = params.height = (int) (resources.getDimension(R.dimen.post_image_size) * Config.mScaleFactor);
        mImagePhoto.setLayoutParams(params);
        mImagePhoto.setOnClickListener(this);

        // About me EditText
        mEditAboutMe = (EmojiconEditText) findViewById(R.id.edit_about);
        params = (ViewGroup.MarginLayoutParams) mEditAboutMe.getLayoutParams();
        params.leftMargin = margin / 2;
        mEditAboutMe.setLayoutParams(params);
        mEditAboutMe.setTypeface(typeface);
        mEditAboutMe.setTextSize(aboutMeFontSize);
        mEditAboutMe.setEmojiconSize((int) (aboutMeFontSize * scaledDensity));
        mEditAboutMe.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = (!TextUtils.isEmpty(s)) ? s.length() : 0;
                mTextAboutMeLength.setText(String.valueOf(MAX_ABOUT_ME_LENGTH - length));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // TextView for length of About me
        mTextAboutMeLength = (TextView) findViewById(R.id.text_about_length);
        mTextAboutMeLength.setTextSize(smallFontSize);

        // FullName EditText
        mEditFullName = (EmojiconEditText) findViewById(R.id.edit_full_name);
        params = (ViewGroup.MarginLayoutParams) mEditFullName.getLayoutParams();
        params.height = cellHeight;
        params.topMargin = paddingTop;
        mEditFullName.setLayoutParams(params);
        mEditFullName.setPadding(paddingLeft, 0, paddingLeft, 0);
        mEditFullName.setTypeface(typeface);
        mEditFullName.setTextSize(fontSize);
        mEditFullName.setEmojiconSize((int) (fontSize * scaledDensity));

        // Location EditText
        mEditLocation = (EditText) findViewById(R.id.edit_location);
        params = (ViewGroup.MarginLayoutParams) mEditLocation.getLayoutParams();
        params.height = cellHeight;
        params.topMargin = paddingTop;
        mEditLocation.setLayoutParams(params);
        mEditLocation.setPadding(paddingLeft, 0, paddingLeft, 0);
        mEditLocation.setTypeface(typeface);
        mEditLocation.setTextSize(fontSize);

        /// Set Profile background layout
        View layout = findViewById(R.id.layout_set_background);
        params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
        params.height = cellHeight;
        params.topMargin = paddingTop;
        layout.setLayoutParams(params);

        Button button = (Button) findViewById(R.id.btn_set_background);
        button.setPadding(paddingLeft, 0, paddingLeft, 0);
        button.setTextSize(fontSize);
        button.setTypeface(typeface);
        button.setOnClickListener(this);

        // User name
        mTextUserName = (TextView) findViewById(R.id.text_username);
        params = (ViewGroup.MarginLayoutParams) mTextUserName.getLayoutParams();
        params.height = cellHeight;
        params.topMargin = paddingTop;
        mTextUserName.setLayoutParams(params);
        mTextUserName.setPadding(paddingLeft, 0, paddingLeft, 0);
        mTextUserName.setTypeface(typeface);
        mTextUserName.setTextSize(fontSize);

        /// Change Password
        mLayoutChangePassword = findViewById(R.id.layout_change_password);
        params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
        params.height = cellHeight;
        params.topMargin = paddingTop;
        mLayoutChangePassword.setLayoutParams(params);

        button = (Button) findViewById(R.id.btn_change_password);
        button.setPadding(paddingLeft, 0, paddingLeft, 0);
        button.setTextSize(fontSize);
        button.setTypeface(typeface);
        button.setOnClickListener(this);

        // Update button
        button = (Button) findViewById(R.id.btn_update_it);
        params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
        params.height = (int) (resources.getDimension(R.dimen.large_button_height) * Config.mScaleFactor);
        button.setLayoutParams(params);
        button.setOnClickListener(this);
    }

    private void initData() {
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser != null) {
            mTextUserName.setText(currentUser.getString("username"));
            mEditFullName.setText(currentUser.getString("fullname"), TextView.BufferType.SPANNABLE);
            mEditLocation.setText(currentUser.getString("location"), TextView.BufferType.SPANNABLE);
            mEditAboutMe.setText(currentUser.getString("about"), TextView.BufferType.SPANNABLE);

            ParseFile photoFile = currentUser.getParseFile("photo");
            if (photoFile != null) {
                photoFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {
                        if (e == null) {
                            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                            mImagePhoto.setImageBitmap(BitmapFactory.decodeStream(byteArrayInputStream));
                        }
                    }
                });
            }

            boolean isLinkedToFacebook = ParseFacebookUtils.isLinked(currentUser);
            boolean isLinkedToTwitter = ParseTwitterUtils.isLinked(currentUser);

            if (isLinkedToFacebook || isLinkedToTwitter) {
                mTextUserName.setVisibility(View.GONE);
                mLayoutChangePassword.setVisibility(View.GONE);
            }
        }
    }

    private void openGallery(int requestCode) {
        if (requestCode == GALLERY_OPEN_FOR_BACKGROUND_REQUEST_CODE) {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image");
            startActivityForResult(galleryIntent, requestCode);
        } else {
            mFileUri = Uri.fromFile(CommonUtils.getOutputMediaFile(true));

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            //intent.putExtra("outputX", 320);
            //intent.putExtra("outputY", 320);
            intent.putExtra("scale", 1);
            intent.putExtra("return-data", false/*return_data*/);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            //intent.putExtra("noFaceDetection",!faceDetection); // lol, negative boolean noFaceDetection
            /*if (circleCrop) {
                intent.putExtra("circleCrop", true);
            }*/

            startActivityForResult(intent, requestCode);
        }
    }

    private void refreshImageViews(int requestCode) {
        if (mNewBitmap == null) return;

        if (requestCode == GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE) {
            mImagePhoto.setImageBitmap(mNewBitmap);
            mBitmapPhoto = mNewBitmap;
        } else if (requestCode == GALLERY_OPEN_FOR_BACKGROUND_REQUEST_CODE) {
            mBitmapBackground = mNewBitmap;
        }
    }

    private String getMimeType(Uri fileUri) {
        ContentResolver cr = this.getContentResolver();
        String mimeType = cr.getType(fileUri);

        Log.d(TAG, "returned mime_type = " + mimeType);
        return mimeType;
    }

    private void onUpdate() {
        final ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser != null) {
            if (mBitmapPhoto != null) {
                final ProgressDialog progressDialog = CommonUtils.createHorizontalProgressDialog(this, getString(R.string.uploading));
                progressDialog.setMax(100);
                progressDialog.setProgress(0);
                progressDialog.show();

                Bitmap bitmap = Bitmap.createScaledBitmap(mBitmapPhoto, 140, 140, true);
                mBitmapPhoto.recycle();

                ByteArrayOutputStream imageData = new ByteArrayOutputStream();

                // JPEG to decrease file size and enable faster uploads & downloads
                //resizeImage.compress(Bitmap.CompressFormat.JPEG, 80, imageData);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageData);

                final ParseFile imageFile = new ParseFile("photo.jpg", imageData.toByteArray());
                imageFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        progressDialog.dismiss();

                        if (e == null) {
                            currentUser.put("photo", imageFile);
                            updateBackgroundImage();
                        } else {
                            CommonUtils.createErrorAlertDialog(EditProfileActivity.this, "Alert", e.getMessage()).show();
                        }
                    }
                }, new ProgressCallback() {
                    @Override
                    public void done(Integer integer) {
                        progressDialog.setProgress(integer);
                    }
                });
            } else {
                updateBackgroundImage();
            }
        }
    }

    private void updateBackgroundImage() {
        final ParseUser currentUser = ParseUser.getCurrentUser();

        if (mBitmapBackground != null) {
            final ProgressDialog progressDialog = CommonUtils.createHorizontalProgressDialog(this, getString(R.string.uploading));
            progressDialog.setMax(100);
            progressDialog.setProgress(0);
            progressDialog.show();

            ByteArrayOutputStream imageData = new ByteArrayOutputStream();

            // JPEG to decrease file size and enable faster uploads & downloads
            //resizeImage.compress(Bitmap.CompressFormat.JPEG, 80, imageData);
            mBitmapBackground.compress(Bitmap.CompressFormat.PNG, 100, imageData);

            final ParseFile imageFile = new ParseFile("back.jpg", imageData.toByteArray());
            imageFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    progressDialog.dismiss();

                    if (e == null) {
                        currentUser.put("background", imageFile);
                        saveUserInfo();
                    } else {
                        CommonUtils.createErrorAlertDialog(EditProfileActivity.this, "Alert", e.getMessage()).show();
                    }
                }
            }, new ProgressCallback() {
                @Override
                public void done(Integer integer) {
                    progressDialog.setProgress(integer);
                }
            });
        } else {
            saveUserInfo();
        }
    }

    private void saveUserInfo() {
        final Dialog progressDialog = CommonUtils.createFullScreenProgress(this);
        progressDialog.show();

        final ParseUser currentUser = ParseUser.getCurrentUser();

        currentUser.put("fullname", mEditFullName.getText().toString());
        currentUser.put("location", mEditLocation.getText().toString());
        currentUser.put("about", mEditAboutMe.getText().toString());

        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                progressDialog.dismiss();
                if (e == null) {
                    mUpdated = true;
                    onBackPressed();
                } else {
                    CommonUtils.createErrorAlertDialog(EditProfileActivity.this, "Alert", e.getMessage()).show();
                }
            }
        });
    }

}
