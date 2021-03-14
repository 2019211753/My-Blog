package com.lrm.service;

import com.lrm.po.Blog;
import com.lrm.vo.BlogQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface BlogService {
    Blog getBlog(Long id);

    Page<Blog> listBlog(Pageable pageable, BlogQuery blog);

    Page<Blog> listBlog(String query, Pageable pageable);

    Page<Blog> listBlog(Pageable pageable);

    Page<Blog> listBlog(Long tagId, Pageable pageable);

    Blog getAndConvert(Long id);

    Blog saveBlog(Blog blog);

    List<Blog> listRecommendBlogTop(Integer size);

    Map<String, Map<String, List<Blog>>> archivesBlog();

    Blog updateBlog(Blog blog);

    Long countBlog();

    void deleteBlog(Long id);

}
