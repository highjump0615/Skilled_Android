package com.iliayugai.skilled.view;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.iliayugai.skilled.BlogViewActivity;
import com.iliayugai.skilled.R;
import com.iliayugai.skilled.data.BlogCategory;
import com.iliayugai.skilled.data.BlogData;
import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.SkilledManager;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;

import java.util.ArrayList;

public class ImagePostViewHolder extends IViewHolder {

    private static final String TAG = ImagePostViewHolder.class.getSimpleName();

    private static final int POST_COUNT_PER_ROW = 3;

    private Activity mActivity;
    private TableLayout mPostTable;
    private LayoutInflater mInflater;

    public ImagePostViewHolder(View tableView, Activity activity) {
        mPostTable = (TableLayout) tableView;
        mActivity = activity;
        mInflater = LayoutInflater.from(mActivity);
    }

    public void fillContent(ArrayList<ParseObject> blogList) {
        mPostTable.removeAllViews();

        View oneTableCell;
        RelativeLayout.LayoutParams params;

        int count = blogList.size();
        int rowCount = (count + POST_COUNT_PER_ROW - 1) / POST_COUNT_PER_ROW;

        final Resources resources = mActivity.getResources();
        int cellSize = (int) (resources.getDimension(R.dimen.profile_grid_cell_size) * Config.mScaleFactor);
        int cellPadding = (int) (resources.getDimension(R.dimen.profile_grid_cell_padding) * Config.mScaleFactor);
        int cameraIconSize = (int) (resources.getDimension(R.dimen.profile_grid_video_icon_size) * Config.mScaleFactor);
        int cameraIconMarginLeft = (int) (resources.getDimension(R.dimen.profile_grid_video_icon_margin_left) * Config.mScaleFactor);
        int cameraIconMarginBottom = (int) (resources.getDimension(R.dimen.profile_grid_video_icon_margin_bottom) * Config.mScaleFactor);

        // remember we will not show 0th and last Row and Columns
        // they are used for calculation purposes only
        for (int row = 0; row < rowCount; row++) {
            TableRow tableRow = new TableRow(mActivity);
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    (cellSize + 2 * cellPadding) * POST_COUNT_PER_ROW,
                    POST_COUNT_PER_ROW + 2 * cellPadding
            ));

            for (int column = 0; column < POST_COUNT_PER_ROW; column++) {
                if (((row * POST_COUNT_PER_ROW) + column) == count) break;

                final ParseObject blogObject = blogList.get(row * POST_COUNT_PER_ROW + column);

                oneTableCell = mInflater.inflate(R.layout.layout_grid_cell, null);
                if (oneTableCell == null) continue;

                final ParseImageView imageThumbnail = (ParseImageView) oneTableCell.findViewById(R.id.image_thumbnail);
                ImageView cameraIcon = (ImageView) oneTableCell.findViewById(R.id.image_video_icon);

                // camera icon
                if (blogObject.getInt("type") == BlogData.BlogVideo) {
                    cameraIcon.setVisibility(View.VISIBLE);

                    params = (RelativeLayout.LayoutParams) cameraIcon.getLayoutParams();
                    params.width = params.height = cameraIconSize;
                    params.bottomMargin = cameraIconMarginBottom;
                    params.leftMargin = cameraIconMarginLeft;
                    cameraIcon.setLayoutParams(params);
                } else {
                    cameraIcon.setVisibility(View.INVISIBLE);
                }

                // Thumbnail image
                params = (RelativeLayout.LayoutParams) imageThumbnail.getLayoutParams();
                params.width = params.height = cellSize;
                imageThumbnail.setLayoutParams(params);

                imageThumbnail.setPlaceholder(mActivity.getResources().getDrawable(R.drawable.profile_img_default));
                imageThumbnail.setParseFile(blogObject.getParseFile("image"));
                imageThumbnail.loadInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {
                        if (e == null) imageThumbnail.setBackgroundColor(Color.BLACK);
                    }
                });

                // Add one cell to tableRow
                oneTableCell.setLayoutParams(new TableRow.LayoutParams(
                        cellSize + 2 * cellPadding,
                        cellSize + 2 * cellPadding
                ));
                oneTableCell.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);
                tableRow.addView(oneTableCell);

                oneTableCell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BlogData blogData = new BlogData();

                        blogData.strId = blogObject.getObjectId();
                        blogData.type = blogObject.getInt("type");
                        blogData.strTitle = blogObject.getString("title");
                        blogData.strContent = blogObject.getString("text");
                        blogData.strVideoName = blogObject.getString("video");
                        blogData.photoImage = blogObject.getParseFile("image");
                        blogData.date = blogObject.getCreatedAt();
                        blogData.user = blogObject.getParseUser("user");
                        blogData.object = blogObject;

                        for (BlogCategory category : SkilledManager.mCategoryList) {
                            if (category.strId.equals(blogObject.getString("category"))) {
                                blogData.category = category;
                                break;
                            }
                        }

                        blogData.bLiked = -1;
                        blogData.nLikeCount = blogObject.getInt("likes");

                        BlogViewActivity.mBlogData = blogData;

                        CommonUtils.moveNextActivityWithoutFinish(mActivity, BlogViewActivity.class);
                    }
                });
            }

            mPostTable.addView(tableRow, new TableLayout.LayoutParams(
                    (cellSize + 2 * cellPadding) * POST_COUNT_PER_ROW,
                    cellSize + 2 * cellPadding
            ));
        }
    }

}
