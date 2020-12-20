package com.twitch.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitch.entity.FavoriteRequestBody;
import com.twitch.entity.Game;
import com.twitch.entity.Item;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ServletUtil {
    public static void addItemsToResponse(HttpServletResponse response,
                                   Map<String, List<Item>> itemMap) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(new ObjectMapper(). writeValueAsString(itemMap));
    }

    public static void addGameToResponse(HttpServletResponse response,
                                         Game game) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(new ObjectMapper(). writeValueAsString(game));
    }

    public static FavoriteRequestBody getFavoriteRequestBody(HttpServletRequest request) throws IOException {
        String userId = request.getParameter("user_id");
        ObjectMapper mapper = new ObjectMapper();
        FavoriteRequestBody body = mapper.readValue(request.getReader(), FavoriteRequestBody.class);
        return body;
    }
}
