package com.hhh.smartwidget.toast;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

public final class ToastManager {
  public static final int LENGTH_INDEFINITE = -2;
  public static final int LENGTH_SHORT = -1;
  public static final int LENGTH_LONG = 0;

  public static final long SHORT_DURATION_MS = 1500L;
  public static final long LONG_DURATION_MS = 2000L;

  private static final int MSG_TIMEOUT = 0;

  private static final ToastManager sToastManager = new ToastManager();

  private final Object mLock;
  private final Handler mHandler;

  private ToastRecord mCurrentToast;
  private ToastRecord mNextToast;

  private ToastManager() {
    mLock = new Object();
    mHandler = new Handler(Looper.getMainLooper(), message -> {
      switch (message.what) {
        case MSG_TIMEOUT:
          handleTimeout((ToastRecord) message.obj);
          return true;
        default:
          return false;
      }
    });
  }

  public static ToastManager getInstance() {
    return sToastManager;
  }

  public void show(int duration, @NonNull Callback callback) {
    synchronized (mLock) {
      if (isCurrentToastLocked(callback)) {
        mCurrentToast.mDuration = duration;
        mHandler.removeCallbacksAndMessages(mCurrentToast);
        scheduleTimeoutLocked(mCurrentToast);
      } else {
        if (isNextToastLocked(callback)) {
          mNextToast.mDuration = duration;
        } else {
          mNextToast = new ToastRecord(duration, callback);
        }

        if (mCurrentToast == null || !cancelToastLocked(mCurrentToast)) {
          mCurrentToast = null;
          showNextToastLocked();
        }
      }
    }
  }

  public void dismiss(@NonNull Callback callback) {
    synchronized (mLock) {
      if (isCurrentToastLocked(callback)) {
        cancelToastLocked(mCurrentToast);
      } else if (isNextToastLocked(callback)) {
        cancelToastLocked(mNextToast);
      }
    }
  }

  public void onDismissed(@NonNull Callback callback) {
    synchronized (mLock) {
      if (isCurrentToastLocked(callback)) {
        mCurrentToast = null;
        if (mNextToast != null) {
          showNextToastLocked();
        }
      }
    }
  }

  public void onShown(@NonNull Callback callback) {
    synchronized (mLock) {
      if (isCurrentToastLocked(callback)) {
        scheduleTimeoutLocked(mCurrentToast);
      }
    }
  }

  public void pauseTimeout(@NonNull Callback callback) {
    synchronized (mLock) {
      if (isCurrentToastLocked(callback) && !mCurrentToast.mPaused) {
        mCurrentToast.mPaused = true;
        mHandler.removeCallbacksAndMessages(mCurrentToast);
      }
    }
  }

  public void restoreTimeoutIfPaused(@NonNull Callback callback) {
    synchronized (mLock) {
      if (isCurrentToastLocked(callback) && mCurrentToast.mPaused) {
        mCurrentToast.mPaused = false;
        scheduleTimeoutLocked(mCurrentToast);
      }
    }
  }

  public boolean isCurrent(@NonNull Callback callback) {
    synchronized (mLock) {
      return isCurrentToastLocked(callback);
    }
  }

  public boolean isCurrentOrNext(@NonNull Callback callback) {
    synchronized (mLock) {
      return isCurrentToastLocked(callback) || isNextToastLocked(callback);
    }
  }

  private void showNextToastLocked() {
    if (mNextToast != null) {
      mCurrentToast = mNextToast;
      mNextToast = null;
      Callback callback = mCurrentToast.mCallback.get();
      if (callback != null) {
        callback.show();
      } else {
        mCurrentToast = null;
      }
    }
  }

  private boolean cancelToastLocked(ToastRecord record) {
    Callback callback = record.mCallback.get();
    if (callback != null) {
      mHandler.removeCallbacksAndMessages(record);
      callback.dismiss();
      return true;
    } else {
      return false;
    }
  }

  private boolean isCurrentToastLocked(Callback callback) {
    return mCurrentToast != null && mCurrentToast.isToast(callback);
  }

  private boolean isNextToastLocked(Callback callback) {
    return mNextToast != null && mNextToast.isToast(callback);
  }

  private void scheduleTimeoutLocked(ToastRecord record) {
    if (record.mDuration != LENGTH_INDEFINITE) {
      long durationMs = LONG_DURATION_MS;
      if (record.mDuration > 0) {
        durationMs = record.mDuration;
      } else if (record.mDuration == LENGTH_SHORT) {
        durationMs = SHORT_DURATION_MS;
      }

      mHandler.removeCallbacksAndMessages(record);
      mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_TIMEOUT, record), durationMs);
    }
  }

  private void handleTimeout(ToastRecord record) {
    synchronized (mLock) {
      if (mCurrentToast == record || mNextToast == record) {
        cancelToastLocked(record);
      }
    }
  }

  interface Callback {
    void show();

    void dismiss();
  }

  private static class ToastRecord {
    private final WeakReference<Callback> mCallback;
    private int mDuration;
    private boolean mPaused;

    private ToastRecord(int duration, Callback callback) {
      mCallback = new WeakReference<>(callback);
      mDuration = duration;
    }

    private boolean isToast(Callback callback) {
      return callback != null && mCallback.get() == callback;
    }
  }
}
