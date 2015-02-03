package com.dp.nlp.review.cluster;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.international.pennchinese.ChineseTreebankLanguagePack;

import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by qiangwang on 15/2/2.
 */
public class ChineseParser {

    public static void main(String[] args) {
        String sentence = "老师 穿 着 一件 很 美丽 的 衣服";
        String keyword = "衣服";
        int kwIndex = 0;
        String sentArry[] = sentence.split(" ");
        for (int i = 0; i < sentArry.length; i++) {
            if (keyword.equals(sentArry[i])) {
                kwIndex = i;
                break;
            }
        }
        extraDepWord(sentence, keyword);
    }

    public static void extraDepWord(String sentence, String keyword) {
        String grammar = "chinesePCFG.ser.gz";
        String[] options = { "-maxLength", "80"};
        LexicalizedParser lp = LexicalizedParser.loadModel(grammar, options);

        TreebankLanguagePack tlp = new ChineseTreebankLanguagePack();
        Tokenizer<? extends HasWord> toke = tlp.getTokenizerFactory().getTokenizer(new StringReader(sentence));
        List<? extends HasWord> sentList = toke.tokenize();
        Tree parse = lp.apply(sentList);
        parse.pennPrint();

        List<Tree> leaves = parse.getLeaves();
        Iterator<Tree> it = leaves.iterator();
        while (it.hasNext()) {
            Tree leaf = it.next();
            if (leaf.nodeString().trim().equals(keyword)) {
                Tree start = leaf;
                start = start.parent(parse);
                String tag = start.value().toString().trim();
                boolean extraedflg = false;
                // 如果当前节点的父节点是NN，则遍历该父节点的父节点的兄弟节点
                if (tag.equals("NN") || tag.equals("VA")) {
                    Tree root = start.parent(parse).parent(parse);
                    myScanTree(root, "NN", keyword);
//                    for (int i = 0; i < parse.depth(); i++) {
//                        start = start.parent(parse);
//                        if (start.value().toString().trim().equals("ROOT") || extraedflg == true) {
//                            break;
//                        }
//                        List<Tree> bros = start.siblings(parse);
//                        if (bros != null) {
//                            Iterator<Tree> it1 = bros.iterator();
//                            while (it1.hasNext()) {
//                                Tree bro = it1.next();
//                                extraedflg = IteratorTree(bro, tag);
//                                if (extraedflg) {
//                                    break;
//                                }
//                            }
//                        }
                }
            }
        }
    }

    public static void myScanTree(Tree root, String tagKey, String key) {
        Queue<Tree> queue = new LinkedList<Tree>();
        queue.add(root);
        while (queue.size() > 0) {
            Tree tmp = queue.poll();
            String tagDep = tmp.value().toString().trim();
            if ((tagKey.equals("NN") && tagDep.equals("VA")) || (tagKey.equals("NN") && tagDep.equals("JJ"))
                    || (tagKey.equals("VA") && tagDep.equals("AD"))) {
                String entity = key + "\t" + tmp.getChild(0).value().toString();
                System.out.println(entity);
            }
            for(Tree child : tmp.getChildrenAsList()) {
                queue.add(child);
            }
        }
    }

    public static boolean IteratorTree(Tree bro, String tagKey) {
        List<Tree> ends = bro.getChildrenAsList();
        Iterator<Tree> it = ends.iterator();

        while (it.hasNext()) {
            Tree end = it.next();
            String tagDep = end.value().toString().trim();
            if ((tagKey.equals("NN") && tagDep.equals("VA"))
                    || (tagKey.equals("NN") && tagDep.equals("JJ"))
                    || (tagKey.equals("VA") && tagDep.equals("AD"))) {
                Tree depTree = end.getChild(0);
                System.out.println(depTree.value().toString());
                return true;
            } else if (IteratorTree(end, tagKey)) {
                return true;
            }
        }
        return false;
    }

}
