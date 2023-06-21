// Copyright (c) 2020 Facebook, Inc. and its affiliates.
// All rights reserved.
//
// This source code is licensed under the BSD-style license found in the
// LICENSE file in the root directory of this source tree.

package org.pytorch.demo.objectdetection;

import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

class Result {
    int classIndex;
    Float score;
    Rect rect;
    Float maxValue;

    public Result(int cls, Float output, Rect rect,Float maxValue) {
        this.classIndex = cls;
        this.score = output;
        this.rect = rect;
        this.maxValue=maxValue;
    }
};

public class PrePostProcessor {
    // for yolov5 model, no need to apply MEAN and STD
    static float[] NO_MEAN_RGB = new float[] {0.0f, 0.0f, 0.0f};
    static float[] NO_STD_RGB = new float[] {1.0f, 1.0f, 1.0f};
    //static float[] NO_MEAN_RGB = new float[] {0.485f, 0.456f, 0.406f};
    //static float[] NO_STD_RGB = new float[] {0.229f, 0.224f, 0.225f};
    // model input image size
    static int mInputWidth = 1024;
    static int mInputHeight = 1024;

    // model output is of size 25200*(num_of_class+5)
    private static int mOutputRow = 64512; // as decided by the YOLOv5 model for input image of size 640*640
    private static int mOutputColumn = 9; // left, top, right, bottom, score and 80 class probability
    private static float mThreshold = 0.25f; // score above which a detection is generated -  NMS confidence threshold

    private static float Iou=0.30f; //NMS IoU threshold
    private static int mNmsLimit = 30;

    static String[] mClasses;

    // The two methods nonMaxSuppression and IOU below are ported from https://github.com/hollance/YOLO-CoreML-MPSNNGraph/blob/master/Common/Helpers.swift
    /**
     Removes bounding boxes that overlap too much with other boxes that have
     a higher score.
     - Parameters:
     - boxes: an array of bounding boxes and their scores
     - limit: the maximum number of boxes that will be selected
     - threshold: used to decide whether boxes overlap too much
     */
    static ArrayList<Result> nonMaxSuppression(ArrayList<Result> boxes, int limit, float threshold) {

        // Do an argsort on the confidence scores, from high to low.
        Collections.sort(boxes, new Comparator<Result>() {
                    @Override
                    public int compare(Result o1, Result o2) {
                        //Log.d("Result 1: ",String.valueOf(o1.score));
                        //Log.d("Result 2: ",String.valueOf(o2.score));
                        //return o1.score.compareTo(o2.score);
                        return Float.compare(o2.score,o1.score);
                    }
                });

        ArrayList<Result> selected = new ArrayList<>();
        boolean[] active = new boolean[boxes.size()];
        Arrays.fill(active, true);
        int numActive = active.length;

        // The algorithm is simple: Start with the box that has the highest score.
        // Remove any remaining boxes that overlap it more than the given threshold
        // amount. If there are any boxes left (i.e. these did not overlap with any
        // previous boxes), then repeat this procedure, until no more boxes remain
        // or the limit has been reached.
        // Aplicar el algoritmo de NMS
        for (int i = 0; i < boxes.size() && selected.size() < limit; i++) {
            if (active[i]) {
                Result boxA = boxes.get(i);
                selected.add(boxA);

                for (int j = i + 1; j < boxes.size(); j++) {
                    if (active[j]) {
                        Result boxB = boxes.get(j);
                        float iou = IOU(boxA.rect, boxB.rect);
                        Log.d("BoxB - Class - Iou",String.valueOf(boxB.score)+"-"+String.valueOf(boxB.classIndex)+"-"+String.valueOf(IOU(boxA.rect, boxB.rect)));
                        if (iou > threshold) {
                            active[j] = false;
                            numActive--;
                            if (numActive <= 0) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return selected;
    }

    static ArrayList<Result> nonMaxSuppressionGPT(ArrayList<Result> boxes, int limit, float threshold) {

        // Do an argsort on the confidence scores, from high to low.
        boxes.sort((r1,r2)->Double.compare(r2.score,r1.score));

        ArrayList<Result> selectedResults = new ArrayList<>();

        while(!boxes.isEmpty()){
            Result selected=boxes.get(0); //0
            selectedResults.add(selected);
            for(int i=0;i<boxes.size();i++){
                Result current =boxes.get(i); //1
                if (IOU(selected.rect,current.rect)>threshold){
                    boxes.remove(i);
                    i--;
                }
            }
        }
        //selectedResults.add(boxes.get(0));

        return selectedResults;
    }

    /**
     Computes intersection-over-union overlap between two bounding boxes.
     */
    static float IOU(Rect a, Rect b) {
        float areaA = (a.right - a.left) * (a.bottom - a.top);
        if (areaA <= 0.0) return 0.0f;

        float areaB = (b.right - b.left) * (b.bottom - b.top);
        if (areaB <= 0.0) return 0.0f;

        float intersectionMinX = Math.max(a.left, b.left);
        float intersectionMinY = Math.max(a.top, b.top);
        float intersectionMaxX = Math.min(a.right, b.right);
        float intersectionMaxY = Math.min(a.bottom, b.bottom);
        float intersectionArea = Math.max(0,intersectionMaxY - intersectionMinY) * Math.max(0,intersectionMaxX - intersectionMinX);

        return intersectionArea / (areaA + areaB - intersectionArea);

    }

    static ArrayList<Result> outputsToNMSPredictions(float[] outputs, float imgScaleX, float imgScaleY, float ivScaleX, float ivScaleY, float startX, float startY) {
        ArrayList<Result> results = new ArrayList<>();
        for (int i = 0; i< mOutputRow; i++) {
            if (outputs[i* mOutputColumn +4] > mThreshold) {
                float x = outputs[i* mOutputColumn];
                float y = outputs[i* mOutputColumn +1];
                float w = outputs[i* mOutputColumn +2];
                float h = outputs[i* mOutputColumn +3];

                float left = imgScaleX * (x - w/2);
                float top = imgScaleY * (y - h/2);
                float right = imgScaleX * (x + w/2);
                float bottom = imgScaleY * (y + h/2);

                float max = outputs[i* mOutputColumn +5];
                int cls = 0;
                for (int j = 0; j < mOutputColumn -5; j++) {
                    if (outputs[i* mOutputColumn +5+j] > max) {
                        max = outputs[i* mOutputColumn +5+j];
                        cls = j;
                    }
                }

                Rect rect = new Rect((int)(startX+ivScaleX*left), (int)(startY+top*ivScaleY), (int)(startX+ivScaleX*right), (int)(startY+ivScaleY*bottom));
                float confidencemax=outputs[i*mOutputColumn+4];
                Result result = new Result(cls, confidencemax, rect,max);
                results.add(result);
            }
        }
        return nonMaxSuppression(results, mNmsLimit, Iou);
    }

}
