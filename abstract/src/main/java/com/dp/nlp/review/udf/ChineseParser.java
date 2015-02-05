package com.dp.nlp.review.udf;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.international.pennchinese.ChineseTreebankLanguagePack;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

/**
 * Created by qiangwang on 15/2/5.
 */
public class ChineseParser extends UDF {
    public static void main(String[] args) {
        //String sentence = "点了 蜀香猪手 觉得 烧的 不够 糯";
        //String sentence = "蹄筋 的 部分 只有 彻底 烧 糯了，然后 油锅 里 一拉 才 外酥里糯";
        //String sentence = "海鲜 石锅面片 超 好吃 哒 里面 有 虾 和 蛤蜊 虾 和 蛤蜊 都 很 新鲜 哦";
        String sentence = "水煮鱼 不太 好 太 油辣 口水鸡 一般 性价比 高";
        //String sentence = "他们 家 的 鱼 不错";
        String keyword = "鱼";
        extraDepWord(sentence, keyword);
    }

    public static void extraDepWord(String sentence, String keyword) {
        String grammar = "chinesePCFG.ser.gz";
        String[] options = {"-maxLength", "80"};
        LexicalizedParser lp = LexicalizedParser.loadModel(grammar, options);

        TreebankLanguagePack tlp = new ChineseTreebankLanguagePack();
        Tokenizer<? extends HasWord> toke = tlp.getTokenizerFactory().getTokenizer(new StringReader(sentence));
        List<? extends HasWord> sentList = toke.tokenize();
        Tree parse = lp.apply(sentList);
        parse.pennPrint();

        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
        System.out.println(tdl);
    }
}
