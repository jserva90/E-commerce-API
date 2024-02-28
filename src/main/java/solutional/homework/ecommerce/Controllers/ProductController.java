package solutional.homework.ecommerce.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import solutional.homework.ecommerce.Models.Product;
import solutional.homework.ecommerce.Services.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Service
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getProductsList(){
        return this.productService.getProductsList();
    }
}
