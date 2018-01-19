package com.lwb.tetrislib;

import android.app.Service;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by Administrator on 2018/1/19.
 */
public class TetrisView extends SurfaceView implements SurfaceHolder.Callback, Runnable, GestureDetector.OnGestureListener {

    private final int T_WIDTH_PIX = 12;

    private final int T_HEIGHT_PIX = 24;

    private final int DEFAULT_STEP_TIME = 1000;

    private int pixSize;

    private Paint mPaint;

    private SurfaceHolder mSurfaceHolder;

    private Canvas mCanvas;

    private Rect gameRect;

    private int gameWidth, gameHeight;

    private boolean isDrawing;

    private int refreshTime = 16, stepTime = DEFAULT_STEP_TIME, stepCount = -1, currentStepTime = DEFAULT_STEP_TIME;

    private Rect tempRect;

    private boolean[][] saveDrawArray;

    private Rect[][] pixRectArray, nextRectArray;

    private boolean isGameOver = false;

    private GestureDetector mGestureDetector;

    private Rect btnLeftRect, btnTopRect, btnRightRect, btnBottomRect, btnTurnRect, longPressRect;

    private Rect scoreRect, nextTetrisRect;

    private Tetris tetris, nextTetris;

    private Rect gameOverRect;

    private int gameBackgroundColor = 0xFF9DACA5, backgroundColor = 0xff8B6508;

    private int offBackgoundColor = 0xff889790, onBackgoundColor = 0xff000604;

    private int scoreTextColor = 0xff4A708B, scoreColor = 0xff008B8B;

    private int buttonColor = 0xff2F4F4F, turnBtnColor = 0xff68228B;

    private boolean leftOrRight = false;

    private int lorSize = 0;

    private Random mRandom = new Random();

    private int score = 0, stepScore = 0, baseScore = 10;

    private Vibrator mVibrator;

    public TetrisView(Context context) {
        super(context);
        init();
    }

