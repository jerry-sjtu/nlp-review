package com.dp.nlp.review.udf;

import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.ArrayList;

/**
 * Created by qiangwang on 15/3/2.
 */
public class TypeExtract extends UDF {
    public static void main(String[] args) {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("沙拉不错:33_45:1:1");
        arr.add("性价比较高:0_11:1:1");
        arr.add("口味一般:21_27:-1:1");
        TypeExtract obj = new TypeExtract();
        System.out.println(obj.evaluate(arr));
    }

    public String evaluate(ArrayList<String> arr) {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for(String x : arr) {
            String[] arr2 = x.split(":");
            if(arr2.length == 4) {
                sb.append(arr2[3]).append(" ");
            }
        }
        sb.insert(sb.length() - 1, '"');
        return sb.toString().trim();
    }
}
