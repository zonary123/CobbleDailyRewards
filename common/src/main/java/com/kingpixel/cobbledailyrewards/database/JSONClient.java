package com.kingpixel.cobbledailyrewards.database;

import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbledailyrewards.models.Rewards;
import com.kingpixel.cobbledailyrewards.models.UserInfo;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 07/08/2024 9:41
 */
public class JSONClient implements DatabaseClient {
  public JSONClient(String uri, String user, String password) {
  }

  @Override public void connect() {
  }

  @Override public UserInfo getUserInfo(ServerPlayerEntity player) {
    return CobbleDailyRewards.manager.getUserInfoMap().getOrDefault(player.getUuid(), new UserInfo(player));
  }

  @Override public boolean isCooldownActive(Rewards rewards, ServerPlayerEntity player) {
    UserInfo userInfo = getUserInfo(player);
    Date date = userInfo.getCooldowns().getOrDefault(rewards.getId(), new Date(1));
    return date.after(new Date());
  }

  @Override public void updateUserInfo(Rewards rewards, ServerPlayerEntity player) {
    UserInfo userInfo = getUserInfo(player);
    userInfo.getCooldowns().compute(rewards.getId(),
      (k, v) -> new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(rewards.getCooldown()))
    );
    CobbleDailyRewards.manager.getUserInfoMap().put(player.getUuid(), userInfo);
    userInfo.writeInfo(player.getUuid());
  }


  @Override public void disconnect() {

  }

  @Override public void save() {

  }
}
