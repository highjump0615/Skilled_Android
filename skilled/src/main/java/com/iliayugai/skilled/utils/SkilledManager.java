package com.iliayugai.skilled.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.iliayugai.skilled.data.BlogCategory;
import com.iliayugai.skilled.data.FollowingLikeData;
import com.iliayugai.skilled.widget.RoundedAvatarDrawable;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

public class SkilledManager {

    private static final String TAG = SkilledManager.class.getSimpleName();

    /* Constant definition */
    public static final String PUSH_ACTION_INTENT = "com.iliayugai.skilled.UPDATE_STATUS";

    public static final int IMAGE_NULL = -1;
    public static final int IMAGE_UNLOADED = 0;
    public static final int IMAGE_LOADED = 1;

    /* Global variable definition */
    public static ArrayList<BlogCategory> mCategoryList = new ArrayList<BlogCategory>();
    public static ArrayList<FollowingLikeData> mFollowingList = new ArrayList<FollowingLikeData>();
    public static HashMap<String, ParseUser> mParseUserMap = new HashMap<String, ParseUser>();
    public static ArrayList<ParseUser> sParseUserList = new ArrayList<ParseUser>();
    public static ArrayList<String> sParseUserNameList = new ArrayList<String>();

    /* Variables for Push notification */
    public static String mStrNotifyType;
    public static ParseObject mNotifyBlogObject;

    /**
     * Set categories which user selects category from category list.
     */
    public static void setCategories(Context context, List<ParseObject> objects) {
        if (mCategoryList.size() > 0) return;

        // make blog category objects
        for (ParseObject object : objects) {
            BlogCategory newCategory = new BlogCategory(object.getObjectId(), object.getString("name"));

            mCategoryList.add(newCategory);
        }

        // set parent objects
        for (ParseObject object : objects) {
            BlogCategory category = null;

            if (!TextUtils.isEmpty(object.getString("parentId"))) {
                for (BlogCategory blogCategory : mCategoryList) {
                    if (blogCategory.strId.equals(object.getObjectId())) {
                        category = blogCategory;
                        break;
                    }
                }

                if (category != null) {
                    for (BlogCategory blogCategory : mCategoryList) {
                        if (blogCategory.strId.equals(object.getString("parentId"))) {
                            category.parent = blogCategory;
                            break;
                        }
                    }
                }
            }
        }

        // set row values
        int nIndex = 0;
        int nSubIndex = 0;

        for (BlogCategory category : mCategoryList) {
            if (category.parent != null) {
                category.nRowNum = nSubIndex;

                nSubIndex++;
            } else {
                category.nRowNum = nIndex;

                nIndex++;
                nSubIndex = 0;
            }
        }

        // determine whether has children or not
        for (BlogCategory category : mCategoryList) {
            if (category.parent != null && !category.parent.bHasSubItem) {
                category.parent.bHasSubItem = true;
            }
        }

        // load the selection info from SharedPreference
        //loadSelectedCategories(context);
    }

    /**
     * Save selected categories to private directory.
     */
    public static void saveSelectedCategories(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (BlogCategory category : mCategoryList) {
            editor.putInt(category.strId, category.nSelected);
        }
        editor.apply();
    }

    /**
     * Load selected categories from private directory.
     */
    private static void loadSelectedCategories(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        for (BlogCategory category : mCategoryList) {
            category.nSelected = sharedPreferences.getInt(category.strId, 1);
        }
    }

    /**
     * Set only one category to ON in category list.
     */
    public static void setCategory(BlogCategory category) {
        for (BlogCategory blogCategory : mCategoryList) {
            if (blogCategory.equals(category))
                blogCategory.nSelected = 1;
            else
                blogCategory.nSelected = 0;
        }
    }

    /**
     * Get index of category which user selects.
     */
    public static int getSelectedCategory() {
        int result = -1;

        for (int  i = 0; i < mCategoryList.size(); i++) {
            BlogCategory category = mCategoryList.get(i);
            if (category.nSelected == 1) {
                result = i;
                break;
            }
        }

        return result;
    }

    /**
     * Get user name from ParseUser variable.
     */
    public static String getUserNameToShow(ParseUser user) {
        if (user == null) return "";

        String userName = user.getString("fullname");

        if (!TextUtils.isEmpty(userName))
            return userName;
        else
            return user.getUsername();
    }

    /**
     * Check if viewHolder has already loaded specific image which has given name by filename.
     */
    public static int isImageLoaded(ParseImageView imageView, ParseUser parseUser, String fileName) {
        if (parseUser == null || imageView == null) return IMAGE_NULL;

        ParseFile photoFile = parseUser.getParseFile(fileName);

        if (photoFile != null) {
            if (photoFile.getUrl().equals(imageView.getTag()))
                return IMAGE_LOADED;
            else
                return IMAGE_UNLOADED;
        } else
            return IMAGE_NULL;
    }

    /**
     * Set image url string after loading image to prevent reload image.
     */
    /*public static void setLoadedImageUrl(IViewHolder viewHolder, ParseUser parseUser, String fileName) {
        if (viewHolder == null) return;

        if (parseUser == null) {
            viewHolder.photoUrl = "";
            return;
        }

        ParseFile photoFile = parseUser.getParseFile(fileName);

        if (photoFile != null) {
            viewHolder.photoUrl = photoFile.getUrl();
        }
    }*/

    /**
     * Set round image to imageView with given filename.
     */
    public static void setAvatarImage(final ParseImageView imageView,
                                      final ParseUser parseUser,
                                      final String fileName,
                                      final Drawable placeholder) {

        if (imageView == null) return;
        imageView.setTag("");

        if (parseUser == null || TextUtils.isEmpty(fileName)) {
            imageView.setImageDrawable(placeholder);
            return;
        }

        final ParseFile photoFile = parseUser.getParseFile(fileName);

        if (photoFile != null) {
            imageView.setPlaceholder(placeholder);
            imageView.setParseFile(photoFile);

            try {
                imageView.loadInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {
                        if (e == null) {
                            imageView.setImageDrawable(new RoundedAvatarDrawable(bytes));
                            imageView.setTag(photoFile.getUrl());
                        } else {
                            imageView.setImageDrawable(placeholder);
                        }
                    }
                });
            } catch (RejectedExecutionException e) {
                if (Config.DEBUG) e.printStackTrace();
            }
        } else {
            imageView.setImageDrawable(placeholder);
        }
    }

    /**
     * Set square image to imageView with given filename
     */
    public static void setSquareImage(final ParseImageView imageView,
                                      final ParseUser parseUser,
                                      final String fileName,
                                      final Drawable placeholder) {

        if (imageView == null) return;
        if (parseUser == null || TextUtils.isEmpty(fileName)) {
            imageView.setImageDrawable(placeholder);
            return;
        }

        ParseFile photoFile = parseUser.getParseFile(fileName);

        if (photoFile != null) {
            imageView.setPlaceholder(placeholder);
            imageView.setParseFile(photoFile);

            try {
                imageView.loadInBackground();
                imageView.setTag(photoFile.getUrl());
            } catch (RejectedExecutionException e) {
                if (Config.DEBUG) e.printStackTrace();
            }
        } else {
            imageView.setImageDrawable(placeholder);
        }
    }

}
