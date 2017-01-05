package com.iliayugai.skilled;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.util.SparseArray;

import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.widget.TabBar.Tab;
import com.iliayugai.skilled.widget.TabBar.TabHostProvider;
import com.iliayugai.skilled.widget.TabBar.TabView;

public class MyTabHost extends TabHostProvider {

    public static final int HOME_TAB_INDEX = 0;
    public static final int FAVOURITE_TAB_INDEX = 1;
    public static final int CAMERA_TAB_INDEX = 2;
    public static final int NOTIFICATION_TAB_INDEX = 3;
    public static final int PROFILE_TAB_INDEX = 4;

    public static final String HOME_TAB_NAME = "HOME";
    public static final String FAVOURITE_TAB_NAME = "FAVOURITE";
    public static final String CAMERA_TAB_NAME = "CAMERA";
    public static final String NOTIFICATION_TAB_NAME = "NOTIFICATION";
    public static final String PROFILE_TAB_NAME = "PROFILE";


    public static final int TAB_COUNT = PROFILE_TAB_INDEX + 1;

    public static final SparseArray<String> TAB_VIEW_ARRAY;

    static {
        TAB_VIEW_ARRAY = new SparseArray<String>();
        TAB_VIEW_ARRAY.put(HOME_TAB_INDEX, HOME_TAB_NAME);
        TAB_VIEW_ARRAY.put(FAVOURITE_TAB_INDEX, FAVOURITE_TAB_NAME);
        TAB_VIEW_ARRAY.put(CAMERA_TAB_INDEX, CAMERA_TAB_NAME);
        TAB_VIEW_ARRAY.put(NOTIFICATION_TAB_INDEX, NOTIFICATION_TAB_NAME);
        TAB_VIEW_ARRAY.put(PROFILE_TAB_INDEX, PROFILE_TAB_NAME);
    }

    private static final int[][] BUTTON_BACK = new int[][]{
            {R.drawable.tab_home, R.drawable.tab_home_select},
            {R.drawable.tab_favourite, R.drawable.tab_favourite_select},
            {R.drawable.tab_shutter, R.drawable.tab_shutter_select},
            {R.drawable.tab_notify, R.drawable.tab_notify_select},
            {R.drawable.tab_profile, R.drawable.tab_profile_select},
    };

    private static final Class<?>[] TAB_TARGET_CLASS_ARRAY = new Class<?>[]{
            HomeActivity.class,
            FavouriteActivity2.class,
            null/*CameraActivity.class*/,
            NotifyActivity.class,
            ProfileActivity.class
    };

    public MyTabHost(Context context) {
        super(context);
        init();
    }

