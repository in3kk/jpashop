package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,Long> {

    //select m form Member m where m.name = ?
    //spring data jpa 로 기존의 findByName 구현
    List<Member> findByName(String name);
}
