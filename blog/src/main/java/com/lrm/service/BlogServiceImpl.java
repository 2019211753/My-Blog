package com.lrm.service;

import com.lrm.NotFoundException;
import com.lrm.dao.BlogRepository;
import com.lrm.po.Blog;
import com.lrm.po.Type;
import com.lrm.util.MarkdownUtils;
import com.lrm.util.MyBeanUtils;
import com.lrm.vo.BlogQuery;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.*;

//记得添加这个注解，否则下面的autowired会报错
@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Override
    public Blog getBlog(Long id) {
        return blogRepository.findOne(id);
    }

    @Override
    public Page<Blog> listBlog(String query, Pageable pageable) {
        return blogRepository.findByQuery(query, pageable);
    }

    @Override
    //找到符合条件的Blog 然后controller中返回了符合条件的page
    public Page<Blog> listBlog(Pageable pageable, BlogQuery blog)
    {
        return blogRepository.findAll(new Specification<Blog>()
        {
            @Override
            public Predicate toPredicate(Root<Blog> root, CriteriaQuery<?> cq, CriteriaBuilder cb)
            {
                List<Predicate> predicates = new ArrayList<>();
                //""是空字节，而不是null 下面这种写法 而不是blog.getTitle().equals()是防止它是null 会造成空指针异常
                if( blog.getTitle() !=null && !"".equals(blog.getTitle()) )
                {
                    predicates.add(cb.like(root.get("title"), "%"+blog.getTitle()+"%"));
                }
                if( blog.getTypeId() !=null)
                {
                    predicates.add(cb.equal(root.<Type>get("type").get("id"), blog.getTypeId()));
                }
                if( blog.isRecommend() )
                {
                    predicates.add(cb.equal(root.<Boolean>get("recommend"), blog.isRecommend()));
                }
                //参数是0么
                cq.where(predicates.toArray(new Predicate[0]));
                return null;
            }
        }, pageable);
    }

    @Override
    public Long countBlog() {
        return blogRepository.count();
    }

    @Override
    public List<Blog> listRecommendBlogTop(Integer size) {
        Sort sort = new Sort(Sort.Direction.DESC, "updateTime");
        Pageable pageable = new PageRequest(0,size, sort);
        return blogRepository.findTop(pageable);
    }

    @Override
    public Map<String, List<Blog>> archivesBlog() {
        List<String> years = blogRepository.findGroupYear();
        Map<String, List<Blog>> map = new HashMap<>();
        for(String year : years)
        {
         map.put(year, blogRepository.findByYear(year));
        }
        return map;
    }

    @Transactional
    @Override
    public Blog getAndConvert(Long id) {
        Blog blog = blogRepository.findOne(id);
        if (blog == null) {
            throw new NotFoundException("该博客不存在");
        }
        Blog b = new Blog();
        BeanUtils.copyProperties(blog, b);
        String content = b.getContent();
        b.setContent(MarkdownUtils.markdownToHtmlExtensions(content));
        blogRepository.updateViews(id);
        return b;
    }


    @Override
    public Page<Blog> listBlog(Long tagId, Pageable pageable) {
        return blogRepository.findAll(new Specification<Blog>() {
            @Override
            public Predicate toPredicate(Root<Blog> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                Join join = root.join("tags");
                return cb.equal(join.get("id"), tagId);
            }
        }, pageable);
    }

    @Override
    public Page<Blog> listBlog(Pageable pageable) {
        return blogRepository.findAll(pageable);
    }


    //一个数据库事务由一条或者多条sql语句构成，它们形成一个逻辑的工作单元。这些sql语句要么全部执行成功，要么全部执行失败 。
    @Transactional
    @Override
    //注意 在没更改之前 编辑和新增用的都是saveBlog 结果是编辑之后 原本的createTime和view变成null了
    //原因：在blogs-input中没有createTime和view的隐含域 所以传到controller里的blog中是空的
    //此外 如果无隐含域 即使你先设了creat和view的值 传递到impl中的save语句的 blog对象中这俩也是空的
    //如果是新增 那么在impl的save语句中 id=null (Long类型的)条件下 有对它和updateTime的赋值语句
    //但是如果是编辑 id不是null 那么没有语句创建createTime和view 而且原有的也没了 所以就null了
    //所以整一个updateBlog 通过id找到原来的blog 这个blog有createTim和view的值 然后更新一下blog 这样就不是null了！
    public Blog saveBlog(Blog blog) {
        //对对象做取值操作的时候要先初始化，否则会nullpointException 并且new Date()不是null 它是占用空间的
        if (blog.getId() == null)
        {
            //时间是date对象所以新增的时候需要初始化 并且不是在input页面显式出现的
            blog.setCreateTime(new Date());
            blog.setUpdateTime(new Date());
            blog.setView(0);
        } else {
            blog.setUpdateTime(new Date());
        }
        return blogRepository.save(blog);
    }

    @Transactional
    @Override
    public Blog updateBlog(Blog blog) {
        //这个b和blog不是一个对象了！！！blog里有新内容，没有view。b没新内容，但是有view，他俩id一样。所以说要不要这么做 主要看前端有没有隐含域
        //然后把blog的新内容赋值给原来的b 并且更新时间
        Blog b = blogRepository.findOne(blog.getId());
        if(b ==null)
        {
            throw new NotFoundException("该博客不存在");
        }
        BeanUtils.copyProperties(blog, b, MyBeanUtils.getNullPropertyNames(blog));
        b.setUpdateTime(new Date());
        return blogRepository.save(b);
        }

    @Override
    public void deleteBlog(Long id) {
        blogRepository.delete(id);
    }
}
