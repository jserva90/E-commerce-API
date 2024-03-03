package solutional.homework.ecommerce.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import solutional.homework.ecommerce.Models.DTO.OrderItemUpdateDTO;
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
    @Operation(summary = "Create a new order", description = "Creates a new order and returns the order details.")
    public ResponseEntity<OrderResponseDTO> createOrder() {
        OrderResponseDTO orderResponseDTO = orderService.createOrder();
        return ResponseEntity.ok(orderResponseDTO);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get an existing order by UUID", description = "Searches for existing order by the order UUID.")
    public void getOrderById(@PathVariable String orderId, HttpServletResponse response) throws IOException {
        try {
            UUID uuid = UUID.fromString(orderId);
            OrderResponseDTO orderResponseDTO = orderService.getOrderById(uuid);
            sendJsonResponse(orderResponseDTO, response, HttpStatus.OK);
        } catch (IllegalArgumentException | MethodArgumentTypeMismatchException | ResponseStatusException ex) {
            sendJsonResponse("Not found", response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{orderId}/products")
    @Operation(summary = "Get the list of order items for the order", description = "Searches for existing order by the order UUID and return the list of order items.")
    public void getProductsByOrderId(@PathVariable String orderId, HttpServletResponse response) throws IOException {
        try {
            UUID uuid = UUID.fromString(orderId);
            List<OrderResponseDTO.OrderItemDTO> productsListDTO = orderService.getProductsByOrderId(uuid);
            sendJsonResponse(productsListDTO, response, HttpStatus.OK);
        } catch (IllegalArgumentException | MethodArgumentTypeMismatchException | ResponseStatusException ex) {
            sendJsonResponse("Not found", response, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{orderId}/products")
    @Operation(summary = "Add products to existing order", description = "Takes in a list of product ids and adds them to the order.")
    public void addProductsToOrder(@PathVariable String orderId, @RequestBody List<Long> productIds, HttpServletResponse response) throws IOException {
        try {
            UUID uuid = UUID.fromString(orderId);
            orderService.addProductsToOrder(uuid,productIds);
            sendJsonResponse("OK", response, HttpStatus.OK);
        } catch (IllegalArgumentException | MethodArgumentTypeMismatchException ex) {
            sendJsonResponse("Not found", response, HttpStatus.NOT_FOUND);
        } catch (ResponseStatusException ex) {
            sendJsonResponse(ex.getReason(), response, HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{orderId}")
    @Operation(summary = "Updates order status", description = "Finds the order by UUID and updates the status to PAID")
    public void updateOrderStatus(@PathVariable String orderId, @RequestBody OrderStatusUpdateDTO statusUpdate, HttpServletResponse response) throws IOException {
        try {
            UUID uuid = UUID.fromString(orderId);
            orderService.updateOrderStatus(uuid, statusUpdate.getStatus());
            sendJsonResponse("OK", response, HttpStatus.OK);
        } catch (IllegalArgumentException | MethodArgumentTypeMismatchException ex) {
            sendJsonResponse("Not found", response, HttpStatus.NOT_FOUND);
        } catch (ResponseStatusException ex) {
            sendJsonResponse(ex.getReason(), response, ex.getStatus());
        }
    }

    @PatchMapping("/{orderId}/products/{orderItemId}")
    @Operation(summary = "Handles the update of order items based on order status", description = "If the order status is new, can change order item quantity, if the order status is paid, can add replacement product for the current order item")
    public void handleOrderItemUpdate(@PathVariable String orderId, @PathVariable String orderItemId, @RequestBody OrderItemUpdateDTO updateDTO, HttpServletResponse response) throws IOException{
        try {
            UUID orderUuid = UUID.fromString(orderId);
            UUID orderItemUuid = UUID.fromString(orderItemId);
            orderService.handleOrderItemUpdate(orderUuid,orderItemUuid,updateDTO);
            sendJsonResponse("OK", response, HttpStatus.OK);
        } catch (IllegalArgumentException | MethodArgumentTypeMismatchException ex) {
            sendJsonResponse("Not found", response, HttpStatus.NOT_FOUND);
        } catch (ResponseStatusException ex) {
            sendJsonResponse(ex.getReason(), response, HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public void handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletResponse response) throws IOException {
        sendJsonResponse("Invalid parameters", response, HttpStatus.BAD_REQUEST);
    }

    private void sendJsonResponse(Object responseObject, HttpServletResponse response, HttpStatus httpStatus) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(responseObject));
        response.setStatus(httpStatus.value());
    }
}
