package com.example.project_economic.controller;

import com.example.project_economic.dto.CommentDTO;
import com.example.project_economic.dto.CountProuductDto;
import com.example.project_economic.dto.Price;
import com.example.project_economic.dto.ProductDto;
import com.example.project_economic.entity.ProductEntity;
import com.example.project_economic.response.PageProductResponse;
import com.example.project_economic.response.ProductResponse;
import com.example.project_economic.service.CartItemService;
import com.example.project_economic.service.CategoryService;
import com.example.project_economic.service.CommentService;
import com.example.project_economic.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping(path = "/api/products")
public class ProductController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private ProductService productService;

    List<Price>prices=List.of(
            new Price(0,100000),
            new Price(100000,200000),
            new Price(200000,400000),
            new Price(400000,1000000)
    );
    public List<CountProuductDto>countProuductDtos(){
        List<CountProuductDto>countProuductDtos=new ArrayList<>();
        List<Object[]>objects=this.productService.countProductByCategoryId();
        for(Object[] obj:objects){
            CountProuductDto countProuductDto=new CountProuductDto();
            countProuductDto.setCount((Long) obj[1]);
            countProuductDto.setCategoryId((Long)obj[0]);
            countProuductDtos.add(countProuductDto);
        }
        return countProuductDtos;
    }
    @GetMapping("/create/{userId}")
    public String showFormCreate(@PathVariable Long userId, Model model) {
        model.addAttribute("countProductByCategory",this.countProuductDtos());
        model.addAttribute("categories", this.categoryService.findAll());
        model.addAttribute("product", new ProductDto());
        model.addAttribute("allproducts", this.productService.findAllProductByUserId(userId));
        return "home/createproduct";
    }

    @PostMapping("/create")
    public String saveProduct(@ModelAttribute("product") ProductDto productDto, @RequestParam("imageProduct") MultipartFile imageProduct, Model model) {
        try {
            this.productService.save(productDto, imageProduct);
            System.out.println("Debug: Your message here");
        } catch (Exception exception) {
            model.addAttribute("error", "error");
        }
        model.addAttribute("countProductByCategory",this.countProuductDtos());
        model.addAttribute("product", new ProductDto());
        model.addAttribute("countProductByCategory",this.countProuductDtos());
        model.addAttribute("categories", this.categoryService.findAll());
        model.addAttribute("allproducts", this.productService.findAllProductByUserId(productDto.getSellerId()));
        return "home/createproduct";
    }

    @GetMapping("/edit/")
    public String showFormEditProduct(@RequestParam("id") Long productId, Model model) {
        model.addAttribute("countProductByCategory",this.countProuductDtos());
        model.addAttribute("showformedit", "show");
        model.addAttribute("productEdit", this.productService.findById(productId));
        model.addAttribute("categories", this.categoryService.findAll());
        model.addAttribute("product", new ProductDto());
        ProductResponse product = this.productService.findById(productId);
        model.addAttribute("allproducts", this.productService.findAllProductByUserId(product.getUserEntity().getId()));
        return "/home/createproduct";
    }

    @PostMapping("/edit/")
    public String updateProduct(@ModelAttribute("product") ProductDto productDto, @RequestParam("imageProduct") MultipartFile imageProduct, Model model, @RequestParam("id") Long productId) {
        this.productService.update(productDto,productId,imageProduct);
        model.addAttribute("countProductByCategory",this.countProuductDtos());
        model.addAttribute("categories", this.categoryService.findAll());
        model.addAttribute("product", new ProductDto());
        model.addAttribute("allproducts", this.productService.findAllProductByUserId(productService.findById(productDto.getId()).getUserEntity().getId()));
        return "/home/createproduct";
    }

    @GetMapping("/delete/")
    public String deleteProduct(@RequestParam("id") Long id,Model model){
        this.productService.deleteById(id);
        model.addAttribute("countProductByCategory",this.countProuductDtos());
        model.addAttribute("categories", this.categoryService.findAll());
        model.addAttribute("product", new ProductDto());
        ProductResponse product = this.productService.findById(id);
        model.addAttribute("allproducts", this.productService.findAllProductByUserId(product.getUserEntity().getId()));
        return "/home/createproduct";
    }
    @GetMapping("/active/")
    public String activeProduct(@RequestParam("id") Long id,Model model){
        this.productService.activeById(id);
        model.addAttribute("countProductByCategory",this.countProuductDtos());
        model.addAttribute("categories", this.categoryService.findAll());
        model.addAttribute("product", new ProductDto());
        ProductResponse product = this.productService.findById(id);
        model.addAttribute("allproducts", this.productService.findAllProductByUserId(product.getUserEntity().getId()));
        return "/home/createproduct";
    }

    @GetMapping("/like/")
    public String likeProduct(@RequestParam("id") Long id,Model model){
        int pageSize = 9;
        int pageNumber = 1;
        this.productService.likeById(id);
        model.addAttribute("categories",this.categoryService.findAllByActived());
        model.addAttribute("products",this.productService.findAllIsActived());
        model.addAttribute("prices",prices);
        return "/home/product-list";
    }

    @GetMapping("/productdetail/{userId}/{productId}")
    public String showProductDetail(@PathVariable Long userId,@PathVariable Long productId,Model model){
        int pageSize = 5;
        int pageNumber = 1;
        model.addAttribute("product",this.productService.findById(productId));
        model.addAttribute("productId",productId);
        model.addAttribute("products", this.productService.findAllIsActived());
        model.addAttribute("categories", this.categoryService.findAllByActived());
        if(this.cartItemService.findCartByProductId(productId,userId)){
            model.addAttribute("findProductInCart","already");
        }
        List<CommentDTO> commentDTOS=this.commentService.findByPostId(productId);
        model.addAttribute("comments",commentDTOS);
        return "home/product-detail";
    }

    @GetMapping("/all")
    public String showAllProduct(Model model){
        int pageSize = 5;
        int pageNumber = 1;
        model.addAttribute("categories",this.categoryService.findAllByActived());
        model.addAttribute("products",this.productService.findAllIsActived());
        model.addAttribute("prices",prices);
        return "home/product-list";
    }

    @GetMapping("/all/{pageNumber}")
    public String getAllPagePagination(
            @PathVariable Integer pageNumber,
            Model model
    ){
        int pageSize=9;
        PageProductResponse pageProductResponse=productService.findAllPagination(pageNumber,pageSize);
        model.addAttribute("currentPage",pageNumber);
        model.addAttribute("show_pagination","all");
        model.addAttribute("lastPage",pageProductResponse.getLastPage());
        model.addAttribute("previousPage",pageNumber>1?pageNumber-1:pageNumber);
        model.addAttribute("totalPage",new int[pageProductResponse.getTotalPage()]);
        model.addAttribute("categories",this.categoryService.findAllByActived());
//        model.addAttribute("products", this.productService.findAllIsActived());
        model.addAttribute("products", this.productService.findAllIsActived(pageSize, pageNumber));
        model.addAttribute("prices",prices);
        return "home/product-list";
    }

    @GetMapping("/search/")
    public String getProductByKeyWord(
            @RequestParam("key") String key,
            @RequestParam("pageNumber") Integer pageNumber,
            Model model
    ){
        int pageSize=9;
        PageProductResponse pageProductResponse=this.productService.findAllProductByKeyword(key,pageSize,(pageNumber-1)*pageSize);
        model.addAttribute("show_pagination","keyword");
        model.addAttribute("key",key);
        model.addAttribute("currentPage",pageNumber);
        model.addAttribute("lastPage",pageProductResponse.getLastPage());
        model.addAttribute("previousPage",pageNumber>1?pageNumber-1:pageNumber);
        model.addAttribute("totalPage",new int[pageProductResponse.getTotalPage()]);
        model.addAttribute("categories",this.categoryService.findAllByActived());
        model.addAttribute("products",pageProductResponse.getProductResponses());
        model.addAttribute("prices",prices);
        return "home/product-list";
    }

    @GetMapping("/all/")
    public String findAllProductByCategory(
            @RequestParam("category_id") Long categoryId,
            @RequestParam("pageNumber") int pageNumber,
            Model model
    ){
        int pageSize=9;
        PageProductResponse pageProductResponse=this.productService.findAllProductByCategory(categoryId,pageSize,(pageNumber-1)*pageSize);
        model.addAttribute("show_pagination","category");
        model.addAttribute("currentPage",pageNumber);
        model.addAttribute("categoryId",categoryId);
        model.addAttribute("lastPage",pageProductResponse.getLastPage());
        model.addAttribute("previousPage",pageNumber>1?pageNumber-1:pageNumber);
        model.addAttribute("totalPage",new int[pageProductResponse.getTotalPage()]);
        model.addAttribute("categories",this.categoryService.findAllByActived());
        model.addAttribute("products",pageProductResponse.getProductResponses());
        model.addAttribute("prices",prices);

        return "home/product-list";
    }

    @GetMapping("/prices/")
    public String findAllProduceByPrice(
            @RequestParam("firstPrice") int firstPrice,
            @RequestParam("secondPrice") int secondPrice,
            @RequestParam("pageNumber") int pageNumber,
            Model model
    ){
        int pageSize=9;
        PageProductResponse pageProductResponse=this.productService.findAllProductByCostPrice(firstPrice,secondPrice,pageSize,(pageNumber-1)*pageSize);
        model.addAttribute("firstPrice",firstPrice);
        model.addAttribute("secondPrice",secondPrice);
        model.addAttribute("show_pagination","prices");
        model.addAttribute("currentPage",pageNumber);
        model.addAttribute("lastPage",pageProductResponse.getLastPage());
        model.addAttribute("previousPage",pageNumber>1?pageNumber-1:pageNumber);
        model.addAttribute("totalPage",new int[pageProductResponse.getTotalPage()]);
        model.addAttribute("categories",this.categoryService.findAllByActived());
        model.addAttribute("products",pageProductResponse.getProductResponses());
        model.addAttribute("prices",prices);
        return "home/product-list";
    }
}
