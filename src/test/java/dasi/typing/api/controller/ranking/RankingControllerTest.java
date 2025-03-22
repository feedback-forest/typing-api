package dasi.typing.api.controller.ranking;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/ranking.sql")
class RankingControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("실시간 랭킹 데이터를 조회하여 응답을 정상적으로 반환한다.")
  void sendRealTimeRankingToClient() throws Exception {
    // when & then
    mockMvc.perform(
            get("/api/v1/rankings/realtime")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.message").value("success"))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  @DisplayName("현재 날짜에 해당하는 연월 랭킹 데이터를 조회하여 응답을 정상적으로 반환한다.")
  void sendMonthlyRankingToClient() throws Exception {
    // when & then
    mockMvc.perform(
            get("/api/v1/rankings/monthly")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.message").value("success"))
        .andExpect(jsonPath("$.data").isArray());
  }

}