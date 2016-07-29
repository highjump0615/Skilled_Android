package com.iliayugai.skilled;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.iliayugai.skilled.data.BlogCategory;
import com.iliayugai.skilled.data.BlogData;
import com.iliayugai.skilled.data.FollowingLikeData;
import com.iliayugai.skilled.utils.AmazonWebServiceUtils;
import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.SkilledManager;
import com.iliayugai.skilled.widget.Emojicon.EmojiconEditText;
import com.netcompss.ffmpeg4android_client.BaseWizard;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;
import java.util.HashMap;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class PostActivity extends BaseWizard implements View.OnClickListener {

    private static final String TAG = PostActivity.class.getSimpleName();

    private static final int MAX_TITLE_LENGTH = 30;
    private static final int MAX_CONTENT_LENGTH = 140;

    private static final boolean VIDEO_POST_TESTING = false;

    public static final String MEDIA_LOCATION = "media_location";

    private static final int ONLY_TEXT_POST = -1;
    private static final int MEDIA_TYPE_IMAGE = 0;
    private static final int MEDIA_TYPE_VIDEO = 1;
    private static final int FROM_MEDIA_LIBRARY = 2;

    public static final int POST_REQUEST_CODE = 10000;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CROP_IMAGE_REQUEST_CODE = 150;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private static final int GALLERY_OPEN_REQUEST_CODE = 300;

    public static final int CHOOSE_CATEGORY_REQUEST_CODE = 400;

    private boolean mIsTextPosting = false;
    private Uri mFileUri;

    private int mMediaSourceType = ONLY_TEXT_POST;

    // Data to post
    private BlogData mBlogData = new BlogData();
    private ParseObject mBlog;
    private Bitmap mBitmapToPost = null;
    private Uri mUrlVideoToPost = null;

    // Widget
    private EmojiconEditText mEditPostTitle;
    private TextView mTextTitleLength;
    private ParseImageView mImagePostThumbnail;
    private EmojiconEditText mEditPostContent;
    private TextView mTextContentLength;
    private Button mButtonCategory;
    private ToggleButton mToggleAddLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null && getIntent().hasExtra(MEDIA_LOCATION)) {
            mMediaSourceType = getIntent().getIntExtra(MEDIA_LOCATION, ONLY_TEXT_POST);
            mIsTextPosting = false;
        } else {
            mIsTextPosting = true;
            mBlogData.type = BlogData.BlogText;
        }

        setContentView(R.layout.activity_post);
        initTitleBar();
        initViews();

        switch (mMediaSourceType) {
            case ONLY_TEXT_POST:
                ParseUser currentUser = ParseUser.getCurrentUser();

                // Fetch user
                String objectId = currentUser.getObjectId();
                final Drawable placeholder = getResources().getDrawable(R.drawable.profile_photo_default);

                if (!SkilledManager.mParseUserMap.containsKey(objectId)) {
                    currentUser.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (e == null) {
                                ParseUser user = (ParseUser) parseObject;

                                SkilledManager.mParseUserMap.put(user.getObjectId(), user);
                                SkilledManager.setSquareImage(mImagePostThumbnail, user, "photo", placeholder);
                            }
                        }
                    });
                } else {
                    ParseUser user = SkilledManager.mParseUserMap.get(objectId);

                    SkilledManager.setSquareImage(mImagePostThumbnail, user, "photo", placeholder);
                }
                break;

            case MEDIA_TYPE_IMAGE:
                captureImage();
                break;

            case MEDIA_TYPE_VIDEO:
                recordVideo();
                break;

            case FROM_MEDIA_LIBRARY:
                openGallery();
                break;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action_transcoding_complete");
        registerReceiver(mTranscodingCompleteReceiver, intentFilter);

    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mTranscodingCompleteReceiver);
    }


    @Override
    public void onBackPressed() {
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

            case R.id.btn_choose_category:
                startActivityForResult(new Intent(this, ChooseCategoryActivity2.class),
                        CHOOSE_CATEGORY_REQUEST_CODE);
                overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                break;

            case R.id.button_facebook:
                break;

            case R.id.button_twitter:
                break;

            case R.id.btn_share_it:
                onShare();
                break;
        }
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", mFileUri);
    }

    /*
     * Here we restore the fileUri again
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        mFileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                //Toast.makeText(this, "Image saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();
                //convertImageUriToFile(mFileUri, this);
                mBitmapToPost = CommonUtils.adjustBitmap(mFileUri);
                cropImage();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the photoImage capture
                onBackPressed();
            } else {
                // Image capture failed, advise user
                onBackPressed();
            }
        }

        if (requestCode == CROP_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                //Toast.makeText(this, "Image saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();
                //convertImageUriToFile(mFileUri, this);
                mBitmapToPost = CommonUtils.adjustBitmap(mFileUri);
                refreshViews();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the photoImage capture
                onBackPressed();
            } else {
                // Image capture failed, advise user
                onBackPressed();
            }
        }

        if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Video captured and saved to fileUri specified in the Intent
                //Toast.makeText(this, "Video saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();
                mBitmapToPost = ThumbnailUtils.createVideoThumbnail(mFileUri.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
                mUrlVideoToPost = mFileUri;
                refreshViews();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the video capture
                onBackPressed();
            } else {
                // Video capture failed, advise user
                onBackPressed();
            }
        }

        if (requestCode == GALLERY_OPEN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Video captured and saved to fileUri specified in the Intent
                //Toast.makeText(this, "Video saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();
                if (data == null) {
                    onBackPressed();
                } else {
                    mFileUri = data.getData();
                    String mimeType = null;
                    String realFilePath;
                    Log.d(TAG, "selected fileName = " + mFileUri.getPath());

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
                            mBitmapToPost = CommonUtils.adjustBitmap(Uri.parse(realFilePath));
                            refreshViews();
                            return;
                        } else if (mimeType.contains("video")) {
                            mBitmapToPost = ThumbnailUtils.createVideoThumbnail(realFilePath, MediaStore.Images.Thumbnails.MINI_KIND);
                            mUrlVideoToPost = Uri.parse(realFilePath);
                            refreshViews();
                            return;
                        }
                    }

                    mBitmapToPost = null;
                    mUrlVideoToPost = null;

                    refreshViews();
                }
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the video capture
                onBackPressed();
            } else {
                // Video capture failed, advise user
                onBackPressed();
            }
        }

        if (requestCode == CHOOSE_CATEGORY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String strId = data.getDataString();

                for (BlogCategory blogCategory : SkilledManager.mCategoryList) {
                    if (blogCategory.strId.equals(strId)) {
                        mBlogData.category = blogCategory;
                        break;
                    }
                }

                mButtonCategory.setText(mBlogData.category.strName);
            }
        }
    }

    private void captureImage() {
        /*
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the photoImage
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the photoImage file name

        // start the photoImage capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        */

        /*
        //create parameters for Intent with filename
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, IMAGE_DIRECTORY_NAME);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");

        //imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
        //mFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        */

        try {
            mFileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

            //create new Intent
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        } catch (ActivityNotFoundException anfe) {
            CommonUtils.createErrorAlertDialog(this, "Alert", "Your device doesn't support capturing images!");
        }
    }

    private void cropImage() {
        try {
            //create new Intent
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            intent.putExtra("crop", "true");
            intent.setDataAndType(mFileUri, "image/*");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            //intent.putExtra("scale", true);
            //intent.putExtra("scaleUpIfNeeded", true);
            //intent.putExtra("outputX", 640); // max value
            //intent.putExtra("outputY", 640);
            intent.putExtra("setWallpaper", false);
            intent.putExtra("return-data", false);
            startActivityForResult(intent, CROP_IMAGE_REQUEST_CODE);
        } catch (ActivityNotFoundException anfe) {
            CommonUtils.createErrorAlertDialog(this, "Alert", "Your device doesn't support capturing images!");
        }
    }

    /*
     * Recording video
     */
    private void recordVideo() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

            mFileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

            // set video quality
            // 1- for high quality video, 0 - for mms quality video
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);

            // start the video capture Intent
            startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
        } catch (ActivityNotFoundException anfe) {
            CommonUtils.createErrorAlertDialog(this, "Alert", "Your device doesn't support capturing video!");
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType(mIsTextPosting ? "image/*" : "video/*,image/*");
        startActivityForResult(galleryIntent, GALLERY_OPEN_REQUEST_CODE);
    }

    private void onShare() {
        // input check
        if (mBlogData.type == BlogData.BlogText) {
            if (TextUtils.isEmpty(mEditPostTitle.getText().toString())) {
                CommonUtils.createErrorAlertDialog(this, "Alert", "Input the title").show();
                return;
            }
        }

        if (TextUtils.isEmpty(mEditPostContent.getText().toString())) {
            CommonUtils.createErrorAlertDialog(this, "Alert", "Input the contents to post").show();
            return;
        }

        if (mBlogData.category == null) {
            CommonUtils.createErrorAlertDialog(this, "Alert", "Select the category").show();
            return;
        }

        mBlogData.strTitle = mEditPostTitle.getText().toString();
        mBlogData.strContent = mEditPostContent.getText().toString();

        mBlog = ParseObject.create("Blogs");
        mBlog.put("type", mBlogData.type);
        mBlog.put("user", ParseUser.getCurrentUser());

        if (mBlogData.type == BlogData.BlogText) {
            mBlog.put("title", mBlogData.strTitle);
        } else {
            //Bitmap resizeImage = CommonUtils.resizeImageWithContentMode(mBitmapToPost, CommonUtils.ContentModeScaleAspectFit, new Point(320, 185), true);
            Bitmap thumbnailImage = Bitmap.createScaledBitmap(mBitmapToPost, 86, 86, true);

            ByteArrayOutputStream imageData = new ByteArrayOutputStream();
            ByteArrayOutputStream thumbnailImageData = new ByteArrayOutputStream();

            // JPEG to decrease file size and enable faster uploads & downloads
            //resizeImage.compress(Bitmap.CompressFormat.JPEG, 80, imageData);
            mBitmapToPost.compress(Bitmap.CompressFormat.PNG, 100, imageData);
            thumbnailImage.compress(Bitmap.CompressFormat.PNG, 100, thumbnailImageData);

            if (/*imageData.size() == 0 || */thumbnailImageData.size() == 0) {
                CommonUtils.createErrorAlertDialog(this, "Alert", "Invalid Image to Post").show();
                return;
            }

            mBlog.put("image", new ParseFile(imageData.toByteArray()));
            mBlog.put("thumbnail", new ParseFile(thumbnailImageData.toByteArray()));

            if (mBlogData.type == BlogData.BlogVideo) {

                //NSData *videoData = [NSData dataWithContentsOfURL:self.mUrlVideoToPost];

//                ParseUser user = ParseUser.getCurrentUser();
//                long nTimeStamp = new Date().getTime();
//
//                String strFileName = String.format("%s%d.mp4", user.getObjectId(), nTimeStamp);

                onAdjustVideoRotation();


                // Upload image data.  Remember to set the content type.
                // new S3PutObjectTask().execute(strFileName);

//                mBlog.put("video", strFileName);
            }
        }

        mBlog.put("text", mBlogData.strContent);
        mBlog.put("category", mBlogData.category.strId);

        if (mBlogData.type == BlogData.BlogText || mBlogData.type == BlogData.BlogImage) {
            saveBlogData();
        }
    }

    BroadcastReceiver mTranscodingCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals("action_transcoding_complete")) {
                Log.e(TAG, "mTranscodingCompleteReceiver onReceive() called");
                ParseUser user = ParseUser.getCurrentUser();
                long nTimeStamp = new Date().getTime();

                String strFileName = String.format("%s%d.mp4", user.getObjectId(), nTimeStamp);
                Log.e(TAG, "strFileName = " + strFileName);
                // Upload image data.  Remember to set the content type.
                new S3PutObjectTask().execute(strFileName);
                mBlog.put("video", strFileName);
            }

        }
    };

    private void onAdjustVideoRotation() {


        String filePath = mUrlVideoToPost.getPath();

        // Get information of video file
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
        try {
            metaRetriever.setDataSource(filePath);
            String heightString = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String widthString = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

            fmmr.setDataSource(filePath);
            String rotation = fmmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

            Log.i(TAG, "video size = " + widthString + "x" + heightString + ", rotation = " + rotation);

            // Set width and height
            int width, height;

            try {
                width = Integer.parseInt(widthString);
            } catch (NumberFormatException ex) {
                width = 320;
            }
            try {
                height = Integer.parseInt(heightString);
            } catch (NumberFormatException ex) {
                height = 320;
            }

            if (width >= height) {
                height = 320 * height / width;
                width = 320;
            } else {
                width = 320 * width / height;
                height = 320;
            }

            // Set orientation
            String mode = "";
            if ("90".equals(rotation)) {
                mode = "-vf transpose=1 ";

                int tmp = height;
                height = width;
                width = tmp;
            } else if ("180".equals(rotation)) {
                mode = "-vf vflip,hflip ";
            } else if ("270".equals(rotation)) {
                mode = "-vf transpose=2 ";

                int tmp = height;
                height = width;
                width = tmp;
            }

            // Adjust rotation

            int extensionIndex = filePath.lastIndexOf(".");
            String outFileName = filePath.substring(0, extensionIndex);
            String outFileExtension = filePath.substring(extensionIndex + 1, filePath.length());
            outFileName += "_out";
            String outFilePath = outFileName + "." + outFileExtension;
            String command = getString(R.string.commandText, filePath, mode, width, height, outFilePath);

            mUrlVideoToPost = Uri.parse(outFilePath);

            int index = filePath.lastIndexOf("/");

            Log.i(TAG, "command = " + command);
            String workingFolder = filePath.substring(0, index) + "/";
            Log.i(TAG, "workingFolder = " + workingFolder);
            setWorkingFolder(workingFolder);

            File licenseFile = new File(workingFolder + "ffmpeglicense.lic");
            if (licenseFile.exists()) {
                Log.i(TAG, "licenseFile Exists");
                licenseFile.delete();
            }

            File vkLogFile = new File(workingFolder + "vk.log");
            if (vkLogFile.exists()) {
                Log.i(TAG, "vkLogFile Exists");
                vkLogFile.delete();
            }

            copyLicenseAndDemoFilesFromAssetsToSDIfNeeded();
            setCommand(command);
            runTranscoing();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "can't open video file = " + filePath);
        } finally {
            if (metaRetriever != null)
                metaRetriever.release();
            if (fmmr != null)
                fmmr.release();
        }
    }

    private void saveBlogData() {
        if (VIDEO_POST_TESTING) return;

        final Dialog progressDialog = CommonUtils.createFullScreenProgress(this);
        progressDialog.show();

        mBlog.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException error) {
                if (error == null) {
                    // add object
                    BlogData blog = new BlogData();
                    blog.strId = mBlog.getObjectId();
                    blog.type = mBlog.getInt("type");
                    blog.strTitle = mBlog.getString("title");
                    blog.strContent = mBlog.getString("text");
                    blog.strVideoName = mBlog.getString("video");
                    blog.photoImage = mBlog.getParseFile("image");
                    blog.date = mBlog.getCreatedAt();
                    blog.user = ParseUser.getCurrentUser();
                    blog.object = mBlog;
                    blog.bLiked = 0;
                    blog.nLikeCount = 0;

                    // set category
                    for (BlogCategory cate : SkilledManager.mCategoryList) {
                        if (cate.strId.equals(mBlog.getString("category"))) {
                            blog.category = cate;
                            break;
                        }
                    }

                    ProfileActivity.mBlogList.add(0, blog);

                    if (blog.type > BlogData.BlogText) {
                        ProfileActivity.mImageBlogList.add(0, mBlog);
                    }

                    // check mentioning and send notification
                    String strContent = blog.strContent;
                    int position;

                    while ((position = strContent.indexOf("@")) != -1) {

                        String strToCompare = strContent.substring(position + 1);

                        for (FollowingLikeData followData : SkilledManager.mFollowingList) {

                            if (strToCompare.contains(followData.username)) {
                                // Save mentionObj
                                ParseObject mentionObj = ParseObject.create("Mentions");
                                mentionObj.put("blog", mBlogData.object);
                                mentionObj.put("user", ParseUser.getCurrentUser());
                                mentionObj.put("username", SkilledManager.getUserNameToShow(ParseUser.getCurrentUser()));
                                mentionObj.put("targetuser", followData.userObject);
                                if (mBlogData.photoImage != null)
                                    mentionObj.put("thumbnail", mBlogData.photoImage);
                                mentionObj.saveInBackground();

                                // send notification to commented user
                                ParseQuery parseQuery = ParseInstallation.getQuery();
                                parseQuery.whereEqualTo("user", mBlogData.user);

                                ParsePush push = new ParsePush();
                                push.setQuery(parseQuery);

                                HashMap<String, Object> params = new HashMap<String, Object>();
                                params.put("alert", String.format("%s mentioned you in the comment\n%s",
                                        SkilledManager.getUserNameToShow(ParseUser.getCurrentUser()),
                                        blog.strContent));
                                params.put("badge", "Increment");
                                params.put("sound", "cheering.caf");
                                params.put("notifyType", "mention");
                                params.put("notifyBlog", mBlogData.object.getObjectId());

                                push.setData(new JSONObject(params));
                                push.sendInBackground();

                                break;
                            }
                        }

                        strContent = strToCompare;
                    }

                    setResult(RESULT_OK);
                    onBackPressed();
                } else {
                    CommonUtils.createErrorAlertDialog(PostActivity.this, "Alert", error.getMessage()).show();
                }

                progressDialog.dismiss();
            }
        });
    }

    /**
     * Creating file uri to store photoImage/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(CommonUtils.getOutputMediaFile(type == MEDIA_TYPE_IMAGE));
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
        textTitle.setText(R.string.post);
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
        int padding = (int) (resources.getDimension(R.dimen.post_layout_padding) * Config.mScaleFactor);
        int layoutMarginTop = (int) (resources.getDimension(R.dimen.post_content_layout_margin_top) * Config.mScaleFactor);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Light.otf");
        float smallFontSize = resources.getDimension(R.dimen.small_font_size) * Config.mFontScaleFactor;

        /// Title view
        View layout = findViewById(R.id.layout_post);
        layout.setPadding(0, layoutMarginTop, 0, 0);

        // Title TextView for Text post
        mEditPostTitle = (EmojiconEditText) findViewById(R.id.edit_title);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mEditPostTitle.getLayoutParams();
        params.height = cellHeight;
        params.bottomMargin = (int) (resources.getDimension(R.dimen.post_content_margin_top) * Config.mScaleFactor);
        mEditPostTitle.setLayoutParams(params);
        mEditPostTitle.setPadding(padding, 0, padding, 0);
        mEditPostTitle.setTypeface(typeface);

        float textSize = resources.getDimension(R.dimen.post_title_edit_size) * Config.mFontScaleFactor;
        float scaledDensity = resources.getDisplayMetrics().scaledDensity;
        mEditPostTitle.setTextSize(textSize);
        mEditPostTitle.setEmojiconSize((int) (textSize * scaledDensity));
        mEditPostTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = (!TextUtils.isEmpty(s)) ? s.length() : 0;
                mTextTitleLength.setText(String.valueOf(MAX_TITLE_LENGTH - length));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // TextView for Title length
        mTextTitleLength = (TextView) findViewById(R.id.text_title_length);
        int paddingBottom = (int) (resources.getDimension(R.dimen.post_title_length_paddingBottom) * Config.mScaleFactor);
        mTextTitleLength.setPadding(0, 0, padding, paddingBottom);
        mTextTitleLength.setTextSize(smallFontSize);

        // Content View
        layout = findViewById(R.id.layout_image_and_content);
        int paddingTop = (int) (resources.getDimension(R.dimen.post_layout_image_padding_top) * Config.mScaleFactor);
        layout.setPadding(padding, paddingTop, padding, paddingTop);

        // TextView for Content length
        mTextContentLength = (TextView) findViewById(R.id.text_content_length);
        mTextContentLength.setTextSize(smallFontSize);

        // Post Image
        mImagePostThumbnail = (ParseImageView) findViewById(R.id.image_post_photo);
        params = (ViewGroup.MarginLayoutParams) mImagePostThumbnail.getLayoutParams();
        params.width = params.height = (int) (resources.getDimension(R.dimen.post_image_size) * Config.mScaleFactor);
        mImagePostThumbnail.setLayoutParams(params);

        // Content EditText
        mEditPostContent = (EmojiconEditText) findViewById(R.id.edit_content);
        params = (ViewGroup.MarginLayoutParams) mEditPostContent.getLayoutParams();
        params.leftMargin = (int) (resources.getDimension(R.dimen.post_content_edit_margin_left) * Config.mScaleFactor);
        mEditPostContent.setLayoutParams(params);
        mEditPostContent.setTypeface(typeface);

        mEditPostContent.setTextSize(smallFontSize);
        mEditPostContent.setEmojiconSize((int) (smallFontSize * scaledDensity));
        mEditPostContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = (!TextUtils.isEmpty(s)) ? s.length() : 0;
                mTextContentLength.setText(String.valueOf(MAX_CONTENT_LENGTH - length));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // TAG People
        ImageView imageView = (ImageView) findViewById(R.id.image_tag_people);
        params = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
        params.height = cellHeight;
        params.topMargin = padding;
        imageView.setLayoutParams(params);

        /// Choose category cell
        layout = findViewById(R.id.layout_choose_category);
        params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
        params.height = cellHeight;
        params.topMargin = (int) (resources.getDimension(R.dimen.post_layout_category_margin_top) * Config.mScaleFactor);
        layout.setLayoutParams(params);

        // category button
        mButtonCategory = (Button) findViewById(R.id.btn_choose_category);
        int paddingLeft = (int) (resources.getDimension(R.dimen.post_category_text_padding_left) * Config.mScaleFactor);
        mButtonCategory.setPadding(paddingLeft, 0, 0, 0);
        mButtonCategory.setTextSize(resources.getDimension(R.dimen.post_category_button_text_size) * Config.mFontScaleFactor);
        mButtonCategory.setTypeface(typeface);
        mButtonCategory.setOnClickListener(this);

        /// Post option view
        layout = findViewById(R.id.layout_location);
        params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
        params.height = (int) (resources.getDimension(R.dimen.post_option_layout_height) * Config.mScaleFactor);
        params.topMargin = (int) (resources.getDimension(R.dimen.post_content_margin_top) * Config.mScaleFactor);
        layout.setLayoutParams(params);

        // toggle button
        mToggleAddLocation = (ToggleButton) findViewById(R.id.toggle_add_location);
        params = (ViewGroup.MarginLayoutParams) mToggleAddLocation.getLayoutParams();
        params.width = (int) (resources.getDimension(R.dimen.toggle_button_width) * Config.mScaleFactor);
        params.height = (int) (resources.getDimension(R.dimen.toggle_button_height) * Config.mScaleFactor);
        params.rightMargin = padding;
        mToggleAddLocation.setLayoutParams(params);
        mToggleAddLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });

        // Share button layout
        layout = findViewById(R.id.layout_share);
        params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
        params.height = cellHeight;
        params.topMargin = padding;
        layout.setLayoutParams(params);

        findViewById(R.id.button_facebook).setOnClickListener(this);
        findViewById(R.id.button_twitter).setOnClickListener(this);

        // Share it button
        Button button = (Button) findViewById(R.id.btn_share_it);
        params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
        params.height = (int) (resources.getDimension(R.dimen.large_button_height) * Config.mScaleFactor);
        params.topMargin = (int) (resources.getDimension(R.dimen.post_share_button_margin_top) * Config.mScaleFactor);
        button.setLayoutParams(params);
        button.setOnClickListener(this);
    }

    private void refreshViews() {
        if (mBitmapToPost != null) {
            if (mUrlVideoToPost != null) {
                mBlogData.type = BlogData.BlogVideo;
            } else {
                mBlogData.type = BlogData.BlogImage;
            }

            mImagePostThumbnail.setImageBitmap(mBitmapToPost);

            mEditPostTitle.setVisibility(View.GONE);
            mTextTitleLength.setVisibility(View.GONE);
        } else {
            mBlogData.type = BlogData.BlogText;

            final ParseUser currentUser = ParseUser.getCurrentUser();
            // Fetch user
            String objectId = currentUser.getObjectId();
            final Drawable placeholder = getResources().getDrawable(R.drawable.profile_photo_default);

            if (!SkilledManager.mParseUserMap.containsKey(objectId)) {
                currentUser.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e == null) {
                            ParseUser user = (ParseUser) parseObject;

                            SkilledManager.mParseUserMap.put(user.getObjectId(), user);
                            SkilledManager.setSquareImage(mImagePostThumbnail, user, "photo", placeholder);
                        }
                    }
                });
            } else {
                ParseUser user = SkilledManager.mParseUserMap.get(objectId);

                SkilledManager.setSquareImage(mImagePostThumbnail, user, "photo", placeholder);
            }
        }
    }

    private String getMimeType(Uri fileUri) {
        ContentResolver cr = this.getContentResolver();
        String mimeType = cr.getType(fileUri);

        Log.d(TAG, "returned mime_type = " + mimeType);
        return mimeType;
    }

    /////////////
    private class S3TaskResult {
        public Uri uri = null;
        public String errorMessage = null;
    }

    private class S3PutObjectTask extends AsyncTask<String, Integer, S3TaskResult> {
        private ProgressDialog mUploadProgressDialog;

        @Override
        protected void onPreExecute() {
            // Set up progress dialog
            mUploadProgressDialog = new ProgressDialog(PostActivity.this);
            mUploadProgressDialog.setMessage(getString(R.string.uploading));
            mUploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mUploadProgressDialog.setCancelable(false);
            mUploadProgressDialog.show();
        }

        @Override
        protected void onPostExecute(S3TaskResult s3TaskResult) {
            mUploadProgressDialog.dismiss();

            if (s3TaskResult.errorMessage != null) {
                CommonUtils.createErrorAlertDialog(PostActivity.this, "Alert", s3TaskResult.errorMessage).show();
            } else {
                saveBlogData();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // Update the progress bar
            mUploadProgressDialog.setProgress(values[0]);
        }

        @Override
        protected S3TaskResult doInBackground(String... params) {
            File videoFile = new File(mUrlVideoToPost.getPath());
            mUploadProgressDialog.setMax((int) videoFile.length());

            S3TaskResult result = new S3TaskResult();
            result.uri = Uri.parse(params[0]);

            Log.d(TAG, "upload key = " + params[0]);

            AmazonWebServiceUtils.uploadVideo(params[0], mUrlVideoToPost.getPath(), new ProgressListener() {
                int total = 0;

                @Override
                public void progressChanged(ProgressEvent progressEvent) {
                    total += (int) progressEvent.getBytesTransferred();
                    publishProgress(total);
                }
            });

            return result;
        }
    }

}
