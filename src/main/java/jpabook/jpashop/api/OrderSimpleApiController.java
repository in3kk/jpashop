package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne
 *
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    /**
     *
     * 1. xToOne 양방향 관계에서 모두 지연로딩인 상태로 해당 방법을 통해 조회할 시,
     *    무한루프에 빠질 수 있기 때문에 한쪽은 반드시 @JsonIgnore 어노테이션을 적용하해야 한다.
     * 2. 1의 방법만 적용하면 지연로딩 프록시로 인해 에러가 발생하므로, hibernate5module 라이브러리를
     *    의존성에 추가하고, bean에 등록한다.
     *
     *    스프링부트 3.0 이상부터는 Hibernate5JakartaModule 을 이용해야 한다.
     *
     * 이런 방식으로 조회를 하게되면 엔티티가 그대로 노출되므로, 엔티티 변경시 api스펙이 모두 변경된다는 문제점이 있다.
     * 성능면에서도 필요가 없는 데이터까지 전부 불러와서 문제가 된다.
     *
     * 결과적으로 이용하면 안되는 방식
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for(Order order : all){
            order.getMember().getName();//LAZY 강제 초기화
            order.getDelivery().getAddress();//LAZY 강제 초기화
        }
        return all;
    }

    /**
     * 해당 방식은 v1의 문제점을 보완한 방식이지만, 쿼리가 필요 이상으로 실행되는 n+1 문제가 있다.
     * -> 회원 N + 배송 N + 1
     */
    @GetMapping("/api/v2/simple-orders")
    public Result ordersV2(){
        //N + 1 문제 발생
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> collect = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return new Result(collect);
    }
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){
        //fetch 조인을 이용해 N + 1 문제 해결
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        //dto로 바로 조회
        //v3와의 차이는 v4는 필요로 하는 칼럼의 데이터만 조회한다는 것
        //성능 에서는 v4가 더 좋지만 재사용성은 v3가 높다.
        return orderRepository.findOrderDtos();
    }



    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
    }

    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();//LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();//LAZY 초기화
        }
    }
}
