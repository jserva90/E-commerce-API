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
            System.out.println(product);
            order.addProduct(product,1);
        });
        orderRepository.save(order);
    }

    public List<OrderResponseDTO.ProductDTO> convertOrderItemsToProductDTOs(UUID orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        orderItems.forEach(orderItem -> System.out.println(orderItem.getProduct().getName()));
        return orderItems.stream().map(item -> new OrderResponseDTO.ProductDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity()
        )).collect(Collectors.toList());
    }
}