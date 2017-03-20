package com.qirui.browser.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.qirui.browser.R;

/**
 * Created by Luooh on 2017/3/17.
 */
public class FileSortItemView extends LinearLayout {

    private ImageView mFileIcon;
    private TextView mFileTitle;
    private TextView mFileCount;

    public FileSortItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.layout_filesort_item, this);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FileType);
        int resId = array.getResourceId(R.styleable.FileType_fileicon, R.drawable.filesystem_icon_apk);
        String file_title = array.getString(R.styleable.FileType_filetitle);
        String file_Count = array.getString(R.styleable.FileType_filecount);
        mFileIcon = (ImageView) contentView.findViewById(R.id.fileIcon);
        mFileTitle = (TextView) contentView.findViewById(R.id.fileTitle);
        mFileCount = (TextView) contentView.findViewById(R.id.fileCount);
        mFileIcon.setImageResource(resId);
        mFileTitle.setText(file_title);
        if(!TextUtils.isEmpty(file_Count)) {
            mFileCount.setText(file_Count);
        }

        array.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setGravity(Gravity.CENTER);
        setOrientation(LinearLayout.VERTICAL);
//        int margin = DisplayUtils.dip2px(getContext(), 15);
//        int width = DisplayUtils.getScreenWidth(getContext()) / 3;
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getLayoutParams();
//        params.bottomMargin = margin;
//        setLayoutParams(params);
    }

    public void setFileCount(String count) {
        mFileCount.setText(count);
    }

    public void setOnFileItemClickListener() {
    }
}
