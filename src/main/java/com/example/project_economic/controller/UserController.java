package com.example.project_economic.controller;

import com.example.project_economic.config.UserInfoDetails;
import com.example.project_economic.dto.ProductDto;
import com.example.project_economic.entity.CategoryEntity;
import com.example.project_economic.entity.UserEntity;
import com.example.project_economic.jwt.JwtService;
import com.example.project_economic.response.PageProductResponse;
import com.example.project_economic.response.ProductResponse;
import com.example.project_economic.service.*;
import jakarta.servlet.http.HttpSession;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private ProductService productService;
    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private HistoryCardService historyCardService;
    @Autowired
    private ProductController productController;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/index")
    public String welcomePage(){
        return "index";
    }

    @GetMapping("/login")
    public String showFormLogin(Model model){
        model.addAttribute("userEntity" ,new UserEntity());
        return "login/index";
    }
    @GetMapping("/register")
    public String showFormRegister(Model model){
        model.addAttribute("userEntity" ,new UserEntity());
        return "register/index";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userEntity") UserEntity userEntity, @RequestParam("reset-password") String pass, Model model){
        try{
            if(pass.equals(userEntity.getPassword()))
                this.userService.createUser(userEntity);
            else{
                model.addAttribute("wrongpass", "Mật khẩu không khớp!");
                model.addAttribute("userEntity", userEntity);
                return "register/index";
            }
            return "login/index";
        }catch (Exception exception){

            model.addAttribute("error",exception.getMessage());
            return "register/index";
        }
    }

    @PostMapping("/home")
    public String loginPage(@ModelAttribute("userEntity") UserEntity userEntity, Model model){
        try{
            Authentication authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    userEntity.getUsername(),
                    userEntity.getPassword()
            ));
//            System.out.println(principal.getName());
//            if(authentication.isAuthenticated()){
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//                String token=this.jwtService.generateToken(authentication.getName());
//                model.addAttribute("token",token);
//                model.addAttribute("username",authentication.getName());
//                model.addAttribute("roles",authentication.getAuthorities().toString());
//                model.addAttribute("products",this.findByAllProductActive());
//                model.addAttribute("numbercart",this.cartItemService.countCart(1L));
//                return "home/index";
//
//            }

        }catch (Exception exception){
            model.addAttribute("error","Sai tài khoản hoặc mật khẩu!");
            return "login/index";
        }
        model.addAttribute("error","Sai tài khoản hoặc mật khẩu!");
        return "login/index";
    }
    @GetMapping("/fail")
    public String loginFail(Model model){
        model.addAttribute("error","Sai tài khoản hoặc mật khẩu!");
        model.addAttribute("userEntity", new UserEntity());
        return "login/index";
    }
    @GetMapping("/product-detail")
    public String getproductdetail(){
        return "home/product-detail";
    }

    @GetMapping("/categories")
    public String getaddnew(Model model, Principal principal){
        if(principal==null){
            return "login/index";
        }
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("category",new CategoryEntity());
        model.addAttribute("allcategories",this.categoryService.findAll());
        model.addAttribute("countProductByCategory",productController.countProuductDtos());
        return "home/addnew";
    }

    @GetMapping("/homepage")
    public String getHomeIndex(Model model,Principal principal){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("userId",((UserInfoDetails)authentication.getPrincipal()).getUserId());
        model.addAttribute("username",principal.getName());
        model.addAttribute("products", this.productService.findAllIsActived(9, 1));
        model.addAttribute("numbercart",this.cartItemService.countCart(((UserInfoDetails)(authentication.getPrincipal())).getUserId()));
        model.addAttribute("categories", this.categoryService.findAllByActived());
        model.addAttribute("prices",new ProductController().prices);
        int pageSize=9;
        PageProductResponse pageProductResponse=productService.findAllPagination(1,9);
        model.addAttribute("currentPage",1);
        model.addAttribute("show_pagination","all");
        model.addAttribute("lastPage",pageProductResponse.getLastPage());
        model.addAttribute("previousPage",1);
        model.addAttribute("totalPage",new int[pageProductResponse.getTotalPage()]);
        return "home/product-list";
    }
    @PostMapping("/update/")
    public String updateUser(@ModelAttribute("users") UserEntity userEntity, @RequestParam("id") Long userId, Model model){
        try{
            this.userService.update(userEntity, userId);
//            model.addAttribute("users",new UserEntity());
        }catch (Exception exception){
            model.addAttribute("error","error");
            return "home/my-account";
        }
        model.addAttribute("history_card",this.historyCardService.findByUserId(userId));
        model.addAttribute("user",this.userService.findUserById(userId));
        return "home/my-account";
    }

    @PostMapping("/change-password/")
    @ResponseBody
    public Integer changePassword(
                                 @RequestParam("userId") Long userId,
                                 @RequestParam("passNow") String passNow,
                                 @RequestParam("pass1") String pass1,
                                 @RequestParam("pass2") String pass2,
                                 Model model){
        try{
            UserEntity userEntity = this.userService.findUserById(userId);
            if(pass1.equals(pass2)){
                if (passwordEncoder.matches(passNow, this.userService.findUserById(userId).getPassword())){
                    userEntity.setPassword(passwordEncoder.encode(pass1));
                    this.userService.update(userEntity, userId);
                    return 0;
                }
                else{
                    return 1;
                }
            }
            else return 2;
        }catch (Exception exception){
            model.addAttribute("error","error");
        }
        model.addAttribute("history_card",this.historyCardService.findByUserId(userId));
        model.addAttribute("user",this.userService.findUserById(userId));
        return 0;
    }

    public List<ProductResponse>findByAllProductActive(){
        int pageSize = 10;
        int pageNumber = 1;
        return this.productService.findAllIsActived();
    }
}
