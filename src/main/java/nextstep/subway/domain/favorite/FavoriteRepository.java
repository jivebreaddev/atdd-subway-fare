package nextstep.subway.domain.favorite;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
  List<Favorite> findAllByMemberId(Long memberId);
}
