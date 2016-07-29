package com.iliayugai.skilled.data;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;

public class BlogData {

    public static final int BlogText = 0;
    public static final int BlogImage = 1;
    public static final int BlogVideo = 2;

    public String strId;
    public int type;
    public String strTitle;
    public String strContent;
    public String videoUrl;
    public ParseFile photoImage;
    public String strVideoName;
    public BlogCategory category;
    public Date date;
    public ParseUser user;
    public ParseObject object;
    public int bLiked; // 1: like, 0: unliked, -1: not determinded
    public int nLikeCount;
    public ArrayList<CommentData> mCommentList;
    public ArrayList<String> mLikeList;

    public BlogData() {
        mCommentList = new ArrayList<CommentData>();
        mLikeList = new ArrayList<String>();
    }

}
