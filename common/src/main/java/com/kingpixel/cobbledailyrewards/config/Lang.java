package com.kingpixel.cobbledailyrewards.config;

import com.google.gson.Gson;
import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;

@Getter
public class Lang {
  private String prefix;
  private String titlemenu;
  private String messageReload;
  private String notHavePermission;
  private String HavePermission;
  private String messageHavePermission;
  private String messageNotHavePermission;
  private String messageCanClaim;
  private String fill;
  private ItemModel noPermission;

  /**
   * Constructor to generate a file if one doesn't exist.
   */
  public Lang() {
    this.prefix = "&8[&6CobbleDailyRewards&8] ";
    this.titlemenu = "&6Daily Rewards";
    this.messageReload = "%prefix% &aReloaded.";
    this.noPermission = new ItemModel("minecraft:barrier");
    this.notHavePermission = "&cNot Permission";
    this.HavePermission = "&aHave Permission";
    this.fill = "minecraft:gray_stained_glass_pane";
    this.messageCanClaim = "%prefix% &aYou can claim dailyRewards.";
    this.messageHavePermission = "%prefix% &aYou have permission to claim this reward.";
    this.messageNotHavePermission = "%prefix% &cYou do not have permission to claim this reward.";
  }

  /**
   * Method to initialize the config.
   */
  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleDailyRewards.PATH_LANG,
      CobbleDailyRewards.config.getLang() + ".json",
      el -> {
        Gson gson = Utils.newGson();
        Lang lang = gson.fromJson(el, Lang.class);
        this.prefix = lang.getPrefix();
        this.titlemenu = lang.getTitlemenu();
        this.messageReload = lang.getMessageReload();
        this.noPermission = lang.getNoPermission();
        this.fill = lang.getFill();
        this.notHavePermission = lang.getNotHavePermission();
        this.HavePermission = lang.getHavePermission();
        this.messageHavePermission = lang.getMessageHavePermission();
        this.messageNotHavePermission = lang.getMessageNotHavePermission();
        this.messageCanClaim = lang.getMessageCanClaim();

        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleDailyRewards.PATH_LANG, CobbleDailyRewards.config.getLang() +
            ".json",
          data);
        if (!futureWrite.join()) {
          CobbleDailyRewards.LOGGER.fatal("Could not write lang.json file for " + CobbleDailyRewards.MOD_NAME + ".");
        }
      });

    if (!futureRead.join()) {
      CobbleDailyRewards.LOGGER.info("No lang.json file found for" + CobbleDailyRewards.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleDailyRewards.PATH_LANG, CobbleDailyRewards.config.getLang() +
          ".json",
        data);

      if (!futureWrite.join()) {
        CobbleDailyRewards.LOGGER.fatal("Could not write lang.json file for " + CobbleDailyRewards.MOD_NAME + ".");
      }
    }
  }

}
