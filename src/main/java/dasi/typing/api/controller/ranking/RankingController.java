package dasi.typing.api.controller.ranking;

import dasi.typing.api.controller.ranking.response.RankingResponse;
import dasi.typing.api.service.ranking.RankingService;
import dasi.typing.exception.ApiResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'GUEST')")
public class RankingController {

  private final RankingService rankingService;

  @GetMapping("/api/v1/rankings/realtime")
  public ApiResponse<Map<String, List<RankingResponse>>> getRealTimeRanking() {
    List<RankingResponse> responses = rankingService.getRealTimeRanking();
    return ApiResponse.success("rankings", responses);
  }

  @GetMapping("/api/v1/rankings/monthly")
  public ApiResponse<Map<String, List<RankingResponse>>> getMonthlyRanking() {
    List<RankingResponse> responses = rankingService.getMonthlyRanking();
    return ApiResponse.success("rankings", responses);
  }

}
