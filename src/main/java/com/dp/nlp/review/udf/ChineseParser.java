package com.dp.nlp.review.udf;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.international.pennchinese.ChineseTreebankLanguagePack;
import love.cq.domain.Value;
import love.cq.library.Library;
import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.ibatis.io.Resources;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by qiangwang on 15/2/5.
 */
public class ChineseParser extends UDF {
    private static String SPLIT_REGX = "，| |\\.|!|。|！|\\t|\\n|~";
    private static Pattern BAD_HEAD_P = Pattern.compile("[a-zA-Z0-9]+");
    private static int MAX_LENGTH = 1024;
    String grammar = "chinesePCFG.ser.gz";
    String[] options = {"-maxLength", "80"};
    LexicalizedParser lp;
    private static int SENTIMENT_POSITIVE = 1;
    private static int SENTIMENT_NEGATIVE = -1;
    private static int SENTIMENT_NEUTRAL = 0;
    private Map<String, Integer> adjSentMap;
    private Map<String, String> nounClusterMap;

    public ChineseParser() {
        try {
            addDict("library/ambiguity.dic");
        } catch (IOException e) {
            System.out.println("ambiguity dictionary is not loaded!");
        }
        try {
            initAdjSentDic("adj_sent.dic");
        }  catch (IOException e) {
            System.out.println("adj sentiment dictionary is not loaded!");
        }
        try {
            initNounCluster("beauty_hair_noun.csv");
        } catch (IOException e) {
            System.out.println("noun cluster is not loaded!");
        }
        this.lp =  LexicalizedParser.loadModel(grammar, options);
    }

    public static void main(String[] args) {
        //String sentence = "圣维拉是杨浦区新开的一家一站式婚礼会所，环境是没的说的，特别喜欢草坪婚礼，它家也可以办，菜式的话个人觉得一般般，会考虑吧. 好吃的鱼";
        //String sentence = "宴厅给人的感觉就是大气、华丽、清爽，高度估计有4、5米，在这种地方办婚礼显得有档次，而其余婚礼设施也是相当齐全，他们家还有独有的嫁衣房，婚纱礼服应有尽有，非常人性化，而其露天平台也不错，午后可以供客人休息，风景很漂亮，与老公是婚博会上定下的，因为有优惠，实体会所早已看过，相当满意，所有也没什么好纠结了，婚礼半年后举行，而他们家的销售以及策划也早早的就联系了我们，还是很负责任的，婚礼交由这种会所来打理会给人相当安心的感觉，很棒。";
        //String sentence = "灯光很赞，模特和婚纱也很美。场地不错，工作人员也热情，只是细节上还有进步空间。";
        //String sentence = "服务好，里面环境不错，就是周围环境一般般，但是交通十分方便，期待11月的婚礼";
        //String sentence = "环境不错，布置的也很漂亮，服务员还把我掉了的手机还给我。菜品对得起这个价格。";
        String s1 = "抽中资生堂护理的免费体验。地方是坐落在华盛大厦的16楼，还蛮好找的。因为做的睫毛店就在楼上19楼，每两个月必报道一次的地方。";
        String s2 = "总结：我做的是施华蔻的染发，手艺，服务态度，环境都很不错，下面听我慢慢说来。";
        ChineseParser parser = new ChineseParser();
        List<String> tList = parser.evaluate(s1);
        System.out.println(tList);
        tList = parser.evaluate(s2);
        System.out.println(tList);
    }

    public LinkedList<String> evaluate(String sentence) {
        LinkedList<String> tagList = new LinkedList<String>();
        //过滤
        sentence = filter(sentence);
        if(StringUtils.isBlank(sentence)) {
            return tagList;
        }
        //分词
        String[] cList = sentence.split(SPLIT_REGX);
        int start = 0;
        for(String c : cList) {
            if(StringUtils.isBlank(c)) {
                continue;
            }

            List<Term> termList = ToAnalysis.parse(c);
            StringBuilder sb = new StringBuilder();
            Map<String, String> posMap = new HashMap<String, String>();
            for(Term t : termList) {
                sb.append(t.getName()).append(" ");
                posMap.put(t.getName(), t.getNatrue().natureStr);
            }
//            System.out.println(sb.toString());
            LinkedList<String> tmpList = extract(sentence, sb.toString(), start, posMap);
            tagList.addAll(tmpList);

            //update the start index
            start += c.length();
        }
        return tagList;
    }

