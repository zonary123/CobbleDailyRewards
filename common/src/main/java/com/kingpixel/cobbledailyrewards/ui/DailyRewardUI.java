package com.kingpixel.cobbledailyrewards.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbledailyrewards.database.DatabaseClientFactory;
import com.kingpixel.cobbledailyrewards.models.UserInfo;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.util.*;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 15/08/2024 15:41
 */
public class DailyRewardUI {
  public static GooeyPage getPage(ServerPlayerEntity player) {

    ChestTemplate template = ChestTemplate
      .builder(CobbleDailyRewards.config.getRows())
      .build();

    CobbleDailyRewards.config.getRewards().forEach(rewards -> {
      if (!CobbleDailyRewards.config.isActive()) return;
      UserInfo userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player);
      Date cooldown = new Date(userInfo.getCooldowns().getOrDefault(rewards.getId(), 1L));
      boolean isCooldown = DatabaseClientFactory.databaseClient.isCooldownActive(rewards, player);
      ItemModel item;
      if (rewards.getPermission().isEmpty() || LuckPermsUtil.checkPermission(player, rewards.getPermission())) {
        if (isCooldown) {
          item = rewards.getWithCooldown();
        } else {
          item = rewards.getWithoutCooldown();
        }
      } else {
        item = CobbleDailyRewards.language.getNoPermission();
      }
      String displayname = item.getDisplayname()
        .replace("%cooldown%", PlayerUtils.getCooldown(cooldown));
      List<String> lore = new ArrayList<>(item.getLore());
      lore.replaceAll(s -> s
        .replace("%cooldown%", PlayerUtils.getCooldown(cooldown))
        .replace("%permission%", LuckPermsUtil.checkPermission(player, rewards.getPermission())
          ? CobbleDailyRewards.language.getHavePermission()
          : CobbleDailyRewards.language.getNotHavePermission())
      );
      GooeyButton button = GooeyButton
        .builder()
        .display(item.getItemStack())
        .title(AdventureTranslator.toNative(displayname))
        .lore(Text.class, AdventureTranslator.toNativeL(lore))
        .onClick(action -> {
          switch (action.getClickType()){
            case LEFT_CLICK, SHIFT_LEFT_CLICK -> {
              if (!rewards.getPermission().isEmpty()) {
                if (!LuckPermsUtil.checkPermission(action.getPlayer(),
                  rewards.getPermission())) {
                  action.getPlayer().sendMessage(
                    AdventureTranslator.toNative(
                      CobbleDailyRewards.language.getMessageNotHavePermission()
                        .replace("%prefix%", CobbleDailyRewards.language.getPrefix())
                    )
                  );
                  return;
                }
              }

              if (DatabaseClientFactory.databaseClient.isCooldownActive(rewards, action.getPlayer())) {
                action.getPlayer().sendMessage(
                  AdventureTranslator.toNative(
                    CobbleUtils.language.getMessageCooldown()
                      .replace("%prefix%", CobbleDailyRewards.language.getPrefix())
                      .replace("%cooldown%", PlayerUtils.getCooldown(new Date(DatabaseClientFactory
                        .databaseClient.getUserInfo(player).getCooldowns().getOrDefault(
                          rewards.getId(), 1L
                        )))
                      )
                  )
                );
                return;
              }
              rewards.getRewards().giveRewards(action.getPlayer());
              DatabaseClientFactory.databaseClient.updateUserInfo(rewards, action.getPlayer());
              UIManager.openUIForcefully(action.getPlayer(), getPage(action.getPlayer()));
            }
            case RIGHT_CLICK, SHIFT_RIGHT_CLICK, MIDDLE_CLICK -> rewards.getRewards().openMenu(action.getPlayer());
          }
        })
        .build();
      template.set(rewards.getSlot(), button);
    });

    ItemStack itemStack = Utils.parseItemId(CobbleDailyRewards.language.getFill());

    GooeyButton fill = GooeyButton.of(itemStack);
    template.fill(fill);

    GooeyButton close = UIUtils.getCloseButton(action -> {
      UIManager.closeUI(action.getPlayer());
    });

    template.set((CobbleDailyRewards.config.getRows() * 9) - 5, close);

    GooeyPage page = GooeyPage.builder()
      .title(AdventureTranslator.toNative(CobbleDailyRewards.language.getTitlemenu()))
      .template(template)
      .build();

    /*page.subscribe(DatabaseClientFactory.databaseClient.getUserInfo(player), page1 -> {
      UIManager.openUIForcefully(player, page1);
    });*/
    return page;
  }
}
