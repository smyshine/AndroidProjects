// Generated code from Butter Knife. Do not modify!
package com.example.smy.photoloading;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class LocalAlbumFragment$$ViewBinder<T extends com.example.smy.photoloading.LocalAlbumFragment> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131492955, "field 'gvAlbum'");
    target.gvAlbum = finder.castView(view, 2131492955, "field 'gvAlbum'");
    view = finder.findRequiredView(source, 2131492957, "field 'mProgressBar'");
    target.mProgressBar = finder.castView(view, 2131492957, "field 'mProgressBar'");
    view = finder.findRequiredView(source, 2131492958, "field 'mNoFileTips'");
    target.mNoFileTips = finder.castView(view, 2131492958, "field 'mNoFileTips'");
    view = finder.findRequiredView(source, 2131492954, "field 'mDeleteMenu'");
    target.mDeleteMenu = finder.castView(view, 2131492954, "field 'mDeleteMenu'");
  }

  @Override public void unbind(T target) {
    target.gvAlbum = null;
    target.mProgressBar = null;
    target.mNoFileTips = null;
    target.mDeleteMenu = null;
  }
}
