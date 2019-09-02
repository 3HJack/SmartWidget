package com.hhh.onepiece;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.hhh.smartwidget.WidgetUtils;
import com.hhh.smartwidget.bubble.Bubble;
import com.hhh.smartwidget.bubble.BubbleFactory;
import com.hhh.smartwidget.dialog.DialogBuilderFactory;
import com.hhh.smartwidget.dialog.SmartDialog;
import com.hhh.smartwidget.inputpanel.InputPanel;
import com.hhh.smartwidget.popup.Popup;
import com.hhh.smartwidget.popup.PopupInterface;
import com.hhh.smartwidget.toast.ToastFactory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

public class MainActivity extends AppCompatActivity {

  public static final String TAG = "widget";

  private static final String KEY_BACKGROUND = "background";

  public static void testImmersive(@NonNull Activity activity) {
    activity.findViewById(R.id.immersive).setOnClickListener(
        view -> activity.startActivity(new Intent(activity, ImmersiveActivity.class)));
  }

  public static void testInputPanel(@NonNull FragmentActivity activity) {
    activity.findViewById(R.id.input_panel).setOnClickListener(v -> new InputPanel.Builder(activity)
        .setSendText("发送")
        .setInputHint("点赞是喜欢，评论才是爱")
        .setOnViewStateCallback((popup, inflater, container, bundle) -> inflater
            .inflate(R.layout.input_panel_default_layout, container, false))
        .show(new PopupInterface.OnVisibilityListener() {
          @Override
          public void onShow(@NonNull Popup popup) {
            Log.e(TAG, "Input Panel onShow");
          }
        }));
  }

  public static void testSmartDialog(@NonNull Activity activity) {
    activity.findViewById(R.id.succession_two_simple_dialog).setOnClickListener(v -> {
      showSimpleDialog(activity, "第一个弹窗");
      showSimpleDialog(activity, "第二个弹窗");
    });
    activity.findViewById(R.id.simple_dialog)
        .setOnClickListener(v -> showSimpleDialog(activity, "OnePiece"));
    activity.findViewById(R.id.simple_dialog_content_multi_line)
        .setOnClickListener(v -> showSimpleMultiContentDialog(activity));
    activity.findViewById(R.id.simple_dialog_title_content_multi_line)
        .setOnClickListener(v -> showSimpleMultiTitleContentDialog(activity));
    activity.findViewById(R.id.simple_dialog_two_button)
        .setOnClickListener(v -> showSimpleTwoButtonDialog(activity));
    activity.findViewById(R.id.simple_dialog_only_title)
        .setOnClickListener(v -> showSimpleNoContentDialog(activity));
    activity.findViewById(R.id.simple_dialog_only_title_multi_line)
        .setOnClickListener(v -> showSimpleMultiTitleNoContentDialog(activity));
    activity.findViewById(R.id.title_content_detail_dialog)
        .setOnClickListener(v -> showSimpleTitleContentDetailDialog(activity));

    activity.findViewById(R.id.input_dialog).setOnClickListener(v -> showInputDialog(activity));

    activity.findViewById(R.id.small_icon_dialog)
        .setOnClickListener(v -> showSmallIconDialog(activity));
    activity.findViewById(R.id.net_small_icon_dialog)
        .setOnClickListener(v -> showNetSmallIconDialog(activity));
    activity.findViewById(R.id.big_icon_dialog)
        .setOnClickListener(v -> showBigIconDialog(activity));
    activity.findViewById(R.id.big_icon_dialog_two_button)
        .setOnClickListener(v -> showBigIconTwoButtonDialog(activity));

    activity.findViewById(R.id.list_dialog).setOnClickListener(v -> showSimpleListDialog(activity));
    activity.findViewById(R.id.list_dialog_multi_line)
        .setOnClickListener(v -> showSimpleListMultiLineDialog(activity));
    activity.findViewById(R.id.multi_dialog).setOnClickListener(v -> showMultiDialog(activity));

    activity.findViewById(R.id.list_button_dialog)
        .setOnClickListener(v -> showListButtonDialog(activity));
    activity.findViewById(R.id.list_button_content_dialog)
        .setOnClickListener(v -> showListButtonContentDialog(activity));

    activity.findViewById(R.id.list_single_dialog)
        .setOnClickListener(v -> showListSingleDialog(activity));
    activity.findViewById(R.id.list_single_button_dialog)
        .setOnClickListener(v -> showListSingleButtonDialog(activity));
  }

