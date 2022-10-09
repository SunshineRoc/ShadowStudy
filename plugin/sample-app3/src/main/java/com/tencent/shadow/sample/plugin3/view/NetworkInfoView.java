package com.tencent.shadow.sample.plugin3.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tencent.shadow.sample.plugin.app.lib.base.plugin.R;

/**
 * 网络信息控件
 */
public class NetworkInfoView extends LinearLayout {

    private static final String INFO_TITLE_CONTENT_SPLIT = "：";     // 信息标题和正文的分隔符
    private Context context;
    private TextView tvTopTitle;                                     // 顶部标题
    private TextView tvInfoTitle;                                    // 信息标题
    private TextView tvInfoContent;                                  // 信息正文

    private String topTitle;                                         // 控件头部标题
    private String information;                                      // 网络信息
    private String infoTitle;                                        // 信息标题
    private String infoContent;                                      // 信息正文

    public NetworkInfoView(Context context) {
        this(context, null);
    }

    public NetworkInfoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworkInfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context;
        initAttrs(attrs, defStyleAttr);
        initView();
        refresh();
    }

    /**
     * 初始化属性
     */
    private void initAttrs(AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NetworkInfoView, defStyleAttr, 0);
        if (typedArray == null || typedArray.length() < 1) {
            return;
        }

        for (int i = 0; i < typedArray.length(); i++) {
            int attr = typedArray.getIndex(i);

            if (attr == R.styleable.NetworkInfoView_topTitle) {
                topTitle = typedArray.getString(attr);
            }

            if (attr == R.styleable.NetworkInfoView_information) {
                information = typedArray.getString(attr);
                analyzeInformation(information);
            }
        }

        typedArray.recycle();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        View.inflate(context, R.layout.network_info_layout, this);

        tvTopTitle = findViewById(R.id.tv_top_title);
        tvInfoTitle = findViewById(R.id.tv_info_title);
        tvInfoContent = findViewById(R.id.tv_info_content);
    }

    /**
     * 获取控件标题
     *
     * @return 控件标题
     */
    public String getTopTitle() {
        return topTitle;
    }

    /**
     * 设置控件标题
     *
     * @param topTitle 控件标题
     */
    public NetworkInfoView setTopTitle(String topTitle) {
        this.topTitle = topTitle;
        return this;
    }

    /**
     * 设置网络信息，包括信息标题和信息正文
     *
     * @param info 网络信息
     * @return 当前控件
     */
    public NetworkInfoView setInformation(String info) {
        this.information = info;
        analyzeInformation(info);
        return this;
    }

    /**
     * 根据 information，解析出信息标题和正文
     *
     * @param info 网络信息
     */
    private void analyzeInformation(String info) {
        if (TextUtils.isEmpty(info)) {
            this.infoTitle = context.getResources().getString(R.string.network_info_announcement_default_title);
            this.infoContent = context.getResources().getString(R.string.network_info_announcement_default_content);
        } else {
            int splitIndex = info.indexOf(INFO_TITLE_CONTENT_SPLIT);
            if (splitIndex > 0 && splitIndex < info.length() - 1) {
                infoTitle = info.substring(0, splitIndex + 1);
                infoContent = info.substring(splitIndex + 1);
            } else {
                infoTitle = context.getResources().getString(R.string.network_info_announcement_default_title);
                infoContent = context.getResources().getString(R.string.network_info_announcement_default_content);
            }
        }
    }

    /**
     * 获取信息标题
     *
     * @return 标题
     */
    public String getInfoTitle() {
        return infoTitle;
    }

    /**
     * 获取信息正文
     *
     * @return 正文
     */
    public String getInfoContent() {
        return infoContent;
    }

    /**
     * 刷新控件
     */
    public void refresh() {
        tvTopTitle.setText(TextUtils.isEmpty(topTitle) ? context.getResources().getString(R.string.network_info_title) : topTitle);
        tvInfoTitle.setText(TextUtils.isEmpty(infoTitle) ? context.getResources().getString(R.string.network_info_announcement_default_title) : infoTitle);
        tvInfoContent.setText(TextUtils.isEmpty(infoContent) ? context.getResources().getString(R.string.network_info_announcement_default_content) : infoContent);
    }
}
