package dasi.typing.domain.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

  boolean existsByKakaoId(String kakaoId);

  boolean existsByNickname(String nickname);

  Optional<Member> findByKakaoId(String kakaoId);

}
