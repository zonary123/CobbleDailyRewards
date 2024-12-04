package com.kingpixel.cobbledailyrewards.managers;

import com.google.gson.Gson;
import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbledailyrewards.models.UserInfo;
import com.kingpixel.cobbleutils.Model.DataBaseType;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
@ToString
public class DailyRewardsManager {
  public static final String PATH_USER_INFO = CobbleDailyRewards.PATH + "/data/";

  private Map<UUID, UserInfo> userInfoMap = new HashMap<>();

  public void init(ServerPlayerEntity player) {
    UUID playerUUID = player.getUuid();
    if (CobbleDailyRewards.config.getDatabase().getType() == DataBaseType.JSON) {
      CompletableFuture<Boolean> futureRead = Utils.readFileAsync(PATH_USER_INFO, playerUUID + ".json",
        fileContent -> {
          Gson gson = new Gson();
          UserInfo userInfo = gson.fromJson(fileContent, UserInfo.class);
          userInfoMap.put(playerUUID, userInfo);
        });

      if (!futureRead.join()) {
        CobbleDailyRewards.LOGGER.info("No userinfo file found for " + CobbleDailyRewards.MOD_NAME + ". Attempting to generate one.");
        UserInfo newUserInfo = new UserInfo(player);
        userInfoMap.put(playerUUID, newUserInfo);
        newUserInfo.writeInfo(playerUUID);
      }
    }
    userInfoMap.get(playerUUID);
  }


}
