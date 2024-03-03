package solutional.homework.ecommerce.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import solutional.homework.ecommerce.Models.DTO.OrderItemUpdateDTO;
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

                    List<OrderResponseDTO.OrderItemDTO> orderItemDTOList = convertOrderItemsToOrderItemDTOs(order.getId());
                    orderResponseDTO.setProducts(orderItemDTOList);
                    orderResponseDTO.getProducts().sort(Comparator.comparingLong(OrderResponseDTO.OrderItemDTO::getProduct_id));
                    return orderResponseDTO;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));
    }

    public List<OrderResponseDTO.OrderItemDTO> getProductsByOrderId(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    List<OrderResponseDTO.OrderItemDTO> orderItemDTOList = convertOrderItemsToOrderItemDTOs(order.getId());
                    orderItemDTOList.sort(Comparator.comparingLong(OrderResponseDTO.OrderItemDTO::getProduct_id));
                    return orderItemDTOList;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));
    }

    public Order updateOrderStatus(UUID orderId,String newStatus){
        return orderRepository.findById(orderId).map(order -> {
            if ("NEW".equals(order.getStatus()) && "PAID".equals(newStatus)){
                order.setStatus(newStatus);
                order.setPaid(order.getTotal());
                return orderRepository.save(order);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid order status");
            }
        }).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Not found"));
    }

    @Transactional
    public void addProductsToOrder(UUID orderId,List<Long> productIds){
        Order order = orderRepository.findById(orderId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Not found"));

        if ("PAID".equals(order.getStatus())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid parameters");
        }

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

    @Transactional
    public void replaceOrderItemInOrder(UUID orderId, UUID orderItemId, Long replacementProductId, int replacementQuantity) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));

        OrderItem orderItem = order.getItems().stream()
                .filter(item -> item.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));

        if (replacementQuantity < 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid parameters");
        }

        Product replacementProduct = productRepository.findById(replacementProductId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters"));

        if (orderItem.getReplacedWith() != null){
            OrderItem existingReplacement = orderItem.getReplacedWith();
            existingReplacement.setReplaced(true);
            orderItemRepository.save(existingReplacement);
        }

        OrderItem replacementItem = new OrderItem();
        replacementItem.setOrder(order);
        replacementItem.setProduct(replacementProduct);
        replacementItem.setQuantity(replacementQuantity);
        replacementItem = orderItemRepository.save(replacementItem);

        orderItem.setReplacedWith(replacementItem);
        order.updateAmountsBasedOnReplacements();
        orderRepository.save(order);
    }

    public void handleOrderItemUpdate(UUID orderId, UUID productItemId, OrderItemUpdateDTO updateDTO) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));

        if ("NEW".equals(order.getStatus()) && updateDTO.getQuantity() != null) {
            changeOrderItemQuantity(orderId, productItemId, updateDTO.getQuantity());
        } else if ("PAID".equals(order.getStatus()) && updateDTO.getReplacedWith() != null) {
            OrderItemUpdateDTO.Replacement replacement = updateDTO.getReplacedWith();
            replaceOrderItemInOrder(orderId, productItemId, replacement.getProductId(), replacement.getQuantity());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }
    }

    public List<OrderResponseDTO.OrderItemDTO> convertOrderItemsToOrderItemDTOs(UUID orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        Set<UUID> replacementItemIds = orderItems.stream()
                .filter(item -> item.getReplacedWith() != null)
                .map(item -> item.getReplacedWith().getId())
                .collect(Collectors.toSet());

        return orderItems.stream()
                .filter(item -> !replacementItemIds.contains(item.getId()))
                .filter(item -> !item.isReplaced())
                .map(this::mapToOrderItemDTO)
                .collect(Collectors.toList());
    }

    private OrderResponseDTO.OrderItemDTO mapToOrderItemDTO(OrderItem orderItem) {
        OrderResponseDTO.OrderItemDTO dto = new OrderResponseDTO.OrderItemDTO(
                orderItem.getId(),
                orderItem.getProduct().getName(),
                orderItem.getProduct().getPrice(),
                orderItem.getProduct().getId(),
                orderItem.getQuantity(),
                null
        );

        if (orderItem.getReplacedWith() != null) {
            dto.setReplaced_with(mapToOrderItemDTO(orderItem.getReplacedWith()));
        }

        return dto;
    }
}