package dasi.typing.api.service.ranking;

import dasi.typing.api.controller.ranking.response.RankingResponse;
import dasi.typing.domain.typing.TypingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

  private final TypingRepository typingRepository;

  public List<RankingResponse> getRealTimeRanking() {
    return typingRepository.findTop50WithSequentialRank();
  }

}
