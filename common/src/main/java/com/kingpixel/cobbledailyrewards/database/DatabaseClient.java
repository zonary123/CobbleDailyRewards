package com.kingpixel.cobbledailyrewards.database;

import com.kingpixel.cobbledailyrewards.models.Rewards;
import com.kingpixel.cobbledailyrewards.models.UserInfo;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Carlos Varas Alonso - 24/07/2024 21:02
 */
public interface DatabaseClient {
  void connect();

  UserInfo getUserInfo(ServerPlayerEntity player);

  boolean isCooldownActive(Rewards rewards, ServerPlayerEntity player);

  void updateUserInfo(Rewards rewards, ServerPlayerEntity player);

  void disconnect();

  void save();
}
