package com.kingpixel.cobbledailyrewards.database;

import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbledailyrewards.models.Rewards;
import com.kingpixel.cobbledailyrewards.models.UserInfo;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Carlos Varas Alonso - 24/07/2024 21:02
 */
public abstract class DatabaseClient {
  abstract void connect();

  public abstract UserInfo getUserInfo(ServerPlayerEntity player);

  public abstract boolean isCooldownActive(Rewards rewards, ServerPlayerEntity player);

  public abstract void updateUserInfo(Rewards rewards, ServerPlayerEntity player);

  void disconnect() {
    CobbleDailyRewards.userInfoMap.clear();
  }

  public abstract void save();

  public abstract void restart(ServerPlayerEntity player);

  public abstract void updateUserInfo(UserInfo userInfo);

}
