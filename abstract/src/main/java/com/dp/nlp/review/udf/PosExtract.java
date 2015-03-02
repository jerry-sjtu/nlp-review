package com.dp.nlp.review.udf;

import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * Created by qiangwang on 15/3/2.
 */
public class PosExtract extends UDF {
    public static void main(String[] args) {
        String[] arr = {"沙拉不错:33_45:1:1", "性价比较高:0_11:1:1", "口味一般:21_27:-1:1"};
        PosExtract obj = new PosExtract();
        System.out.println(obj.evaluate(arr));
    }

    public String evaluate(String[] arr) {
        StringBuilder sb = new StringBuilder();

        for(String x : arr) {
            String[] arr2 = x.split(":");
            if(arr2.length == 4) {
                sb.append("\"").append(arr2[1]).append("\"").append(" ");
            }
        }
        return sb.toString().trim();
    }
}
