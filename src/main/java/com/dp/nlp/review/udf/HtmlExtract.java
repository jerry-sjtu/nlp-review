package com.dp.nlp.review.udf;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;

import java.io.*;

/**
 * Created by qiangwang on 15/2/4.
 */
public class HtmlExtract extends UDF {

    public static void main(String[] args) throws IOException{
        if(args.length != 2) {
            System.out.println("the length of arguments is not equal to 2!");
        }
        HtmlExtract obj = new HtmlExtract();
        obj.convFile(args[0], args[1]);
    }

    public String evaluate(String str) {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        InputStream inputStream = new ByteArrayInputStream(str.getBytes());
        ParseContext pcontext = new ParseContext();

        String result = "";
        try {
            HtmlParser htmlparser = new HtmlParser();
            htmlparser.parse(inputStream, handler, metadata, pcontext);
            result = handler.toString();
        }
        catch (Exception e) {
            result = "";
        }
        return result;
    }

    public void convFile(String path1, String path2) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(new File(path1)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path2)));

        String line;
        while ((line = reader.readLine()) != null) {
            line = evaluate(line).trim();
            if(!StringUtils.isBlank(line)) {
                writer.write(line);
                writer.write("\n");
            }
        }

        reader.close();
        writer.close();
    }

    public void test() {
        String html = "<b><span style=\"\">最近和老公一起来玩了一趟，这里毕竟在郊区地点偏僻，<a href=\"baidu\"></a>不过正因为这样才有大片土地开发使用，温泉度假区里显得十分宽敞舒适，远离喧闹拥挤的市中心来到这里安静开阔的地方首先就觉得精神放松了。建筑外观体现汉唐风格，古典气派。步入温泉中心大堂又体现出欧洲皇室古典风格，金碧辉煌的感觉。</span>  </b>";
        String result = evaluate(html);
        System.out.println(result);
    }

}
