// Generated code from Butter Knife. Do not modify!
package com.frankzhu.recyclerviewdemo.fragment;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class NormalFragment$$ViewBinder<T extends com.frankzhu.recyclerviewdemo.fragment.NormalFragment> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558479, "field 'mRecyclerView'");
    target.mRecyclerView = finder.castView(view, 2131558479, "field 'mRecyclerView'");
  }

  @Override public void unbind(T target) {
    target.mRecyclerView = null;
  }
}
