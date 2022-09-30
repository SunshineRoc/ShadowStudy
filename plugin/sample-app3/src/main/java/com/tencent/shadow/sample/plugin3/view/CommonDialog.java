package com.tencent.shadow.sample.plugin3.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tencent.shadow.sample.plugin.app.lib.base.plugin.R;
import com.tencent.shadow.sample.plugin3.data.DialogParams;

/**
 * 日期  2021/4/25 15:00
 * <p>
 * 描述：通用的可定制对话框
 */
public class CommonDialog extends Dialog {

    private Context context;
    private DialogParams dialogParams;

    private View rootView;
    private TextView tvMessage;
    private TextView tvNo;
    private TextView tvYes;

    public CommonDialog(@NonNull Context activity) {
        this(activity, R.style.CommonDialogStyle);
    }

    public CommonDialog(@NonNull Context context, int themeResId) {
        super(context, R.style.CommonDialogStyle);
        initData(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initData(Context context) {
        this.context = context;
        dialogParams = new DialogParams();
    }

    /**
     * 初始化dialog
     */
    private void initView() {
        rootView = View.inflate(context, R.layout.dialog_common, null);
        tvMessage = rootView.findViewById(R.id.tv_message);

        tvNo = rootView.findViewById(R.id.tv_no);
        tvNo.setOnClickListener(v -> {
            if (dialogParams.getClickListener() != null) {
                dialogParams.getClickListener().onClickNoListener();
            }
        });

        tvYes = rootView.findViewById(R.id.tv_yes);
        tvYes.setOnClickListener(v -> {
            if (dialogParams.getClickListener() != null) {
                dialogParams.getClickListener().onClickYesListener();
            }
        });

        setContentView(rootView);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
    }

    @Override
    public void show() {
        try {
            super.show();

            // 设置“确定”和“取消”按钮的默认文案
            if (TextUtils.isEmpty(dialogParams.getYesContent())) {
                dialogParams.setYesContent(context.getResources().getString(R.string.confirm_text));
            }
            if (TextUtils.isEmpty(dialogParams.getNoContent())) {
                dialogParams.setNoContent(context.getResources().getString(R.string.cancel_text));
            }

            tvMessage.setText(dialogParams.getMessage());
            tvNo.setText(TextUtils.isEmpty(dialogParams.getNoContent()) ? context.getResources().getString(R.string.cancel_text) : dialogParams.getNoContent());
            tvYes.setText(TextUtils.isEmpty(dialogParams.getYesContent()) ? context.getResources().getString(R.string.confirm_text) : dialogParams.getYesContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /********************************************* 定制对话框 *********************************************/
    /**
     * 设置对话框标题
     *
     * @param title 标题标题
     */
    public CommonDialog setTitle(String title) {
        dialogParams.setTitle(title);
        return this;
    }

    /**
     * 设置对话框内容
     *
     * @param message 标题内容
     */
    public CommonDialog setMessage(String message) {
        dialogParams.setMessage(message);
        return this;
    }

    /**
     * 设置确定按钮的文案，不设置时，默认显示“确定”
     *
     * @param yesContent 确定按钮的文案
     */
    public CommonDialog setYesContent(String yesContent) {
        dialogParams.setYesContent(yesContent);
        return this;
    }

    /**
     * 设置取消按钮的文案，不设置时，默认显示“取消”
     *
     * @param noContent 取消按钮的文案
     */
    public CommonDialog setNoContent(String noContent) {
        dialogParams.setNoContent(noContent);
        return this;
    }

    /**
     * 设置只显示确定按钮，还是只显示取消按钮，还是两个都显示
     *
     * @param buttonStyle
     */
    public CommonDialog setButtonStyle(DialogParams.ButtonStyle buttonStyle) {
        dialogParams.setButtonStyle(buttonStyle);
        return this;
    }

    /**
     * 设置按钮的点击事件
     */
    public CommonDialog setClickListener(DialogParams.ClickListener clickListener) {
        dialogParams.setClickListener(clickListener);
        return this;
    }
    /********************************************* 定制对话框 *********************************************/
}
