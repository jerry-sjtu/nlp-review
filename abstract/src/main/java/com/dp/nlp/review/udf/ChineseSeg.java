package com.dp.nlp.review.udf;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.List;

/**
 * Created by qiangwang on 15/2/5.
 */
public class ChineseSeg extends UDF {
    private static String SPLIT_REGX = " |\\.|!|。|！|\\t|\\n|~";

    public static void main(String[] args) {
        //String reivew = "年底了天天都有聚会，周五约了老同事在久光吃饭，大家都爱辣菜，选了8楼的品川，正好8楼是久光积分兑换点，换了Fissler 14cm 两个锅，蛮爽的。品川就在商场会员中心斜对面，大大字的品川，远远已见。";
        String reivew = "最近和老公一起来玩了一趟，这里毕竟在郊区地点偏僻，不过正因为这样才有大片土地开发使用，温泉度假区里显得十分宽敞舒适，远离喧闹拥挤的市中心来到这里安静开阔的地方首先就觉得精神放松了。建筑外观体现汉唐风格，古典气派。步入温泉中心大堂又体现出欧洲皇室古典风格，金碧辉煌的感觉。";
        ChineseSeg seg = new ChineseSeg();
        String clause =  seg.evaluate(reivew, 0);
        System.out.println(clause);
    }

    public String evaluate(String review, int hasPOS) {
        if(StringUtils.isBlank(review)) {
            return StringUtils.EMPTY;
        }
        String[] cList = review.split(SPLIT_REGX);
        StringBuilder sb = new StringBuilder();
        for (String clause : cList) {
            System.out.println(clause);
            List<Term> termList = ToAnalysis.parse(clause);
            for(Term t : termList) {
                if(hasPOS == 0) {
                    sb.append(t.getName()).append(" ");
                }
                else {
                    sb.append(t.toString()).append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}

