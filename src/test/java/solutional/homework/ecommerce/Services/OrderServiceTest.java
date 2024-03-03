package solutional.homework.ecommerce.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;
import solutional.homework.ecommerce.Models.DTO.OrderResponseDTO;
import solutional.homework.ecommerce.Models.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

;
;
;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_ShouldCreateNewOrder() {
        Order savedOrder = new Order();
        savedOrder.setId(UUID.randomUUID());

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponseDTO result = orderService.createOrder();

        assertNotNull(result);
        assertEquals(savedOrder.getId(), result.getId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void getOrderById_ExistingOrder_ShouldReturnOrder() {
        UUID orderId = UUID.randomUUID();
        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setStatus("NEW");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        OrderResponseDTO result = orderService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
    }

    @Test
    void getOrderById_NonExistingOrder_ShouldThrowException() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            orderService.getOrderById(orderId);
        });
    }

    @Test
    void addProductsToOrder_ValidInputs_ShouldAddProducts() {
        UUID orderId = UUID.randomUUID();
        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setStatus("NEW");

        Product product1 = new Product();
        product1.setId(1L);
        product1.setPrice("10.00");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setPrice("20.00");

        List<Long> productIds = Arrays.asList(1L,2L);
        List<Product> products = Arrays.asList(product1,product2);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(productRepository.findAllById(productIds)).thenReturn(products);

        orderService.addProductsToOrder(orderId, productIds);

        verify(orderRepository, times(1)).save(mockOrder);
        assertEquals(2, mockOrder.getItems().size());
    }

    @Test
    void updateOrderStatus_ValidOrder_ShouldUpdateStatus() {
        UUID orderId = UUID.randomUUID();
        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setStatus("NEW");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order updatedOrder = orderService.updateOrderStatus(orderId, "PAID");

        assertNotNull(updatedOrder);
        assertEquals("PAID", updatedOrder.getStatus());
    }
}