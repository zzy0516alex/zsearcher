package com.Z.NovelReader.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.RequiresApi;

public class FlexibleRectDrawable extends Drawable {
    private Paint paint_stroke;
    private Paint paint_fill;
    private Paint paint_ripple;

    private RectF outerRect;
    private RectF innerRect;
    private RectF zeroRect;
    //矩形内部颜色及边框颜色
    private int solidColor;
    private int strokeColor;
    private int solidColorHolder;
    private int strokeColorHolder;
    //边框设置
    private boolean hasStroke;
    private float strokeWidth;
    private float strokeWidthVariable;//可变动的边框宽度，用于生成后调整宽度
    private float strokeInPercent;//0~1 边框与整体大小的占比
    //圆角半径
    private float rectRadius;
    //圆角位置
    private int corners;
    //阴影设置
    private boolean needShadow;
    private float shadowRange;//阴影粗细
    private float shadowDx;//阴影中心x轴偏移
    private float shadowDy;//阴影中心y轴偏移
    private int shadowColor;//阴影颜色
    //阴影位置
    private float offsetLeft;
    private float offsetTop;
    private float offsetRight;
    private float offsetBottom;
    //Ripple Effect
    enum RippleAnimState{STATE_ENTER, STATE_EXIT, STATE_EXIT_LATER}
    private RippleAnimState ripple_anim_state;
    private ObjectAnimator ripple_alpha_animator;
    private ObjectAnimator ripple_radius_animator;
    private PointF currentPoint;
    private PointF pressedPoint;
    private Path ripple_bound_path;
    private boolean needRipple;
    private int rippleSpeed;//ms
    private float maxRippleRadius;
    private float rippleRadius;//属性动画
    private int rippleColor;
    private int maxRippleAlpha;
    private int rippleAlpha;//属性动画

    public static final int SQUARE_CORNER = 0;
    public static final int CORNER_TOP_LEFT = 1;
    public static final int CORNER_TOP_RIGHT = 1 << 1;
    public static final int CORNER_BOTTOM_LEFT = 1 << 2;
    public static final int CORNER_BOTTOM_RIGHT = 1 << 3;
    public static final int CORNER_HALF_LEFT = CORNER_TOP_LEFT | CORNER_BOTTOM_LEFT;
    public static final int CORNER_HALF_RIGHT = CORNER_TOP_RIGHT | CORNER_BOTTOM_RIGHT;
    public static final int CORNER_ALL = CORNER_TOP_LEFT | CORNER_TOP_RIGHT | CORNER_BOTTOM_LEFT | CORNER_BOTTOM_RIGHT;

    public enum RectType{BORDER_ONLY,SOLID_BLOCK,BORDERED_BLOCK,NOT_DEFINED}

    private RectType type;

    public FlexibleRectDrawable() {
        //默认值
        this.outerRect = new RectF();
        this.innerRect = new RectF();
        this.zeroRect = new RectF();
        this.type = RectType.NOT_DEFINED;
        this.solidColor = 0;//透明色
        this.strokeColor = 0;//透明色
        this.solidColorHolder = 0;
        this.strokeColorHolder = 0;
        this.hasStroke = false;
        this.strokeWidth = 0;
        this.strokeWidthVariable = 0;
        this.corners = SQUARE_CORNER;
        this.rectRadius = 0;
        this.needShadow = false;
        this.shadowRange = 0;
        this.shadowDx = 0;
        this.shadowDy = 0;
        this.shadowColor = Color.parseColor("#aa000000");
        this.offsetBottom = 0;
        this.offsetLeft = 0;
        this.offsetTop = 0;
        this.offsetRight = 0;

        this.currentPoint = new PointF();
        this.pressedPoint = new PointF();
        this.rippleColor = Color.parseColor("#21000000");
        this.maxRippleAlpha = this.rippleColor>>24 & 0xFF;
        this.ripple_bound_path = new Path();
    }

