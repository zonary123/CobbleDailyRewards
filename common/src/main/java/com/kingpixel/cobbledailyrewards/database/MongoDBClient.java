package com.kingpixel.cobbledailyrewards.database;

import com.kingpixel.cobbledailyrewards.models.Rewards;
import com.kingpixel.cobbledailyrewards.models.UserInfo;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Carlos Varas Alonso - 07/08/2024 9:41
 */
public class MongoDBClient implements DatabaseClient {
  public MongoDBClient(String uri, String database, String user, String password) {
  }

  @Override public void connect() {

  }

  @Override public UserInfo getUserInfo(ServerPlayerEntity player) {
    return null;
  }

  @Override public boolean isCooldownActive(Rewards rewards, ServerPlayerEntity player) {
    return false;
  }

  @Override public void updateUserInfo(Rewards rewards, ServerPlayerEntity player) {

  }

  @Override public void disconnect() {

  }

  @Override public void save() {

  }
}
