package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepositoryOld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
//테스트 시 메모리 모드로 진행해 db까지 모두 볼 수 있도록 하는 어노테이션
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class MemberServiceTest {
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepositoryOld memberRepositoryOld;
    @Test
    public void 회원가입() throws Exception{
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long saveId = memberService.join(member);

        //then
        assertEquals(member, memberRepositoryOld.findOne(saveId));
    }

    @Test
    public void 중복_회원_예외() throws Exception{
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);
        assertThrows(IllegalStateException.class,() ->{
            memberService.join(member2);
        });
        //junit4 에서는 @Test(expected = IllegalStateException.class)로 대체할 수 있지만
        //junit5 에서는 assertThrows를 사용해야 함
//        try{
//            memberService.join(member2);//예외가 발생해야 함
//        }catch (IllegalStateException e){
//            return;
//        }

        //then
//        fail("예외 발생");
    }
}