// Generated code from Butter Knife. Do not modify!
package com.frankzhu.recyclerviewdemo.adapter;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class MultiSettingSelectAdapter$MultiSettingSelectViewHolder$$ViewBinder<T extends com.frankzhu.recyclerviewdemo.adapter.MultiSettingSelectAdapter.MultiSettingSelectViewHolder> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558484, "field 'mTvName'");
    target.mTvName = finder.castView(view, 2131558484, "field 'mTvName'");
    view = finder.findRequiredView(source, 2131558485, "field 'mIvCheck'");
    target.mIvCheck = finder.castView(view, 2131558485, "field 'mIvCheck'");
    view = finder.findRequiredView(source, 2131558483, "method 'onSelected' and method 'onLongSelected'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onSelected();
        }
      });
    view.setOnLongClickListener(
      new android.view.View.OnLongClickListener() {
        @Override public boolean onLongClick(
          android.view.View p0
        ) {
          return target.onLongSelected();
        }
      });
  }

  @Override public void unbind(T target) {
    target.mTvName = null;
    target.mIvCheck = null;
  }
}
