package com.twitch.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitch.database.MySQLClient;
import com.twitch.database.MySQLException;
import com.twitch.entity.FavoriteRequestBody;
import com.twitch.entity.Item;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "FavoriteServlet", urlPatterns = {"/favorite"})
public class FavoriteServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        FavoriteRequestBody body = ServletUtil.getFavoriteRequestBody(request);
        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        MySQLClient connection = null;
        try {
            connection = new MySQLClient();
            connection.setFavoriteItem(request.getParameter("user_id"), body.getFavoriteItem());
        } catch (MySQLException e) {
            throw new ServletException(e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Map<String, List<Item>> itemMap;
        MySQLClient connection = null;
        try {
            connection = new MySQLClient();
            itemMap = connection.getFavoriteItems(request.getParameter("user_id"));
            ServletUtil.addItemsToResponse(response, itemMap);
        } catch (MySQLException e) {
            throw new ServletException(e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        FavoriteRequestBody body = ServletUtil.getFavoriteRequestBody(request);
        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        MySQLClient connection = null;
        try {
            connection = new MySQLClient();
            connection.unsetFavoriteItem(request.getParameter("user_id"), body.getFavoriteItem());
        } catch (MySQLException e) {
            throw new ServletException(e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
