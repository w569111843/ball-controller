package com.example.ball.ui.common

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.customview.widget.ViewDragHelper
import kotlin.math.max
import kotlin.math.min


class SwipeDragLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr) {
    private var contentView: View? = null //列表的view
    private var menuView: View? = null //滑出菜单的view
    private var mDragHelper: ViewDragHelper? = null
    private val originPos = Point()
    var isOpen: Boolean = false
        private set
    private var ios = true
    private var clickToClose = true
    private var offset = 0f
    private var needOffset = 0.2f
    private var mListener: SwipeListener? = null

    init {
        init()
    }

    //初始化dragHelper，对拖动的view进行操作
    private fun init() {
        mDragHelper = ViewDragHelper.create(this, 1.0f, object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return child === contentView
            }


            override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
                if (releasedChild === contentView) {
                    if (isOpen) {
                        if (offset != 1f && offset > (1 - needOffset)) {
                            open()
                        } else if (offset == 1f) {
                            if (clickToClose) {
                                close()
                            }
                        } else {
                            close()
                        }
                    } else {
                        if (offset != 0f && offset < needOffset) {
                            close()
                        } else if (offset == 0f) {
                            parent.requestDisallowInterceptTouchEvent(false)
                        } else {
                            open()
                            Log.d("Released and isOpen", "" + isOpen)
                            if (mListener != null) {
                                mListener!!.onOpened(this@SwipeDragLayout)
                            }
                        }
                    }
                    invalidate()
                }
            }


            override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
                //滑动距离,如果启动效果，则可滑动3/2倍菜单宽度的距离
                val leftBound =
                    paddingLeft - (if (ios) menuView!!.width * 3 / 2 else menuView!!.width)
                val rightBound = width - child.width
                val newLeft = min(max(left.toDouble(), leftBound.toDouble()), rightBound.toDouble())
                    .toInt()
                return newLeft
            }

            override fun getViewHorizontalDragRange(child: View): Int {
                return if (contentView === child) menuView!!.width else 0
            }

            override fun onViewPositionChanged(
                changedView: View,
                left: Int,
                top: Int,
                dx: Int,
                dy: Int
            ) {
                val childWidth = menuView!!.width
                offset = -(left - paddingLeft).toFloat() / childWidth
                //offset can callback here
                if (mListener != null) {
                    mListener!!.onUpdate(this@SwipeDragLayout, offset)
                }
            }
        })
    }

    fun setClickToClose(clickToClose: Boolean) {
        this.clickToClose = clickToClose
    }

    fun setIos(ios: Boolean) {
        this.ios = ios
    }

    fun open() {
        mCacheView = this@SwipeDragLayout
        mDragHelper!!.settleCapturedViewAt(originPos.x - menuView!!.width, originPos.y)
        isOpen = true
    }

    fun smoothOpen(smooth: Boolean) {
        mCacheView = this@SwipeDragLayout
        if (smooth) {
            mDragHelper!!.smoothSlideViewTo(
                contentView!!,
                originPos.x - menuView!!.width,
                originPos.y
            )
        } else {
            contentView!!.layout(
                originPos.x - menuView!!.width,
                originPos.y,
                menuView!!.left,
                menuView!!.bottom
            )
        }
    }

    //滑动关闭方法
    private fun smoothClose(smooth: Boolean) {
        if (smooth) {
            mDragHelper!!.smoothSlideViewTo(contentView!!, paddingLeft, paddingTop)
            postInvalidate()
        } else {
            contentView!!.layout(originPos.x, originPos.y, menuView!!.right, menuView!!.bottom)
        }
        isOpen = false
        mCacheView = null
    }

    fun close() {
        contentView!!.layout(originPos.x, originPos.y, menuView!!.right, menuView!!.bottom)
        isOpen = false
        mCacheView = null
        if (mListener != null) {
            mListener!!.onClosed(this@SwipeDragLayout)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> if (mCacheView != null) {
                if (mCacheView !== this) {
                    mCacheView!!.smoothClose(true)
                    return true //点击关闭后不执行其他click事件
                } else if (isOpen && ev.x < menuView!!.left) {
                    mDragHelper!!.smoothSlideViewTo(contentView!!, paddingLeft, paddingTop)
                    postInvalidate()
                    return true //点击关闭后不执行其他click事件
                }
                parent.requestDisallowInterceptTouchEvent(true)
            }
        }
        return mDragHelper!!.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mDragHelper!!.processTouchEvent(event)
        return true
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        originPos.x = contentView!!.left
        originPos.y = contentView!!.top
    }

    override fun computeScroll() {
        if (mDragHelper!!.continueSettling(true)) {
            invalidate()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        contentView = getChildAt(1)
        menuView = getChildAt(0)
        val params =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.RIGHT
        menuView!!.setLayoutParams(params)
        //重写OnClickListener会导致关闭失效
        if (contentView != null) {
            contentView!!.setOnClickListener(object : OnClickListener {
                override fun onClick(v: View) {
                    if (clickToClose && isOpen) {
                        smoothClose(true)
                        return
                    }
                    if (mListener != null) {
                        mListener!!.onClick(this@SwipeDragLayout)
                    }
                }
            })
        }
    }

    override fun onDetachedFromWindow() {
        if (mCacheView === this) {
            mCacheView!!.smoothClose(false)
            mCacheView = null
        }
        super.onDetachedFromWindow()
    }


    override fun setOnTouchListener(l: OnTouchListener) {
        super.setOnTouchListener(l)
    }

    fun addListener(listener: SwipeListener?) {
        mListener = listener
    }

    //滑动监听
    interface SwipeListener {
        /**
         * 拖动中，可根据offset 进行其他动画
         * @param layout
         * @param offset 偏移量
         */
        fun onUpdate(layout: SwipeDragLayout?, offset: Float)

        /**
         * 展开完成
         * @param layout
         */
        fun onOpened(layout: SwipeDragLayout?)

        /**
         * 关闭完成
         * @param layout
         */
        fun onClosed(layout: SwipeDragLayout?)

        /**
         * 点击内容layout [.onFinishInflate]
         * @param layout
         */
        fun onClick(layout: SwipeDragLayout?)
    }

    companion object {
        private var mCacheView: SwipeDragLayout? = null
        fun getmCacheView(): SwipeDragLayout? {
            return mCacheView
        }
    }
}