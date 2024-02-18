package com.Z.NovelReader.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;

import com.Z.NovelReader.R;

public class SwitchRopeView extends View {
    private static final float MAX_ROPE_SWITCH = 200;

    //主体绳框
    private float mRopeLeft;
    private float mRopeWidth;
    private float ropeHeight;
    private int mRopeColor;
    //斜线的属性(默认45度)
    private float mPieceAngel = 45.0f;//与水平方向的夹角
    private float mPieceHeight;//垂直方向间隔，由角度计算
    private float mPieceCount = 0;
    private int mPieceColor = Color.BLACK;
    //圆圈的属性
    private float mRingRadius;
    private float mRingStrokeWidth;
    private int mRingColor = Color.BLACK;
    //
    private float inline_padding;
    private PointF lastPos;
    private float ropeHeightHolder;
    private float deltaMove;
    private ObjectAnimator animator;
    private onSwitchListener listener;

    //绘图
    RectF rect_main_rope;
    Paint paint_main_rope;
    Paint paint_piece;
    Paint paint_ring;

    public SwitchRopeView(Context context) {
        this(context,null);
    }

    public SwitchRopeView(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public SwitchRopeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        lastPos = new PointF();
        animator = new ObjectAnimator();
        //设置动画属性
        animator.setPropertyName("ropeHeight");
        //设置执行动画的View
        animator.setTarget(this);

        TypedArray att_list = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SwitchRopeView, defStyleAttr, 0);
        mRopeWidth = att_list.getDimensionPixelSize(R.styleable.SwitchRopeView_rope_width, getDefaultValueInPixel(3));
        ropeHeight = att_list.getDimensionPixelSize(R.styleable.SwitchRopeView_rope_height, getDefaultValueInPixel(150));
        mRingRadius = att_list.getDimensionPixelSize(R.styleable.SwitchRopeView_ring_radius, getDefaultValueInPixel(8));
        mRingStrokeWidth = att_list.getDimensionPixelSize(R.styleable.SwitchRopeView_ring_stroke_width, getDefaultValueInPixel(3));;
        inline_padding = att_list.getDimensionPixelSize(R.styleable.SwitchRopeView_rope_width, getDefaultValueInPixel(2));;
        mRopeColor = att_list.getColor(R.styleable.SwitchRopeView_line_color,Color.BLACK);
        mPieceColor = att_list.getColor(R.styleable.SwitchRopeView_line_color,Color.BLACK);
        mRingColor = att_list.getColor(R.styleable.SwitchRopeView_ring_color,Color.BLACK);

        ropeHeightHolder = ropeHeight;

        mRopeLeft = mRingRadius+inline_padding-mRopeWidth/2;
        rect_main_rope = new RectF(mRopeLeft,0,mRopeLeft+mRopeWidth,0+ ropeHeight);
        paint_main_rope = new Paint();
        paint_main_rope.setAntiAlias(true);//抗锯齿
        paint_main_rope.setColor(mRopeColor);
        paint_main_rope.setStyle(Paint.Style.STROKE);
        paint_main_rope.setStrokeWidth(1);

        mPieceHeight = (float) (Math.tan(mPieceAngel*Math.PI/180) * mRopeWidth);
        //mPieceCount = mRopeHeight/mPieceHeight-1;
        paint_piece = new Paint();
        paint_piece.setAntiAlias(true);//抗锯齿
        paint_piece.setColor(mPieceColor);
        paint_piece.setStyle(Paint.Style.STROKE);
        paint_piece.setStrokeWidth(1);

        paint_ring = new Paint();
        paint_ring.setAntiAlias(true);//抗锯齿
        paint_ring.setColor(mRingColor);
        paint_ring.setStyle(Paint.Style.STROKE);
        paint_ring.setStrokeWidth(mRingStrokeWidth);
    }

    private int getDefaultValueInPixel(int dip) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics());
    }

    public void setRopeHeight(float ropeHeight) {
        this.ropeHeight = ropeHeight;
        invalidate();
    }

    public void setLineColor(int color){
        this.mRopeColor = color;
        this.mPieceColor = color;
        paint_main_rope.setColor(mRopeColor);
        paint_piece.setColor(mPieceColor);
        invalidate();
    }

    public void setRingColor(int color){
        this.mRingColor = color;
        paint_ring.setColor(mRingColor);
        invalidate();
    }

    public void setListener(onSwitchListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制线
        rect_main_rope.set(mRopeLeft,0,mRopeLeft+mRopeWidth,0+ ropeHeight);
        canvas.drawRect(rect_main_rope,paint_main_rope);
        //绘制斜线
        mPieceCount = ropeHeight /mPieceHeight-1;
        for (int i = 0; i < mPieceCount; i++) {
            canvas.drawLine(mRopeLeft+mRopeWidth,0+i*mPieceHeight,mRopeLeft,(i+1)*mPieceHeight,paint_piece);
        }
        //绘制圆圈和点
        canvas.drawCircle(mRingRadius+inline_padding, ropeHeight +mRingRadius,mRingRadius,paint_ring);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension((int) (2 * (mRingRadius+inline_padding)), (int) (ropeHeight+2*mRingRadius+ MAX_ROPE_SWITCH *2));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://按下
                lastPos.x = event.getX();
                lastPos.y = event.getY();
                deltaMove = 0;
                break;
            case MotionEvent.ACTION_UP://抬起
                if(deltaMove!=0){
                    animator.setFloatValues(ropeHeight,ropeHeightHolder);
                    animator.setInterpolator(new OvershootInterpolator());
                    animator.setDuration(300);
                    animator.start();
                }
                else
                {
                    animator.setFloatValues(ropeHeightHolder,ropeHeightHolder+ MAX_ROPE_SWITCH,ropeHeightHolder);
                    animator.setInterpolator(new OvershootInterpolator());
                    animator.setDuration(400);
                    animator.start();
                }
                if(listener!=null)listener.onSwitch();
                break;
            case MotionEvent.ACTION_MOVE://2
                float x = event.getX();
                float y = event.getY();
                PointF curPos = new PointF(x,y);

                float dataY = curPos.y - lastPos.y;
                if(ropeHeight <ropeHeightHolder+ MAX_ROPE_SWITCH && dataY>0){
                    ropeHeight += dataY;
                    deltaMove += dataY;
                }
                invalidate();
                lastPos = curPos;//更新位置
                break;
        }
        return true;
    }

    public interface onSwitchListener{
        void onSwitch();
    }
}
