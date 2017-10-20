package com.customview.view.flabbybird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.customview.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SMY on 2017/10/20.
 */

public class GameFlabbyBird extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private Thread mThread;
    private boolean isRunning;

    private int mWidth;
    private int mHeight;
    private RectF mGamePanelRect = new RectF();
    private Bitmap mBackground;
    private Paint mPaint;

    //bird
    private Bird mBird;
    private Bitmap mBirdBitmap;

    //floor
    private Floor mFloor;
    private Bitmap mFloorBitmap;
    private int mSpeed;

    //pipe
    private Bitmap mPipeTop;
    private Bitmap mPipeBottom;
    private RectF mPipeRect;
    private int mPipeWidth;
    private static final int PIPE_WIDTH = 48;
    private List<Pipe> mPipes = new ArrayList<>();
    private List<Pipe> mNeedRemovePipe = new ArrayList<Pipe>();

    //score
    private final int[] mNums = new int[]{R.drawable.s0, R.drawable.s1, R.drawable.s2, R.drawable.s3,
            R.drawable.s4, R.drawable.s5, R.drawable.s6, R.drawable.s7, R.drawable.s8, R.drawable.s8};
    private Bitmap[] mScoresBitmap;
    private int mScore = 0;
    private static final float RADIO_SINGLE_SCORE_HEIGHT = 1 / 15f;
    private int mSingleScoreWidth;
    private int mSingleScoreHeight;
    private RectF mSingleScoreRect;
    private int mRemovedPipe;

    private enum GameStatus{
        WAITING, RUNNING, OVER
    }

    private GameStatus mGameStatus = GameStatus.WAITING;
    private static final int TOUCH_UP_SIZE = -16;
    private final int mBirdUpDis = Bird.dp2px(getContext(), TOUCH_UP_SIZE);
    private int mTmpBirdDis;
    private final int mAutoDownSpeed = Bird.dp2px(getContext(), 2);

    private final int PIPE_DIS_BETWEEN_TWO = Bird.dp2px(getContext(), 100);
    private int mTmpMoveDistance;

    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private boolean showedToast = false;

    private void logic(){
        switch (mGameStatus){
            case WAITING:
                if (!showedToast){
                    showedToast = true;
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Game Over, click to restart", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            case RUNNING:
                mScore = 0;
                mFloor.setX(mFloor.getX() - mSpeed);
                loginPipes();
                mTmpBirdDis += mAutoDownSpeed;
                mBird.setY(mBird.getY() + mTmpBirdDis);

                mScore += mRemovedPipe;
                for (Pipe pipe : mPipes){
                    if (pipe.getX() + mPipeWidth < mBird.getX()){
                        ++mScore;
                    }
                }
                checkGameOver();
                break;
            case OVER:
                if (mBird.getY() < mFloor.getY() - mBird.getmWidth()){
                    mTmpBirdDis += mAutoDownSpeed;
                    mBird.setY(mBird.getY() + mTmpBirdDis);
                } else {
                    showedToast = false;
                    mGameStatus = GameStatus.WAITING;
                    initPos();
                }
                break;
            default:
                break;
        }
    }

    private void loginPipes(){
        for (Pipe pipe : mPipes){
            if (pipe.getX() < -mPipeWidth){
                mNeedRemovePipe.add(pipe);
                ++mRemovedPipe;
                continue;
            }
            pipe.setX(pipe.getX() - mSpeed);
        }
        mPipes.removeAll(mNeedRemovePipe);

        mTmpMoveDistance += mSpeed;
        if (mTmpMoveDistance >= PIPE_DIS_BETWEEN_TWO){
            Pipe pipe = new Pipe(getContext(), getWidth(), getHeight(), mPipeTop, mPipeBottom);
            mPipes.add(pipe);
            mTmpMoveDistance = 0;
        }
    }

    private void initPos(){
        mPipes.clear();
        mNeedRemovePipe.clear();
        mBird.setY(mHeight * 2 / 3);
        mTmpBirdDis = 0;
        mRemovedPipe = 0;
    }

    private void checkGameOver(){
        if (mBird.getY() > mFloor.getY() - mBird.getmHeight()){
            mGameStatus = GameStatus.OVER;
            return;
        }
        for (Pipe pipe : mPipes){
            if (pipe.getX() + mPipeWidth < mBird.getX()){
                continue;
            }
            if (pipe.touchBird(mBird)){
                mGameStatus = GameStatus.OVER;
                break;
            }
        }
    }

    public GameFlabbyBird(Context context) {
        this(context, null);
    }

    public GameFlabbyBird(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);

        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);

        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        initBitmaps();

        mSpeed = Bird.dp2px(getContext(), 2);
        mPipeWidth = Bird.dp2px(getContext(), PIPE_WIDTH);
    }

    private void initBitmaps(){
        mBackground = loadImageByResId(R.drawable.profile_bg);
        mBirdBitmap = loadImageByResId(R.drawable.community_like_hl);
        mFloorBitmap = loadImageByResId(R.drawable.floor);
        mPipeTop = loadImageByResId(R.drawable.icon_privacy_bg);
        mPipeBottom = loadImageByResId(R.drawable.icon_privacy_bg);
        mScoresBitmap = new Bitmap[mNums.length];
        for (int i = 0; i < mScoresBitmap.length; ++i){
            mScoresBitmap[i] = loadImageByResId(mNums[i]);
        }
    }

    private Bitmap loadImageByResId(int id){
        return BitmapFactory.decodeResource(getResources(), id);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mGamePanelRect.set(0, 0, mWidth, mHeight);
        mBird = new Bird(getContext(), mWidth, mHeight, mBirdBitmap);
        mFloor = new Floor(mWidth, mHeight, mFloorBitmap);
        mPipeRect = new RectF(0, 0, mPipeWidth, mHeight);
        Pipe pipe = new Pipe(getContext(), w, h, mPipeTop, mPipeBottom);
        mPipes.add(pipe);

        mSingleScoreHeight = (int) (h * RADIO_SINGLE_SCORE_HEIGHT);
        mSingleScoreWidth = (int) (mSingleScoreHeight * 1.0f / mScoresBitmap[0].getHeight() * mScoresBitmap[0].getWidth());
        mSingleScoreRect = new RectF(0, 0, mSingleScoreWidth, mSingleScoreHeight);
    }

    @Override
    public void run() {
        while (isRunning){
            long start = System.currentTimeMillis();
            logic();
            draw();
            long end = System.currentTimeMillis();
            if (end - start < 50){
                try {
                    Thread.sleep(50 - (end - start));
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            switch (mGameStatus){
                case WAITING:
                    mGameStatus = GameStatus.RUNNING;
                    break;
                case RUNNING:
                    mTmpBirdDis = mBirdUpDis;
                    break;
            }
        }
        return true;
    }

    private void draw(){
        try {
            mCanvas = mHolder.lockCanvas();
            if (mCanvas != null){
                drawBackground();
                drawBird();
                drawPipe();
                drawFloor();
                drawScores();
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (mCanvas != null){
                mHolder.unlockCanvasAndPost(mCanvas);
            }
            mFloor.setX(mFloor.getX() - mSpeed);
        }
    }

    private void drawBackground(){
        mCanvas.drawBitmap(mBackground, null, mGamePanelRect, null);
    }

    private void drawBird(){
        mBird.draw(mCanvas);
    }

    private void drawFloor(){
        mFloor.draw(mCanvas, mPaint);
    }

    private void drawPipe(){
        for (Pipe pipe: mPipes){
            pipe.setX(pipe.getX() - mSpeed);
            pipe.draw(mCanvas, mPipeRect);
        }
    }

    private void drawScores(){
        String score = mScore + "";
        mCanvas.save(Canvas.MATRIX_SAVE_FLAG);
        mCanvas.translate(mWidth / 2 - score.length() * mSingleScoreWidth / 2, 1f / 8 * mHeight);
        for (int i = 0; i < score.length(); ++i){
            String numStr = score.substring(i, i + 1);
            int num = Integer.valueOf(numStr);
            mCanvas.drawBitmap(mScoresBitmap[num], null, mSingleScoreRect, null);
            mCanvas.translate(mSingleScoreWidth, 0);
        }
        mCanvas.restore();
    }
}
