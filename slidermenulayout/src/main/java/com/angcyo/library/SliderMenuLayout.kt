package com.angcyo.library

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：模仿QQ侧滑菜单布局
 * 创建人员：Robi
 * 创建时间：2018/04/03 15:19
 * 修改人员：Robi
 * 修改时间：2018/04/03 15:19
 * 修改备注：
 * Version: 1.0.0
 */
class SliderMenuLayout(context: Context, attributeSet: AttributeSet? = null)
    : TouchLayout(context, attributeSet) {

    private var menuMaxWidthRatio = 0.8f

    /**回调接口*/
    var sliderCallback: SliderCallback? = null

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.SliderMenuLayout)
        menuMaxWidthRatio = typedArray.getFloat(R.styleable.SliderMenuLayout_r_menu_max_width, menuMaxWidthRatio)
        typedArray.recycle()

        setWillNotDraw(false)
    }

    private var needInterceptTouchEvent = false
    private var isTouchDown = false
    private var isTouchDownInContentWithMenuOpen = false //菜单打开的状态下, 点击在内容区域
    var isOldMenuOpen = false //事件触发之前,菜单的打开状态

    /*是否激活滑动菜单*/
    private fun canSlider(): Boolean {
        if (sliderCallback == null) {
            return true
        }
        return sliderCallback!!.canSlider()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val intercept = needInterceptTouchEvent
        super.onInterceptTouchEvent(ev)
        return if (canSlider()) {
            intercept
        } else {
            false
        }
    }

    override fun handleCommonTouchEvent(event: MotionEvent) {
        super.handleCommonTouchEvent(event)
        if (canSlider()) {

        } else {
            return
        }

        if (needInterceptTouchEvent) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        if (event.isDown()) {
            isTouchDown = true
            isTouchDownInContentWithMenuOpen = false
            touchDownX = event.x
            touchDownY = event.y

            isOldMenuOpen = isMenuOpen()
            overScroller.abortAnimation()

            if (isOldMenuOpen) {
                //打开已经打开
                if (event.x >= maxMenuWidth) {
                    //点击在内容区域
                    isTouchDownInContentWithMenuOpen = true
                    needInterceptTouchEvent = true
                }
            } else {
                if (contentLayoutLeft in 1..(maxMenuWidth - 1)) {
                    //当菜单滑动到一半, 突然被终止, 又再次点击时
                    needInterceptTouchEvent = true
                }
            }
        } else if (event.isFinish()) {
            isTouchDown = false
            parent.requestDisallowInterceptTouchEvent(false)

            if (needInterceptTouchEvent) {
                if (isTouchDownInContentWithMenuOpen &&
                        ((touchEventX - touchDownX) == 0f) ||
                        (touchEventY - touchDownY).abs() > (touchEventX - touchDownX).abs()) {
                    if (event.x >= maxMenuWidth) {
                        //在菜单打开的情况下,点击了内容区域, 并且没有触发横向滚动
                        closeMenu()
                    } else {
                        resetLayout()
                    }
                } else {
                    resetLayout()
                }
                isTouchDownInContentWithMenuOpen = false
                needInterceptTouchEvent = false
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        return true
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount < 2) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            var widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            var heightSize = MeasureSpec.getSize(heightMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)

            if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
                //测量菜单, 和内容的宽度
                getChildAt(0).measure(exactlyMeasure(menuMaxWidthRatio * widthSize), heightMeasureSpec)
                getChildAt(1).measure(exactlyMeasure(widthSize), heightMeasureSpec)

                setMeasuredDimension(widthSize, heightSize)
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        if (childCount > 2) {
            throw IllegalStateException("不支持2个以上的子布局")
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        refreshContentLayout(lastContentLayoutLeft)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        sliderCallback?.onSizeChanged(this)
    }


    override fun onScrollChange(orientation: ORIENTATION, distance: Float /*瞬时值*/) {
        super.onScrollChange(orientation, distance)
        //refreshMenuLayout(((secondMotionEvent?.x ?: 0f) - (firstMotionEvent?.x ?: 0f)).toInt())
        if (isHorizontal(orientation)) {
            if (!needInterceptTouchEvent) {
                if (distance > 0) {
                    //左滑动
                    if (isMenuClose()) {

                    } else {
                        needInterceptTouchEvent = true
                    }
                } else {
                    //又滑动
                    if (isMenuOpen()) {

                    } else {
                        needInterceptTouchEvent = true
                    }
                }
            }

            if (needInterceptTouchEvent) {
                refreshLayout(distance.toInt())
            }
        }
    }

    override fun onFlingChange(orientation: ORIENTATION, velocity: Float /*瞬时值*/) {
        super.onFlingChange(orientation, velocity)
        //L.e("call: onFlingChange -> $velocity")
        if (isHorizontal(orientation)) {
            if (velocity < -2000) {
                //快速向左
                closeMenu()
            } else if (velocity > 2000) {
                //快速向右
                openMenu()
            }
        }
    }

    /**菜单是否完全打开*/
    fun isMenuOpen(): Boolean {
        return contentLayoutLeft >= maxMenuWidth
    }

    /**菜单完全关闭*/
    fun isMenuClose(): Boolean {
        return contentLayoutLeft <= 0
    }

    /**根据当前打开程度, 决定*/
    fun resetLayout() {
        if (isOldMenuOpen) {
            //菜单已经打开
            if (contentLayoutLeft <= maxMenuWidth * 2 / 3) {
                closeMenu()
            } else {
                openMenu()
            }
        } else {
            //菜单未打开
            if (contentLayoutLeft >= maxMenuWidth * 1 / 3) {
                openMenu()
            } else {
                closeMenu()
            }
        }
    }

    /**关闭菜单*/
    fun closeMenu() {
        if (contentLayoutLeft == 0) {
            if (isOldMenuOpen) {
                sliderCallback?.onMenuSlider(this, 0f, isTouchDown)
            } else {
            }
        } else {
            startScrollTo(contentLayoutLeft, 0)
        }
    }

    /**打开菜单*/
    fun openMenu() {
        if (contentLayoutLeft == maxMenuWidth) {
            if (isOldMenuOpen) {
            } else {
                sliderCallback?.onMenuSlider(this, 1f, isTouchDown)
            }
        } else {
            startScrollTo(contentLayoutLeft, maxMenuWidth)
        }
    }

    /**刷新布局位置*/
    private fun refreshLayout(distanceX: Int /*没次移动的距离*/) {
        //L.e("call: refreshMenuLayout -> $distanceX")
        refreshContentLayout(clampViewPositionHorizontal(contentLayoutLeft - distanceX))
    }

    //
    private fun refreshContentLayout(left: Int) {
        if (childCount == 2) {
            getChildAt(1).apply {
                layout(left, 0, left + this.measuredWidth, this.measuredHeight)
                lastContentLayoutLeft = left
                sliderCallback?.onMenuSlider(this@SliderMenuLayout, left.toFloat() / maxMenuWidth, isTouchDown)
            }
            refreshMenuLayout()
        }
    }

    override fun computeScroll() {
        if (overScroller.computeScrollOffset()) {
            //scrollTo(overScroller.currX, overScroller.currY)
            val currX = overScroller.currX
            if (contentLayoutLeft != currX) {
                refreshContentLayout(currX)
            }
            postInvalidate()
        }
    }

    //记录最后一次内容布局的left坐标, 用来恢复时使用,比如熄屏后亮屏.就需要恢复
    private var lastContentLayoutLeft = 0

    //当前内容布局的Left坐标
    private val contentLayoutLeft: Int
        get() {
            return if (childCount >= 2) {
                getChildAt(1).left
            } else {
                0
            }
        }

    //菜单允许展开的最大宽度
    private val maxMenuWidth: Int
        get() {
            return (menuMaxWidthRatio * measuredWidth).toInt()
        }

    //单独更新菜单,营造视差滚动
    private fun refreshMenuLayout() {
        //计算出菜单展开的比例
        val fl = contentLayoutLeft.toFloat() / maxMenuWidth
        if (fl > 0f && childCount > 0) {
            getChildAt(0).apply {
                //视差开始时的偏移值
                val menuOffsetStart = -maxMenuWidth / 2
                val left = menuOffsetStart + (menuOffsetStart.abs() * fl).toInt()
                layout(left, 0, left + this.measuredWidth, this.measuredHeight)
            }
        }
    }

    /**约束内容允许滚动的范围*/
    private fun clampViewPositionHorizontal(value: Int): Int {
        val minValue = 0
        val maxValue = maxMenuWidth

        var result = value
        if (value < minValue) {
            result = minValue
        } else if (value > maxValue) {
            result = maxValue
        }
        return result
    }

    private val maskRect: Rect by lazy {
        Rect()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        //绘制内容区域的阴影遮盖
        if (isMenuClose()) {

        } else {
            val layoutLeft = contentLayoutLeft
            debugPaint.color = Color.BLACK.tranColor((255 * (layoutLeft.toFloat() / maxMenuWidth) * 0.4f /*限制一下值*/).toInt())
            debugPaint.style = Paint.Style.FILL_AND_STROKE
            maskRect.set(layoutLeft, 0, measuredWidth, measuredHeight)
            canvas.drawRect(maskRect, debugPaint)
        }
    }

    interface SliderCallback {

        /**当前是否可以操作*/
        fun canSlider(): Boolean

        fun onSizeChanged(menuLayout: SliderMenuLayout)

        /**
         * 菜单打开的完成度
         * @param ratio [0-1]
         * */
        fun onMenuSlider(menuLayout: SliderMenuLayout, ratio: Float, isTouchDown: Boolean /*手指是否还在触摸*/)
    }

    open class SimpleSliderCallback : SliderCallback {
        override fun canSlider(): Boolean {
            return true
        }

        override fun onSizeChanged(menuLayout: SliderMenuLayout) {
        }

        override fun onMenuSlider(menuLayout: SliderMenuLayout, ratio: Float, isTouchDown: Boolean) {
        }
    }
}

