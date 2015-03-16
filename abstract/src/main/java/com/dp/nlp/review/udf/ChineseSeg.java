package com.dp.nlp.review.udf;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.xmlbeans.impl.xb.xsdschema.MaxLengthDocument;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by qiangwang on 15/2/5.
 */
public class ChineseSeg extends UDF {
    private static String SPLIT_REGX = " |\\.|!|。|！|\\t|\\n|~";
    private static Pattern BAD_HEAD_P = Pattern.compile("[a-zA-Z0-9]+");
    private static int MAX_LENGTH = 1024;

    public static void main(String[] args) {
        //String reivew = "年底了天天都有聚会，周五约了老同事在久光吃饭，大家都爱辣菜，选了8楼的品川，正好8楼是久光积分兑换点，换了Fissler 14cm 两个锅，蛮爽的。品川就在商场会员中心斜对面，大大字的品川，远远已见。";
        //String reivew = "最近和老公一起来玩了一趟，这里毕竟在郊区地点偏僻，不过正因为这样才有大片土地开发使用，温泉度假区里显得十分宽敞舒适，远离喧闹拥挤的市中心来到这里安静开阔的地方首先就觉得精神放松了。建筑外观体现汉唐风格，古典气派。步入温泉中心大堂又体现出欧洲皇室古典风格，金碧辉煌的感觉。";
        //String reivew = "sadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsdsadsd";
        String reivew = "独立的一幢小楼，可惜装修很差，椅子坐得很不舒服。菜非常棒，如果你象我一样天天在外面吃饭，隔一段时间来到这儿吃一次会觉得是一种调理。特别喜欢有个菜是田螺里面装了肉蒸着吃的，每次必点。";
        ChineseSeg seg = new ChineseSeg();
        String clause =  seg.evaluate(reivew, 0, "|");
        System.out.println(clause);
//        String fc = seg.filter("原 帖bbs 地址 http");
//        System.out.println(fc);
    }

    public String evaluate(String review, int hasPOS, String delimeter) {
        review = filter(review);
        if(StringUtils.isBlank(review)) {
            return StringUtils.EMPTY;
        }
        String[] cList = review.split(SPLIT_REGX);
        StringBuilder sb = new StringBuilder();
        for (String clause : cList) {
            clause = clause.trim();
            if(StringUtils.isBlank(clause)) {
                continue;
            }

            List<Term> termList = ToAnalysis.parse(clause);
            for(Term t : termList) {
                if(hasPOS == 0) {
                    sb.append(t.getName()).append(" ");
                }
                else {
                    sb.append(t.toString()).append(" ");
                }
            }
            sb.append(delimeter);
        }
        return sb.toString();
    }

    public String filter(String originText) {
        if(StringUtils.isBlank(originText)) {
            return StringUtils.EMPTY;
        }
        if(originText.length() > MAX_LENGTH) {
            String tmpStr = originText.substring(0, 30);
            Matcher m = BAD_HEAD_P.matcher(tmpStr);
            if(m.matches()) {
                return StringUtils.EMPTY;
            }
        }
        originText = originText.replaceAll("([a-z]|[A-Z]|[0-9]|\\t|=|\\-|\\.|/|:|：| )+", " ").trim();
        return originText;
    }
}