    public TetrisView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        resetPaint();
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);

        gameRect = new Rect();
        tempRect = new Rect();
        btnLeftRect = new Rect();
        btnTopRect = new Rect();
        btnRightRect = new Rect();
        btnBottomRect = new Rect();
        btnTurnRect = new Rect();
        scoreRect = new Rect();
        nextTetrisRect = new Rect();

        saveDrawArray = new boolean[T_HEIGHT_PIX][T_WIDTH_PIX];
        pixRectArray = new Rect[T_HEIGHT_PIX][T_WIDTH_PIX];
        nextRectArray = new Rect[4][4];

        mGestureDetector = new GestureDetector(getContext(), this);
        mVibrator = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isDrawing = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isDrawing = false;
    }

    @Override
    public void run() {
        long gameStartTime = System.currentTimeMillis();
        while (isDrawing) {
            long start = System.currentTimeMillis();
            changeTetris();
            int temp = (int) ((start - gameStartTime) / currentStepTime);
            if (stepCount != temp) {
                stepCount = temp;
                createOrMoveTetris();
            }

            try {
                drawTetris();
                long end = System.currentTimeMillis();
                if (end - start < refreshTime) {
                    Thread.sleep(refreshTime - (end - start));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (isGameOver) {
                break;
            }
        }
    }

    private void drawGameOver() {
        gameOverRect = new Rect(gameRect.left, gameRect.top + gameHeight / 2 - gameWidth / 4,
                gameRect.right, gameRect.bottom - gameWidth / 2 - gameWidth / 4);
        resetPaint();

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0x33ff0000);
        mCanvas.drawRect(gameOverRect, mPaint);
        mPaint.setColor(0xffffffff);
        mPaint.setTextSize((gameOverRect.bottom - gameOverRect.top) / 4);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mCanvas.drawText("GAME", gameOverRect.left + (gameOverRect.right - gameOverRect.left) / 2,
                gameOverRect.top + (gameOverRect.bottom - gameOverRect.top) * 1 / 4, mPaint);
        mCanvas.drawText("OVER", gameOverRect.left + (gameOverRect.right - gameOverRect.left) / 2,
                gameOverRect.top + (gameOverRect.bottom - gameOverRect.top) * 2 / 4, mPaint);

        mPaint.setTextSize((gameOverRect.bottom - gameOverRect.top) / 5);
        mPaint.setColor(0xFF3A5FCD);
        mCanvas.drawText("Replay", gameOverRect.left + (gameOverRect.right - gameOverRect.left) / 2,
                gameOverRect.top + (gameOverRect.bottom - gameOverRect.top) * 4 / 5, mPaint);
    }

    private void changeTetris() {
        if (leftOrRight) {
            if (tetris != null) {
                if (lorSize == 0) {
                    tetris.turn(saveDrawArray);
                } else {
                    tetris.leftOrRight(saveDrawArray, lorSize);
                }
            }
            leftOrRight = false;
            lorSize = 0;
        }
    }

    private void createOrMoveTetris() {
        if (tetris == null) {
            tetris = createTetris();
        }
        if (nextTetris == null) {
            nextTetris = createTetris();
        }
        if (!tetris.moveDown(saveDrawArray)) {
            if (tetris.isGameOver()) {
                isGameOver = true;
                return;
            }
            stepScore = 0;
            checkScore();
            tetris = nextTetris;
            nextTetris = createTetris();
        }
    }

    private Tetris createTetris() {
        Tetris tetris = new Tetris(T_WIDTH_PIX, T_HEIGHT_PIX);
        int turnTime = mRandom.nextInt(4);
        for (int i = 0; i < turnTime; i++) {
            tetris.turn(saveDrawArray);
        }
        return tetris;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (height > width) {
            gameWidth = width * 5 / 10;
        } else {
            gameWidth = height / 2;
        }

        pixSize = gameWidth / T_WIDTH_PIX;
        gameWidth = pixSize * T_WIDTH_PIX;
        gameHeight = pixSize * T_HEIGHT_PIX;
        gameRect.set((width - gameWidth) / 2, 0, (width - gameWidth) / 2 + gameWidth, gameHeight);
        for (int i = 0; i < T_HEIGHT_PIX; i++) {
            for (int j = 0; j < T_WIDTH_PIX; j++) {
                int l = gameRect.left + j * pixSize;
                int t = gameRect.top + i * pixSize;
                int r = l + pixSize;
                int b = t + pixSize;
                pixRectArray[i][j] = new Rect(l, t, r, b);
            }
        }

        if (height > width) {
            int btnHeight = height - gameHeight;
            int btnCentX = width / 4;
            int btnRadius = btnHeight / 9;
            int btnCentY = gameHeight + btnHeight / 2;
            btnTopRect.set(btnCentX - btnRadius, btnCentY - btnRadius, btnCentX + btnRadius, btnCentY + btnRadius);
            btnTopRect.offset(0, -btnRadius * 2);
            btnLeftRect.set(btnCentX - btnRadius, btnCentY - btnRadius, btnCentX + btnRadius, btnCentY + btnRadius);
            btnLeftRect.offset(-btnRadius * 2, 0);
            btnRightRect.set(btnCentX - btnRadius, btnCentY - btnRadius, btnCentX + btnRadius, btnCentY + btnRadius);
            btnRightRect.offset(btnRadius * 2, 0);
            btnBottomRect.set(btnCentX - btnRadius, btnCentY - btnRadius, btnCentX + btnRadius, btnCentY + btnRadius);
            btnBottomRect.offset(0, btnRadius * 2);
            btnTurnRect.set(btnCentX - btnRadius, btnCentY - btnRadius, btnCentX + btnRadius, btnCentY + btnRadius);
            btnTurnRect.offset(width / 2, 0);

            int offsetX = width / 20;
            btnTopRect.offset(offsetX, 0);
            btnLeftRect.offset(offsetX, 0);
            btnRightRect.offset(offsetX, 0);
            btnBottomRect.offset(offsetX, 0);
            btnTurnRect.offset(offsetX, 0);

            int tagCenterX = width - (width - gameWidth) / 4;
            int tagCenterY = gameHeight / 2;
            scoreRect.set(tagCenterX - (width - gameWidth) / 5, tagCenterY - (width - gameWidth) / 5,
                    tagCenterX + (width - gameWidth) / 5, tagCenterY + (width - gameWidth) / 5);
            tagCenterX = (width - gameWidth) / 4;
            nextTetrisRect.set(tagCenterX - (width - gameWidth) / 6, tagCenterY - (width - gameWidth) / 6,
                    tagCenterX + (width - gameWidth) / 6, tagCenterY + (width - gameWidth) / 6);
            for (int i = 0; i < nextRectArray.length; i++) {
                for (int j = 0; j < nextRectArray[i].length; j++) {
                    nextRectArray[i][j] = new Rect(nextTetrisRect.left + j * (nextTetrisRect.width() / 4),
                            nextTetrisRect.top + i * (nextTetrisRect.height() / 4),
                            nextTetrisRect.left + (j + 1) * (nextTetrisRect.width() / 4),
                            nextTetrisRect.top + (i + 1) * (nextTetrisRect.height() / 4)
                    );
                }
            }
        }
    }

    private void drawTetris() {
        mCanvas = mSurfaceHolder.lockCanvas();
        drawBackground();
        drawPix();
        drawCurrentTetris();
        if (isGameOver) {
            drawGameOver();
        }
        drawButton();
        drawScore();
        drawNextTetris();
        drawMark();
        mSurfaceHolder.unlockCanvasAndPost(mCanvas);
    }

    private void drawMark() {
        resetPaint();
        mPaint.setTextSize(30);
        mPaint.setColor(0xff545941);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mCanvas.drawText("作者:卢伟斌  QQ:2200213300", getWidth() / 2, getHeight() - 5, mPaint);
    }

    private void drawNextTetris() {
        resetPaint();
        mPaint.setColor(gameBackgroundColor);
        mCanvas.drawRect(nextTetrisRect, mPaint);
        if (nextTetris != null) {
            for (int i = 0; i < nextRectArray.length; i++) {
                for (int j = 0; j < nextRectArray[i].length; j++) {
                    if (nextTetris.getTetrisPixs()[i][j].display) {
                        drawPixWithVisible(nextRectArray[i][j], true);
                    } else {
                        drawPixWithVisible(nextRectArray[i][j], false);
                    }
                }
            }
        }
    }

    private void drawScore() {
        resetPaint();
        mPaint.setColor(gameBackgroundColor);
        mCanvas.drawRect(scoreRect, mPaint);
        mPaint.setColor(scoreTextColor);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(scoreRect.height() / 3);
        mCanvas.drawText("score", scoreRect.centerX(), scoreRect.centerY() - scoreRect.height() / 8, mPaint);
        mPaint.setTextSize(scoreRect.width() / 4);
        mPaint.setColor(scoreColor);
        mCanvas.drawText(String.valueOf(score), scoreRect.centerX(), scoreRect.centerY() + scoreRect.height() / 4, mPaint);
    }

    private void drawButton() {
        resetPaint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(buttonColor);
        if (longPressRect == btnTopRect) {
            mPaint.setAlpha((int) (255 * 0.5f));
        } else {
            mPaint.setAlpha(255);
        }
        mCanvas.drawCircle(btnTopRect.centerX(), btnTopRect.centerY(), btnTopRect.height() / 2, mPaint);
        if (longPressRect == btnLeftRect) {
            mPaint.setAlpha((int) (255 * 0.5f));
        } else {
            mPaint.setAlpha(255);
        }
        mCanvas.drawCircle(btnLeftRect.centerX(), btnLeftRect.centerY(), btnLeftRect.height() / 2, mPaint);
        if (longPressRect == btnRightRect) {
            mPaint.setAlpha((int) (255 * 0.5f));
        } else {
            mPaint.setAlpha(255);
        }
        mCanvas.drawCircle(btnRightRect.centerX(), btnRightRect.centerY(), btnRightRect.height() / 2, mPaint);
        if (longPressRect == btnBottomRect) {
            mPaint.setAlpha((int) (255 * 0.5f));
        } else {
            mPaint.setAlpha(255);
        }
        mCanvas.drawCircle(btnBottomRect.centerX(), btnBottomRect.centerY(), btnBottomRect.height() / 2, mPaint);
        mPaint.setColor(turnBtnColor);
        if (longPressRect == btnTurnRect) {
            mPaint.setAlpha((int) (255 * 0.5f));
        } else {
            mPaint.setAlpha(255);
        }
        mCanvas.drawCircle(btnTurnRect.centerX(), btnTurnRect.centerY(), btnTurnRect.height(), mPaint);
    }

    private void drawBackground() {
        resetPaint();
        mCanvas.drawColor(backgroundColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(gameBackgroundColor);
        mCanvas.drawRect(gameRect, mPaint);

    }

    private void drawPix() {
        resetPaint();
        mPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < T_HEIGHT_PIX; i++) {
            for (int j = 0; j < T_WIDTH_PIX; j++) {
                drawPixWithVisible(pixRectArray[i][j], saveDrawArray[i][j]);
            }
        }
    }

    private void drawCurrentTetris() {
        if (tetris != null) {
            for (Point point : tetris.getDisplayPoint()) {
                drawPixWithVisible(pixRectArray[point.x][point.y], true);
            }
        }
    }

    private void drawPixWithVisible(Rect pixRect, boolean visible) {
        int padding = pixSize / 25;
        tempRect.set(pixRect.left + padding, pixRect.top + padding, pixRect.right - padding, pixRect.bottom - padding);
        if (visible) {
            mPaint.setColor(onBackgoundColor);
        } else {
            mPaint.setColor(offBackgoundColor);
        }
        mPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawRect(tempRect, mPaint);

        mPaint.setStyle(Paint.Style.STROKE);
        padding = pixSize / 5;
        tempRect.set(pixRect.left + padding, pixRect.top + padding, pixRect.right - padding, pixRect.bottom - padding);
        mPaint.setColor(gameBackgroundColor);
        mPaint.setStrokeWidth(pixSize / 15);
        mCanvas.drawRect(tempRect, mPaint);
    }

    private void checkScore() {
        for (int i = T_HEIGHT_PIX - 1; i >= 0; i--) {
            boolean success = true;
            for (int j = 0; j < T_WIDTH_PIX; j++) {
                success = success && saveDrawArray[i][j];
            }
            if (success) {
                score += baseScore + stepScore;
                stepScore = baseScore;
                updateStepTime();
                for (int r = i - 1; r >= -1; r--) {
                    if (r > 0) {
                        saveDrawArray[r + 1] = saveDrawArray[r];
                    } else {
                        saveDrawArray[r + 1] = new boolean[T_WIDTH_PIX];
                    }
                }
                checkScore();
                break;
            }
        }
    }

    private void updateStepTime() {
        stepTime = DEFAULT_STEP_TIME - (DEFAULT_STEP_TIME / 10) * (score / 100);
        baseScore = 10 + 5 * (score / 100);
        if (stepTime < DEFAULT_STEP_TIME / 10) {
            stepTime = DEFAULT_STEP_TIME / 10;
        }
    }

    public void resetPaint() {
        if (mPaint == null) {
            mPaint = new Paint();
        }
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                longPressRect = null;
                currentStepTime = stepTime;
                break;
            default:
                break;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        if (btnLeftRect.contains(x, y)) {
            longPressRect = btnLeftRect;
            onLeft();
        }
        if (btnTopRect.contains(x, y)) {
            longPressRect = btnTopRect;
            onTop();
        }
        if (btnRightRect.contains(x, y)) {
            longPressRect = btnRightRect;
            onRight();
        }
        if (btnBottomRect.contains(x, y)) {
            longPressRect = btnBottomRect;
            onBottom();
        }
        if (btnTurnRect.contains(x, y)) {
            longPressRect = btnTurnRect;
            onTurn();
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (gameOverRect != null && gameOverRect.contains((int) e.getX(), (int) e.getY())) {
            onReplay();
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (longPressRect == btnLeftRect) {
            onLeft();
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (longPressRect == btnLeftRect) {
                    onLeft();
                }
                if (longPressRect == btnRightRect) {
                    onRight();
                }
                if (longPressRect == btnTopRect) {
                    onTop();
                }
                if (longPressRect == btnBottomRect) {
                    onBottom();
                }
                if (longPressRect == btnTurnRect) {
                    onTurn();
                }
                if (longPressRect != null) {
                    postDelayed(this, 200);
                }
            }
        }, 200);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    private void onReplay() {
        gameOverRect = null;
        isGameOver = false;
        score = 0;
        baseScore = 10;
        nextTetris = null;
        currentStepTime = stepTime = DEFAULT_STEP_TIME;
        saveDrawArray = new boolean[T_HEIGHT_PIX][T_WIDTH_PIX];
        new Thread(this).start();
    }

    private void onLeft() {
        vibrator();
        leftOrRight = true;
        lorSize = -1;
    }

    private void onTop() {
        vibrator();
    }

    private void onRight() {
        vibrator();
        leftOrRight = true;
        lorSize = 1;
    }

    private void onBottom() {
        vibrator();
        currentStepTime = 30;
    }

    private void onTurn() {
        vibrator();
        leftOrRight = true;
        lorSize = 0;
    }

    private void vibrator() {
        mVibrator.vibrate(new long[]{0, 50}, -1);
    }
}

