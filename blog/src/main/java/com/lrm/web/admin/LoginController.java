package com.lrm.web.admin;

import com.lrm.po.User;
import com.lrm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

//这里的url和视图的跳转我很迷TAT

@Controller
//@Getmapping/@PostMapping 后面的url路径自动加上/admin前缀了 起限定作用 方便管理
@RequestMapping("/admin")
public class LoginController
{
    @Autowired
    private UserService userService;

    @GetMapping
    public String loginPage()
    {
        return "admin/login";
    }

    //发出映射从前端向后端，接受在login.html中的form表单提交的传入到后端了的数据。
    @PostMapping("/login")
    //前端action传递参数中的name="xxx" 必须要和@RequestParam("xxx")一致!!!
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes attributes)
    {
        User user = userService.checkUser(username, password);
        if(user !=null)
        {
            user.setPassword(null);
            session.setAttribute("user", user);
//          url没变!url没变!只是视图变了比如这里 登录成功之后url还是login 但是视图变成了templates/index
//          POST方法不支持直接返回页面 必须重定向 否则就会像这样只有返回视图 url不变
            return "admin/index";
        } else {
            attributes.addFlashAttribute("message","用户名或密码错误");
            return "redirect:/admin";
        }
    }
    @GetMapping("/logout")
    public String logout(HttpSession session)
    {
        session.removeAttribute("user");
        return "redirect:/admin";
    }
}