    private LinkedList<String> extract(String originalSent, String splitSent, int start, Map<String, String> posMap) {
        TreebankLanguagePack tlp = new ChineseTreebankLanguagePack();
        Tokenizer<? extends HasWord> toke = tlp.getTokenizerFactory().getTokenizer(new StringReader(splitSent));
        List<? extends HasWord> sentList = toke.tokenize();
        Tree parse = lp.apply(sentList);

        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
//        System.out.println(tdl);


        LinkedList<String> list = new LinkedList<String>();
        for(TypedDependency d : tdl) {
            TreeGraphNode dep = d.dep();
            TreeGraphNode gov = d.gov();
            GrammaticalRelation rel = d.reln();
            String highlight = highlight(originalSent, start, dep.value(), gov.value());
            if(rel.toString().equals("nsubj") && "a".equals(posMap.get(gov.value()))) {
                int sent = sentiment(dep.value(), gov.value(), 0, 0, 0);
                int type = 1;
                String cluster = nounClusterMap.get(dep.value());
                String target = StringUtils.isBlank(cluster) ? dep.value() : cluster;
                list.add(String.format("%s%s:%s:%s:%s", target, gov.value(), highlight, sent, type));
            }
            else if(rel.toString().equals("amod") && "a".equals(posMap.get(dep.value())))
            {
                int sent = sentiment(dep.value(), gov.value(), 0, 0, 0);
                int type = 1;
                String cluster = nounClusterMap.get(dep.value());
                String target = StringUtils.isBlank(cluster) ? dep.value() : cluster;
                list.add(String.format("%s%s:%s:%s:%s", target, gov.value(), highlight, sent, type));
            }
        }
        return list;
    }

    private String highlight(String sentence, int start, String word1, String word2) {
        int x1 = sentence.indexOf(word1, start);
        int x2 = x1 + word1.length();
        int y1 = sentence.indexOf(word2, start);
        int y2 = y1 + word2.length();
        if(x1 > y1) {
            return String.format("%s_%s", y1, x2);
        }
        return String.format("%s_%s", x1, y2);
    }



    private int sentiment(String noun, String adj, int score1, int score2, int score3) {
        int r = 0;
        Integer sent = adjSentMap.get(adj);
        if(sent != null && sent > 0) {
            r = SENTIMENT_POSITIVE;
        }
        else if(sent != null && sent < 0) {
            r = SENTIMENT_NEGATIVE;
        }
        else if(score1 > 3 && score2 > 3 && score3 > 3) {
            r = SENTIMENT_NEUTRAL;
        }
        return r;
    }

    private String filter(String originText) {
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
        return originText;
    }

    private static void addDict(String dictPath) throws IOException{
        FileReader fr = new FileReader(Resources.getResourceAsFile(dictPath));
        BufferedReader br = new BufferedReader(fr);
        String line;
        while((line = br.readLine()) != null) {
            String[] arr = line.split("( )+");
            if(arr.length == 3) {
                Value value = new Value(arr[0], arr[1], arr[2]);
                Library.insertWord(UserDefineLibrary.ambiguityForest, value);
            }
            else if(arr.length == 5) {
                Value value = new Value(arr[0], arr[1], arr[2], arr[3], arr[4]);
                Library.insertWord(UserDefineLibrary.ambiguityForest, value);
            }
        }
        br.close();
        fr.close();
    }

    private void initAdjSentDic(String dictPath) throws IOException{
        FileReader fr = new FileReader(Resources.getResourceAsFile(dictPath));
        BufferedReader br = new BufferedReader(fr);
        String line;
        adjSentMap = new HashMap<String, Integer>();
        while((line = br.readLine()) != null) {
            String[] arr = line.split(":");
            if(arr.length == 2) {
                String adj = arr[0];
                Float score = Float.parseFloat(arr[1]);
                adjSentMap.put(adj, score.intValue());
            }
        }
        br.close();
        fr.close();
    }

    private void initNounCluster(String dictPath) throws IOException{
        FileReader fr = new FileReader(Resources.getResourceAsFile(dictPath));
        BufferedReader br = new BufferedReader(fr);
        String line;
        nounClusterMap = new HashMap<String, String>();
        while((line = br.readLine()) != null) {
            String[] arr = line.split(",");
            if(arr.length == 2) {
                String[] wordArr = arr[1].split(" ");
                String cluster = arr[0];
                for(String w : wordArr) {
                    nounClusterMap.put(w, cluster);
                }
            }
        }
        br.close();
        fr.close();
    }
}
