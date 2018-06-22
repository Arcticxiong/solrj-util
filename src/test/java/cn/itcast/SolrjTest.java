package cn.itcast;

import cn.itcast.model.Product;
import cn.itcast.util.BeanData;
import cn.itcast.util.SolrConvertUtil;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;


public class SolrjTest {


    private HttpSolrClient httpSolrClient;

    @Before
    public void init(){
        //solr的http请求地址
        String baseURL = "http://localhost:8080/solr/collection1";
        httpSolrClient = new HttpSolrClient(baseURL);
    }

    /***
     * 增加索引
     * @throws Exception
     */
    @Test
    public void testSearch() throws  Exception{
        //创建Query查询，查询所有
        SolrQuery solrQuery = new SolrQuery("title:张");

        //开启高亮
        SolrConvertUtil.setHighlighting(solrQuery,Product.class);

        //执行查询
        QueryResponse response = httpSolrClient.query(solrQuery);

       /* Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();

        SolrDocumentList results = response.getResults();*/

        //List<Product> products = SolrConvertUtil.document2Bean(Product.class, results);
        //for (Product product : products) {
        //    System.out.println(product);
        //}


        /*BeanData<Product> beanData = SolrConvertUtil.response2BeanData(response, Product.class);
        System.out.println(beanData.getTotal());
        for (Product product : beanData.getList()) {
            System.out.println(product);
        }*/


        List<Product> products = SolrConvertUtil.response2Bean(response, Product.class,true);
        for (Product product : products) {
            System.out.println(product);
        }
    }


}
