package com.example.smy.photoloading;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by SMY on 2016/9/2.
 */
public class LocalAlbumFragment extends AlbumBaseFragment {

    @Bind(R.id.gvPhotoList)
    StickyGridHeadersGridView gvAlbum;

    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;

    @Bind(R.id.no_file)
    TextView mNoFileTips;

    @Bind(R.id.bottomMenu)
    SingleButtonMenu mDeleteMenu;


    /**
     * 在checkMode, 可以选择图片并且删除
     */
    private boolean isCheckedMode = false;
    private List<LocalMediaInfo> mediaItems = new ArrayList<LocalMediaInfo>();
    private int selectedCount = 0;
    private LocalMediaAdapter adapter;

    private IAlbumCheckModelListener mCheckListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_local_album, null);
        ButterKnife.bind(this, view);

        gvAlbum.setAreHeadersSticky(true);
        gvAlbum.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LocalMediaInfo info = mediaItems.get(position);
                if (isCheckedMode) {
                    if (info.checked) {
                        info.checked = false;
                        selectedCount--;
                    } else {
                        info.checked = true;
                        selectedCount++;
                    }
                    adapter.notifyDataSetChanged();
                } else {

                }
            }
        });
        gvAlbum.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= mediaItems.size()) {
                    return false;
                }
                if (!isCheckedMode) {
                    LocalMediaInfo info = mediaItems.get(position);
                    info.checked = true;
                    if (selectedCount >= 1) {
                        selectedCount++;
                    } else {
                        selectedCount = 1;
                    }
                    changeCheckmode();
                    adapter.notifyDataSetChanged();
                    return true;
                }
                return false;
            }
        });

        mDeleteMenu.setMenuClickListener(new SingleButtonMenu.SingleMenuClickListener() {
            @Override
            public void onClick() {
                doDeleteFile();
            }
        });

        loadFileInfos();

        return view;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VideoDurationLoader.getInstance().stop();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    public void setCheckModeListener(IAlbumCheckModelListener listener){
        mCheckListener = listener;
    }

    @Override
    public boolean onBackPressed() {
        if (isCheckedMode) {
            changeCheckmode();
            return true;
        }
        return false;
    }

    /**
     * 切换到选择模式
     */
    private void switchToCheckMode() {
        isCheckedMode = true;
        mDeleteMenu.setVisibility(View.VISIBLE);
    }

    /**
     * 退出选择模式
     */
    public void cancelCheckMode() {
        isCheckedMode = false;
        mDeleteMenu.setVisibility(View.GONE);
        for (int i = 0; i < mediaItems.size(); i++) {
            mediaItems.get(i).checked = false;
        }
        selectedCount = 0;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void changeCheckmode() {
        if(isCheckedMode){
            cancelCheckMode();
        } else {
            switchToCheckMode();
        }
        if(mCheckListener != null){
            mCheckListener.onAlbumCheckChanged(isCheckedMode);
        }
    }

    @Override
    public void clearCheckStatus(){
        if(isCheckedMode){
            changeCheckmode();
        }
    }

    @Override
    public void selectAll() {
        if (isCheckedMode && !mediaItems.isEmpty()) {
            for (LocalMediaInfo mediaInfo:mediaItems){
                if(!mediaInfo.checked){
                    mediaInfo.checked = true;
                    selectedCount++;
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    private class LocalMediaAdapter extends BaseListAdapter implements StickyGridHeadersSimpleAdapter {

        private int listItemWidth;
        private int listItemHeight;


        private LocalMediaAdapter(Context context) {
            super(R.layout.album_media_item);
            listItemWidth = (DensityUtil.getScreenWidth(context) - DensityUtil.dip2px(context, 13)) / 2;
            listItemHeight = listItemWidth / 2;
        }

        @Override
        public long getHeaderId(int position) {
            if (mediaItems == null || mediaItems.size() <= position) {
                return 0;
            }
            LocalMediaInfo info = mediaItems.get(position);
            return info.headerId;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.album_item_header, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String headerValue = "";
            LocalMediaInfo info = mediaItems.get(position);
            if (info.headerId > 0) {
                headerValue = DateUtil.getAlbumDateString(getActivity(), info.time);
                holder.getTextView(R.id.albumTitle).setGravity(Gravity.CENTER_VERTICAL);
            }
            holder.getTextView(R.id.albumTitle).setText(headerValue);
            if(position == 0){
                holder.getView(R.id.dividerLine).setVisibility(View.GONE);
            } else {
                holder.getView(R.id.dividerLine).setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        @Override
        public int getCount() {
            return mediaItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mediaItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(mResourceId, null);
                holder = new ViewHolder(convertView);
                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(listItemWidth, listItemHeight);
                holder.getView(R.id.ivMediaThumbnail).setLayoutParams(param);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            final LocalMediaInfo info = mediaItems.get(position);

            /*根据图片位置，设置item的左右padding*/
            if(position==0){
                info.gridPos = LocalMediaInfo.LEFT;
            }else {
                LocalMediaInfo preInfo = mediaItems.get(position-1);
                if(preInfo.headerId!=info.headerId){
                    info.gridPos = LocalMediaInfo.LEFT;
                } else {
                    info.gridPos = preInfo.gridPos*(-1);
                }
            }
            RelativeLayout view = (RelativeLayout) holder.getItemView();
            if(info.gridPos==LocalMediaInfo.LEFT){
                view.setPadding(getResources().getDimensionPixelSize(R.dimen.length_10),0,0,0);
            } else {
                view.setPadding(0,0,getResources().getDimensionPixelSize(R.dimen.length_10),0);
            }

            final LinearLayout llvideoTagFinal = holder.getView(R.id.llVideoFlag);
            final ImageView hightLightFlagFinal = holder.getView(R.id.highlightsFlogs);
            final View flCheked = holder.getView(R.id.flChecked);
            final TextView videoDurationText = holder.getView(R.id.video_duration);
            final ImageView flChekedImage = holder.getView(R.id.ivCheck);

            ImageView ivPhoto = holder.getView(R.id.ivMediaThumbnail);

            if (isCheckedMode) {
                flCheked.setVisibility(View.VISIBLE);
                if (info.checked) {
                    flChekedImage.setImageResource(R.drawable.ic_choose);
                } else {
                    flChekedImage.setImageResource(R.drawable.ic_tag);
                }
            } else {
                flCheked.setVisibility(View.GONE);
            }

            llvideoTagFinal.setVisibility(View.GONE);
            hightLightFlagFinal.setVisibility(View.GONE);

            videoDurationText.setVisibility(View.GONE);
            VideoDurationLoader.getInstance().loadDuration(getActivity(),info, videoDurationText,new VideoDurationLoader.DurationLoadingListener(){
                @Override
                public void onLoadingStarted(String path, View view) {

                }

                @Override
                public void onLoadingFailed(String path, View view) {
                }

                @Override
                public void onLoadingComplete(String path, final View view, final int durationS) {
                    if (getActivity() == null) {
                        return;
                    }
                    if (view == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            view.setVisibility(View.VISIBLE);
                            ((TextView)view).setText(DateUtil.getTime(durationS));
                        }
                    });
                }

                @Override
                public void onLoadingCancelled(String path, View view) {

                }
            });
            if (info.type == LocalMediaInfo.TYPE_VIDEO) {
                llvideoTagFinal.setVisibility(View.VISIBLE);
                if (info.fileName.contains("_splendid_")) {
                    hightLightFlagFinal.setVisibility(View.VISIBLE);
                }
                YiImageLoader.loadYiImage(getActivity(), Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(info.id))
                        .toString(), ivPhoto, R.drawable.album_list_video);
            } else if (info.type == LocalMediaInfo.TYPE_PICTURE || info.type == LocalMediaInfo.TYPE_GIF) {
                ivPhoto.setImageResource(R.drawable.album_list_photo);
                llvideoTagFinal.setVisibility(View.GONE);
                if (info.fileName.contains("_puzzle_")) {
                    hightLightFlagFinal.setVisibility(View.VISIBLE);
                }
                YiImageLoader.loadYiImage(getActivity(), Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(info.id))
                        .toString(), ivPhoto, R.drawable.album_list_photo);
            }
            return holder.getItemView();
        }
    }

    /**
     * 加载相册媒体,
     */
    private void loadFileInfos() {
        new AsyncTask<Void, Void, Void>() {
            protected void onPreExecute() {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                getFiles(AppConstants.MEDIA_DIR);
                sortVideos();
                return null;
            }

            protected void onPostExecute(Void result) {
                mProgressBar.setVisibility(View.GONE);
                initGrid();
            }

            private void sortVideos() {
                Collections.sort(mediaItems, new Comparator<LocalMediaInfo>() {
                    @Override
                    public int compare(LocalMediaInfo lhs, LocalMediaInfo rhs) {
                        if (lhs == null) {
                            return 1;
                        }

                        if (rhs == null) {
                            return -1;
                        }
                        return (int) ((rhs.time - lhs.time) / 1000);
                    }

                });
            }

            /**
             * 遍历文件夹
             *
             * @param path
             */
            private void getFiles(String path) {
                File dir = new File(path);
                if (TextUtils.isEmpty(path)) {
                    return;
                }
                if (getActivity() == null) {
                    return;
                }

                if(!dir.exists()){
                    dir.mkdir();
                }

                mediaItems.clear();

                List<MediaInfoUtil.FileInfo> mediaInfo = MediaInfoUtil.getAllMediaInfo(getActivity(), dir.getAbsolutePath());
                for (MediaInfoUtil.FileInfo fileInfo : mediaInfo) {
                    LocalMediaInfo videoInfo = new LocalMediaInfo();
                    videoInfo.fileName = fileInfo.fileName;
                    videoInfo.filePath = fileInfo.filePath;
                    videoInfo.type = LocalMediaInfo.getFileType(fileInfo.fileName);
                    videoInfo.time = fileInfo.time * 1000;
                    videoInfo.id = fileInfo.id;
                    videoInfo.size = fileInfo.size;
                    videoInfo.width_height[0] = fileInfo.width;
                    videoInfo.width_height[1] = fileInfo.height;
                    videoInfo.headerId = Long.valueOf(DateUtil.formatYYYYMMDD(videoInfo.time));

                    if (videoInfo.type != LocalMediaInfo.TYPE_OTHER) {
                        mediaItems.add(videoInfo);
                    }
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initGrid() {
        if (getActivity() == null) {
            return;
        }

        if (mediaItems.size() == 0) {
            mNoFileTips.setVisibility(View.VISIBLE);
        } else {
            mNoFileTips.setVisibility(View.GONE);
        }

        if (adapter == null) {
            adapter = new LocalMediaAdapter(getActivity());
            gvAlbum.setAdapter(adapter);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void doDeleteFile() {
        if (selectedCount > 0) {
            deleteFiles();
        } else {
            changeCheckmode();
            //ToastUtils.showToast(getActivity(),getString(R.string.select_photo));
        }
    }

    @Override
    public void doVisibleForUser() {
        if(adapter == null){
            loadFileInfos();
        }
    }

    private void deleteFiles() {
        for (int i = 0; i < mediaItems.size(); i++) {
            LocalMediaInfo info = mediaItems.get(i);
            if (info.checked) {
                deleteItem(i);
                mediaItems.remove(i);
                i -= 1;
            }
        }
        changeCheckmode();

        if (mediaItems.size() == 0) {
            mNoFileTips.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 删除单个,根据相机相册还是导入相册区分
     *
     * @param position
     */
    private void deleteItem(int position) {
        LocalMediaInfo info = mediaItems.get(position);
        if (info.filePath.toUpperCase().contains(AppConstants.MEDIA_NAME)) {
            if (info.type == LocalMediaInfo.TYPE_VIDEO) {
                MediaInfoUtil.deleteVideoMediaInfo(getActivity(), info.filePath);
            } else {
                MediaInfoUtil.deletePicMediaInfo(getActivity(), info.filePath);
            }
            File file = new File(info.filePath);
            file.delete();
        }
    }
}
