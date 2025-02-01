package com.kingpixel.cobbledailyrewards.config;

import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbledailyrewards.models.Rewards;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 12/01/2025 6:57
 */
@Getter
public class RewardsConfig {
  public List<Rewards> rewards = new ArrayList<>();

  public void init() {
    rewards.clear();
    File folder = Utils.getAbsolutePath(CobbleDailyRewards.PATH_REWARDS);
    if (!folder.exists()) {
      folder.mkdirs();
    }
    File[] files = folder.listFiles();

    if (files == null) {
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleDailyRewards.PATH_REWARDS, "default.json",
        Utils.newGson().toJson(new Rewards()));
      if (!futureWrite.join()) {
        CobbleDailyRewards.LOGGER.fatal("Could not write default.json file for " + CobbleDailyRewards.MOD_NAME + ".");
      }
      return;
    }

    for (File file : files) {
      if (file.getName().endsWith(".json")) {
        CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleDailyRewards.PATH_REWARDS, file.getName(),
          call -> {
            Rewards reward = Utils.newGson().fromJson(call, Rewards.class);
            reward.setId(file.getName().replace(".json", ""));
            rewards.add(reward);
          });
        if (!futureRead.join()) {
          CobbleDailyRewards.LOGGER.fatal("Could not read " + file.getName() + " file for " + CobbleDailyRewards.MOD_NAME + ".");
        }
      }
    }

  }
}
