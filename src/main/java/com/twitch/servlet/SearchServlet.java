package com.twitch.servlet;

import com.twitch.entity.Item;
import com.twitch.external.TwitchClient;
import com.twitch.external.TwitchException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "SearchServlet", urlPatterns = {"/search"})
public class SearchServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String gameId = request.getParameter("game_id");
        if (gameId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            TwitchClient twitchClient = new TwitchClient();
            Map<String, List<Item>> itemMap = twitchClient.searchItems(gameId);
            ServletUtil.addItemsToResponse(response, itemMap);
        } catch (TwitchException e) {
            throw new ServletException(e);
        }
    }
}
