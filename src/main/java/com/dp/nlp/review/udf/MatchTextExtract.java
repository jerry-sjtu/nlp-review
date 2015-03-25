package com.dp.nlp.review.udf;

import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.ArrayList;

/**
 * Created by qiangwang on 15/3/25.
 */
public class MatchTextExtract extends UDF {

    public static void main(String[] args) {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("沙拉不错:33_45:1:1:后来又\n开\"始说\"你们\t态度\n好点");
        arr.add("性价比较高:0_11:1:1:鸡肉也很鲜美");
        arr.add("口味一般:21_27:-1:1:鸡肉也很鲜美");
        MatchTextExtract obj = new MatchTextExtract();
        System.out.println(obj.evaluate(arr));
    }

    public String evaluate(ArrayList<String> arr) {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for(String x : arr) {
            String[] arr1 = x.split(":");
            if(arr1.length != 5) {
                continue;
            }
            String m = arr1[4].replaceAll("\\r|\\t|\\n| |\"", "");
            sb.append(m).append(" ");
        }
        sb.insert(sb.length() - 1, '"');
        return sb.toString().trim();
    }
}
