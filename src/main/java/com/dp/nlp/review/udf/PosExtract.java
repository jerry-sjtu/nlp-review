package com.dp.nlp.review.udf;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by qiangwang on 15/3/2.
 */
public class PosExtract extends UDF {
    public static void main(String[] args) {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("沙拉不错:33_45:1:1:鸡肉也很鲜美");
        arr.add("性价比较高:0_11:1:1:鸡肉也很鲜美");
        arr.add("口味一般:21_27:-1:1:鸡肉也很鲜美");
        PosExtract obj = new PosExtract();
        System.out.println(obj.evaluate(arr));
    }

    public String evaluate(ArrayList<String> arr) {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for(String x : arr) {
            String[] arr2 = x.split(":");
            if(arr2.length == 5) {
                sb.append(arr2[1]).append(" ");
            }
        }
        sb.insert(sb.length() - 1, '"');
        return sb.toString().trim();
    }
}