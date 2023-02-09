package integration;

import app.foot.FootApi;
import app.foot.controller.rest.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FootApi.class)
@AutoConfigureMockMvc
public class MatchIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();  //Allow 'java.time.Instant' mapping

    @Test
    void read_match_by_id_ok() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/matches/2"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        Match actual = objectMapper.readValue(
                response.getContentAsString(), Match.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(expectedMatch2(), actual);
    }


    @Test
    void read_match_ok() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/matches"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        List<Match> actual = convertFromHttpResponse(response);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(3, actual.size());
        assertTrue(actual.contains(expectedMatch2()));
    }

    @Test
    void read_match_ko() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/matchs"))
                .andReturn()
                .getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

    }

    @Test
    void add_goals_ok() throws Exception {
        PlayerScorer playerScorer = new PlayerScorer();
        playerScorer.getPlayer();
        playerScorer.getScoreTime();
        playerScorer.getIsOG();


        MockHttpServletResponse response =   mockMvc
                .perform(post("/matches/3/goals")
                        .content(objectMapper.writeValueAsString(List.of(playerScorer)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        Match actual = objectMapper.readValue(
                response.getContentAsString(), Match.class);


        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(actual.getTeamA().getScorers().contains(
                PlayerScorer.builder()
                        .player(player1())
                        .scoreTime(69)
                        .isOG(false)
                        .build()
        ));
    }

    @Test
    void add_goals_ko() throws Exception {
        PlayerScorer request = new PlayerScorer();
        request.getPlayer();
        request.getScoreTime();
        request.getIsOG();


        MockHttpServletResponse response = mockMvc
                .perform(post("/match/3/goals")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType("application/json")
                        .accept("application/json"))
                .andReturn()
                .getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }



    private static Match expectedMatch2() {
        return Match.builder()
                .id(2)
                .teamA(teamMatchA())
                .teamB(teamMatchB())
                .stadium("Borussia Parc")
                .datetime(Instant.parse("2023-02-01T20:00:00Z"))
                .build();
    }


    private static TeamMatch teamMatchB() {
        return TeamMatch.builder()
                .team(team3())
                .score(0)
                .scorers(List.of())
                .build();
    }

    private static TeamMatch teamMatchA() {
        return TeamMatch.builder()
                .team(team2())
                .score(2)
                .scorers(List.of(PlayerScorer.builder()
                                .player(player3())
                                .scoreTime(70)
                                .isOG(false)
                                .build(),
                        PlayerScorer.builder()
                                .player(player6())
                                .scoreTime(80)
                                .isOG(true)
                                .build()))
                .build();
    }



    private static Team team3() {
        return Team.builder()
                .id(3)
                .name("PSV")
                .build();
    }

    private static Player player6() {
        return Player.builder()
                .id(6)
                .name("Xavi")
                .teamName(team3().getName())
                .isGuardian(false)
                .build();
    }

    private static Player player3() {
        return Player.builder()
                .id(3)
                .name("Icardi")
                .teamName(team2().getName())
                .isGuardian(false)
                .build();
    }

    private static Player player1() {
        return Player.builder()
                .id(1)
                .name("Dusan")
                .teamName(team1().getName())
                .isGuardian(false)
                .build();
    }

    private static Team team1() {
        return Team.builder()
                .id(1)
                .name("Psv")
                .build();
    }
    private static Team team2() {
        return Team.builder()
                .id(2)
                .name("Dortmund")
                .build();
    }

    private List<Match> convertFromHttpResponse(MockHttpServletResponse response)
            throws JsonProcessingException, UnsupportedEncodingException {
        CollectionType matchListType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, Match.class);
        return objectMapper.readValue(
                response.getContentAsString(),
                matchListType);
    }
}
