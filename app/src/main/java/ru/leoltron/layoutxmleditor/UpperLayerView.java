package ru.leoltron.layoutxmleditor;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Выделение используемых в данный момент View
 */
public class UpperLayerView extends SurfaceView implements SurfaceHolder.Callback {

    private DrawThread drawThread;

    /**
     * Здесь хранится выделямый на экране View
     */
    private View fictionSelectedView;
    private View selectedView;

    public View getFictionSelectedView() {
        return fictionSelectedView;
    }

    public void setFictionSelectedView(View fictionSelectedView) {
        this.fictionSelectedView = fictionSelectedView;
    }

    public View getSelectedView(){
        return selectedView;
    }

    public void setSelectedView(View v){
        this.selectedView = v;
    }

    public void setSelectedAndFictionView(View v){
        setSelectedView(v);
        setFictionSelectedView(v);
    }

    public UpperLayerView(Context context) {
        super(context);
        init();
    }

    public UpperLayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UpperLayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(getHolder(),this);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

//    public void onPause(){
//        if(drawThread != null)
//            drawThread.running = false;
//    }
//
//    public void onResume(){
//        if(drawThread != null)
//            drawThread.running = true;
//    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        drawThread.requestStop();
        boolean retry = true;
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class DrawThread extends Thread {

        private SurfaceHolder surfaceHolder;
        private Paint paint;
        private UpperLayerView upv;

        private volatile boolean running = true;

        public DrawThread(SurfaceHolder surfaceHolder,UpperLayerView upv) {
            this.surfaceHolder = surfaceHolder;
            paint = new Paint();
            this.upv = upv;
        }

        public void requestStop() {
            running = false;
        }

        public int getLeft(View view){
            int left = view.getLeft();
            if(view.getParent() != null && view.getParent() instanceof View)
                left += ((View)view.getParent()).getLeft();
            return left;
        }

        public int getTop(View view){
            int top = view.getTop();
            if(view.getParent() != null && view.getParent() instanceof View)
                top += ((View)view.getParent()).getTop();
            return top;
        }

        public int getRight(View view){
            int right = view.getRight();
            if(view.getParent() != null && view.getParent() instanceof View)
                right += ((View)view.getParent()).getLeft();
            return right;
        }

        public int getBottom(View view){
            int bottom = view.getBottom();
            if(view.getParent() != null && view.getParent() instanceof View)
                bottom += ((View)view.getParent()).getTop();
            return bottom;
        }

        @Override
        public void run() {
            while (running) {
                Canvas canvas = surfaceHolder.lockCanvas();
                if(canvas != null) {
                    try {
                        paint.setColor(Color.WHITE);
                        canvas.drawPaint(paint);
                        if (upv.getFictionSelectedView() != null) {
                            paint.setColor(MainActivity.instance.getResources().getColor(MainActivity.mode == MainActivity.Mode.NORMAL ? R.color.colorAccent : R.color.colorPrimaryDark));
                            paint.setStyle(Paint.Style.STROKE);
                            final int s = 2;
                            paint.setStrokeWidth(s*2);
                            canvas.drawRect(getLeft(upv.getFictionSelectedView())+s, getTop(upv.getFictionSelectedView())+s, getRight(upv.getFictionSelectedView())-s, getBottom(upv.getFictionSelectedView())-s, paint);
                            paint.setStyle(Paint.Style.FILL);
                            paint.setAlpha(125);
                            canvas.drawRect(getLeft(upv.getFictionSelectedView()), getTop(upv.getFictionSelectedView()), getRight(upv.getFictionSelectedView()), getBottom(upv.getFictionSelectedView()), paint);
                        }
                    }catch(Exception e){
                        Log.e(MainActivity.LOG_TAG,e.getMessage());
                    } finally {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}
