package solutional.homework.ecommerce.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import solutional.homework.ecommerce.Models.Product;
import solutional.homework.ecommerce.Models.ProductRepository;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    @Autowired
    public  ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public List<Product> getProductsList(){
        return this.productRepository.findAll();
    }
}
