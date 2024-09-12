package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepositoryOld;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
//@AllArgsConstructor//자동으로 생성자 인젝션 코드 생성 lombok 적용 시에만 가능
@RequiredArgsConstructor//final로 선언된 필드의 생성자 인젝션 코드를 자동으로 생성 lombok 적용 시에만 가능
public class MemberService {


    private final MemberRepositoryOld memberRepositoryOld;

    //생성자 주입 가장 권장하는 방법
//    @Autowired
//    public MemberService(MemberRepository memberRepository){
//        this.memberRepository = memberRepository;
//    }
    //세터 주입 테스트 시에는 좋지만 서비스 중일 때는 권장하지 않음
//    @Autowired
//    public void setMemberRepository(MemberRepository memberRepository){
//        this.memberRepository = memberRepository;
//    }

    //회원 가입
    @Transactional
    public Long join(Member member){
        validateDuplicateMember(member);//중복 회원 확인
        memberRepositoryOld.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepositoryOld.findByName(member.getName());
        //exception
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    //회원 전체 조회
    //(readOnly = true)는 자동으로 최적화를 해주기 때문에 데이터를 읽어오는 메소드에는 가급적이면 사용하는 것이 좋다.
    @Transactional(readOnly = true)
    public List<Member> findMembers(){
        return memberRepositoryOld.findAll();
    }

    //회원 단건 조회
    @Transactional(readOnly = true)
    public Member findOne(Long memberId){
        return memberRepositoryOld.findOne(memberId);
    }

    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepositoryOld.findOne(id);
        member.setName(name);
    }
}