  /**
   * 单行标题 + 单行内容 + 单按钮
   */
  public static void showSimpleDialog(@NonNull Activity context, @NonNull String extra) {
    DialogBuilderFactory.applySimpleDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("(无 id）标题")
        .setContentText(extra)
        .setPositiveText("确定")
        .onPositive((dialog, view) -> Log.e(TAG, "onPositive"))
        .setOnCancelListener((popup, cancelType) -> Log.e(TAG, "onCancel " + cancelType)))
        .show(new PopupInterface.OnVisibilityListener() {
          @Override
          public void onDismiss(@NonNull Popup popup, int dismissType) {
            Log.e(TAG, "onDismiss " + dismissType);
          }
        });
  }

  /**
   * 单行标题 + 多行内容 + 单按钮
   */
  public static void showSimpleMultiContentDialog(@NonNull Activity context) {
    DialogBuilderFactory.applySimpleDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("这是标题文字")
        .setContentText("告知当前状态，信息和解决方法如果文字换行的情况")
        .setPositiveText("确定"))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * 多行标题 + 多行内容 + 单按钮
   */
  public static void showSimpleMultiTitleContentDialog(@NonNull Activity context) {
    DialogBuilderFactory.applySimpleDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("这是标题文字标题文字如果两行这样显示")
        .setContentText("告知当前状态，信息和解决方法如果文字换行的情况")
        .setPositiveText("确定"))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * 单行标题 + 单行内容 + 双按钮
   */
  public static void showSimpleTwoButtonDialog(@NonNull Activity context) {
    DialogBuilderFactory.applySimpleDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("这是标题文字")
        .setContentText("告知当前状态，信息和解决方法")
        .setPositiveText("确定")
        .setNegativeText("取消")
        .onPositive((dialog, view) -> Log.e(TAG, "onPositive"))
        .onNegative((dialog, view) -> Log.e(TAG, "onNegative"))
        .setOnCancelListener((popup, cancelType) -> Log.e(TAG, "onCancel")))
        .show(new PopupInterface.OnVisibilityListener() {
          @Override
          public void onShow(@NonNull Popup popup) {
            Log.e(TAG, "onShow");
          }

          @Override
          public void onDismiss(@NonNull Popup popup, int dismissType) {
            Log.e(TAG, "onDismiss " + dismissType);
          }
        });
  }

  /**
   * 单行标题 + 单按钮
   */
  public static void showSimpleNoContentDialog(@NonNull Activity context) {
    DialogBuilderFactory.applySimpleDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("告知当前状态和解决方案")
        .setPositiveText("确定"))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * 多行标题 + 单按钮
   */
  public static void showSimpleMultiTitleNoContentDialog(@NonNull Activity context) {
    DialogBuilderFactory.applySimpleDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("告知当前状态，信息和解决方案如果文字换行的情况")
        .setPositiveText("确定"))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * 标题 + 内容 + 细节 + 双按钮
   */
  public static void showSimpleTitleContentDetailDialog(@NonNull Activity context) {
    DialogBuilderFactory.applySimpleDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("这是标题文字")
        .setContentText("告知当前状态，信息和解决方法")
        .setDetailText("告知当前状态，信息和解决方法告知当前状态，信息和解决方法告知当前状态，信息和解决方法")
        .setPositiveText("确定")
        .setNegativeText("取消"))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * 标题 + 内容 + 输入框 + 双按钮
   */
  public static void showInputDialog(@NonNull Activity context) {
    DialogBuilderFactory.applyInputDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("这是标题文字")
        .setContentText("告知当前状态，信息和解决方法")
        .setPositiveText("确定")
        .setNegativeText("取消")
        .setInput("默认文案", null, (dialog, view, input) -> Log.e(TAG, input.toString())))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * 小图标 + 标题 + 内容 + 单按钮
   */
  public static void showSmallIconDialog(@NonNull Activity context) {
    DialogBuilderFactory.applySmallIconDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("这是标题文字")
        .setContentText("告知当前状态，信息和解决方法")
        .setIcon(R.drawable.dialog_small_icon_background)
        .setPositiveText("确定"))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * 网络图标 + 标题 + 内容 + 单按钮
   */
  public static void showNetSmallIconDialog(@NonNull Activity context) {
    DialogBuilderFactory.applySmallIconDialogStyle(new SmartDialog.Builder(context)
        .setIconUri(Uri.parse(
            "https://raw.githubusercontent.com/3HJack/plugin/master/dialog_net_icon_background.png"))
        .setTitleText("这是标题文字")
        .setContentText("告知当前状态，信息和解决方法")
        .setPositiveText("确定"))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * 大图标 + 标题 + 内容 + 单按钮
   */
  public static void showBigIconDialog(@NonNull Activity context) {
    DialogBuilderFactory.applyBigIconDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("这是标题文字")
        .setContentText("告知当前状态，信息和解决方法")
        .setIcon(R.drawable.dialog_big_icon_background)
        .setPositiveText("确定"))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * 大图标 + 标题 + 内容 + 双按钮
   */
  public static void showBigIconTwoButtonDialog(@NonNull Activity context) {
    DialogBuilderFactory.applyBigIconDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("这是标题文字")
        .setContentText("告知当前状态，信息和解决方法")
        .setIcon(R.drawable.dialog_big_icon_background)
        .setPositiveText("确定")
        .setNegativeText("取消"))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * 简单list对话框
   */
  public static void showSimpleListDialog(@NonNull Activity context) {
    List<CharSequence> stringList = new ArrayList<>();
    stringList.add("告知当前状态，信息和解决方");
    stringList.add("法信息和解决方法");
    stringList.add("告知当前状态，信息和解决方法");
    stringList.add("告知当前状态，信息和解决方法");
    stringList.add("告知当前状态，信息和解决方法");
    DialogBuilderFactory.applyListDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("这是标题文字")
        .setListItems(stringList)
        .setPositiveText("确定")
        .setNegativeText("取消"))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * 简单list对话框（内容可能多行），内容不可点
   */
  public static void showSimpleListMultiLineDialog(@NonNull Activity context) {
    List<CharSequence> stringList = new ArrayList<>();
    stringList.add("告知当前状态，信息和解决方法两行这样显示");
    stringList.add("告知当前状态，信息和解决方法两行这样显示显示");
    stringList.add("告知当前状态，信息和解决方法");
    stringList.add("告知当前状态，信息和解决方法");
    stringList.add("告知当前状态，信息和解决方法");
    stringList.add("告知当前状态，信息和解决方法两行这样显示");
    stringList.add("告知当前状态，信息和解决方法两行这样显示");
    stringList.add("告知当前状态，信息和解决方法两行这样显示");
    DialogBuilderFactory.applyListDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("这是标题文字")
        .setListItems(stringList)
        .setPositiveText("确定")
        .setNegativeText("取消"))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * list多选框
   */
  public static void showMultiDialog(@NonNull Activity context) {
    List<CharSequence> stringList = new ArrayList<>();
    stringList.add("选项一");
    stringList.add("选项二");
    stringList.add("选项三");
    stringList.add("选项四");
    DialogBuilderFactory.applyListMultiDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("这是标题文字")
        .setListItems(stringList)
        .itemsCallbackMultiChoice(null, (dialog, position) -> {
          for (Integer integer : position) {
            Log.e(TAG, "position " + integer);
          }
        }).setPositiveText("确定")
        .setNegativeText("取消"))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * list多button对话框，没有图标，上下布局，无内容
   */
  public static void showListButtonDialog(@NonNull Activity context) {
    List<CharSequence> stringList = new ArrayList<>();
    stringList.add("选项一");
    stringList.add("选项二");
    stringList.add("选项三");
    DialogBuilderFactory.applyListButtonDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("告知当前状态，信息和解决方案如果文字换行的情况")
        .setListItems(stringList)
        .setSelectedIndex(1)
        .setItemsCallback(
            (dialog, itemView, position) -> Log.e(TAG, "showListButtonDialog " + position)))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * list多button对话框，没有图标，上下布局，有内容
   */
  public static void showListButtonContentDialog(@NonNull Activity context) {
    List<CharSequence> stringList = new ArrayList<>();
    stringList.add("选项一");
    stringList.add("选项二");
    stringList.add("选项三");
    DialogBuilderFactory.applyListButtonDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("这是标题文字")
        .setContentText("告知当前状态，信息和解决方法如果文字换行的情况")
        .setListItems(stringList)
        .setSelectedIndex(1)
        .setItemsCallback(
            (dialog, itemView, position) -> Log.e(TAG, "showListButtonContentDialog " + position)))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * list单选对话框，无按钮
   */
  public static void showListSingleDialog(@NonNull Activity context) {
    List<CharSequence> stringList = new ArrayList<>();
    stringList.add("user03@gmail.com");
    stringList.add("user03@gmail.com");
    stringList.add("user03@gmail.com");
    DialogBuilderFactory.applyListSingleDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("这是标题文字")
        .setListItems(stringList)
        .setSelectedIndex(1)
        .setItemsCallback(
            (dialog, itemView, position) -> Log.e(TAG, "showListButtonContentDialog " + position)))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  /**
   * list单选对话框，有按钮，图标在右边
   */
  public static void showListSingleButtonDialog(@NonNull Activity context) {
    List<CharSequence> stringList = new ArrayList<>();
    stringList.add("选项一");
    stringList.add("选项二");
    stringList.add("选项三");
    stringList.add("选项四");
    DialogBuilderFactory.applyListSingleButtonDialogStyle(new SmartDialog.Builder(context)
        .setTitleText("这是标题文字")
        .setListItems(stringList)
        .setSelectedIndex(1)
        .setItemsCallback(
            (dialog, itemView, position) -> Log.e(TAG, "showListSingleButtonDialog " + position))
        .setPositiveText("确定")
        .setNegativeText("取消"))
        .show(PopupInterface.EMPTY_VISIBILITY_LISTENER);
  }

  public static void testSmartToast(@NonNull Activity activity) {
    String title = activity.getIntent().getStringExtra(KEY_BACKGROUND);
    if (!TextUtils.isEmpty(title)) {
      activity.findViewById(R.id.container).setBackgroundColor(Color.BLUE);
    }

    activity.findViewById(R.id.info).setOnClickListener(v -> ToastFactory.info("一般toast"));
    activity.findViewById(R.id.notify).setOnClickListener(v -> ToastFactory.notify("成功toast"));
    activity.findViewById(R.id.alert).setOnClickListener(v -> ToastFactory.alert("失败toast"));

    activity.findViewById(R.id.sub_thread)
        .setOnClickListener(v -> new Thread(() -> ToastFactory.info("子线程发出的toast")).start());

    activity.findViewById(R.id.next_activity).setOnClickListener(v -> {
      ToastFactory.info("下一个Activity立即展示的toast");
      Intent intent = new Intent(activity, MainActivity.class);
      intent.putExtra(KEY_BACKGROUND, "下一个");
      activity.startActivity(intent);
    });
    activity.findViewById(R.id.next_activity_delay).setOnClickListener(v -> {
      ToastFactory.info("下一个Activity延迟展示的toast");
      Intent intent = new Intent(activity, MainActivity.class);
      intent.putExtra(KEY_BACKGROUND, "下一个");
      WidgetUtils.runOnUIThread(() -> activity.startActivity(intent), 500L);
    });
    activity.findViewById(R.id.prev_activity).setOnClickListener(v -> {
      ToastFactory.info("前一个Activity立即展示的toast");
      activity.finish();
    });
    activity.findViewById(R.id.prev_activity_delay).setOnClickListener(v -> {
      ToastFactory.info("前一个Activity延迟展示的toast");
      WidgetUtils.runOnUIThread(() -> activity.finish(), 500L);
    });
  }

  public static void testBubble(@NonNull Activity activity) {
    Bubble.Builder builder =
        new Bubble.Builder(activity).setAnchorView(activity.findViewById(R.id.anchor));
    activity.findViewById(R.id.left)
        .setOnClickListener(v -> BubbleFactory.showLeftWhiteBubble(builder.setText("左侧提示，长按黑色")));
    activity.findViewById(R.id.left).setOnLongClickListener(v -> {
      BubbleFactory.showLeftBlackBubble(builder.setText("左侧提示，单击白色"));
      return true;
    });
    activity.findViewById(R.id.top)
        .setOnClickListener(v -> BubbleFactory.showTopWhiteBubble(builder.setText("上侧提示，长按黑色")));
    activity.findViewById(R.id.top).setOnLongClickListener(v -> {
      BubbleFactory.showTopBlackBubble(builder.setText("上侧提示，单击白色"));
      return true;
    });
    activity.findViewById(R.id.right)
        .setOnClickListener(v -> BubbleFactory.showRightWhiteBubble(builder.setText("右侧提示，长按黑色")));
    activity.findViewById(R.id.right).setOnLongClickListener(v -> {
      BubbleFactory.showRightBlackBubble(builder.setText("右侧提示，单击白色"));
      return true;
    });
    activity.findViewById(R.id.bottom)
        .setOnClickListener(v -> BubbleFactory.showBottomWhiteBubble(builder.setText("下侧提示，长按黑色")));
    activity.findViewById(R.id.bottom).setOnLongClickListener(v -> {
      BubbleFactory.showBottomBlackBubble(builder.setText("下侧提示，单击白色"));
      return true;
    });
    activity.findViewById(R.id.top_right).setOnClickListener(v -> BubbleFactory
        .showTopWhiteBubble(builder.setText("上侧提示，上侧提示，上侧提示，上侧提示，上侧提示，上边偏右")
            .setArrowOffset(WidgetUtils.dip2px(10f))));
    activity.findViewById(R.id.top_left).setOnClickListener(v -> BubbleFactory
        .showTopWhiteBubble(builder.setText("上侧提示，上侧提示，上侧提示，上侧提示，上侧提示，上边偏左")
            .setArrowOffset(-WidgetUtils.dip2px(10f))));
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    testImmersive(this);
    testInputPanel(this);
    testSmartDialog(this);
    testSmartToast(this);
    testBubble(this);
  }
}
