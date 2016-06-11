package com.example.smy.phonewall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by SMY on 2016/6/11.
 */
public class PhotoWallAdapter extends ArrayAdapter<String> implements AbsListView.OnScrollListener {
    private Set<BitmapWorkerTask> taskCollection;
    private LruCache<String, Bitmap> mMemoryCache;
    private GridView mPhotoWall;
    private int mFirstVisibleItem;
    private int mVisibleItemCount;
    private boolean isFirstEnter = true;

    public PhotoWallAdapter(Context context, int textViewRid, String[] objs, GridView photoWall)
    {
        super(context, textViewRid, objs);
        mPhotoWall = photoWall;
        taskCollection = new HashSet<BitmapWorkerTask>();
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize)
        {
            @Override
            protected  int sizeOf(String key, Bitmap bitmap)
            {
                return bitmap.getByteCount();
            }
        };
        mPhotoWall.setOnScrollListener(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final String url = getItem(position);
        View view;
        if(convertView == null)
        {
            view = LayoutInflater.from(getContext()).inflate(R.layout.photo_layout, null);
        }
        else
        {
            view = convertView;
        }
        final ImageView imageView = (ImageView) view.findViewById(R.id.photo);
        imageView.setTag(url);
        setImageView(url, imageView);
        return view;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int state)
    {
        if(state == SCROLL_STATE_IDLE)
        {
            loadBitmaps(mFirstVisibleItem, mVisibleItemCount);
        }
        else
        {
            cancelAllTasks();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount)
    {
        mFirstVisibleItem = firstVisible;
        mVisibleItemCount = visibleCount;
        if(isFirstEnter && visibleCount > 0)
        {
            loadBitmaps(firstVisible, visibleCount);
            isFirstEnter = false;
        }
    }

    private void setImageView(String imageUrl, ImageView imageView)
    {
        Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
        if(bitmap != null)
        {
            imageView.setImageBitmap(bitmap);
        }
        else
        {
            imageView.setImageResource(R.drawable.empty_photo);
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap)
    {
        if(getBitmapFromMemoryCache(key) == null)
        {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemoryCache(String key)
    {
        return mMemoryCache.get(key);
    }

    private void loadBitmaps(int firstVisible, int visibleCount)
    {
        try
        {
            for(int i = firstVisible; i < firstVisible + visibleCount; ++i)
            {
                String imageUrl = Images.imageThumbUrls[i];
                Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
                if(bitmap == null)
                {
                    BitmapWorkerTask task = new BitmapWorkerTask();
                    taskCollection.add(task);
                    task.execute(imageUrl);
                }
                else
                {
                    ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUrl);
                    if(imageUrl != null && bitmap != null)
                    {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void cancelAllTasks()
    {
        if(taskCollection != null)
        {
            for(BitmapWorkerTask task : taskCollection)
            {
                task.cancel(false);
            }
        }
    }


    //asyn task to download pictures
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap>
    {
        private String imageUrl;
        @Override
        protected Bitmap doInBackground(String... params)
        {
            imageUrl = params[0];
            Bitmap bitmap = downloadBitmap(params[0]);
            if (bitmap != null)
            {
                addBitmapToMemoryCache(params[0], bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            super.onPostExecute(bitmap);
            ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUrl);
            if(imageView != null && bitmap != null)
            {
                imageView.setImageBitmap(bitmap);
            }
            taskCollection.remove(this);
        }

        private Bitmap downloadBitmap(String imageUrl)
        {
            Bitmap bitmap = null;
            HttpURLConnection con = null;
            try {
                URL url = new URL(imageUrl);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5 * 1000);
                con.setReadTimeout(10 * 1000);
                bitmap = BitmapFactory.decodeStream(con.getInputStream());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally {
                if(con != null)
                {
                    con.disconnect();
                }
            }
            return bitmap;
        }
    }
}
