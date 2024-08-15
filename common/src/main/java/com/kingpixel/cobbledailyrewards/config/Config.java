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
  private String lang;
  private DataBaseConfig database;
  private short rows;
  private int checkReward;
  private List<Rewards> rewards;


  public Config() {
    this.debug = false;
    this.lang = "en";
    this.database = new DataBaseConfig(
      DataBaseType.JSON,
      "",
      "dailyrewards",
      "user",
      "password"
    );
    this.rows = 6;
    this.checkReward = 15;
    this.rewards = new ArrayList<>();
    this.rewards.add(new Rewards());
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleDailyRewards.PATH, "config.json",
      el -> {
        Gson gson = Utils.newGson();
        Config config = gson.fromJson(el, Config.class);
        this.debug = config.isDebug();
        this.lang = config.getLang();
        this.database = config.getDatabase();
        this.rows = config.getRows();
        this.checkReward = config.getCheckReward();
        this.rewards = config.getRewards();
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

  }

}