package com.lrm.web.admin;

import com.lrm.po.Type;
import com.lrm.service.TypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/admin")
public class TypeController
{
    @Autowired
    private TypeService typeService;

    @GetMapping("/types")
    //pageable会根据前端封装好的数据构建 是springboot封装好了的 types中href到这个GetMapping方法下 传入pageable和model参数进行页数跳转
    //用于href="..."那块的上下翻页配合GetMapping传参(page) 返回page 返回page到前端 并th:...显示
    public String list(@PageableDefault(size =6, sort = {"id"},direction = Sort.Direction.DESC)
                                   Pageable pageable, Model model)
    {
        model.addAttribute("page", typeService.listType(pageable));
        return "admin/types";
    }

    @GetMapping("/types/input")
    public String input(Model model)
    {
        //传入一个type对象
        model.addAttribute("type", new Type());
        return "admin/types-input";
    }

    @GetMapping("/types/{id}/input")
    public String editInput(@PathVariable Long id, Model model)
    {
        //向前端传递一个对象
        //GetMapping从url获取值
        model.addAttribute("type", typeService.getType(id));
        return "admin/types-input";
    }

    @PostMapping("/types")
    //这里封装成了Type对象是因为要 "上传" 到数据库 且在TypeService中定义的方法也是以Type作为参数而不是字符串
    //@Valid表示要校验
    public String post(@Valid Type type, BindingResult result, RedirectAttributes attributes)
    {
        //校验是否有重复分类
        Type type1 = typeService.getTypeByName(type.getName());
        if(type1 != null)
        {
            //name 需要和Type中的name变量名一样
            result.rejectValue("name","nameError","不能添加重复的分类");
        }

        if (result.hasErrors())
        {
            return "admin/types-input";
        }

        //上传了一个Type类型的数据到数据库
        Type t=typeService.saveType(type);
        if ( t == null)
        {
            attributes.addFlashAttribute("message", "新增失败");
        } else {
            attributes.addFlashAttribute("message", "新增成功");
        }
        return "redirect:/admin/types";
    }

    @PostMapping("/types/{id}")
    //这里封装成了Type对象是因为要 "上传" 到数据库 且在TypeService中定义的方法也是以Type作为参数而不是字符串
    //@Valid表示要校验
    public String post(@Valid Type type, BindingResult result, @PathVariable Long id, RedirectAttributes attributes)
    {
        //校验是否有重复分类
        Type type1 = typeService.getTypeByName(type.getName());
        if(type1 != null)
        {
            //name 需要和Type中的name变量名一样
            result.rejectValue("name","nameError","该分类早已存在");
        }

        if (result.hasErrors())
        {
            return "admin/types-input";
        }

        //上传了一个Type类型的数据到数据库
        Type t=typeService.updateType(id,type);
        if ( t == null)
        {
            attributes.addFlashAttribute("message", "更新失败");
        } else {
            attributes.addFlashAttribute("message", "更新成功");
        }
        return "redirect:/admin/types";
    }

    @GetMapping("/types/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes attributes)
    {
        typeService.deleteType(id);
        attributes.addFlashAttribute("message", "删除成功");
        return "redirect:/admin/types";
    }
}
