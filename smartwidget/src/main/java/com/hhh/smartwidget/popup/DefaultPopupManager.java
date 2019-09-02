package com.hhh.smartwidget.popup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import android.app.Activity;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DefaultPopupManager implements PopupInterface.PopupManager {

  private final WeakHashMap<Activity, List<Popup>> POPUP_MAP_LIST = new WeakHashMap<>();

  @Override
  public boolean enableShowNow(@NonNull Activity activity, @NonNull Popup popup) {
    if (isEmpty(activity) || popup.getExcluded() == PopupInterface.Excluded.NOT_AGAINST) {
      return true;
    } else if (popup.getExcluded() == PopupInterface.Excluded.ALL_TYPE) {
      return false;
    } else {
      boolean existSameTypePopup = false;
      for (Popup popupItem : getPopupList(activity)) {
        if (TextUtils.equals(popupItem.getPopupType(), popup.getPopupType())) {
          existSameTypePopup = true;
          break;
        }
      }
      return !existSameTypePopup;
    }
  }

  @Override
  public void onPopupShow(@NonNull Activity activity, @NonNull Popup popup) {
    put(activity, popup);
  }

  @Override
  public void onPopupDismiss(@NonNull Activity activity, @NonNull Popup popup) {
    remove(activity, popup);
    Popup pendingPopup = getNextPendingPopup(activity);
    if (pendingPopup != null) {
      pendingPopup.show();
    }
  }

  @Override
  public void onPopupPending(@NonNull Activity activity, @NonNull Popup popup) {
    put(activity, popup);
  }

  @Override
  public void onPopupDiscard(@NonNull Activity activity, @NonNull Popup popup) {
    remove(activity, popup);
  }

  @Override
  public void onActivityDestroy(@NonNull Activity activity) {
    clear(activity);
  }

  @Nullable
  public Popup getNextPendingPopup(@NonNull Activity activity) {
    List<Popup> popupList = getShowingPopupList(activity);
    if (!popupList.isEmpty()) {
      for (Popup popup : popupList) {
        if (!Popup.isPermanentPopup(popup)) {
          return null;
        }
      }
    }
    popupList = POPUP_MAP_LIST.get(activity);
    if (popupList == null || popupList.isEmpty() || activity.isFinishing()) {
      return null;
    }
    for (Popup popup : popupList) {
      if (!popup.isShowing()) {
        popupList.remove(popup);
        return popup;
      }
    }
    return null;
  }

  private void put(@NonNull Activity activity, @NonNull Popup popup) {
    List<Popup> popupList = POPUP_MAP_LIST.get(activity);
    if (popupList == null) {
      popupList = new ArrayList<>();
      POPUP_MAP_LIST.put(activity, popupList);
    }
    if (!popupList.contains(popup)) {
      popupList.add(popup);
    }
  }

  private void remove(@NonNull Activity activity, @NonNull Popup popup) {
    List<Popup> popupList = POPUP_MAP_LIST.get(activity);
    if (popupList != null) {
      popupList.remove(popup);
    }
  }

  private void clear(@NonNull Activity activity) {
    List<Popup> popupList = POPUP_MAP_LIST.remove(activity);
    if (popupList != null) {
      for (Popup popup : popupList) {
        if (popup.isShowing()) {
          popup.dismiss(PopupInterface.CLOSE_TYPE_AUTO);
        } else {
          popup.discard();
        }
      }
    }
  }

  public boolean isEmpty(@NonNull Activity activity) {
    List<Popup> popupList = POPUP_MAP_LIST.get(activity);
    return popupList == null || popupList.isEmpty();
  }

  @Nullable
  public Popup getPopupByTag(@NonNull Activity activity, @Nullable Object tag) {
    List<Popup> popupList = getPopupList(activity);
    if (popupList.isEmpty()) {
      return null;
    }
    if (tag == null) {
      return popupList.get(0);
    }
    for (Popup popup : popupList) {
      if (tag.equals(popup.getTag())) {
        return popup;
      }
    }
    return null;
  }

  @NonNull
  public List<Popup> getPopupByType(@NonNull Activity activity, @NonNull String popupType) {
    List<Popup> popupList = new ArrayList<>();
    if (isEmpty(activity)) {
      return popupList;
    }
    for (Popup popup : getPopupList(activity)) {
      if (TextUtils.equals(popupType, popup.getPopupType())) {
        popupList.add(popup);
      }
    }
    return Collections.unmodifiableList(popupList);
  }

  @NonNull
  private List<Popup> getShowingPopupList(@NonNull Activity activity) {
    List<Popup> popupList = new ArrayList<>();
    if (isEmpty(activity)) {
      return popupList;
    }
    for (Popup popup : getPopupList(activity)) {
      if (popup.isShowing()) {
        popupList.add(popup);
      }
    }
    return Collections.unmodifiableList(popupList);
  }

  @NonNull
  public List<Popup> getPopupList(@NonNull Activity activity) {
    List<Popup> popupList = POPUP_MAP_LIST.get(activity);
    if (popupList == null) {
      popupList = new ArrayList<>();
    }
    return Collections.unmodifiableList(popupList);
  }
}
