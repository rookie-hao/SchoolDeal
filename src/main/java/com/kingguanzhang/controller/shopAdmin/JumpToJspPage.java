package com.kingguanzhang.controller.shopAdmin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class JumpToJspPage {

    @RequestMapping("/index")
    public String showIndex(){
        return "/shop/index";
    }

    @RequestMapping("/register")
    public String showRegister(){
        return "/shop/registerShop";
    }

    @RequestMapping("/login")
    public String showLogin(){
        return "/shop/login";
    }

    @RequestMapping("/editShop")
    public String showEditShop(){
        return "/shop/editShop";
    }

    @RequestMapping("/shopManagement")
    public String showShopManagement(){
        return "/shop/shopManagement";
    }

    @RequestMapping("/productManagement")
    public String showProductManagement(){
        return "/shop/productManagement1";
    }

    @RequestMapping("/productManagement2")
    public String showProductManagement2(){
        return "/shop/productManagement2";
    }

    @RequestMapping("/productCategory")
    public String showProductCategory(){
        return "/shop/productCategory";
    }

    @RequestMapping("/addProduct")
    public String addProduct(){
        return "/shop/addProduct";
    }


}
