package cn.itcast.util;

import cn.itcast.model.Product;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SolrConvertUtil {


    /****
     * QueryResponse转对应JavaBean
     * 同时获取高亮数据
     * @param response
     * @param clazz
     * @param ishl
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> response2Bean(QueryResponse response,Class clazz,boolean ishl) throws Exception {
        //获取结果数据
        SolrDocumentList solrDocumentList = response.getResults();

        if(ishl){
            //将非高亮的SolrDocument转成高亮SolrDocument
            doc2HighlightDoc(solrDocumentList,getHighlightingField(clazz),response.getHighlighting());
        }
        //获取数据
        return  document2Bean(clazz, response.getResults());
    }

    /****
     * QueryResponse转对应JavaBean
     * @param response
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> response2Bean(QueryResponse response,Class clazz) throws Exception {
        return document2Bean(clazz,response.getResults());
    }

    /***
     * 返回数据
     * 非高亮数据
     * @param response
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> BeanData<T> response2BeanData(QueryResponse response,Class clazz) throws Exception {
        return response2BeanData(response,clazz,false);
    }
    /***
     * 返回总记录数
     * 返回集合数据
     * 可以携带高亮数据
     * @param response
     * @param clazz
     * @return
     * @throws Exception
     */
    public static <T> BeanData<T> response2BeanData(QueryResponse response,Class clazz,boolean ishl) throws Exception {
        BeanData<T> beanData = new BeanData<T>();

        //获得SolrDocumentList
        SolrDocumentList solrDocumentList = response.getResults();

        //高亮获取
        if(ishl){
            //将非高亮的SolrDocument转成高亮SolrDocument
            doc2HighlightDoc(solrDocumentList,getHighlightingField(clazz),response.getHighlighting());
        }

        //设置总记录数
        beanData.setTotal(solrDocumentList.getNumFound());
        //设置搜索结果集
        beanData.setList((List<T>) document2Bean(clazz, solrDocumentList));
        return  beanData;
    }

    /****
     * 将多个Bean同时转成SolrInputDcoument对象
     * @param
     * @return
     * @throws Exception
     */
    public static List<SolrInputDocument> bean2Document(Object... objects) throws Exception {
        //定义一个集合存放所有对象
        List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();

        //循环转换
        for (Object object : objects) {
            //实现转换,将转换对象加入到集合中
            documents.add(bean2Document(object));
        }
        return  documents;
    }


    /*****
     * 实现JavaBean转SolrInputDocument对象
     * @param object
     * @return
     * @throws Exception
     */
    public static SolrInputDocument bean2Document(Object object) throws Exception {
        //Class
        Class clazz = object.getClass();

        //定义一个Document
        SolrInputDocument document = new SolrInputDocument();

        //获取所有属性
        Field[] declaredFields = clazz.getDeclaredFields();

        //循环有SolrField注解的属性
        for (Field field : declaredFields) {

            //属性注解
            SolrField fieldAnnotation = field.getAnnotation(SolrField.class);

            //实现索引域和JavaBean属性映射转换
            if(fieldAnnotation!=null){
                //设置默认域的名字为属性名字
                String fieldName = field.getName();

                //获取设置的field名字
                if(fieldAnnotation.name()!=null && !"".equals(fieldAnnotation.name())){
                    fieldName = fieldAnnotation.name();
                }

                //暴力破解
                field.setAccessible(true);
                //获取该属性值
                Object value = field.get(object);

                //给而对应域设置值
                document.addField(fieldName,value);
            }
        }

        return  document;
    }


    /****
     * SolrDocument批量转JavaBean
     * @param document
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> document2Bean(Class clazz,SolrDocument... document) throws Exception{
        //定义集合进行存储
        List<T> list = new ArrayList<T>();

        //循环实现换换
        for (SolrDocument solrDocument : document) {
            list.add((T) document2Bean(solrDocument,clazz));
        }
        return list;
    }

    /***
     * SolrDocumentList批量转JavaBean
     * @param clazz
     * @param document
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> document2Bean(Class clazz,SolrDocumentList document) throws Exception{
        //定义集合进行存储
        List<T> list = new ArrayList<T>();

        //循环实现换换
        for (SolrDocument solrDocument : document) {
            list.add((T) document2Bean(solrDocument,clazz));
        }
        return list;
    }


    /****
     * SolrInputDocument转JavaBean
     * @param document
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T document2Bean(SolrDocument document,Class clazz) throws Exception{
        //获取Class对应实例
        Object instance = clazz.newInstance();

        //获取所有的列
        Field[] declaredFields = clazz.getDeclaredFields();

        //循环所有属性
        for (Field field : declaredFields) {
            //域的名字，默认和属性名一致
            String fieldName = field.getName();

            //获取注解，如果存在，就替换默认域名字
            SolrField fieldAnnotation = field.getAnnotation(SolrField.class);
            if(fieldAnnotation!=null){
                //如果指定域为非存储数据，则不需要对JavaBean赋值
                if(!fieldAnnotation.storead()){
                    continue;
                }

                //获取指定域
                fieldName = fieldAnnotation.name();
            }

            //获取对应域的值
            Object result = document.get(fieldName);

            if(result!=null && !"".equals(result)){
                //暴力破解
                field.setAccessible(true);

                //给对应属性赋值,通过BeanUtils工具将对应值转换成对应类型的值
                field.set(instance, ConvertUtils.convert(result,field.getType()));
            }
        }
        return (T) instance;
    }


    /***
     * 批量将非高亮SolrDocument转高亮SolrDocument
     * @param documents
     * @param hlfields
     * @param highlighting
     * @throws Exception
     */
    public static void doc2HighlightDoc(SolrDocumentList documents, List<Field> hlfields, Map<String, Map<String, List<String>>> highlighting) throws Exception{
        //循环将非高亮SolrDocument转成高亮SolrDocument
        for (SolrDocument document : documents) {
            doc2HighlightDoc(document, hlfields, highlighting);
        }
    }

    /***
     * 批量将非高亮SolrDocument转高亮SolrDocument
     * @param documents
     * @param hlfields
     * @param highlighting
     * @throws Exception
     */
    public static void doc2HighlightDoc(List<SolrDocument> documents, List<Field> hlfields, Map<String, Map<String, List<String>>> highlighting) throws Exception{
        //循环将非高亮SolrDocument转成高亮SolrDocument
        for (SolrDocument document : documents) {
            doc2HighlightDoc(document, hlfields, highlighting);
        }
    }

    /*****
     * SolrDocument转高亮SolrDocument
     * @param document
     * @param hlfields
     * @param highlighting
     * @return
     * @throws Exception
     */
    public static void doc2HighlightDoc(SolrDocument document, List<Field> hlfields, Map<String, Map<String, List<String>>> highlighting) throws Exception{
        if(hlfields!=null && hlfields.size()>0){
            //获取ID
            String id = document.get("id").toString();

            //获取高亮数据
            Map<String, List<String>> stringListMap = highlighting.get(id);

            if(stringListMap!=null){
                //循环高亮域
                for (Field hlfield : hlfields) {
                    //获取域的名字
                    String fieldName = fieldName(hlfield);

                    //获取对应高亮数据
                    List<String> hlstrings = stringListMap.get(fieldName);
                    if(hlstrings!=null && hlstrings.size()>0){
                        //定义一个字符串，存储高亮数据
                        String result = "";
                        for (String hlstring : hlstrings) {
                            result+=hlstring;
                        }
                        //将非高亮数据替换成高亮数据
                        document.setField(fieldName,result);
                    }
                }
            }
        }
    }



    /****
     * 获取高亮域
     * @param clazz
     * @return
     */
    public static List<Field> getHighlightingField(Class clazz){
        //定义一个集合存储所有高亮域
        List<Field> hlfields = new ArrayList<Field>();

        //获取所有需要高亮显示的域
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            //获取属性上的注解
            SolrField fieldAnnotation = field.getAnnotation(SolrField.class);

            //判断当前对象是否为高亮对象
            if(fieldAnnotation.ishl()){
                hlfields.add(field);
            }
        }
        return hlfields;
    }


    /****
     * 高亮设置
     * @param solrQuery
     * @param clazz
     */
    public static void setHighlighting(SolrQuery solrQuery,Class clazz){
        //获取所有高亮域
        List<Field> highlightingFields = getHighlightingField(clazz);

        if(highlightingFields.size()>0){
            //获取前缀和后缀
            Field field = highlightingFields.get(0);
            SolrField annotation = field.getAnnotation(SolrField.class);
            String prefix = annotation.prefix();
            String suffix = annotation.suffix();

            //开启高亮
            solrQuery.setHighlight(true);

            //设置前缀
            solrQuery.setHighlightSimplePre(prefix);
            //设置后缀
            solrQuery.setHighlightSimplePost(suffix);

            //设置高亮域
            for (Field highlightingField : highlightingFields) {
                //获取域的名字
                String fieldName = fieldName(highlightingField);

                //添加高亮域
                solrQuery.addHighlightField(fieldName);
            }
        }
    }


    /****
     * 获取属性对应域的名字
     * @param field
     * @return
     */
    public static String fieldName(Field field){
        //获取域的名字
        SolrField annotation = field.getAnnotation(SolrField.class);
        //默认为属性名
        String fieldName = field.getName();

        //获取注解定义的域名字
        if(annotation!=null && !"".equals(annotation.name())){
            fieldName = annotation.name();
        }

        return fieldName;
    }

}