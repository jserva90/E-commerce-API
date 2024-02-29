package solutional.homework.ecommerce.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import solutional.homework.ecommerce.Models.DTO.OrderResponseDTO;
import solutional.homework.ecommerce.Models.*;
import solutional.homework.ecommerce.utils.BigDecimalToStringConverter;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private ProductRepository productRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository,OrderItemRepository orderItemRepository,ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    public OrderResponseDTO createOrder() {
        Order order = new Order();
        order.setStatus("NEW");
        order.setDiscount(BigDecimal.ZERO);
        order.setPaid(BigDecimal.ZERO);
        order.setReturns(BigDecimal.ZERO);
        order.setTotal(BigDecimal.ZERO);
        order = orderRepository.save(order);

        OrderResponseDTO orderResponseDTO = new OrderResponseDTO();
        orderResponseDTO.setId(order.getId());
        orderResponseDTO.setStatus(order.getStatus());
        OrderResponseDTO.Amount amount = new OrderResponseDTO.Amount();
        amount.setDiscount(BigDecimalToStringConverter.convert(order.getDiscount()));
        amount.setPaid(BigDecimalToStringConverter.convert(order.getPaid()));
        amount.setReturns(BigDecimalToStringConverter.convert(order.getReturns()));
        amount.setTotal(BigDecimalToStringConverter.convert(order.getTotal()));
        orderResponseDTO.setAmount(amount);
        orderResponseDTO.setProducts(new ArrayList<>());

        return orderResponseDTO;
    }

    public OrderResponseDTO getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    OrderResponseDTO orderResponseDTO = new OrderResponseDTO();
                    orderResponseDTO.setId(order.getId());
                    orderResponseDTO.setStatus(order.getStatus());

                    OrderResponseDTO.Amount amount = new OrderResponseDTO.Amount(
                            BigDecimalToStringConverter.convert(order.getDiscount()),
                            BigDecimalToStringConverter.convert(order.getPaid()),
                            BigDecimalToStringConverter.convert(order.getReturns()),
                            BigDecimalToStringConverter.convert(order.getTotal())
                    );
                    orderResponseDTO.setAmount(amount);

                    List<OrderResponseDTO.ProductDTO> productDTOList = convertOrderItemsToProductDTOs(order.getId());
                    orderResponseDTO.setProducts(productDTOList);

                    return orderResponseDTO;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));
    }

    public List<OrderResponseDTO.ProductDTO> getProductsByOrderId(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    List<OrderResponseDTO.ProductDTO> productDTOList = convertOrderItemsToProductDTOs(order.getId());

                    return productDTOList;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));
    }

    public Order updateOrderStatus(UUID orderId,String newStatus){
        return orderRepository.findById(orderId).map(order -> {

            if ("NEW".equals(order.getStatus()) && "PAID".equals(newStatus)){
                order.setStatus(newStatus);
                return orderRepository.save(order);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid order status");
            }


        }).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found"));
    }

    @Transactional
    public void addProductsToOrder(UUID orderId,List<Long> productIds){
        Order order = orderRepository.findById(orderId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Not found"));

        Set<Long> uniqueProductIds = new HashSet<>(productIds);
        if (uniqueProductIds.size() != productIds.size()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid parameters");
        }

        List<Product> productsToAdd = productRepository.findAllById(productIds);
        if (productsToAdd.size() != productIds.size()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid parameters");
        }

        productsToAdd.forEach(product -> {
            Optional<OrderItem> existingItem = order.getItems().stream()
                    .filter(item -> item.getProduct().equals(product))
                    .findFirst();

            if (existingItem.isPresent()) {
                OrderItem itemToUpdate = existingItem.get();
                itemToUpdate.setQuantity(itemToUpdate.getQuantity() + 1);
            } else {
                order.addProduct(product, 1);
            }
        });

        order.calculateTotal();
        orderRepository.save(order);
    }

    public void changeOrderItemQuantity(UUID orderId,UUID orderItemId, int newQuantity){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Not found"));

        OrderItem orderItem = order.getItems().stream()
                .filter(item -> item.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Not found"));

        if (newQuantity < 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid parameters");
        }

        orderItem.setQuantity(newQuantity);
        order.calculateTotal();
        orderRepository.save(order);
    }

    public List<OrderResponseDTO.ProductDTO> convertOrderItemsToProductDTOs(UUID orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return orderItems.stream().map(item -> new OrderResponseDTO.ProductDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity()
        )).collect(Collectors.toList());
    }
}