package com.tencent.shadow.sample.plugin3.data;

/**
 * 日期  2021/4/25 15:30
 *
 * @author Zhao
 * 描述：通用的可定制对话框的定制参数
 */
public class DialogParams {

    private String title;
    private String message;
    private String yesContent;
    private String noContent;
    private ButtonStyle buttonStyle = ButtonStyle.BOTH; // 默认显示确定和取消按钮
    private ClickListener clickListener;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getYesContent() {
        return yesContent;
    }

    public void setYesContent(String yesContent) {
        this.yesContent = yesContent;
    }

    public String getNoContent() {
        return noContent;
    }

    public void setNoContent(String noContent) {
        this.noContent = noContent;
    }

    public ButtonStyle getButtonStyle() {
        return buttonStyle;
    }

    public void setButtonStyle(ButtonStyle buttonStyle) {
        this.buttonStyle = buttonStyle;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public ClickListener getClickListener() {
        return clickListener;
    }

    /**
     * 显示哪些按钮
     * ONLY_YES：只显示确定键
     * ONLY_NO：只显示取消键
     * BOTH：确定键和取消键都显示
     */
    public enum ButtonStyle {
        BOTH, ONLY_YES, ONLY_NO
    }

    /**
     * 监听按钮点击事件
     */
    public interface ClickListener {
        /**
         * 点击确定按钮
         */
        void onClickYesListener();

        /**
         * 点击取消按钮
         */
        void onClickNoListener();
    }
}
