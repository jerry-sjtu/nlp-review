package com.dp.nlp.review.udf;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.international.pennchinese.ChineseTreebankLanguagePack;
import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.nlpcn.commons.lang.jianfan.JianFan;

import java.io.*;
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
    private static String SPLIT_REGX = ",|，| |\\.|!|。|！|\\t|\\n|~";
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
            //MyStaticValue.ambiguityLibrary = "library/ambiguity.dic";
            addDict("/library/ambiguity.dic");
        } catch (IOException e) {
            System.out.println("ambiguity dictionary is not loaded!");
        }
        try {
            initAdjSentDic("/adj_sent.dic");
        }  catch (IOException e) {
            System.out.println("adj sentiment dictionary is not loaded!");
        }
        try {
            initNounCluster("/beauty_hair_noun.csv");
        } catch (IOException e) {
            System.out.println("noun cluster is not loaded!");
        }
        this.lp =  LexicalizedParser.loadModel(grammar, options);
    }

    public static void main(String[] args) throws IOException{
        //String sentence = "圣维拉是杨浦区新开的一家一站式婚礼会所，环境是没的说的，特别喜欢草坪婚礼，它家也可以办，菜式的话个人觉得一般般，会考虑吧. 好吃的鱼";
        //String sentence = "宴厅给人的感觉就是大气、华丽、清爽，高度估计有4、5米，在这种地方办婚礼显得有档次，而其余婚礼设施也是相当齐全，他们家还有独有的嫁衣房，婚纱礼服应有尽有，非常人性化，而其露天平台也不错，午后可以供客人休息，风景很漂亮，与老公是婚博会上定下的，因为有优惠，实体会所早已看过，相当满意，所有也没什么好纠结了，婚礼半年后举行，而他们家的销售以及策划也早早的就联系了我们，还是很负责任的，婚礼交由这种会所来打理会给人相当安心的感觉，很棒。";
        //String sentence = "灯光很赞，模特和婚纱也很美。场地不错，工作人员也热情，只是细节上还有进步空间。";
        //String sentence = "服务好，里面环境不错，就是周围环境一般般，但是交通十分方便，期待11月的婚礼";
        //String sentence = "环境不错，布置的也很漂亮，服务员还把我掉了的手机还给我。菜品对得起这个价格。";
        //String sentence = "圣维拉是杨浦区新开的一家一站式婚礼会所，环境是没的说的， ";
        //String sentence = "前几天去洗牙，因为我第一次洗牙，不太适应，医生态度邪恶，机械落后，还禁止我呜咽！之前说好180的，洗完之后变220了";
        //String sentence = "日本发型师比较贵，中国的就便宜点，我觉得他们剪得不错的，价格也还可以。尤其是我第一次从长发剪成短发，就很划算，剪得也好，同学同事都说成熟了。第二次替我剪了个太新潮的发型，我实在不能接受。";

        String sentence = "雖然開在永琦旁邊，但是生意卻比“永”好。住在曹楊附近的朋友應該大多數都知道吧。髮型師都" +
                "香港、廣東的，有個叫“阿華”的髮型師我覺得是最好的，但好像已經不做了。建議不要找一個矮矮、黃頭髮的髮型師，我覺得他的技術不怎麽，個人意見哦~服務員都挺主動熱心的，但是會一直在耳邊嘮叨，希望客戶多做幾個項目。。。。。。";
        ChineseParser parser = new ChineseParser();
        List<String> tList = parser.evaluate(sentence);
        System.out.println(tList);


//        String path1 = "/Users/qiangwang/Downloads/hair.csv";
//        String path2 = "/Users/qiangwang/Downloads/r.csv";
//        int lineNum = 0;
//        ChineseParser parser = new ChineseParser();
//        parser.processFile(path1, path2, lineNum);
    }

    public void processFile(String path1, String path2, int lineNum) throws IOException{
        if(lineNum < 0) {
            return;
        }

        File file1 = new File(path1);
        File file2 = new File(path2);
        BufferedReader br = new BufferedReader(new FileReader(file1));
        BufferedWriter bw = new BufferedWriter(new FileWriter(file2));
        String line;
        int i = 0;
        while((line = br.readLine()) != null) {
            String[] arr = line.split("\01");
            if(arr.length != 2) {
                i++;
                continue;
            }

            String reviewId = arr[0];
            String sentence = arr[1];
            LinkedList<String> tagList = evaluate(sentence);
            StringBuilder sb = new StringBuilder();
            sb.append(reviewId).append("\01").append(sentence).append("\01").append(tagList.toString()).append("\n");
            bw.write(sb.toString());
            i++;
            System.out.println(i);

            if(lineNum > 0 && i > lineNum) {
                break;
            }
        }
        br.close();
        bw.close();
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
//            System.out.println(termList);
            if(hasOpinion(termList) == false) {
                start += c.length();
                continue;
            }

            StringBuilder sb = new StringBuilder();
            Map<String, String> posMap = new HashMap<String, String>();
            for(Term t : termList) {
                sb.append(t.getName()).append(" ");
                posMap.put(t.getName(), t.getNatrue().natureStr);
            }
            LinkedList<String> tmpList = extract(sentence, sb.toString(), start, posMap);
            tagList.addAll(tmpList);

            //update the start index
            start += c.length();
        }
        return tagList;
    }

    private boolean hasOpinion(List<Term> termList) {
        boolean hasNoun = false;
        boolean hasAdj = false;
        for(Term t : termList) {
            if(t.getNatrue().natureStr.equals("a") || t.getNatrue().natureStr.equals("an")) {
                hasAdj = true;
            }
            if(t.getNatrue().natureStr.equals("n")) {
                hasNoun = true;
            }
        }
        return hasAdj && hasNoun;
    }

    private LinkedList<String> extract(String originalSent, String splitSent, int start, Map<String, String> posMap) {
        LinkedList<String> list = new LinkedList<String>();

        TreebankLanguagePack tlp = new ChineseTreebankLanguagePack();
        Tokenizer<? extends HasWord> toke = tlp.getTokenizerFactory().getTokenizer(new StringReader(splitSent));
        List<? extends HasWord> sentList = toke.tokenize();
        Tree parse = lp.apply(sentList);

        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
        for(TypedDependency d : tdl) {
//            System.out.println(d);
            TreeGraphNode dep = d.dep();
            TreeGraphNode gov = d.gov();
            GrammaticalRelation rel = d.reln();
            if(rel.toString().equals("nsubj")
                  &&  ("a".equals(posMap.get(gov.value())) || "an".equals(posMap.get(gov.value())))) {
                int sent = sentiment(dep.value(), gov.value(), 0, 0, 0);
                int type = 1;
                String cluster = nounClusterMap.get(dep.value());
                String target = StringUtils.isBlank(cluster) ? dep.value() : cluster;
                String[] highlight = highlight(originalSent, start, dep.value(), gov.value());
                if(StringUtils.isBlank(highlight[0])) {
                    continue;
                }
                list.add(String.format("%s%s:%s:%s:%s:%s", target, gov.value(), highlight[0], sent, type, highlight[1]));
            }
//            else if(rel.toString().equals("amod") && "a".equals(posMap.get(dep.value())))
//            {
//                int sent = sentiment(dep.value(), gov.value(), 0, 0, 0);
//                int type = 1;
//                String cluster = nounClusterMap.get(dep.value());
//                String target = StringUtils.isBlank(cluster) ? dep.value() : cluster;
//                list.add(String.format("%s%s:%s:%s:%s", target, gov.value(), highlight, sent, type));
//            }
        }
        return list;
    }

    private String[] highlight(String sentence, int start, String word1, String word2) {
        String[] highlight = new String[2];
        if(sentence.contains(word1) == false) {
            word1 = JianFan.j2F(word1);
            word2 = JianFan.j2F(word2);
        }
        int x1 = sentence.indexOf(word1, start);
        int x2 = x1 + word1.length();
        int y1 = sentence.indexOf(word2, start);
        int y2 = y1 + word2.length();
        if(x1 > y1 && y1 > -1) {
            highlight[0] = String.format("%s_%s", y1, x2);
            highlight[1] = sentence.substring(y1, x2);
        }
        else if(x1 < y1 && x1 > -1) {
            highlight[0] = String.format("%s_%s", x1, y2);
            highlight[1] = sentence.substring(x1, y2);
        }
        return highlight;
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
            r = SENTIMENT_POSITIVE;
        }
        else if(score1 < 2 && score2 < 2 && score3 < 2) {
            r = SENTIMENT_NEGATIVE;
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
        originText = originText.replaceAll("[a-zA-Z]+","");
        return originText;
    }

    private void addDict(String dictPath) throws IOException{
//        FileReader fr = new FileReader(Resources.getResourceAsFile(dictPath));
        InputStream in = this.getClass().getResourceAsStream(dictPath);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        while((line = br.readLine()) != null) {
            String[] arr = line.split("( )+");
            if(arr.length == 3) {
                UserDefineLibrary.insertWord(arr[0], arr[1], Integer.parseInt(arr[2]));
            }
        }
        br.close();
        in.close();
    }

    private void initAdjSentDic(String dictPath) throws IOException{
        //FileReader fr = new FileReader(Resources.getResourceAsFile(dictPath));
        InputStream in = this.getClass().getResourceAsStream(dictPath);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
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
        in.close();
    }

    private void initNounCluster(String dictPath) throws IOException{
//        FileReader fr = new FileReader(Resources.getResourceAsFile(dictPath));
        InputStream in = this.getClass().getResourceAsStream(dictPath);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
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
        in.close();
    }
}
