package solutional.homework.ecommerce.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import solutional.homework.ecommerce.Models.ProductRepository;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getProductsList_Success() {
        productService.getProductsList();

        verify(productRepository, times(1)).findAll();
    }
}
