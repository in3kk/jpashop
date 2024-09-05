package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    //실무에서는 엔티티가 실제로 변경될 상황이 많이 발생하는데
    // 이 때마다 api스펙을 변경할 수 없기 때문에 api를
    // 직접 엔티티에 매핑하지 않고 별도의 dto를 생성해 매핑한다.
    //@Valid 어노테이션은 @RequestBody 로 들어오는 객체를 검증하는데 사용된다.
    //검증 세부사항은 dto 혹은 엔티티에 미리 정의해 두어야 한다.
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request){

        memberService.update(id,request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(),findMember.getName());
    }

    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result memberV2(){
        List<Member> findMembers = memberService.findMembers();

        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
        //바로 반환하면 json 배열 타입으로 반환하기 때문에 유연성이 떨어진다.
        //따라서 Result 로 한 번 감싸서 반환하는 게 좋다.
        return new Result(collect);
    }
    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto{
        private String name;
    }

    @Data
    static  class UpdateMemberRequest{
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse{
        private Long id;
        private String name;
    }

    @Data
    static class CreateMemberRequest{
        private String name;
    }
    @Data
    static class CreateMemberResponse{
        private Long id;
        CreateMemberResponse(Long id){
            this.id=id;
        }

    }
}
