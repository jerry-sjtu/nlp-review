package com.dp.nlp.review.udf;

/**
 * Created by qiangwang on 15/3/2.
 */
public class SentimentExtract {
    public static void main(String[] args) {
        String str1 = "[\"沙拉不错:33_45:1:1\",\"性价比较高:0_11:1:1\",\"口味一般:21_27:-1:1\"]";
        SentimentExtract obj = new SentimentExtract();
        System.out.println(obj.evaluate(str1));
    }

    public String evaluate(String str) {
        String[] arr1 = str.split(",");
        StringBuilder sb = new StringBuilder();

        for(String x : arr1) {
            String[] arr2 = x.split(":");
            if(arr2.length == 4) {
                sb.append(arr2[2]).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
