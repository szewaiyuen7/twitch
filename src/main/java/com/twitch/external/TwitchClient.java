package com.twitch.external;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitch.entity.Game;
import com.twitch.entity.Item;
import com.twitch.entity.ItemType;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import java.io.IOException;
import java.util.*;

public class TwitchClient {
    private static final String GET_TOP_GAME_URL_TEMPLATE = "https://api.twitch.tv/helix/games/top?first=%s";
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    private static final String STREAM_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/streams?game_id=%s&first=%s";
    private static final String VIDEO_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/videos?game_id=%s&first=%s";
    private static final String CLIP_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/clips?game_id=%s&first=%s";
    private static final String TWITCH_BASE_URL = "https://www.twitch.tv/";
    private static final int DEFAULT_ITEM_LIMIT = 10;
    public static void main(String[] args) {
        TwitchClient test = new TwitchClient();
        String url = test.buildGameURL(GET_TOP_GAME_URL_TEMPLATE, "",10);
        String response = test.searchTwitch(url);
        List<Game> gameList = test.getGameList(response);
        for (Game game : gameList) {
            System.out.println(game.getName());
            System.out.println(game.getId());
        }
        List<Item> itemList = test.searchByType("21779", ItemType.STREAM, 10);
        for (Item item : itemList) {
            System.out.println(item.getUrl());
            System.out.println(item.getTitle());
        }

    }
    private String buildGameURL(String urlTemplate, String gameName, int limit) {
        /*
            This builds url for GET_TOP_GAME_URL_TEMPLATE and GAME_SEARCH_URL_TEMPLATE
         */
        if (limit <= 0) {
            limit = DEFAULT_ITEM_LIMIT;
        }
        if (gameName == "") {
            return String.format(urlTemplate, limit);
        }
        try {
            gameName = URLEncoder.encode(gameName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String gameUrl = String.format(urlTemplate, gameName);
        return gameUrl;
    }

    private String buildSearchURL(String urlTemplate, String gameId, int limit) {
        if (limit <= 0) {
            limit = DEFAULT_ITEM_LIMIT;
        }
        try {
            gameId = URLEncoder.encode(gameId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format(urlTemplate, gameId, limit);
    }

    private String searchTwitch(String url) throws TwitchException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = httpResponse -> {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                HttpEntity entity = httpResponse.getEntity();
                if (entity == null) {
                    throw new TwitchException("Failed to fetch data from Twitch");
                } else {
                    JSONObject obj = new JSONObject(EntityUtils.toString(entity));
                    return obj.getJSONArray("data").toString();
                }
            } else {
                throw new TwitchException("Failed to fetch data from Twitch");
            }
        };

        try {
            HttpGet getRequest = new HttpGet(url);
            TwitchUtils utils = new TwitchUtils();
            getRequest.setHeader("Authorization", utils.getToken());
            getRequest.setHeader("Client-Id", utils.getClientId());
            return httpClient.execute(getRequest, responseHandler);

        } catch(IOException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to fetch data from Twitch");
        }
    }

    private List<Game> getGameList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        Game[] gameList = null;
        try {
            gameList = mapper.readValue(data, Game[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to fetch data from twitch");
        }
        return Arrays.asList(gameList);
    }

    public List<Game> getTopGames(int limit) throws TwitchException {
        if (limit <= 0) {
            limit = DEFAULT_ITEM_LIMIT;
        }
        String url = buildGameURL(GET_TOP_GAME_URL_TEMPLATE, "", limit);
        List<Game> gameList = getGameList(searchTwitch(url));
        return gameList;
    }

    public Game searchGame(String gameName) throws TwitchException {
        String url = buildGameURL(GAME_SEARCH_URL_TEMPLATE, gameName, 1);
        List<Game> gameList = getGameList(searchTwitch(url));
        if (gameList.size() != 0) {
            return gameList.get(0);
        } else {
            return null;
        }
    }

    private List<Item> getItemList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(data, Item[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to fetch data from Twitch");
        }
    }

    private List<Item> searchStreams(String gameId, int limit) throws TwitchException {
        String url = buildSearchURL(STREAM_SEARCH_URL_TEMPLATE, gameId, limit);
        List<Item> itemList = getItemList(searchTwitch(url));
        for (Item item : itemList) {
            item.setType(ItemType.STREAM);
            item.setUrl(TWITCH_BASE_URL + item.getBroadcasterName());
        }
        return itemList;
    }

    private List<Item> searchVideos(String gameId, int limit) throws TwitchException {
        String url = buildSearchURL(VIDEO_SEARCH_URL_TEMPLATE, gameId, limit);
        List<Item> itemList = getItemList(searchTwitch(url));
        for (Item item : itemList) {
            item.setType(ItemType.VIDEO);
        }
        return itemList;
    }

    private List<Item> searchClips(String gameId, int limit) throws TwitchException {
        String url = buildSearchURL(CLIP_SEARCH_URL_TEMPLATE, gameId, limit);
        List<Item> itemList = getItemList(searchTwitch(url));
        for (Item item : itemList) {
            item.setType(ItemType.CLIP);
        }
        return itemList;
    }

    public List<Item> searchByType(String gameId, ItemType type, int limit) {
        List<Item> itemList = Collections.emptyList();
        switch (type) {
            case STREAM:
                itemList = searchStreams(gameId, limit);
            case VIDEO:
                itemList = searchVideos(gameId, limit);
            case CLIP:
                itemList = searchClips(gameId, limit);
        }
        return itemList;
    }

    public Map<String, List<Item>> searchItems(String gameId) {
        Map<String, List<Item>> output = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            output.put(type.toString(), searchByType(gameId, type, DEFAULT_ITEM_LIMIT));
        }
        return output;
    }

}
