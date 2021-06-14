package com.example.android.kidsdrawingapp

import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.graphics.minus
import com.example.android.kidsdrawingapp.databinding.ActivityMainBinding

class DrawingView (context:Context,attrs:AttributeSet): View(context,attrs) {
    private var mDrawingPath:customPath?=null
    private var mCanvasBitmap:Bitmap?=null
    private var mDrawPaint:Paint?=null
    private var mCanvasPaint:Paint?=null
    private var mBrushSize:Float=0.toFloat()
    private var color= Color.BLACK
    private var canvas:Canvas?=null
    private val mPaths=ArrayList<customPath>()
    private val mUndoPaths=ArrayList<customPath>()

    init{
        setUpDrawing()
    }

    fun onClickUndo(){
        if (mPaths.size>0) {
            mUndoPaths.add(mPaths.removeAt(mPaths.size - 1))
            invalidate()
        }
    }

    fun onClickRedo(){
        if (mUndoPaths.size>0) {
            mPaths.add(mUndoPaths.removeAt(mUndoPaths.size - 1))
            invalidate()
        }
    }


    private fun setUpDrawing(){
        mDrawPaint= Paint()
        mDrawingPath=customPath(color,mBrushSize)
        mDrawPaint!!.color=color
        mDrawPaint!!.style=Paint.Style.STROKE
        mDrawPaint!!.strokeJoin=Paint.Join.ROUND
        mDrawPaint!!.strokeCap=Paint.Cap.ROUND
        mCanvasPaint= Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap= Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas= Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)

        for (path in mPaths){
            mDrawPaint!!.strokeWidth=path.brushThickness
            mDrawPaint!!.color=path.color
            canvas.drawPath(path,mDrawPaint!!)

        }
        if (!mDrawingPath!!.isEmpty){
            mDrawPaint!!.strokeWidth=mDrawingPath!!.brushThickness
            mDrawPaint!!.color=mDrawingPath!!.color
            canvas.drawPath(mDrawingPath!!,mDrawPaint!!)
        }
    }

    fun setColor(newColor:String){
        color=Color.parseColor(newColor)
        mDrawPaint!!.color=color
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX=event?.x
        val touchY=event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                mDrawingPath!!.color=color
                mDrawingPath!!.brushThickness=mBrushSize
                mDrawingPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawingPath!!.moveTo(touchX,touchY)
                    }
                }
            }

            MotionEvent.ACTION_MOVE->{
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawingPath!!.lineTo(touchX,touchY)
                    }
                }
            }

            MotionEvent.ACTION_UP->{
                mPaths.add(mDrawingPath!!)
                mDrawingPath=customPath(color,mBrushSize)
            }
            else -> return false
        }
        invalidate()
        return true
    }

    fun setBrushSize(newSize:Float){
        mBrushSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize,resources.displayMetrics)
        mDrawPaint!!.strokeWidth=mBrushSize
    }

    internal inner class customPath(var color:Int,var brushThickness:Float):Path(){

    }
}