package com.dp.nlp.review.udf;

import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * Created by qiangwang on 15/3/2.
 */
public class TagExtract extends UDF {

    public static void main(String[] args) {
        String str1 = "[\"沙拉不错:33_45:1:1\",\"性价比较高:0_11:1:1\",\"口味一般:21_27:-1:1\"]";
        TagExtract obj = new TagExtract();
        System.out.println(obj.evaluate(str1));
    }

    public String evaluate(String str) {
        String[] arr = str.split(",");
        StringBuilder sb = new StringBuilder();

        for(String x : arr) {
            int i =  x.indexOf(":");
            if(i >= 0) {
                sb.append("\"").append(x.substring(1, i)).append("\"").append(" ");
            }
        }
        return sb.toString().trim();
    }
}