    public void setupPainters() {
        paint_stroke = new Paint();
        paint_stroke.setAntiAlias(true);
        paint_stroke.setFilterBitmap(true);
        paint_stroke.setDither(true);
        paint_stroke.setStyle(Paint.Style.FILL);
        paint_stroke.setColor(strokeColor);
        //设置阴影
        if(needShadow)
            paint_stroke.setShadowLayer(shadowRange, shadowDx, shadowDy, shadowColor);

        paint_fill = new Paint();
        paint_fill.setAntiAlias(true);
        paint_fill.setFilterBitmap(true);
        paint_fill.setDither(true);
        paint_fill.setStyle(Paint.Style.FILL);
        paint_fill.setColor(solidColor);
        //设置水波纹效果
        paint_ripple = new Paint();
        paint_ripple.setAntiAlias(true);
        paint_ripple.setStyle(Paint.Style.FILL);
        paint_ripple.setColor(rippleColor);

        invalidateSelf();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        if (bounds.right - bounds.left > 0 && bounds.bottom - bounds.top > 0) {

            int width = bounds.right - bounds.left;
            int height = bounds.bottom - bounds.top;

            outerRect = new RectF(offsetLeft, offsetTop, width - offsetRight, height - offsetBottom);
            innerRect = new RectF(offsetLeft + strokeWidth, offsetTop + strokeWidth,
                    width - offsetRight - strokeWidth, height - offsetBottom - strokeWidth);
            zeroRect = new RectF(width/2.0f, height/2.0f, width/2.0f, height/2.0f);
            invalidateSelf();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void draw(Canvas canvas) {
        float[] Radii = {0,0,0,0,0,0,0,0};

        if ((corners & CORNER_TOP_LEFT) != 0) {
            Radii[0] = rectRadius;
            Radii[1] = rectRadius;
        }
        if ((corners & CORNER_TOP_RIGHT) != 0) {
            Radii[2] = rectRadius;
            Radii[3] = rectRadius;
        }
        if ((corners & CORNER_BOTTOM_RIGHT) != 0) {
            Radii[4] = rectRadius;
            Radii[5] = rectRadius;
        }
        if ((corners & CORNER_BOTTOM_LEFT) != 0) {
            Radii[6] = rectRadius;
            Radii[7] = rectRadius;
        }
        switch(type){
            case BORDER_ONLY:
                canvas.drawDoubleRoundRect(outerRect, Radii, innerRect, Radii, paint_stroke);
                break;
            case SOLID_BLOCK:
                canvas.drawDoubleRoundRect(outerRect,Radii,zeroRect,Radii, paint_fill);
                break;
            case BORDERED_BLOCK:
                canvas.drawDoubleRoundRect(outerRect, Radii, innerRect, Radii, paint_stroke);
                canvas.drawDoubleRoundRect(innerRect,Radii,zeroRect,Radii, paint_fill);
                break;
            case NOT_DEFINED:
                throw new RuntimeException("RectType undefined");
            default:
        }
        //draw ripple
        canvas.save();
        ripple_bound_path.addRoundRect(innerRect,Radii,Path.Direction.CW);
        canvas.clipPath(ripple_bound_path);
        if(ripple_anim_state == RippleAnimState.STATE_ENTER){
            paint_ripple.setAlpha(rippleAlpha);
            canvas.drawCircle(pressedPoint.x, pressedPoint.y, rippleRadius, paint_ripple);
        }else if(ripple_anim_state == RippleAnimState.STATE_EXIT){
            paint_ripple.setAlpha(rippleAlpha);
            canvas.drawDoubleRoundRect(innerRect,Radii,zeroRect,Radii, paint_ripple);
        }
        canvas.restore();
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        boolean enable = false;
        boolean pressed = false;
        for (int st : stateSet) {
            switch (st) {
                case android.R.attr.state_pressed:
                    pressed = true;
                    break;
                case android.R.attr.state_enabled:
                    enable = true;
                    break;
            }
        }

        if (!enable) return false;
        if (!needRipple)return false;
        if (pressed) {
            startRippleAnimation();
            return true;
        } else if (ripple_anim_state == RippleAnimState.STATE_ENTER) {
            exitRippleAnimation();
            return true;
        } else {
            return false;
        }
    }

    private void startRippleAnimation() {
        ripple_anim_state = RippleAnimState.STATE_ENTER;
        pressedPoint.set(currentPoint);
        maxRippleRadius = Math.max(innerRect.width(), innerRect.height());
        if(ripple_radius_animator != null && ripple_radius_animator.isRunning()){
            ripple_radius_animator.cancel();
        }
        ripple_radius_animator = new ObjectAnimator();
        ripple_radius_animator.setTarget(this);
        ripple_radius_animator.setPropertyName("rippleRadius");
        ripple_radius_animator.setInterpolator(new LinearInterpolator());
        ripple_radius_animator.setDuration(rippleSpeed);
        ripple_radius_animator.setFloatValues(0,maxRippleRadius);
        ripple_radius_animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(ripple_anim_state == RippleAnimState.STATE_EXIT_LATER){
                    ripple_anim_state = RippleAnimState.STATE_EXIT;
                    exitRippleAnimation();
                }
            }
        });
        ripple_radius_animator.start();
    }

    private void exitRippleAnimation() {
        ripple_alpha_animator = new ObjectAnimator();
        ripple_alpha_animator.setTarget(this);
        ripple_alpha_animator.setPropertyName("rippleAlpha");
        ripple_alpha_animator.setInterpolator(new LinearInterpolator());
        ripple_alpha_animator.setDuration(300);
        ripple_alpha_animator.setIntValues(maxRippleAlpha,0);
        ripple_alpha_animator.start();
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    public void setHotspot(float x, float y) {
        currentPoint.set(x,y);
    }

    public float getOffsetLeft() {
        return offsetLeft;
    }

    public void setOffsetLeft(float offsetLeft) {
        this.offsetLeft = offsetLeft;
    }

    public float getOffsetTop() {
        return offsetTop;
    }

    public void setOffsetTop(float offsetTop) {
        this.offsetTop = offsetTop;
    }

    public float getOffsetRight() {
        return offsetRight;
    }

    public void setOffsetRight(float offsetRight) {
        this.offsetRight = offsetRight;
    }

    public float getOffsetBottom() {
        return offsetBottom;
    }

    public void setOffsetBottom(float offsetBottom) {
        this.offsetBottom = offsetBottom;
    }

    public float getRectRadius() {
        return rectRadius;
    }

    public void setRectRadius(float rectRadius) {
        this.rectRadius = rectRadius;
    }

    public void setCorners(int corners) {
        this.corners = corners;
    }

    public FlexibleRectDrawable setColor(int color) {
        paint_stroke.setColor(color);
        return this;
    }

    public int getSolidColor() {
        return solidColor;
    }

    public void setSolidColor(int solidColor) {
        this.solidColor = solidColor;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public boolean isHasStroke() {
        return hasStroke;
    }

    public void setHasStroke(boolean hasStroke) {
        this.hasStroke = hasStroke;
    }

    public boolean isNeedShadow() {
        return needShadow;
    }

    public void setNeedShadow(boolean needShadow) {
        this.needShadow = needShadow;
    }

    public float getShadowRange() {
        return shadowRange;
    }

    public void setShadowRange(float shadowRange) {
        this.shadowRange = shadowRange;
    }

    public float getShadowDx() {
        return shadowDx;
    }

    public void setShadowDx(float shadowDx) {
        this.shadowDx = shadowDx;
    }

    public float getShadowDy() {
        return shadowDy;
    }

    public void setShadowDy(float shadowDy) {
        this.shadowDy = shadowDy;
    }

    public int getShadowColor() {
        return shadowColor;
    }

    public void setShadowColor(int shadowColor) {
        this.shadowColor = shadowColor;
    }

    float getStrokeWidth() {
        return strokeWidth;
    }

    void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        this.strokeWidthVariable = strokeWidth;
    }

    public float getStrokeWidthVariable() {
        if(type== RectType.SOLID_BLOCK)
            return Math.min((getBounds().width() - offsetRight),(getBounds().height() - offsetBottom));
        return strokeWidthVariable;
    }

    public void setStrokeWidthVariable(float strokeWidthVariable) {
        this.strokeWidthVariable = strokeWidthVariable;
        int width = getBounds().width();
        int height = getBounds().height();
        //System.out.println("type= " + type.name() + " stroke = " + strokeWidthVariable+" width = "+width+" height = "+height);
        if((width - offsetRight)<=strokeWidthVariable||
            (height - offsetBottom)<=strokeWidthVariable){
            //边框宽大到可以认为是纯色块
            if(type == RectType.BORDERED_BLOCK && this.solidColor!=0){
                this.solidColorHolder = this.solidColor;
                this.solidColor = this.strokeColor;
            }
            //若内部无色，则用边框颜色作为填充
            if(type == RectType.BORDER_ONLY && this.strokeColor!=0) {
                this.solidColor = this.strokeColor;
            }
            type = RectType.SOLID_BLOCK;
        }
        else{
            if(type== RectType.SOLID_BLOCK){
                this.strokeColor = this.solidColor;
                if(this.solidColorHolder == 0){
                    type = RectType.BORDER_ONLY;//纯色块转变为仅带边框的块
                }
                else {
                    this.solidColor = this.solidColorHolder;
                    type = RectType.BORDERED_BLOCK;
                }
            }
            innerRect.set(offsetLeft + strokeWidthVariable, offsetTop + strokeWidthVariable,
                    width - offsetRight - strokeWidthVariable,
                    height - offsetBottom - strokeWidthVariable);
        }
        setupPainters();//重设画笔，并重绘
    }

    public void setStrokeInPercent(@FloatRange(from= 0.0f,to= 1.0f) float strokeInPercent) {
        if(strokeColor==0)throw new IllegalArgumentException("setStrokeInPercent函数仅适用于带边框的Drawable");
        this.strokeInPercent = strokeInPercent;
        float delta_width = outerRect.width()*strokeInPercent/2;
        float delta_height = outerRect.height()*strokeInPercent/2;
        System.out.println("delta_height= " + delta_height + " delta_width= " + delta_width);
        innerRect.set(outerRect.left+delta_width, outerRect.top+delta_height,
                outerRect.right-delta_width,outerRect.bottom-delta_height);
        if(this.strokeInPercent>0 && this.strokeInPercent<1){
            //带边框的块
            if(this.solidColor==0 && this.solidColorHolder == 0)
                this.type = RectType.BORDER_ONLY;//转变为仅带边框的块
            else{
                if(this.solidColorHolder!=0)
                    this.solidColor = this.solidColorHolder;
                this.type = RectType.BORDERED_BLOCK;
            }
        }
        else if(this.strokeInPercent == 0){
            //内部填充的纯色块
            this.type = RectType.SOLID_BLOCK;
            if(solidColor == 0)Log.e("FlexibleRectDrawable","Drawable被绘制为透明色");
        }
        else if(this.strokeInPercent == 1){
            //边框填充的纯色块
            this.type = RectType.SOLID_BLOCK;
            if(this.solidColorHolder==0)
                this.solidColorHolder = this.solidColor;
            this.solidColor = this.strokeColor;
        }
        setupPainters();
    }

    public float getStrokeInPercent() {
        float inner_width = innerRect.width();
        float outer_width = outerRect.width();
        return (1-inner_width/outer_width);
    }

    public RectType getType() {
        return type;
    }

    public void setType(RectType type) {
        this.type = type;
    }

    public float getRippleRadius() {
        return rippleRadius;
    }

    public void setRippleRadius(float rippleRadius) {
        this.rippleRadius = rippleRadius;
        invalidateSelf();
    }

    public int getRippleColor() {
        return rippleColor;
    }

    public void setRippleColor(int rippleColor) {
        this.rippleColor = rippleColor;
    }

    public int getMaxRippleAlpha() {
        return maxRippleAlpha;
    }

    public void setMaxRippleAlpha(int maxRippleAlpha) {
        this.maxRippleAlpha = maxRippleAlpha;
    }

    public int getRippleAlpha() {
        return rippleAlpha;
    }

    public void setRippleAlpha(int rippleAlpha) {
        this.rippleAlpha = rippleAlpha;
        invalidateSelf();
    }

    public boolean isNeedRipple() {
        return needRipple;
    }

    public void setNeedRipple(boolean needRipple) {
        this.needRipple = needRipple;
    }

    public float getMaxRippleRadius() {
        return maxRippleRadius;
    }

    public void setMaxRippleRadius(float maxRippleRadius) {
        this.maxRippleRadius = maxRippleRadius;
    }

    public int getRippleSpeed() {
        return rippleSpeed;
    }

    public void setRippleSpeed(int rippleSpeed) {
        this.rippleSpeed = rippleSpeed;
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public static class Builder{
        private FlexibleRectDrawable drawable;

        public Builder() {
            this.drawable = new FlexibleRectDrawable();
        }

        public static Builder create(){
            return new Builder();
        }

        public Builder setSolidFill(@ColorInt int color){
            this.drawable.setSolidColor(color);
            switch(this.drawable.getType()){
                case BORDER_ONLY:
                    this.drawable.setType(RectType.BORDERED_BLOCK);
                    break;
                case SOLID_BLOCK:case BORDERED_BLOCK:
                    Log.i("DrawableBuilder","cover solid color");
                    break;
                case NOT_DEFINED:
                    this.drawable.setType(RectType.SOLID_BLOCK);
                    break;
                default:
            }
            return this;
        }

        public Builder setStroke(float width,@ColorInt int color){
            this.drawable.setHasStroke(true);
            this.drawable.setStrokeColor(color);
            this.drawable.setStrokeWidth(width);
            switch(this.drawable.getType()){
                case BORDER_ONLY:case BORDERED_BLOCK:
                    Log.i("DrawableBuilder","cover solid color");
                    break;
                case SOLID_BLOCK:
                    this.drawable.setType(RectType.BORDERED_BLOCK);
                    break;
                case NOT_DEFINED:
                    this.drawable.setType(RectType.BORDER_ONLY);
                    break;
                default:
            }
            return this;
        }

        public Builder setShadow(float shadowRange,@ColorInt int color){
            this.drawable.setNeedShadow(true);
            this.drawable.setShadowRange(shadowRange);
            this.drawable.setShadowColor(color);
            return this;
        }

        public Builder setShadowOffset(float top, float bottom, float left, float right){
            if(!this.drawable.isNeedShadow())throw new IllegalArgumentException("必须先调用setShadow,再设置阴影位置");
            this.drawable.setOffsetTop(top);
            this.drawable.setOffsetBottom(bottom);
            this.drawable.setOffsetLeft(left);
            this.drawable.setOffsetRight(right);
            return this;
        }

        public Builder setShadowOffsetCenter(float offset){
            if(!this.drawable.isNeedShadow())throw new IllegalArgumentException("必须先调用setShadow,再设置阴影位置");
            this.drawable.setOffsetTop(offset);
            this.drawable.setOffsetBottom(offset);
            this.drawable.setOffsetLeft(offset);
            this.drawable.setOffsetRight(offset);
            return this;
        }

        public Builder setCorners(int radius,int corner_type){
            this.drawable.setRectRadius(radius);
            this.drawable.setCorners(corner_type);
            return this;
        }

        public Builder setRipple(int color, int speed_millisecond){
            this.drawable.setNeedRipple(true);
            int check = color >>24;
            if (check==-1)throw new IllegalArgumentException("ripple颜色必须具有透明色");
            this.drawable.setRippleColor(color);
            this.drawable.setMaxRippleAlpha(color>>24 & 0xFF);
            this.drawable.setRippleSpeed(speed_millisecond);
            return this;
        }

        public FlexibleRectDrawable build(){
            this.drawable.setupPainters();
            return this.drawable;
        }
    }
}
