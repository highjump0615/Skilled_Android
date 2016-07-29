package com.iliayugai.skilled.widget.TabBar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.iliayugai.skilled.R;
import com.iliayugai.skilled.utils.Config;

public class Tab {

    private int resourceIcon;
    private int resourceIconSelected = 0;

    private final Context context;
    private Intent intent;

    private View view;
    private Button btn;
    private final String tabTag;

    public int preferredHeight = -1;
    public int padding = -1;

    private boolean isSelected;
    private Dialog dialog;
    private int requestCode = -1;
    private String btnText;
    private int textColor;
    private int selectedTextColor;
    //	private int btnColor;
//	private int selectedBtnColor;
    private GradientDrawable btnGradient;
    private GradientDrawable selectedBtnGradient;
    private float btnTextSize;
    private boolean isLargeIcon = false;

    public Tab(Context context, String tabTag, boolean isLargeIcon) {
        if (context == null) {
            throw new IllegalStateException("Context can't be null");
        }
        this.tabTag = tabTag;
        this.context = context;
        this.isLargeIcon = isLargeIcon;

        final Resources resources = context.getResources();
        preferredHeight = (int) (resources.getDimension(R.dimen.tab_bar_height) * Config.mScaleFactor);
        padding = (int) (resources.getDimension(R.dimen.tab_button_padding) * Config.mScaleFactor);
    }

    public void setIcon(int resourceIcon) {
        this.resourceIcon = resourceIcon;
    }

    public void setIconSelected(int resourceIcon) {
        this.resourceIconSelected = resourceIcon;
    }

//	public void setBtnColor(int btnColor) {
//		this.btnColor = btnColor;
//	}
//	
//	public void setSelectedBtnColor(int btnColor) {
//		this.selectedBtnColor = btnColor;
//	}

    public void setBtnGradient(GradientDrawable btnGradient) {
        this.btnGradient = btnGradient;
    }

    public void setSelectedBtnGradient(GradientDrawable btnGradient) {
        this.selectedBtnGradient = btnGradient;
    }

    public void setBtnTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setSelectedBtnTextColor(int textColor) {
        this.selectedTextColor = textColor;
    }

    public void setBtnTextSize(float btnTextSize) {
        this.btnTextSize = btnTextSize;
    }

    public void setIntent(Intent intent, int requestForResult) {
        this.intent = intent;
        this.requestCode = requestForResult;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Intent getIntent() {
        return intent;
    }

    public String getTag() {
        return tabTag;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setBtnText(String btnText) {
        this.btnText = btnText;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public View getView() {
        if (view == null) {
            createView();
        }
        return view;
    }

    private void createView() {
        //btn = (Button) (LayoutInflater.from(context).inflate(R.layout.tab_button, null));
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.tab_button, null);

        btn = (Button) layout.findViewById(R.id.button);
        ImageView imageView = (ImageView) layout.findViewById(R.id.imageView);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) btn.getLayoutParams();
        params.height = (int) (context.getResources().getDimension(R.dimen.tab_bar_height) * Config.mScaleFactor);

        if (!isLargeIcon) {
            params.height -= (2 * padding);
        } else {
            params.height -= padding;
        }
        btn.setLayoutParams(params);

        int iconId = resourceIcon;
//		int btnBackColor = btnColor;
        GradientDrawable btnBackGrad = btnGradient;
        int btnTextColor = textColor;
        if (isSelected && resourceIconSelected != 0) {
            iconId = resourceIconSelected;
//			btnBackColor = selectedBtnColor;
            btnBackGrad = selectedBtnGradient;
            btnTextColor = selectedTextColor;
        }

        //btn.setCompoundDrawablesWithIntrinsicBounds(0, iconId, 0, 0);
        btn.setText(btnText);
        btn.setTextColor(btnTextColor);
        btn.setTextSize(btnTextSize);
//		btn.setBackgroundColor(btnBackColor);
        layout.setBackgroundDrawable(btnBackGrad);
        layout.setMinimumHeight(preferredHeight);
        layout.setPadding(0, padding, 0, padding);

        imageView.setImageResource(iconId);

        bindListeners();
        view = layout;
    }

    private void bindListeners() {
        btn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (intent == null && dialog == null) {
                    /*
                    Toast.makeText(context,
                            "Intent or Dialog not set for '" + tabTag + "'",
                            Toast.LENGTH_SHORT).show();
                    */
                } else if (intent != null && dialog != null) {
                    /*
                    Toast.makeText(context,
                            " Only ONE can be set Intent or Dialog for '"
                                    + tabTag + "'", Toast.LENGTH_SHORT
                    ).show();
                    */
                } else {
                    if (intent != null) {
                        if (requestCode != -1) {
                            // This will start activity for result
                        } else if (context instanceof Activity) {
                            Activity activity = (Activity) context;
                            activity.startActivity(intent);
                            activity.overridePendingTransition(0, 0);
                            //activity.finish();
                        }
                    } else {
                        dialog.show();
                    }
                }
            }
        });

        btn.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    //btn.setBackgroundColor(0x400000FF);
                } else if (e.getAction() == MotionEvent.ACTION_UP) {
                    //btn.setBackgroundColor(0x00000000);
                }
                return false;
            }
        });
    }

}