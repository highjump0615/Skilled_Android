package com.iliayugai.skilled.data;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

public class NotificationData {

    public ParseUser user;
    public String strUsername;
    public String strComment;
    public ParseFile image;
    public int type;
    public int postType;
    public Date date;
    public ParseObject blog;

}
