package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class,id);
    }
    /**
     * String
     */
    public List<Order> findAllByString(OrderSearch orderSearch){
        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;

        //주문 상태 검색
        if(orderSearch.getOrderStatus() != null){
            if(isFirstCondition){
                jpql += " where";
                isFirstCondition = false;
            }else{
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())){
            if(isFirstCondition){
                jpql += " where";
                isFirstCondition = false;
            }else{
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if(orderSearch.getOrderStatus() != null){
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if(StringUtils.hasText(orderSearch.getMemberName())){
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    /**
     *  JPA Criteria
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }

    public List<Order> findAllWithMemberDelivery() {
        //fetch 조인 lazy 여도 진짜 값을 처음부터 쿼리해오도록 한다.
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
                ).getResultList();

    }
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        //fetch 조인 lazy 여도 진짜 값을 처음부터 쿼리해오도록 한다.
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

    }

    public List<OrderSimpleQueryDto> findOrderDtos() {
        //Dto를 반환한다면 반드시 new 를 이용해야 한다.
        return em.createQuery(
                    "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status,d.address) from Order o" +
                    " join o.member m" +
                    " join o.delivery d",OrderSimpleQueryDto.class)
                    .getResultList();
    }

    public List<Order> findAllWithItem() {
        //jpql에서의 distinct는 실제 쿼리에 distinct를 추가하는 것 뿐 아니라
        //엔티티의 데이터를 가져와서 컬렉션에 담을 때 id 값을 기준으로 중복을 제거한 뒤
        // 가져온다.
        //양방향 관계에서 페치 조인시 페이징이 불가능하다. > 데이터를 모두 조회한 뒤
        //메모리에서 페이징 처리를 하므로 서비스 시에 문제가 발생할 수 있다.
        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class)
                .setFirstResult(1)
                .setMaxResults(100)
                .getResultList();
    }
}
