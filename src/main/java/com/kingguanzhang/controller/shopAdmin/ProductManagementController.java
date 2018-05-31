package com.kingguanzhang.controller.shopAdmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingguanzhang.dto.Msg;
import com.kingguanzhang.pojo.Product;

import com.kingguanzhang.service.ProductService;
import com.kingguanzhang.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@Controller
public class ProductManagementController {


    @Autowired
    private ProductService productService;


    @RequestMapping("/showEditProduct/{productId}")
    public String showEditProduct(@PathVariable("productId") Integer productId, HttpServletRequest request) {
        request.getSession().setAttribute("productId", productId);
        return "shop/editProduct";
    }

    /**
     * 通过ajax调用的删除商品的方法;
     *
     * @param productId
     * @return
     */
    @RequestMapping(value = "/removeProduct", method = RequestMethod.POST)
    @ResponseBody
    public Msg removeProduct(@RequestParam("productId") Integer productId) {
        Integer i = productService.removeProduct(productId);
        if (0 < i) {
            return Msg.success().setMsg("删除成功!");
        }
        return Msg.fail().setMsg("删除失败");
    }

    @RequestMapping(value = "/getProductList")
    @ResponseBody
    public Msg getProductList(HttpServletRequest request) {
        Integer shopId = (Integer) request.getSession().getAttribute("shopId");
        List<Product> productList = productService.getProductList(shopId);
        return Msg.success().setMsg("获取商品集合成功").add("productList", productList);
    }

    @RequestMapping(value = "/getProduct")
    @ResponseBody
    public Msg getProduct(HttpServletRequest request) {
        //从session中获取商品Id,页面和js中也不要暴露商品id,防止用户修改id
        Integer productId = (Integer) request.getSession().getAttribute("productId");
        Product product = productService.getProduct(productId);
        return Msg.success().setMsg("获取商品集合成功").add("product", product);
    }

    /**
     * 更新商品信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/updateProduct", method = RequestMethod.POST)
    @ResponseBody
    public Msg updateProduct(HttpServletRequest request) {

        //从前端传来的请求中获取键为productStr的值;
        String productStr = RequestUtil.parserString(request, "productStr");
        ObjectMapper objectMapper = new ObjectMapper();
        Product product = null;

        try {
            //将前端传来的商品信息转换为product实体类;
            System.out.print("productStr的值是:" + productStr);
            product = objectMapper.readValue(productStr, Product.class);

            /*这里需要注意,productId需要小心处理,建议页面上一步查询时就写入session,防止用户在前端修改id导致处理了错误的数据;*/
            int productId = (int) request.getSession().getAttribute("productId");//从session中取出商品id
            product.setProductId(productId);

        } catch (Exception e) {
            e.printStackTrace();
            return Msg.fail().setMsg("商品信息不能正确解析");
        }

        //从request中解析出上传的文件图片;
        CommonsMultipartFile productImg = null;
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        if (commonsMultipartResolver.isMultipart(request)) {
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
            productImg = (CommonsMultipartFile) multipartHttpServletRequest.getFile("productImg");
        } else {
            return Msg.fail().setMsg("图片文件解析失败");
        }

        //判断是否需要更新图片;
        if (null != productImg) {
            try {
                //使用文件.getOriginalFilename可以获取带后缀.jpg的全名;或者文件.getItem.getName也可以获取带后缀的文件名;否则只能取到不带后缀的文件名;
                productService.addProduct(product, productImg.getInputStream(), productImg.getOriginalFilename());
            } catch (IOException e) {
                System.out.print(e.getMessage());
                return Msg.fail().setMsg("更新商品信息失败");
            }
        }
        int i = productService.updateProduct(product);
        if (i >= 0) {
            product = productService.getProduct(product.getProductId());
            return Msg.success().setMsg("更新商品成功").add("product", product);
        }
        return Msg.fail().setMsg("更新商品信息失败");
    }

    /**
     * 新增商品的方法;
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/addProduct", method = RequestMethod.POST)
    @ResponseBody
    private Msg addProduct(HttpServletRequest request) {

        //从前端传来的请求中获取键为productStr的值;
        String productStr = RequestUtil.parserString(request, "productStr");
        ObjectMapper objectMapper = new ObjectMapper();
        Product product = null;
        Integer shopId = (Integer) request.getSession().getAttribute("shopId");

        try {
            //将前端传来的商店信息转换为product实体类;
            System.out.print("productStr的值是:" + productStr);
            product = objectMapper.readValue(productStr, Product.class);
        } catch (Exception e) {
            e.printStackTrace();
            return Msg.fail().setMsg("设置商品信息失败!");
        }

        //从request中解析出上传的文件图片;
        CommonsMultipartFile productImg = null;
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        if (commonsMultipartResolver.isMultipart(request)) {
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
            productImg = (CommonsMultipartFile) multipartHttpServletRequest.getFile("productImg");
        } else {
            return Msg.fail().setMsg("解析图片出错了!");
        }

        //新增商品,尽可能的减少从前端获取的值;
        if (null != product && null != productImg) {
            product.setShopId(shopId);//这个shopid必须从session中获取,这是为了安全
            try {
                //使用文件.getOriginalFilename可以获取带后缀.jpg的全名;或者文件.getItem.getName也可以获取带后缀的文件名;否则只能取到不带后缀的文件名;
                productService.addProduct(product, productImg.getInputStream(), productImg.getOriginalFilename());
            } catch (IOException e) {
                System.out.print(e.getMessage());
                return Msg.fail().setMsg("图片保存出错了");
            }
            //返回新增商品的最终结果;
            return Msg.success().setMsg("添加成功");
        } else {
            return Msg.fail().setMsg("添加失败,商品信息不完整!");
        }

    }

}
