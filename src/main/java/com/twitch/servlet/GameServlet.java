package com.twitch.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitch.entity.Game;
import com.twitch.entity.Item;
import com.twitch.external.TwitchClient;
import com.twitch.external.TwitchException;
import org.json.JSONObject;

@WebServlet(name = "GameServlet", urlPatterns = {"/game"})
public class GameServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String gameName = request.getParameter("game_name");
        TwitchClient twitchClient = new TwitchClient();
        response.setContentType("application/json;charset=UTF-8");
        try {
            if (gameName == null) {
                List<Game> games = twitchClient.getTopGames(20);
                response.getWriter().print(new ObjectMapper().writeValueAsString(games));
            } else {
                Game game  = twitchClient.searchGame(gameName);
                response.getWriter().print(new ObjectMapper().writeValueAsString(game));
            }
        } catch (TwitchException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
}
