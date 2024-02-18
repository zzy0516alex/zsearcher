package com.Z.NovelReader.Processors;

import com.Z.NovelReader.Utils.StringUtils;
import com.Z.NovelReader.Objects.beans.SearchQuery;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class NovelSearchProcessor {

    /**
     *
     * @param rawURL 数据库查询结果
     * @param key 用户输入
     * @return 完整搜索方式
     * @throws UnsupportedEncodingException charset错误
     */
    public static SearchQuery getCompleteSearchUrl(SearchQuery rawURL, String key) throws UnsupportedEncodingException {
        SearchQuery completedURL=new SearchQuery();
        String temp = StringUtils.completeUrl(rawURL.getSearch_url(), rawURL.getBook_source_url());
        String[] s=temp.split(",\\{");
        if (s.length>1){
            SearchQuery search_detail=new Gson().fromJson("{"+s[1],SearchQuery.class);
            if (search_detail.hasCharset())completedURL.setCharset(search_detail.getCharset());
            if (search_detail.hasMethod()){
                completedURL.setMethod(search_detail.getMethod());
                completedURL.setBody(search_detail.getBody());
            }
        }
        String encoded_key= URLEncoder.encode(key,completedURL.getCharset());
        temp=s[0].replace("{{page}}","0").replace("{{key}}",encoded_key);
        if(completedURL.hasMethod()) completedURL.setBody(completedURL.getBody().replace("{{key}}",key));//post方法会自动编码request内容，无需再次编码
        completedURL.setSearch_url(temp);
        completedURL.setId(rawURL.getId());
        completedURL.setBook_source_url(rawURL.getBook_source_url());
        return completedURL;
    }


}
