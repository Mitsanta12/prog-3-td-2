package integration;

import app.foot.FootApi;
import app.foot.controller.rest.Player;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest(classes = FootApi.class)
@AutoConfigureMockMvc
class PlayerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    Player player1() {
        return Player.builder()
                .id(1)
                .name("J1")
                .isGuardian(false)
                .build();
    }

    Player player2() {
        return Player.builder()
                .id(2)
                .name("J2")
                .isGuardian(false)
                .build();
    }

    Player player3() {
        return Player.builder()
                .id(3)
                .name("J3")
                .isGuardian(false)
                .build();
    }

    @Test
    void read_players_ok() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(get("/players"))
                .andReturn()
                .getResponse();
        List<Player> actual = convertFromHttpResponse(response);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(9, actual.size());
        assertTrue(actual.containsAll(List.of(
                player1(),
                player2(),
                player3())));
    }

    @Test
    void read_players_ko() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(get("/player"))
                .andReturn()
                .getResponse();
        List<Player> actual = convertFromHttpResponse(response);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }
    @Test
    void create_players_ok() throws Exception {
        Player toCreate = Player.builder()
                .name("Joe Doe")
                .isGuardian(false)
                .teamName("E1")
                .build();
        MockHttpServletResponse response = mockMvc
                .perform(post("/players")
                        .content(objectMapper.writeValueAsString(List.of(toCreate)))
                        .contentType("application/json")
                        .accept("application/json"))
                .andReturn()
                .getResponse();
        List<Player> actual = convertFromHttpResponse(response);

        assertEquals(1, actual.size());
        assertEquals(toCreate, actual.get(0).toBuilder().id(null).build());
    }

    @Test
    void create_players_ko() throws Exception {
        Player toCreate = Player.builder()
                .name("Xavi Simeone")
                .isGuardian(false)
                .teamName("PSV")
                .build();

        MockHttpServletResponse response = mockMvc
                .perform(post("/player")
                        .content(objectMapper.writeValueAsString(List.of(toCreate)))
                        .contentType("application/json")
                        .accept("application/json"))
                .andReturn()
                .getResponse();


        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

    }

    @Test
    void update_players_ok() throws Exception {
        List<Player> toSave = List.of(Player.builder()
                .id(1)
                .name("Leroy Sané")
                .isGuardian(false)
                .teamName("PSV")
                .build());
        mockMvc.perform(put("/players")
                        .content(objectMapper.writeValueAsString(List.of(toSave)))
                        .contentType("application/json")
                        .accept("application/json"))
                .andReturn()
                .getResponse();
        MockHttpServletResponse response = mockMvc.
                perform(get("/players"))
                .andReturn()
                .getResponse();
        List<Player> actual = convertFromHttpResponse(response);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(10, actual.size());

    }

    @Test
    void update_players_ko() throws Exception {
        List<Player> toSave = List.of(Player.builder()
                .id(1)
                .name("Leroy Sané")
                .isGuardian(false)
                .teamName("E1")
                .build());
        MockHttpServletResponse response =  mockMvc.perform(put("/player")
                        .content(objectMapper.writeValueAsString(List.of(toSave)))
                        .contentType("application/json")
                        .accept("application/json"))
                .andReturn()
                .getResponse();


        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

    }


    private List<Player> convertFromHttpResponse(MockHttpServletResponse response)
            throws JsonProcessingException, UnsupportedEncodingException {
        CollectionType playerListType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, Player.class);
        return objectMapper.readValue(
                response.getContentAsString(),
                playerListType);
    }
}
