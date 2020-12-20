package com.twitch.database;

import com.twitch.entity.Item;
import com.twitch.entity.ItemType;

import java.sql.*;
import java.util.*;

public class MySQLClient {
    private final Connection conn;

    public MySQLClient() throws MySQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(MySQLUtil.getMySQLAddress());
        } catch (Exception e) {
            e.printStackTrace();
            throw new MySQLException("Failed to connect to Database");
        }
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveItem(Item item) throws MySQLException {
        if (conn == null) {
            System.err.println("Failed to connect to DB");
            throw new MySQLException("Failed to connect to MySQL");
        }
        String sql = "INSERT IGNORE INTO items VALUES(?, ?, ?, ?, ?, ?, ?)";
        try {
            // Prepared statement type allows for building the query.
            // This resembles the string format function in Java.
            // With additional checks and prevents SQL injection
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, item.getId());
            statement.setString(2, item.getTitle());
            statement.setString(3, item.getUrl());
            statement.setString(4, item.getThumbnailUrl());
            statement.setString(5, item.getBroadcasterName());
            statement.setString(6, item.getGameId());
            statement.setString(7, item.getType().toString());
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setFavoriteItem(String userId, Item item)  throws MySQLException {
        if (conn == null) {
            System.err.println("Failed to connect to DB");
            throw new MySQLException("Cannot connect to DB");
        }
        saveItem(item);
        // Ignore keyword ignore duplicate entry, no need to throw exception.
        String sql = "INSERT IGNORE INTO favorite_records (user_id, item_id) VALUES(?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, item.getId());
            statement.execute();

        } catch (Exception e) {
            e.printStackTrace();
            throw new MySQLException("Cannot insert into favorite item");
        }
    }

    public void unsetFavoriteItem(String userId, Item item) throws MySQLException {
        if (conn == null) {
            System.err.println("failed to connect to DB");
            throw new MySQLException("Cannot connect to database");
        }

        String sql = "DELETE FROM favorite_records WHERE user_id = ? and item_id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, item.getId());
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
            throw new MySQLException("Failed to unset favorite");
        }
    }

    public Set<String> getFavoriteItemIds(String userId) throws MySQLException {
        Set<String> output = new HashSet<>();
        if (conn == null) {
            System.err.println("Cannot retrieve favorite items");
            throw new MySQLException("Cannot retrieve favorite items");
        }
        String sql = "SELECT item_id FROM favorite_records where user_id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet res = statement.executeQuery();
            // Result set interface is like an iterator
            while (res.next()) {
                System.out.print(res);
                output.add(res.getString("item_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MySQLException("Cannot retrieve favorite item");
        }

        return output;
    }

    public Map<String, List<Item>> getFavoriteItems(String userId) throws MySQLException {
        Map<String, List<Item>> output = new HashMap<>();
        if (conn == null) {
            System.err.println("Cannot retrieve favorite items");
            throw new MySQLException("Cannot retrieve favorite items");
        }
        for (ItemType type : ItemType.values()) {
            output.put(type.name(), new ArrayList<>());
        }
        Set<String> favoriteItemIds = getFavoriteItemIds(userId);
        String sql = "SELECT * FROM items WHERE id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (String id : favoriteItemIds) {
                statement.setString(1, id);
                ResultSet res = statement.executeQuery();
                if (res.next()) {
                    ItemType itemType = ItemType.valueOf(res.getString("type"));
                    Item item = new Item.Builder().id(res.getString("id")).
                            title(res.getString("title")).url(res.getString("url"))
                            .thumbnailUrl(res.getString("thumbnail_url"))
                            .broadcasterName(res.getString("broadcaster_name"))
                            .gameId(res.getString("game_id")).type(itemType).build();
                    output.get(item.getType().toString()).add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MySQLException("Cannot retrieve favorite items");
        }
        return output;
    }
}
