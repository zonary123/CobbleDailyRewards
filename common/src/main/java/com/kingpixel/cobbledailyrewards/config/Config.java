package com.kingpixel.cobbledailyrewards.config;

import com.google.gson.Gson;
import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbledailyrewards.models.Rewards;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.Model.DataBaseType;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 29/04/2024 0:14
 */
@Getter
@Data
@ToString
public class Config {
  private boolean debug;
  private boolean active;
  private String lang;
  private List<String> commands;
  private DataBaseConfig database;
  private short rows;
  private int checkReward;
  private List<Rewards> rewards;


  public Config() {
    this.debug = false;
    this.active = true;
    this.lang = "en";
    this.commands = new ArrayList<>();
    this.commands.add("dailyrewards");
    this.database = new DataBaseConfig(
      DataBaseType.JSON,
      "dailyrewards",
      "mongodb://localhost:27017",
      "user",
      "password"
    );
    this.rows = 3;
    this.checkReward = 15;
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleDailyRewards.PATH, "config.json",
      el -> {
        Gson gson = Utils.newGson();
        CobbleDailyRewards.config = gson.fromJson(el, Config.class);
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleDailyRewards.PATH, "config.json",
          data);
        if (!futureWrite.join()) {
          CobbleDailyRewards.LOGGER.fatal("Could not write config.json file for " + CobbleDailyRewards.MOD_NAME + ".");
        }
      });

    if (!futureRead.join()) {
      CobbleDailyRewards.LOGGER.info("No config.json file found for" + CobbleDailyRewards.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleDailyRewards.PATH, "config.json",
        data);

      if (!futureWrite.join()) {
        CobbleDailyRewards.LOGGER.fatal("Could not write config.json file for " + CobbleDailyRewards.MOD_NAME + ".");
      }
    }


    if (CobbleDailyRewards.config.getRewards() != null && !CobbleDailyRewards.config.getRewards().isEmpty()) {
      for (Rewards reward : CobbleDailyRewards.config.getRewards()) {
        CompletableFuture<Boolean> futureWriteRewards = Utils.writeFileAsync(CobbleDailyRewards.PATH_REWARDS, reward.getId() + ".json", Utils.newGson().toJson(reward));
        if (!futureWriteRewards.join()) {
          CobbleDailyRewards.LOGGER.fatal("Could not write rewards.json file for " + CobbleDailyRewards.MOD_NAME + ".");
        }
      }
      rewards.clear();
    }

  }

}