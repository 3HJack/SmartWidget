package com.hhh.smartwidget;

import android.os.Parcel;
import android.os.Parcelable;

public class SystemBarInfo implements Parcelable {

  public static final Creator<SystemBarInfo> CREATOR = new Creator<SystemBarInfo>() {
    @Override
    public SystemBarInfo createFromParcel(Parcel source) {
      return new SystemBarInfo(source);
    }

    @Override
    public SystemBarInfo[] newArray(int size) {
      return new SystemBarInfo[size];
    }
  };
  public boolean mIsExist;
  public int mHeight;

  public SystemBarInfo() {}

  protected SystemBarInfo(Parcel in) {
    this.mIsExist = in.readByte() != 0;
    this.mHeight = in.readInt();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeByte(this.mIsExist ? (byte) 1 : (byte) 0);
    dest.writeInt(this.mHeight);
  }
}
