package com.dp.nlp.review.udf;

import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.ArrayList;

/**
 * Created by qiangwang on 15/3/3.
 */
public class TagSentimentExtract extends UDF {

    public static void main(String[] args) {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("沙拉不错:33_45:1:1");
        arr.add("性价比较高:0_11:1:1");
        arr.add("口味一般:21_27:-1:1");
        TagSentimentExtract obj = new TagSentimentExtract();
        System.out.println(obj.evaluate(arr));
    }

    public String evaluate(ArrayList<String> arr) {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for(String x : arr) {
            String[] arr1 = x.split(":");
            if(arr1.length != 4) {
                continue;
            }
            sb.append(arr1[0]).append("_").append(arr1[2]).append(" ");
        }
        sb.insert(sb.length() - 1, '"');
        return sb.toString().trim();
    }
}
