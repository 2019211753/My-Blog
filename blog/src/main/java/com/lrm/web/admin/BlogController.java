package com.lrm.web.admin;

import com.lrm.po.Blog;
import com.lrm.po.User;
import com.lrm.service.BlogService;
import com.lrm.service.TagService;
import com.lrm.service.TypeService;
import com.lrm.vo.BlogQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class BlogController
{
    private static final String INPUT = "admin/blogs-input";
    private static final String LIST = "admin/blogs";
    private static final String REDIRECT_LIST = "redirect:/admin/blogs";

    @Autowired
    private TagService tagService;

    @Autowired
    private BlogService blogService;

    @Autowired
    private TypeService typeService;
    //翻页
    @GetMapping("/blogs")
    //因为有一堆数据，所以查询条件封装成BlogQuery了
    public String  blogs(@PageableDefault(size = 6, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                         BlogQuery blog, Model model)
    {
        model.addAttribute("types", typeService.listType());
        model.addAttribute("page", blogService.listBlog(pageable, blog));
        return LIST;
    }

    //搜索
    @PostMapping("/blogs/search")
    public String search(@PageableDefault(size = 6, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                          BlogQuery blog, Model model)
    {
        model.addAttribute("page", blogService.listBlog(pageable, blog));
        return "admin/blogs :: blogList";
    }

    private  void setTypeAndTag(Model model)
    {
        model.addAttribute("types", typeService.listType());
        model.addAttribute("tags", tagService.listTag());
    }

    //有初始化的作用 所有属性都是null
    @GetMapping("/blogs/input")
    public String input(Model model)
    {
        setTypeAndTag(model);
        //id自增1，传递到前端了
        model.addAttribute("blog", new Blog());
        return INPUT;
    }

    @GetMapping("/blogs/{id}/input")
    public String editInput(@PathVariable Long id, Model model)
    {
        setTypeAndTag(model);
        Blog blog= blogService.getBlog(id);
        blog.init();
        model.addAttribute("blog", blog);
        return INPUT;
    }

    @PostMapping("/blogs")
    public String post(Blog blog, RedirectAttributes attributes, HttpSession session)
    {
        blog.setUser((User)session.getAttribute("user"));
//      传入blog对象了根据blog获取type，为什么不直接赋过去，还要得到id，再得到type?
//      我觉得要用service就是因为与数据库挂钩，不然可能得不到这个映射的Type?
        blog.setType(typeService.getType(blog.getType().getId()));
        blog.setTags(tagService.listTag(blog.getTagIds()));
        //保存重复属性会覆盖原有的 没有被覆盖的保持
        Blog b;
        //注意 在没更改之前 编辑和新增用的都是saveBlog 结果是编辑之后 原本的createTime和view变成null了
        //原因：在blogs-input中没有createTime和view的隐含域 所以传到controller里的blog中是空的
        //此外 如果无隐含域 即使你先设了creat和view的值 传递到impl中的save语句的 blog对象中这俩也是空的
        //如果是新增 那么在impl的save语句中 id=null (Long类型的)条件下 有对它和updateTime的赋值语句
        //但是如果是编辑 id不是null 那么没有语句创建createTime和view 而且原有的也没了 所以就null了
        //所以整一个updateBlog 通过id找到原来的blog 这个blog有createTim和view的值 然后更新一下blog 这样就不是null了！
        if(blog.getId() == null)
        {
            b = blogService.saveBlog(blog);
        } else {
            b = blogService.updateBlog(blog.getId(), blog);
        }

        if (b == null)
        {
            attributes.addFlashAttribute("message", "操作失败");
        } else {
            attributes.addFlashAttribute("message", "操作成功");
        }
        return  REDIRECT_LIST;
    }

    @GetMapping("/blogs/{id}/delete")
    public String delete(@PathVariable Long id,RedirectAttributes attributes)
    {
        blogService.deleteBlog(id);
        attributes.addFlashAttribute("message","删除成功");
        return  REDIRECT_LIST;
    }
}
