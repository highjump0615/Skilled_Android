/**
 * @author Ry
 * @date 2013.12.21
 * @filename CommentActivity.java
 */

package com.iliayugai.skilled;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.skilled.adapter.PersonAdapter;
import com.iliayugai.skilled.data.BlogData;
import com.iliayugai.skilled.data.CommentData;
import com.iliayugai.skilled.data.FollowingLikeData;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.SkilledManager;
import com.iliayugai.skilled.view.CommentItemView;
import com.iliayugai.skilled.widget.RoundedAvatarDrawable;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class CommentActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = CommentActivity.class.getSimpleName();

    private EditText mEditComment;
    private Button mBtnComment;
    private LinearLayout mCommentList;

    private View mLayoutPersonList;
    private ListView mListPerson;
    private PersonAdapter mAdapterPerson;
    private ArrayList<FollowingLikeData> mPersonList = new ArrayList<FollowingLikeData>();
    private int m_nAtPos;

    public static BlogData mBlogData;
    private RoundedAvatarDrawable mDefaultAvatarDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comment);

        mDefaultAvatarDrawable = new RoundedAvatarDrawable(this.getResources(), R.drawable.profile_photo_default);

        initTitleBar();
        initViews();


        loadPersonData();
        loadComments();
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

            case R.id.btn_comment:
                onBtnComment();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FollowingLikeData data = mAdapterPerson.getItem(position);

        String originStr = mEditComment.getText().toString();

        if (m_nAtPos + 1 < originStr.length())
            mEditComment.setText(originStr.substring(0, m_nAtPos) + data.username + originStr.substring(m_nAtPos + 1));
        else {
            mCommentList.setVisibility(View.VISIBLE);
            String text = originStr + data.username;
            mEditComment.setText(text);
            mEditComment.setSelection(text.length());
        }

        mLayoutPersonList.setVisibility(View.INVISIBLE);
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
        textTitle.setText(R.string.comments);
        textTitle.setTypeface(typeFace);
        textTitle.setTextSize(textSize);

        View viewEdge = findViewById(R.id.view_h_line);
        params = (RelativeLayout.LayoutParams) viewEdge.getLayoutParams();
        params.height = (int) (res.getDimension(R.dimen.navigation_view_h_line_height) * Config.mScaleFactor);
        viewEdge.setLayoutParams(params);
    }

    private void initViews() {
        final Resources res = getResources();

        // Comment input layout
        int paddingLeft = (int) (res.getDimension(R.dimen.comment_layout_padding_left) * Config.mScaleFactor);
        int paddingTop = (int) (res.getDimension(R.dimen.comment_layout_padding_top) * Config.mScaleFactor);

        View layoutCommentInput = findViewById(R.id.layout_comment_input);
        layoutCommentInput.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop);

        // Profile photoImage
        final int photoViewSize = (int) (res.getDimension(R.dimen.comment_layout_image_size) * Config.mScaleFactor);
        final ParseImageView imagePhoto = (ParseImageView) findViewById(R.id.image_profile);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imagePhoto.getLayoutParams();
        params.width = params.height = photoViewSize;
        imagePhoto.setLayoutParams(params);
        imagePhoto.setPlaceholder(mDefaultAvatarDrawable);

        ParseUser userInfo = ParseUser.getCurrentUser();

        // Fetch user
        String objectId = userInfo.getObjectId();

        if (!SkilledManager.mParseUserMap.containsKey(objectId)) {
            userInfo.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        ParseUser user = (ParseUser) parseObject;

                        SkilledManager.mParseUserMap.put(user.getObjectId(), user);
                        SkilledManager.setAvatarImage(imagePhoto, (ParseUser) parseObject, "photo", mDefaultAvatarDrawable);
                    }
                }
            });
        } else {
            ParseUser user = SkilledManager.mParseUserMap.get(objectId);

            SkilledManager.setAvatarImage(imagePhoto, user, "photo", mDefaultAvatarDrawable);
        }

        // Comment Button
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-Regular.otf");
        mBtnComment = (Button) findViewById(R.id.btn_comment);
        mBtnComment.setTypeface(typeFace);
        mBtnComment.setOnClickListener(this);
        mBtnComment.setEnabled(false);

        params = (LinearLayout.LayoutParams) mBtnComment.getLayoutParams();
        params.width = (int) (res.getDimension(R.dimen.comment_layout_button_width) * Config.mScaleFactor);
        params.height = (int) (res.getDimension(R.dimen.comment_layout_button_height) * Config.mScaleFactor);
        mBtnComment.setLayoutParams(params);
        mBtnComment.setTextSize(res.getDimension(R.dimen.comment_layout_button_text_size) * Config.mFontScaleFactor);

        // Status Comment
        final TextView commentStatus = (TextView) findViewById(R.id.text_comment_status);
        float textSize = res.getDimension(R.dimen.comment_status_text_size) * Config.mFontScaleFactor;

        commentStatus.setTypeface(typeFace);
        commentStatus.setTextSize(textSize);


        if (mBlogData.mCommentList.size() == 0) {
            commentStatus.setVisibility(View.VISIBLE);
        }

        // Write Comment EditText
        mCommentList = (LinearLayout) findViewById(R.id.comment_list);
        mEditComment = (EditText) findViewById(R.id.edit_comment);
        mEditComment.setTypeface(typeFace);
        mEditComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String str = charSequence.toString();

                if (m_nAtPos >= str.length() || start >= str.length()) {
                    mLayoutPersonList.setVisibility(View.INVISIBLE);
                    m_nAtPos = -1;
                } else if (!TextUtils.isEmpty(str) && str.charAt(start) == '@') {
                    mLayoutPersonList.setVisibility(View.VISIBLE);
                    mCommentList.setVisibility(View.GONE);
                    commentStatus.setVisibility(View.GONE);
                    mAdapterPerson.notifyDataSetChanged();
                    m_nAtPos = start;
                }

                mBtnComment.setEnabled(charSequence.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        params = (LinearLayout.LayoutParams) mEditComment.getLayoutParams();
        textSize = res.getDimension(R.dimen.comment_layout_edit_text_size) * Config.mFontScaleFactor;
        params.leftMargin = (int) (res.getDimension(R.dimen.comment_layout_margin) * Config.mScaleFactor);
        params.rightMargin = (int) (res.getDimension(R.dimen.comment_layout_margin) * Config.mScaleFactor);
        mEditComment.setLayoutParams(params);
        mEditComment.setTextSize(textSize);

        // Person list
        mLayoutPersonList = findViewById(R.id.layout_persons);
        mLayoutPersonList.setVisibility(View.INVISIBLE);

        mAdapterPerson = new PersonAdapter(this, mPersonList);
        mListPerson = (ListView) findViewById(R.id.list_person);
        mListPerson.setAdapter(mAdapterPerson);
        mListPerson.setOnItemClickListener(this);
    }

    private void loadPersonData() {
        // fill mention list
        // get user from follow data
        for (FollowingLikeData data : SkilledManager.mFollowingList)
            mPersonList.add(data);

        // get user from comment list
        if (mBlogData.mCommentList != null) {
            for (CommentData cdata : mBlogData.mCommentList) {
                FollowingLikeData newData = new FollowingLikeData();
                newData.username = cdata.strUsername;
                newData.userObject = cdata.user;

                mPersonList.add(newData);
            }
        }

        HashSet<FollowingLikeData> listToSet = new HashSet<FollowingLikeData>(mPersonList);
        mPersonList.clear();
        mPersonList.addAll(listToSet);

        for (FollowingLikeData data : mPersonList) {
            String objectId = data.userObject.getObjectId();

            if (!SkilledManager.mParseUserMap.containsKey(objectId)) {
                data.userObject.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e == null) {
                            ParseUser user = (ParseUser) parseObject;
                            SkilledManager.mParseUserMap.put(user.getObjectId(), user);
                            mAdapterPerson.notifyDataSetChanged();
                        }
                    }
                });
            }
        }
    }

    private void loadComments() {
        mCommentList.removeAllViews();

        if (mBlogData != null && mBlogData.mCommentList != null) {
            Log.d(TAG, "Comment count = " + mBlogData.mCommentList.size());

            int i = 0;
            for (CommentData commentData : mBlogData.mCommentList) {
                mCommentList.addView(new CommentItemView(CommentActivity.this, commentData,
                        i % 2 == 0 ? Color.rgb(31, 31, 41) : Color.rgb(31, 31, 51)));
                i++;
            }
        }
    }

    private void onBtnComment() {
        String comment = mEditComment.getText().toString();

        ParseObject commentObj = ParseObject.create("Comments");
        commentObj.put("blog", mBlogData.object);
        commentObj.put("user", ParseUser.getCurrentUser());
        commentObj.put("content", comment);
        commentObj.put("username", SkilledManager.getUserNameToShow(ParseUser.getCurrentUser()));
        commentObj.put("targetuser", mBlogData.user);
        if (mBlogData.photoImage != null)
            commentObj.put("thumbnail", mBlogData.photoImage);
        commentObj.saveInBackground();

        CommentData commentData = new CommentData();
        commentData.strContent = comment;
        commentData.user = ParseUser.getCurrentUser();
        commentData.strUsername = SkilledManager.getUserNameToShow(ParseUser.getCurrentUser());
        commentData.date = new Date();
        commentData.object = commentObj;

        mBlogData.mCommentList.add(0, commentData);

        onSendNotification();

        mEditComment.setText("");
        loadComments();
    }

    private void onSendNotification() {
        // send notification to commented user
        ParseQuery parseQuery = ParseInstallation.getQuery();
        parseQuery.whereEqualTo("user", mBlogData.user);

        // Send the notification.
        ParsePush push = new ParsePush();
        push.setQuery(parseQuery);

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("alert", String.format("%s commented you", SkilledManager.getUserNameToShow(ParseUser.getCurrentUser())));
        params.put("badge", "Increment");
        params.put("sound", "cheering.caf");
        params.put("notifyType", "comment");
        params.put("notifyBlog", mBlogData.object.getObjectId());

        JSONObject data = new JSONObject(params);

        push.setData(data);
        push.sendInBackground();

        // check mentioning and send notification
        String strCommentText = mEditComment.getText().toString();
        int position;

        while ((position = strCommentText.indexOf("@")) != -1) {

            String strToCompare = strCommentText.substring(position + 1);

            for (FollowingLikeData followData : mPersonList) {

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
                    push = new ParsePush();
                    push.setQuery(parseQuery);

                    params = new HashMap<String, Object>();
                    params.put("alert", String.format("%s mentioned you in the comment\n%s",
                            SkilledManager.getUserNameToShow(ParseUser.getCurrentUser()),
                            mEditComment.getText().toString()));
                    params.put("badge", "Increment");
                    params.put("sound", "cheering.caf");
                    params.put("notifyType", "mention");
                    params.put("notifyBlog", mBlogData.object.getObjectId());

                    push.setData(new JSONObject(params));
                    push.sendInBackground();

                    break;
                }
            }

            strCommentText = strToCompare;
        }
    }

}
