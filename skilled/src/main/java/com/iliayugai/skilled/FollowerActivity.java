/**
 * @author Ry
 * @date 2013.12.21
 * @filename CommentActivity.java
 */

package com.iliayugai.skilled;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.skilled.adapter.FollowerAdapter;
import com.iliayugai.skilled.data.FollowingLikeData;
import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.SkilledManager;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class FollowerActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = FollowerActivity.class.getSimpleName();

    private static final boolean DBG = false;

    public static ParseUser mUser;
    public static ParseObject mBlogObject = null;
    public static boolean m_bFollowing = false;

    private FollowerAdapter mAdapter;
    private ArrayList<FollowingLikeData> mFollowerList = new ArrayList<FollowingLikeData>();

    private String mStrTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_follower);

        if (mBlogObject != null) {
            mStrTitle = "Likes";
        } else {
            if (m_bFollowing)
                mStrTitle = String.format("%s's Following", SkilledManager.getUserNameToShow(mUser));
            else
                mStrTitle = String.format("%s's Followers", SkilledManager.getUserNameToShow(mUser));
        }

        initTitleBar();
        initViews();

        loadFollowers();

        if (Config.DEBUG) Log.d(TAG, "initialize completed!");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.image_trend:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FollowingLikeData data = mFollowerList.get(position);

        SkilledManager.sParseUserList.add(data.userObject);
        SkilledManager.sParseUserNameList.add(data.username);

        int idx1 = SkilledManager.sParseUserList.size() - 1;
        OtherProfileActivity.mUser = SkilledManager.sParseUserList.get(idx1);

        if (DBG) {
            Log.e(TAG, "size = " + SkilledManager.sParseUserNameList.size());
            for (int i = 0; i < SkilledManager.sParseUserNameList.size(); i++) {
                Log.e(TAG, "name[" + i + "] = " + SkilledManager.sParseUserNameList.get(i));
            }
        }

        int idx2 = SkilledManager.sParseUserNameList.size() - 1;
        OtherProfileActivity.mUserFullName = SkilledManager.sParseUserNameList.get(idx2);

        startActivity(new Intent(this, OtherProfileActivity.class));
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
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
        imageView.setImageResource(R.drawable.btn_back_bg);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        params.width = params.height = size;
        imageView.setLayoutParams(params);
        imageView.setPadding(padding, padding, padding, padding);

        // Search ImageView
        imageView = (ImageView) findViewById(R.id.image_search);
        imageView.setVisibility(View.INVISIBLE);

        // Title TextView
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-DemiBold.otf");
        TextView textTitle = (TextView) findViewById(R.id.text_nav_title);
        textTitle.setText(mStrTitle);
        textTitle.setTypeface(typeFace);
        textTitle.setTextSize(textSize);

        View viewEdge = findViewById(R.id.view_h_line);
        params = (RelativeLayout.LayoutParams) viewEdge.getLayoutParams();
        params.height = (int) (res.getDimension(R.dimen.navigation_view_h_line_height) * Config.mScaleFactor);
        viewEdge.setLayoutParams(params);
    }

    private void initViews() {
        mAdapter = new FollowerAdapter(this, mFollowerList);

        ListView listView = (ListView) findViewById(R.id.list_follower);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
    }

    private void loadFollowers() {
        final Dialog progressDialog = CommonUtils.createFullScreenProgress(this);
        progressDialog.show();

        mFollowerList.clear();

        if (mBlogObject != null) {
            // get like info
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Likes");
            query.whereEqualTo("blog", mBlogObject);
            query.orderByDescending("updatedAt");

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    progressDialog.dismiss();

                    if (e == null) {
                        for (ParseObject obj : parseObjects) {
                            FollowingLikeData followData = new FollowingLikeData();

                            followData.username = obj.getString("username");
                            followData.userObject = obj.getParseUser("user");
                            fetchUser(followData);

                            mFollowerList.add(followData);
                            mAdapter.notifyDataSetChanged();
                        }
                    } else {
                        CommonUtils.createErrorAlertDialog(FollowerActivity.this, "Alert", e.getMessage()).show();
                    }
                }
            });

        } else {
            if (mUser == null) return;

            if (m_bFollowing) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Following");
                query.whereEqualTo("user", mUser);

                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        progressDialog.dismiss();

                        if (e == null) {
                            for (ParseObject obj : parseObjects) {
                                FollowingLikeData followData = new FollowingLikeData();

                                followData.username = obj.getString("followingusername");
                                followData.userObject = obj.getParseUser("followinguser");
                                fetchUser(followData);

                                mFollowerList.add(followData);
                                mAdapter.notifyDataSetChanged();
                            }
                        } else {
                            CommonUtils.createErrorAlertDialog(FollowerActivity.this, "Alert", e.getMessage()).show();
                        }
                    }
                });
            } else {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Following");
                query.whereEqualTo("followinguser", mUser);

                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        progressDialog.dismiss();

                        if (e == null) {
                            for (ParseObject obj : parseObjects) {
                                FollowingLikeData followData = new FollowingLikeData();

                                followData.username = obj.getString("username");
                                followData.userObject = obj.getParseUser("user");
                                fetchUser(followData);

                                mFollowerList.add(followData);
                                mAdapter.notifyDataSetChanged();
                            }
                        } else {
                            CommonUtils.createErrorAlertDialog(FollowerActivity.this, "Alert", e.getMessage()).show();
                        }
                    }
                });
            }
        }
    }

    private void fetchUser(FollowingLikeData followData) {
        // Fetch user
        String objectId = followData.userObject.getObjectId();

        if (!SkilledManager.mParseUserMap.containsKey(objectId)) {
            followData.userObject.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        ParseUser user = (ParseUser) parseObject;

                        SkilledManager.mParseUserMap.put(user.getObjectId(), user);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

}
