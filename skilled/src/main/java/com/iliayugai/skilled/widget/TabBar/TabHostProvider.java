package com.iliayugai.skilled.widget.TabBar;

import android.content.Context;

public abstract class TabHostProvider {

    public Context context;

    public TabHostProvider(Context context) {
        this.context = context;
    }

    public abstract TabView getTabHost(String category);

}