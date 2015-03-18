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

import java.io.*;
import java.net.URL;
import java.util.*;
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

    public ChineseParser() {
//        try {
//            addDict("ambiguity.dic");
//        } catch (IOException e) {
//            System.out.println("ambiguity dictionary is not loaded!");
//        }
        addDict();
        this.lp =  LexicalizedParser.loadModel(grammar, options);
    }

    public static void main(String[] args) {
        //String sentence = "圣维拉是杨浦区新开的一家一站式婚礼会所，环境是没的说的，特别喜欢草坪婚礼，它家也可以办，菜式的话个人觉得一般般，会考虑吧. 好吃的鱼";
        //String sentence = "宴厅给人的感觉就是大气、华丽、清爽，高度估计有4、5米，在这种地方办婚礼显得有档次，而其余婚礼设施也是相当齐全，他们家还有独有的嫁衣房，婚纱礼服应有尽有，非常人性化，而其露天平台也不错，午后可以供客人休息，风景很漂亮，与老公是婚博会上定下的，因为有优惠，实体会所早已看过，相当满意，所有也没什么好纠结了，婚礼半年后举行，而他们家的销售以及策划也早早的就联系了我们，还是很负责任的，婚礼交由这种会所来打理会给人相当安心的感觉，很棒。";
        String sentence = "灯光很赞，模特和婚纱也很美。场地不错，工作人员也热情，只是细节上还有进步空间。";
        //String sentence = "服务好，里面环境不错，就是周围环境一般般，但是交通十分方便，期待11月的婚礼";
        //String sentence = "环境不错，布置的也很漂亮，服务员还把我掉了的手机还给我。菜品对得起这个价格。";
        ChineseParser parser = new ChineseParser();
        List<String> tList = parser.evaluate(sentence);
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
        for(String c : cList) {
            if(StringUtils.isBlank(c)) {
                continue;
            }
            List<Term> termList = ToAnalysis.parse(c);
            StringBuilder sb = new StringBuilder();
            Map<String, String> posMap = new HashMap<String, String>();
            for(Term t : termList) {
                sb.append(t.getName()).append(" ");
//                System.out.println(t.toString());
                posMap.put(t.getName(), t.getNatrue().natureStr);
            }
            System.out.println(sb.toString());
            LinkedList<String> tmpList = extract(sb.toString(), posMap);
            tagList.addAll(tmpList);
        }
        return tagList;
    }

    private LinkedList<String> extract(String sentence, Map<String, String> posMap) {
        TreebankLanguagePack tlp = new ChineseTreebankLanguagePack();
        Tokenizer<? extends HasWord> toke = tlp.getTokenizerFactory().getTokenizer(new StringReader(sentence));
        List<? extends HasWord> sentList = toke.tokenize();
        Tree parse = lp.apply(sentList);
//        parse.pennPrint();

        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
        System.out.println(tdl);

        LinkedList<String> list = new LinkedList<String>();
        for(TypedDependency d : tdl) {
            TreeGraphNode dep = d.dep();
            TreeGraphNode gov = d.gov();
            GrammaticalRelation rel = d.reln();
            if(rel.toString().equals("nsubj") && "a".equals(posMap.get(gov.value()))) {
                list.add(String.format("%s_%s", dep.value(), gov.value()));
            }
            else if(rel.toString().equals("amod") && "a".equals(posMap.get(dep.value())))
            {
                list.add(String.format("%s_%s", dep.value(), gov.value()));
            }
        }
        return list;
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

    private static void addDict() {
        Value value = new Value("没的说的", "没的说的", "a");
        Library.insertWord(UserDefineLibrary.ambiguityForest, value);
        value = new Value("一站式", "一站式", "a");
        Library.insertWord(UserDefineLibrary.ambiguityForest, value);
        value = new Value("会所", "会所", "n");
        Library.insertWord(UserDefineLibrary.ambiguityForest, value);
    }

    private static void addDict(String dictPath) throws IOException{
        FileReader fr = new FileReader(new File(dictPath));
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
    }
}
