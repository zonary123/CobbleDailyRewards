package com.kingpixel.cobbledailyrewards.database;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kingpixel.cobbledailyrewards.models.Rewards;
import com.kingpixel.cobbledailyrewards.models.UserInfo;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import net.minecraft.server.network.ServerPlayerEntity;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of MySQLClient for the DatabaseClient interface.
 */
public class MySQLClient implements DatabaseClient {
  private final String url;
  private final String user;
  private final String password;
  private Connection connection;

  public MySQLClient(DataBaseConfig config) {
    this.url = config.getUrl();
    this.user = config.getUser();
    this.password = config.getPassword();
  }

  @Override
  public void connect() {
    try {
      connection = DriverManager.getConnection(url, user, password);
      try (Statement stmt = connection.createStatement()) {
        String sql = "CREATE TABLE IF NOT EXISTS user_info (" +
          "uuid VARCHAR(36) PRIMARY KEY," +
          "cooldowns TEXT)";
        stmt.executeUpdate(sql);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public UserInfo getUserInfo(ServerPlayerEntity player) {
    String uuid = player.getUuid().toString();
    try (PreparedStatement stmt = connection.prepareStatement("SELECT cooldowns FROM user_info WHERE uuid = ?")) {
      stmt.setString(1, uuid);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        String cooldownsJson = rs.getString("cooldowns");
        Map<String, Long> cooldowns = new Gson().fromJson(cooldownsJson, new TypeToken<HashMap<String, Long>>() {
        }.getType());
        return new UserInfo(player.getUuid(), player.getGameProfile().getName(), cooldowns);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return new UserInfo(player);
  }

  @Override
  public boolean isCooldownActive(Rewards rewards, ServerPlayerEntity player) {
    UserInfo userInfo = getUserInfo(player);
    return PlayerUtils.isCooldown(userInfo.getCooldowns().getOrDefault(rewards.getId(), 1L));
  }

  @Override
  public void updateUserInfo(Rewards rewards, ServerPlayerEntity player) {
    UserInfo userInfo = getUserInfo(player);
    userInfo.addCooldown(rewards, player);
    updateUserInfo(userInfo);
  }

  @Override
  public void disconnect() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void save() {
    // Optional implementation if required
  }

  @Override
  public void restart(ServerPlayerEntity player) {
    UserInfo userInfo = getUserInfo(player);
    userInfo.getCooldowns().clear();
    updateUserInfo(userInfo);
  }

  @Override
  public void updateUserInfo(UserInfo userInfo) {
    String uuid = userInfo.getUuid().toString();
    String cooldownsJson = new Gson().toJson(userInfo.getCooldowns());
    try (PreparedStatement stmt = connection.prepareStatement("REPLACE INTO user_info (uuid, cooldowns) VALUES (?, ?)")) {
      stmt.setString(1, uuid);
      stmt.setString(2, cooldownsJson);
      stmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}