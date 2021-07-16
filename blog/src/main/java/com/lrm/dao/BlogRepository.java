package com.lrm.dao;

import com.lrm.po.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BlogRepository extends JpaRepository<Blog,Long>, JpaSpecificationExecutor<Blog> {
    @Query("select b from Blog b where b.recommend = true")
    List<Blog> findTop(Pageable pageable);

    @Query("select b from Blog  b where b.title like ?1 or b.content like ?1")
    Page<Blog> findByQuery(String query, Pageable pageable);

    @Transactional
    @Modifying
    @Query("update Blog b set b.view = b.view+1 where b.id = ?1")
    int updateViews(Long id);

    /**
     * 将所有博客按时间的年分割
     * @return  顺序返回年份List集合
     */
    @Query("select function('date_format', b.createTime, '%Y') as year " +
            "from Blog b order by year desc ")
    List<String> findGroupYear();


    /**
     * 查询该年份下发布过博客的所有月份
     * 注意sql语句中不能使用%M 因为jpa似乎不能对%M得到的英文的月份进行排序
     * @param year 需要查询的年份
     * @return 月份的List集合
     */
    @Query("select function('date_format', b.createTime, '%c') as month " +
            "from Blog b where function('date_format', b.createTime, '%Y') = ?1 " +
            " order by month desc ")
    List<String> findGroupMonthByYear(String year);


    /**
     * 按月份查询博客
     * @param year 年份
     * @param month 月份
     * @return 博客的List集合
     */
    @Query("select b from Blog  b where function('date_format', b.createTime, '%Y') = ?1 and function('date_format', b.createTime, '%c') =?2 order by b desc")
    List<Blog> findByYearAndMonth(String year, String month);

}
