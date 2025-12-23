package com.kingpixel.cobbledailyrewards.config;

import com.google.gson.Gson;
import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
public class Lang {
  private String prefix;
  private String cooldown;
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
    this.cooldown = "%prefix% &cYou have to wait %cooldown% to claim this reward again.";
    this.titlemenu = "&6Daily Rewards";
    this.messageReload = "%prefix% &aReloaded.";
    this.noPermission = new ItemModel("minecraft:gray_dye", "&cNo Permission", List.of(
      "<red>You do not have permission to claim this reward."
    ));
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
        CobbleDailyRewards.language = gson.fromJson(el, Lang.class);
        String data = gson.toJson(CobbleDailyRewards.language);
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
      CobbleDailyRewards.language = this;
      String data = gson.toJson(CobbleDailyRewards.language);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleDailyRewards.PATH_LANG, CobbleDailyRewards.config.getLang() +
          ".json",
        data);

      if (!futureWrite.join()) {
        CobbleDailyRewards.LOGGER.fatal("Could not write lang.json file for " + CobbleDailyRewards.MOD_NAME + ".");
      }
    }
  }

}
