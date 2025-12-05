package com.kingpixel.cobbledailyrewards.database;

import com.google.gson.Gson;
import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbledailyrewards.models.Rewards;
import com.kingpixel.cobbledailyrewards.models.UserInfo;
import com.kingpixel.cobbleutils.Model.DataBaseType;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.Utils;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 07/08/2024 9:41
 */
public class JSONClient extends DatabaseClient {


  public JSONClient(String uri, String user, String password) {
  }

  @Override public void connect() {
  }

  @Override public UserInfo getUserInfo(ServerPlayerEntity player) {
    UserInfo userInfo = CobbleDailyRewards.userInfoMap.get(player.getUuid());
    if (userInfo == null) {
      UUID playerUUID = player.getUuid();
      if (CobbleDailyRewards.config.getDatabase().getType() == DataBaseType.JSON) {
        CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleDailyRewards.PATH_DATA, playerUUID + ".json",
          fileContent -> {
            Gson gson = Utils.newWithoutSpacingGson();
            CobbleDailyRewards.userInfoMap.put(playerUUID, gson.fromJson(fileContent, UserInfo.class));
          });

        if (!futureRead.join()) {
          CobbleDailyRewards.LOGGER.info("No userinfo file found for " + CobbleDailyRewards.MOD_NAME + ". Attempting to generate one.");
          UserInfo newUserInfo = new UserInfo(player);
          CobbleDailyRewards.userInfoMap.put(playerUUID, newUserInfo);
          newUserInfo.writeInfo(playerUUID);
        }
      }
    }
    return CobbleDailyRewards.userInfoMap.get(player.getUuid());
  }

  @Override public boolean isCooldownActive(Rewards rewards, ServerPlayerEntity player) {
    UserInfo userInfo = getUserInfo(player);
    return PlayerUtils.isCooldown(userInfo.getCooldowns().getOrDefault(rewards.getId(), 1L));
  }

  @Override public void updateUserInfo(Rewards rewards, ServerPlayerEntity player) {
    UserInfo userInfo = getUserInfo(player);
    userInfo.addCooldown(rewards, player);
    userInfo.writeInfo(player.getUuid());
  }


  @Override public void disconnect() {

  }

  @Override public void save() {

  }

  @Override public void restart(ServerPlayerEntity player) {
    UserInfo userInfo = getUserInfo(player);
    userInfo.getCooldowns().clear();
    userInfo.writeInfo(player.getUuid());
  }

  @Override public void updateUserInfo(UserInfo userInfo) {
    userInfo.writeInfo(userInfo.getUuid());
  }
}
