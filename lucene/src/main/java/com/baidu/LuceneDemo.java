package com.baidu;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
// import org.apache.lucene.queryparser.surround.parser.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;

public class LuceneDemo {

    @Test
    public void test(){
        try {
            Directory directory = FSDirectory.open(new File("D:\\luceneIndex"));
            Analyzer ikAnalyzer = new IKAnalyzer();
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3, ikAnalyzer);
            // 创建索引库写入对象
            IndexWriter indexWriter = new IndexWriter(directory,indexWriterConfig);

            File file = new File("D:\\searchsource");
            File[] files = file.listFiles();
            for (File f : files) {
                System.out.println("文件的名字是：-------------"+f.getName());
                System.out.println("文件的路径是：-------------"+f.getPath());
                System.out.println("文件的大小是：-------------"+FileUtils.sizeOf(f));
                System.out.println("文件的内容是：-------------"+FileUtils.readFileToString(f));
                Document document = new Document();
                document.add(new TextField("fileName",f.getName(),Field.Store.YES));
                document.add(new TextField("fileContent",FileUtils.readFileToString(f),Field.Store.YES));
                document.add(new LongField("fileSize",FileUtils.sizeOf(f),Field.Store.YES));
                document.add(new StringField("filePath",f.getPath(),Field.Store.YES));
                indexWriter.addDocument(document);
            }
            indexWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 查询全部
    @Test
    public void readAll(){
        try {
            // 创建读取索引库的对象
            Directory directory = FSDirectory.open(new File("D:\\luceneIndex"));
            IndexReader open = DirectoryReader.open(directory);
            // 创建查询全部的query对象
            Query query = new MatchAllDocsQuery();
            // 创建查询索引库对象
            IndexSearcher indexSearcher = new IndexSearcher(open);
            // 20表示显示多少条数
            /*
             * 返回值为TopDocs
             * totalHits  总共命中数量
             * ScoreDocs数组  包含文档id和文档得分的数值
             * */
            TopDocs search = indexSearcher.search(query, 20);

            System.out.println("总数是"+search.totalHits);
            ScoreDoc[] scoreDocs = search.scoreDocs;
            for (ScoreDoc s : scoreDocs) {
                System.out.println("文件的ID为：===="+s.doc);
                System.out.println("文件的分数是：-----"+s.score);
                Document doc = indexSearcher.doc(s.doc);
                System.out.println("文件的名称是：  ---"+doc.getField("fileName"));
                System.out.println("文件的路径是：  ---"+doc.getField("filePath"));
                System.out.println("文件的内容是：  ---"+doc.getField("fileContent"));
                System.out.println("文件的大小是：  ---"+doc.getField("fileSize"));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
     * 在域字段查询
     */
    @Test
    public void query(){
        try {
            String str = "传智播客是一个优秀的名词解释";
            // 创建分词解析对象
            // filename表示只在这一字段查询
            QueryParser fileName = new QueryParser("fileContent", new IKAnalyzer());
            // 把关键字解析成字条（搜索的最小单位）
            Query parse = fileName.parse(str);
            d(parse);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

  /*  在多个域中查询*/
    @Test
    public void queryMany(){
        String str = "传智播客是一个优秀的解释";
        String [] s = {"fileName","fileContent"};
        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(s,new IKAnalyzer());
        try {
            Query parse = multiFieldQueryParser.parse(str);
            d(parse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void d(Query query){
        try {
            Directory directory = FSDirectory.open(new File("D:\\luceneIndex"));
            IndexReader open = DirectoryReader.open(directory);

            IndexSearcher indexSearcher = new IndexSearcher(open);

            TopDocs search = indexSearcher.search(query, 20);

            System.out.println("总数是"+search.totalHits);
            ScoreDoc[] scoreDocs = search.scoreDocs;
            for (ScoreDoc s : scoreDocs) {
                System.out.println("文件的ID为：===="+s.doc);
                System.out.println("文件的分数是：-----"+s.score);
                Document doc = indexSearcher.doc(s.doc);
                System.out.println("文件的名称是：  ---"+doc.getField("fileName"));
                System.out.println("文件的路径是：  ---"+doc.getField("filePath"));
                System.out.println("文件的内容是：  ---"+doc.getField("fileContent"));
                System.out.println("文件的大小是：  ---"+doc.getField("fileSize"));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 更新索引库
    @Test
    public void uodate(){
        try {
            Directory directory = FSDirectory.open(new File("D:\\luceneIndex"));
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3, new IKAnalyzer());
           // 创建索引库写入流对象
            IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
            Document document = new Document();
            document.add(new TextField("fileName","数据库索引的更新",Field.Store.YES));
            // 更新
            indexWriter.updateDocument(new Term("fileName","spring"),document);
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 删除一个Document对象
    @Test
    public void delete(){
        try {
            Directory directory = FSDirectory.open(new File("D:\\luceneIndex"));
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3, new IKAnalyzer());
            IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
            indexWriter.deleteDocuments(new Term("fileName","不明"));
            // indexWriter.deleteAll();
            indexWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 按照文件大小查询
    @Test
    public void longQuery(){
        NumericRangeQuery<Long> fileSize = NumericRangeQuery.newLongRange("fileSize", 10l, 50l, true, true);
        d(fileSize);
    }

    // 组合查询
    @Test
    public void zuhe(){
        try {
            BooleanQuery b = new BooleanQuery();
            Query t = new TermQuery(new Term("fileName", "传智播客"));
            Query t2 = new TermQuery(new Term("fileName", "不明"));
            b.add(t,BooleanClause.Occur.MUST);
            b.add(t2,BooleanClause.Occur.MUST);
            d(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 根据词条查询(不分词)
    @Test
    public void one(){
        TermQuery termQuery = new TermQuery(new Term("fileName", "传智播客"));
        d(termQuery);
    }

}