    private void init() {
//        Tab homeTab;
//        Tab favouriteTab;
//        Tab cameraTab;
//        Tab notificationsTab;
//        Tab profileTab;

//        homeTab = new Tab(context, TAB_VIEW_ARRAY.get(HOME_TAB_INDEX));
//        homeTab.setIcon(R.drawable.tab_home);
//        homeTab.setIconSelected(R.drawable.tab_home_select);
//        homeTab.setBtnText(TAB_VIEW_ARRAY.get(HOME_TAB_INDEX));
//        homeTab.setBtnTextColor(Color.WHITE);
//        homeTab.setSelectedBtnTextColor(Color.BLACK);
//        // homeTab.setBtnColor(Color.parseColor("#00000000"));
//        // homeTab.setSelectedBtnColor(Color.parseColor("#0000FF"));
//        homeTab.setBtnGradient(transGradientDrawable);
//        homeTab.setSelectedBtnGradient(gradientDrawable);
//        homeTab.setIntent(new Intent(context, HomeActivity.class));
//
//        favouriteTab = new Tab(context, category);
//        favouriteTab.setIcon(R.drawable.tab_favourite);
//        favouriteTab.setIconSelected(R.drawable.tab_favourite_select);
//        favouriteTab.setBtnText("Contact");
//        favouriteTab.setBtnTextColor(Color.WHITE);
//        favouriteTab.setSelectedBtnTextColor(Color.BLACK);
//        // favouriteTab.setBtnColor(Color.parseColor("#00000000"));
//        // favouriteTab.setSelectedBtnColor(Color.parseColor("#0000FF"));
//        favouriteTab.setBtnGradient(transGradientDrawable);
//        favouriteTab.setSelectedBtnGradient(gradientDrawable);
//        favouriteTab.setIntent(new Intent(context, FavouriteActivity.class));
//
//        cameraTab = new Tab(context, category);
//        cameraTab.setIcon(R.drawable.tab_shutter);
//        cameraTab.setIconSelected(R.drawable.tab_shutter_select);
//        cameraTab.setBtnText("Share");
//        cameraTab.setBtnTextColor(Color.WHITE);
//        cameraTab.setSelectedBtnTextColor(Color.BLACK);
//        // cameraTab.setBtnColor(Color.parseColor("#00000000"));
//        // cameraTab.setSelectedBtnColor(Color.parseColor("#0000FF"));
//        cameraTab.setBtnGradient(transGradientDrawable);
//        cameraTab.setSelectedBtnGradient(gradientDrawable);
//        cameraTab.setIntent(new Intent(context, CameraActivity.class));
//
//        notificationsTab = new Tab(context, category);
//        notificationsTab.setIcon(R.drawable.tab_notify);
//        notificationsTab.setIconSelected(R.drawable.tab_notify_select);
//        notificationsTab.setBtnText("More");
//        notificationsTab.setBtnTextColor(Color.WHITE);
//        notificationsTab.setSelectedBtnTextColor(Color.BLACK);
//        // notificationsTab.setBtnColor(Color.parseColor("#00000000"));
//        // notificationsTab.setSelectedBtnColor(Color.parseColor("#0000FF"));
//        notificationsTab.setBtnGradient(transGradientDrawable);
//        notificationsTab.setSelectedBtnGradient(gradientDrawable);
//        notificationsTab.setIntent(new Intent(context, NotifyActivity.class));
//
//        profileTab = new Tab(context, category);
//        profileTab.setIcon(R.drawable.tab_profile);
//        profileTab.setIconSelected(R.drawable.tab_profile_select);
//        profileTab.setBtnText("More");
//        profileTab.setBtnTextColor(Color.WHITE);
//        profileTab.setSelectedBtnTextColor(Color.BLACK);
//        // profileTab.setBtnColor(Color.parseColor("#00000000"));
//        // profileTab.setSelectedBtnColor(Color.parseColor("#0000FF"));
//        profileTab.setBtnGradient(transGradientDrawable);
//        profileTab.setSelectedBtnGradient(gradientDrawable);
//        profileTab.setIntent(new Intent(context, ProfileActivity.class));
//
//        tabView.addTab(homeTab);
//        tabView.addTab(favouriteTab);
//        tabView.addTab(cameraTab);
//        tabView.addTab(notificationsTab);
//        tabView.addTab(profileTab);
    }

    @Override
    public TabView getTabHost(String category) {
        TabView tabView = new TabView(context);

        tabView.setOrientation(TabView.Orientation.BOTTOM);
        tabView.setBackgroundID(R.drawable.tab_background_gradient);

        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFFB2DA1D, 0xFF85A315});
        gradientDrawable.setCornerRadius(0.0f);
        gradientDrawable.setDither(true);

        GradientDrawable transGradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x00000000, 0x00000000}
        );
        transGradientDrawable.setCornerRadius(0f);
        transGradientDrawable.setDither(true);

        for (int i = 0; i < TAB_COUNT; i++) {
            Tab tab = new Tab(context, TAB_VIEW_ARRAY.get(i), i == CAMERA_TAB_INDEX);

            tab.setIcon(BUTTON_BACK[i][0]);
            tab.setIconSelected(BUTTON_BACK[i][1]);
            tab.setBtnTextSize(1);
            //tab.setBtnText(TAB_VIEW_ARRAY.get(i));
            //tab.setBtnTextColor(Color.WHITE);
            //tab.setSelectedBtnTextColor(Color.BLACK);
            // homeTab.setBtnColor(Color.parseColor("#00000000"));
            // homeTab.setSelectedBtnColor(Color.parseColor("#0000FF"));
            //tab.setBtnGradient(transGradientDrawable);
            //tab.setSelectedBtnGradient(gradientDrawable);

            if (i == CAMERA_TAB_INDEX) {
                tab.setDialog(createSelectDialog());
                tab.setIntent(null);
            } else if (!category.equals(TAB_VIEW_ARRAY.get(i))) {
                tab.setIntent(new Intent(context, TAB_TARGET_CLASS_ARRAY[i]));
            } else {
                tab.setIntent(null);
            }

            tabView.addTab(tab);
        }

        return tabView;
    }

    private Dialog createSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        return builder.setItems(R.array.select_media_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0 || which == 1) {
                    if (!CommonUtils.isDeviceSupportCamera(context)) {
                        CommonUtils.createErrorAlertDialog(context, "Alert", "No camera").show();
                        return;
                    }
                }

                Intent intent = new Intent(context, PostActivity.class);
                intent.putExtra(PostActivity.MEDIA_LOCATION, which);
                ((Activity) context).startActivityForResult(intent, PostActivity.POST_REQUEST_CODE);
            }
        }).setNegativeButton(android.R.string.cancel, null).create();
    }

}
