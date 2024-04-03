package com.example.evaluate.utils;

import com.newtranx.eval.metrics.IEvaluate;
import com.newtranx.eval.metrics.MetricUtil;
import com.newtranx.eval.metrics.Score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author wyq
 * @date 2024/4/3
 * @desc
 */
public class BlueScoreUtils {

    public static void main(String[] args) {

//        String lang = "zh";
        String lang = "en";
        String trans = "Going to play basketball in the afternoon ?";
        List<String> referenceList = new ArrayList<>(Arrays.asList("Going to play basketball this afternoon ?"
                , "Going to play basketball afternoon ?"));

        getScore(lang, trans, referenceList);
    }

    public static void getScore(String langCode, String trans, List<String> referenceList) {
        IEvaluate evaluate = MetricUtil.buildBleuMetric(langCode);
        Score score = evaluate.sentenceScore(trans, referenceList);
        System.out.println(score);
    }
}
