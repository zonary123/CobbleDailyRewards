package com.kingpixel.cobbledailyrewards.api.ui;

import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbledailyrewards.models.Rewards;
import com.kingpixel.cobbledailyrewards.ui.DailyRewardUI;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Default GooeyLibs-based menu provider for CobbleDailyRewards.
 */
public class DefaultGooeyDailyRewardMenuProvider implements DailyRewardMenuProvider {

  @Override
  public void openMainMenu(ServerPlayerEntity player) {
    if (player == null) return;
    DailyRewardUI.open(player);
  }

  @Override
  public void openMainMenu(ServerPlayerEntity viewer, ServerPlayerEntity target) {
    if (target != null) {
      DailyRewardUI.open(target);
    } else if (viewer != null) {
      DailyRewardUI.open(viewer);
    }
  }

  @Override
  public void openRewardPreviewMenu(ServerPlayerEntity player, String rewardId) {
    if (player == null || rewardId == null) return;
    for (Rewards reward : CobbleDailyRewards.rewardsConfig.getRewards()) {
      if (reward.getId().equalsIgnoreCase(rewardId)) {
        if (CobbleDailyRewards.server != null) {
          CobbleDailyRewards.server.execute(() -> reward.getRewards().openMenu(player, template -> {}, close -> openMainMenu(player)));
        } else {
          reward.getRewards().openMenu(player, template -> {}, close -> openMainMenu(player));
        }
        break;
      }
    }
  }
}
