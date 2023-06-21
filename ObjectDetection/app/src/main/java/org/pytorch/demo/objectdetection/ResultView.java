// Copyright (c) 2020 Facebook, Inc. and its affiliates.
// All rights reserved.
//
// This source code is licensed under the BSD-style license found in the
// LICENSE file in the root directory of this source tree.

package org.pytorch.demo.objectdetection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Locale;


public class ResultView extends View {

    private final static int TEXT_X = 40;
    private final static int TEXT_Y = 35;
    private final static int TEXT_WIDTH = 260;
    private final static int TEXT_HEIGHT = 50;

    private Paint mPaintRectangle;
    private Paint mPaintText;
    private ArrayList<Result> mResults;


    public ResultView(Context context) {
        super(context);
    }

    public ResultView(Context context, AttributeSet attrs){
        super(context, attrs);
        mPaintRectangle = new Paint();
        mPaintRectangle.setColor(Color.YELLOW);
        mPaintText = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mResults == null) return;
        for (Result result : mResults) {
            mPaintRectangle.setStrokeWidth(5);
            mPaintRectangle.setStyle(Paint.Style.STROKE);
            float scaleX = (float) 1088 / getWidth();  // Factor de escala para el ancho
            float scaleY = (float) 1088 / getHeight();  // Factor de escala para el alto
            canvas.drawRect(result.rect, mPaintRectangle);
            //canvas.drawRect(result.rect.left*scaleX, result.rect.top*scaleY, result.rect.right*scaleX,  result.rect.bottom*scaleY, mPaintRectangle);
            Log.d("DataRect 1",String.valueOf(result.rect));
            Log.d("Canvas width",String.valueOf(getWidth()));
            Log.d("Canvas he",String.valueOf(getHeight()));

            Path mPath = new Path();
            RectF mRectF = new RectF(result.rect.left, result.rect.top, result.rect.left + TEXT_WIDTH,  result.rect.top + TEXT_HEIGHT);
            //RectF mRectF = new RectF(result.rect.left*scaleX, result.rect.top*scaleY, result.rect.left*scaleX + TEXT_WIDTH,  result.rect.top*scaleY + TEXT_HEIGHT);

            if (result.classIndex==0){
                mPath.addRect(mRectF, Path.Direction.CW);
                mPaintText.setColor(Color.TRANSPARENT);
                canvas.drawPath(mPath, mPaintText);

                mPaintText.setColor(Color.BLACK);
                mPaintText.setStrokeWidth(1);
                mPaintText.setStyle(Paint.Style.FILL);
                mPaintText.setTextSize(32);
                canvas.drawText(String.format(Locale.getDefault(), PrePostProcessor.mClasses[result.classIndex], result.score), result.rect.left + TEXT_X, result.rect.top + TEXT_Y, mPaintText);
                //canvas.drawText(String.format(Locale.getDefault(),"%s %.2f", PrePostProcessor.mClasses[result.classIndex], result.score), result.rect.left*scaleX + TEXT_X, result.rect.top*scaleY + TEXT_Y, mPaintText);
            }else {
                mPath.addRect(mRectF, Path.Direction.CW);
                mPaintText.setColor(Color.MAGENTA);
                canvas.drawPath(mPath, mPaintText);
                mPaintText.setColor(Color.WHITE);
                mPaintText.setStrokeWidth(0);
                mPaintText.setStyle(Paint.Style.FILL);
                mPaintText.setTextSize(32);
                canvas.drawText(String.format(Locale.getDefault(), PrePostProcessor.mClasses[result.classIndex], result.score), result.rect.left + TEXT_X, result.rect.top + TEXT_Y, mPaintText);
                //canvas.drawText(String.format(Locale.getDefault(),"%s %.2f", PrePostProcessor.mClasses[result.classIndex], result.score), result.rect.left*scaleX + TEXT_X, result.rect.top*scaleY + TEXT_Y, mPaintText);
            }
        }
    }

    public Bitmap getBitmapFromResults(Bitmap bitmap) {

        // Crea un objeto Canvas utilizando el Bitmap creado
        Bitmap mergeBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mergeBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);
        if (mResults == null) return bitmap;
        for (Result result : mResults) {
            mPaintRectangle.setStrokeWidth(5);
            mPaintRectangle.setStyle(Paint.Style.STROKE);
            float scaleX = (float) bitmap.getWidth() / getWidth();  // Factor de escala para el ancho
            float scaleY = (float) bitmap.getHeight() / getHeight();  // Factor de escala para el alto

            canvas.drawRect(result.rect.left*scaleX, result.rect.top*scaleY, result.rect.right*scaleX,  result.rect.bottom*scaleY, mPaintRectangle);
            //.d("DataRect 2",String.valueOf(result.rect));
            //Log.d("Canvas width",String.valueOf(getWidth()));
            //Log.d("Canvas he",String.valueOf(getHeight()));

            Path mPath = new Path();
            RectF mRectF = new RectF(result.rect.left*scaleX, result.rect.top*scaleY, result.rect.left*scaleX + TEXT_WIDTH,  result.rect.top*scaleY + TEXT_HEIGHT);

            //Log.d("Data", String.valueOf(result.classIndex)+" -  " + String.valueOf(result.score));
            if (result.classIndex==0){
                mPath.addRect(mRectF, Path.Direction.CW);
                mPaintText.setColor(Color.TRANSPARENT);
                canvas.drawPath(mPath, mPaintText);

                mPaintText.setColor(Color.BLACK);
                mPaintText.setStrokeWidth(1);
                mPaintText.setStyle(Paint.Style.FILL);
                mPaintText.setTextSize(32);

                //canvas.drawText(String.format("%s", PrePostProcessor.mClasses[result.classIndex]), result.rect.left + TEXT_X, result.rect.top + TEXT_Y, mPaintText);
                canvas.drawText(String.format(Locale.getDefault(),"%s %.2f", PrePostProcessor.mClasses[result.classIndex], result.score), result.rect.left*scaleX + TEXT_X, result.rect.top*scaleY + TEXT_Y, mPaintText);
            }else {
                mPath.addRect(mRectF, Path.Direction.CW);
                mPaintText.setColor(Color.MAGENTA);
                canvas.drawPath(mPath, mPaintText);

                mPaintText.setColor(Color.WHITE);
                mPaintText.setStrokeWidth(0);
                mPaintText.setStyle(Paint.Style.FILL);
                mPaintText.setTextSize(32);
                //canvas.drawText(String.format("%s", PrePostProcessor.mClasses[result.classIndex]), result.rect.left + TEXT_X, result.rect.top + TEXT_Y, mPaintText);
                canvas.drawText(String.format(Locale.getDefault(),"%s %.2f", PrePostProcessor.mClasses[result.classIndex], result.score), result.rect.left*scaleX + TEXT_X, result.rect.top*scaleY + TEXT_Y, mPaintText);
        }
        }
        return mergeBitmap;
    }

    public void setResults(ArrayList<Result> results) {
        mResults = results;
    }



}
