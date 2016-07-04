// Generated code from Butter Knife. Do not modify!
package com.frankzhu.recyclerviewdemo.adapter;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class MultipleItemAdapter$TextViewHolder$$ViewBinder<T extends com.frankzhu.recyclerviewdemo.adapter.MultipleItemAdapter.TextViewHolder> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558482, "field 'mTextView'");
    target.mTextView = finder.castView(view, 2131558482, "field 'mTextView'");
    view = finder.findRequiredView(source, 2131558480, "method 'onItemClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onItemClick();
        }
      });
  }

  @Override public void unbind(T target) {
    target.mTextView = null;
  }
}
