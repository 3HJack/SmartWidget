package com.hhh.smartwidget.toast;

import java.util.List;

import androidx.annotation.NonNull;

public final class RealInterceptorChain implements Interceptor.Chain {
  private final List<Interceptor> mInterceptors;
  private SmartToast.Builder mRequest;
  private int mIndex;

  public RealInterceptorChain(@NonNull List<Interceptor> interceptors,
      @NonNull SmartToast.Builder request) {
    mInterceptors = interceptors;
    mRequest = request;
  }

  @NonNull
  @Override
  public SmartToast.Builder request() {
    return mRequest;
  }

  @NonNull
  @Override
  public SmartToast.Builder proceed(@NonNull SmartToast.Builder request) {
    if (mIndex >= mInterceptors.size()) {
      return request;
    }
    mRequest = request;
    Interceptor interceptor = mInterceptors.get(mIndex++);
    request = interceptor.intercept(this);
    if (mIndex != mInterceptors.size()) {
      throw new IllegalStateException(
          "interceptor " + interceptor + " must call proceed() exactly once");
    }
    return request;
  }
}
