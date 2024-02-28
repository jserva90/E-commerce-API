package solutional.homework.ecommerce.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import solutional.homework.ecommerce.Models.DTO.OrderResponseDTO;
import solutional.homework.ecommerce.Models.DTO.OrderStatusUpdateDTO;
import solutional.homework.ecommerce.Services.OrderService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder() {
        OrderResponseDTO orderResponseDTO = orderService.createOrder();
        return ResponseEntity.ok(orderResponseDTO);
    }

    @GetMapping("/{orderId}")
    public void getOrderById(@PathVariable UUID orderId, HttpServletResponse response) throws IOException {
        try {
            OrderResponseDTO orderResponseDTO = orderService.getOrderById(orderId);
            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(orderResponseDTO));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ResponseStatusException ex) {
            response.setContentType("application/json");
            response.getWriter().write("\"Not found\"");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @GetMapping("/{orderId}/products")
    public void getProductsByOrderId(@PathVariable UUID orderId, HttpServletResponse response) throws IOException {
        try {
            List<OrderResponseDTO.ProductDTO> productsListDTO = orderService.getProductsByOrderId(orderId);
            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(productsListDTO));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ResponseStatusException ex) {
            response.setContentType("application/json");
            response.getWriter().write("\"Not found\"");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @PostMapping("/{orderId}/products")
    public void getProductsByOrderId(@PathVariable UUID orderId, @RequestBody List<Long> productIds, HttpServletResponse response) throws IOException {
        try {
            orderService.addProductsToOrder(orderId,productIds);
            response.setContentType("application/json");
            response.getWriter().write("\"OK\"");
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ResponseStatusException ex) {
            response.setContentType("application/json");
            response.getWriter().write("\"Not found\"");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @PatchMapping("/{orderId}")
    public void updateOrderStatus(@PathVariable UUID orderId, @RequestBody OrderStatusUpdateDTO statusUpdate, HttpServletResponse response) throws IOException {
        try {
            orderService.updateOrderStatus(orderId, statusUpdate.getStatus());
            response.setContentType("application/json");
            response.getWriter().write("\"OK\"");
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ResponseStatusException ex) {
            response.setContentType("application/json");
            response.getWriter().write("\"Invalid order status\"");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
