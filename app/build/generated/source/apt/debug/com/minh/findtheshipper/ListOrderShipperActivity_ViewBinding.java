// Generated code from Butter Knife. Do not modify!
package com.minh.findtheshipper;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.Toolbar;
import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ListOrderShipperActivity_ViewBinding implements Unbinder {
  private ListOrderShipperActivity target;

  @UiThread
  public ListOrderShipperActivity_ViewBinding(ListOrderShipperActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public ListOrderShipperActivity_ViewBinding(ListOrderShipperActivity target, View source) {
    this.target = target;

    target.toolbar = Utils.findRequiredViewAsType(source, R.id.toolBar, "field 'toolbar'", Toolbar.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    ListOrderShipperActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.toolbar = null;
  }
}