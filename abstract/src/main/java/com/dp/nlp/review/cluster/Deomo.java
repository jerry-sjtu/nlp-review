package com.dp.nlp.review.cluster;

import com.sun.xml.internal.xsom.impl.Ref;
import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.FilterModifWord;

import java.util.List;

/**
 * Created by qiangwang on 15/1/26.
 */
public class Deomo {

    public static void main(String[] args) {
        String reivew = "年底了天天都有聚会，周五约了老同事在久光吃饭，大家都爱辣菜，选了8楼的品川，正好8楼是久光积分兑换点，换了Fissler 14cm 两个锅，蛮爽的。品川就在商场会员中心斜对面，大大字的品川，远远已见。";
        test(reivew);
        dictionay();
    }

    public static void test(String review) {
        List<Term> simpleParse = BaseAnalysis.parse(review);
        System.out.println(simpleParse);

        List<Term> accurateParse = ToAnalysis.parse(review);
        System.out.println(accurateParse);

        List<Term> nlpParse = NlpAnalysis.parse(review);
        System.out.println(nlpParse);
    }

    public static void dictionay() {
        //增加两个停用词
        FilterModifWord.insertStopWord("并且") ;
        FilterModifWord.insertStopWord("但是") ;
        // 增加新词,中间按照'\t'隔开
        UserDefineLibrary.insertWord("ansj中文分词", "userDefine", 1000);
        List<Term> terms = ToAnalysis.parse("我觉得Ansj中文分词是一个不错的系统!我是王婆!");
        System.out.println("增加新词例子:" + terms);
        // 删除词语,只能删除.用户自定义的词典.
        UserDefineLibrary.removeWord("ansj中文分词");
        terms = ToAnalysis.parse("我觉得ansj中文分词是一个不错的系统!我是王婆!");
        System.out.println("删除用户自定义词典例子:" + terms);
    }
}
