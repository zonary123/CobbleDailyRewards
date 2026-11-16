package com.kingpixel.cobbledailyrewards.application;

import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbledailyrewards.api.model.DailyRewardEntryView;
import com.kingpixel.cobbledailyrewards.api.model.PlayerDailyRewardsContext;
import com.kingpixel.cobbledailyrewards.database.DatabaseClientFactory;
import com.kingpixel.cobbledailyrewards.models.Rewards;
import com.kingpixel.cobbledailyrewards.models.UserInfo;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Application service providing query and projection views for Daily Rewards.
 */
public class DailyRewardContextService {

  /**
   * Retrieves the full daily rewards context for a given player asynchronously.
   *
   * @param player the player whose context is requested
   * @return future containing PlayerDailyRewardsContext
   */
  public CompletableFuture<PlayerDailyRewardsContext> getPlayerContext(ServerPlayerEntity player) {
    if (player == null) {
      return CompletableFuture.completedFuture(
        new PlayerDailyRewardsContext(UUID.randomUUID(), "", false, Collections.emptyList())
      );
    }

    return CompletableFuture.supplyAsync(() -> {
      UserInfo userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player);
      boolean isActive = CobbleDailyRewards.config.isActive();
      List<DailyRewardEntryView> entries = new ArrayList<>();

      for (Rewards reward : CobbleDailyRewards.rewardsConfig.getRewards()) {
        entries.add(buildEntryView(player, reward, userInfo, isActive));
      }

      return new PlayerDailyRewardsContext(
        player.getUuid(),
        player.getGameProfile().getName(),
        isActive,
        entries
      );
    }, CobbleDailyRewards.EXECUTOR_DAILY_REWARDS);
  }

  /**
   * Retrieves a single daily reward view for a given player asynchronously.
   *
   * @param player   the player
   * @param rewardId the reward ID
   * @return future containing DailyRewardEntryView, or null if reward not found
   */
  public CompletableFuture<DailyRewardEntryView> getRewardView(ServerPlayerEntity player, String rewardId) {
    if (player == null || rewardId == null) {
      return CompletableFuture.completedFuture(null);
    }

    return CompletableFuture.supplyAsync(() -> {
      Rewards target = null;
      for (Rewards reward : CobbleDailyRewards.rewardsConfig.getRewards()) {
        if (reward.getId().equalsIgnoreCase(rewardId)) {
          target = reward;
          break;
        }
      }

      if (target == null) return null;

      UserInfo userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player);
      return buildEntryView(player, target, userInfo, CobbleDailyRewards.config.isActive());
    }, CobbleDailyRewards.EXECUTOR_DAILY_REWARDS);
  }

  private DailyRewardEntryView buildEntryView(ServerPlayerEntity player,
                                              Rewards reward,
                                              UserInfo userInfo,
                                              boolean isSystemActive) {
    boolean hasPermission = reward.getPermission() == null
      || reward.getPermission().isEmpty()
      || LuckPermsUtil.checkPermission(player, reward.getPermission());

    boolean onCooldown = DatabaseClientFactory.databaseClient.isCooldownActive(reward, player);

    long cooldownTimestamp = (userInfo != null && userInfo.getCooldowns() != null)
      ? userInfo.getCooldowns().getOrDefault(reward.getId(), 1L)
      : 1L;

    long remainingMillis = Math.max(0L, cooldownTimestamp - System.currentTimeMillis());
    String formattedCooldown = PlayerUtils.getCooldown(cooldownTimestamp);

    ItemModel displayItem;
    if (!hasPermission) {
      displayItem = CobbleDailyRewards.language.getNoPermission();
    } else if (onCooldown) {
      displayItem = reward.getWithCooldown();
    } else {
      displayItem = reward.getWithoutCooldown();
    }

    String displayName = displayItem.getDisplayname()
      .replace("%cooldown%", formattedCooldown);

    List<String> lore = new ArrayList<>(displayItem.getLore());
    lore.replaceAll(s -> s
      .replace("%cooldown%", formattedCooldown)
      .replace("%permission%", hasPermission
        ? CobbleDailyRewards.language.getHavePermission()
        : CobbleDailyRewards.language.getNotHavePermission())
    );

    boolean claimable = isSystemActive && hasPermission && !onCooldown;

    return new DailyRewardEntryView(
      reward.getId(),
      reward.getSlot(),
      displayName,
      lore,
      hasPermission,
      onCooldown,
      remainingMillis,
      formattedCooldown,
      claimable,
      displayItem
    );
  }
}
