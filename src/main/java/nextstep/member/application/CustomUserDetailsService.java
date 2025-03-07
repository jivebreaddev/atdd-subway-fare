package nextstep.member.application;

import nextstep.auth.AuthenticationException;
import nextstep.auth.userdetails.UserDetails;
import nextstep.auth.userdetails.UserDetailsService;
import nextstep.member.domain.CustomUserDetails;
import nextstep.member.domain.Member;
import nextstep.member.domain.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Member member = memberRepository.findByEmail(username).orElseThrow(() -> new AuthenticationException("없는 유저이름입니다."));
        return new CustomUserDetails(member.getEmail(), member.getPassword(), member.getRole());
    }
}
