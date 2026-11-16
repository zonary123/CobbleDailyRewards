package com.kingpixel.cobbledailyrewards.application;

import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbledailyrewards.api.action.DailyRewardActionResult;
import com.kingpixel.cobbledailyrewards.database.DatabaseClientFactory;
import com.kingpixel.cobbledailyrewards.models.Rewards;
import com.kingpixel.cobbledailyrewards.models.UserInfo;
import com.kingpixel.cobbleutils.api.PermissionApi;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Application service providing headless actions for Daily Rewards.
 */
public class DailyRewardActionService {

  /**
   * Attempts to claim a specific daily reward for the given player.
   *
   * @param player   the player claiming the reward
   * @param rewardId the reward identifier
   * @return future containing the action result
   */
  public CompletableFuture<DailyRewardActionResult> claimReward(ServerPlayerEntity player, String rewardId) {
    if (player == null) {
      return CompletableFuture.completedFuture(
        DailyRewardActionResult.failure(DailyRewardActionResult.Status.ERROR, "Player cannot be null")
      );
    }
    if (rewardId == null || rewardId.trim().isEmpty()) {
      return CompletableFuture.completedFuture(
        DailyRewardActionResult.failure(DailyRewardActionResult.Status.REWARD_NOT_FOUND, "Reward ID cannot be null or empty")
      );
    }
    if (!CobbleDailyRewards.config.isActive()) {
      return CompletableFuture.completedFuture(
        DailyRewardActionResult.failure(DailyRewardActionResult.Status.DISABLED, "Daily rewards system is disabled")
      );
    }

    return CompletableFuture.supplyAsync(() -> {
      Rewards targetReward = null;
      for (Rewards reward : CobbleDailyRewards.rewardsConfig.getRewards()) {
        if (reward.getId().equalsIgnoreCase(rewardId)) {
          targetReward = reward;
          break;
        }
      }

      if (targetReward == null) {
        return DailyRewardActionResult.failure(
          DailyRewardActionResult.Status.REWARD_NOT_FOUND,
          "Reward not found: " + rewardId,
          rewardId
        );
      }

      // Check permissions
      if (!targetReward.getPermission().isEmpty()) {
        if (!PermissionApi.hasPermission(player, targetReward.getPermission(), 2)) {
          return DailyRewardActionResult.failure(
            DailyRewardActionResult.Status.PERMISSION_DENIED,
            CobbleDailyRewards.language.getMessageNotHavePermission(),
            rewardId
          );
        }
      }

      // Check cooldown
      if (DatabaseClientFactory.databaseClient.isCooldownActive(targetReward, player)) {
        UserInfo userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player);
        long cdTimestamp = (userInfo != null && userInfo.getCooldowns() != null)
          ? userInfo.getCooldowns().getOrDefault(targetReward.getId(), 0L)
          : 0L;
        long remaining = Math.max(0L, cdTimestamp - System.currentTimeMillis());
        return DailyRewardActionResult.cooldown("Reward is on cooldown", rewardId, remaining);
      }

      // Apply cooldown to database
      DatabaseClientFactory.databaseClient.updateUserInfo(targetReward, player);

      // Dispense rewards
      Rewards finalTarget = targetReward;
      if (CobbleDailyRewards.server != null) {
        CobbleDailyRewards.server.execute(() -> finalTarget.getRewards().giveRewards(player));
      } else {
        finalTarget.getRewards().giveRewards(player);
      }

      return DailyRewardActionResult.successClaim("Reward claimed successfully", rewardId);
    }, CobbleDailyRewards.EXECUTOR_DAILY_REWARDS);
  }

  /**
   * Attempts to claim all currently claimable rewards for the player.
   *
   * @param player the player claiming rewards
   * @return future containing aggregate action result
   */
  public CompletableFuture<DailyRewardActionResult> claimAllAvailable(ServerPlayerEntity player) {
    if (player == null) {
      return CompletableFuture.completedFuture(
        DailyRewardActionResult.failure(DailyRewardActionResult.Status.ERROR, "Player cannot be null")
      );
    }
    if (!CobbleDailyRewards.config.isActive()) {
      return CompletableFuture.completedFuture(
        DailyRewardActionResult.failure(DailyRewardActionResult.Status.DISABLED, "Daily rewards system is disabled")
      );
    }

    return CompletableFuture.supplyAsync(() -> {
      List<Rewards> toClaim = new ArrayList<>();
      for (Rewards reward : CobbleDailyRewards.rewardsConfig.getRewards()) {
        if (!reward.getPermission().isEmpty() && !PermissionApi.hasPermission(player, reward.getPermission(), 2)) {
          continue;
        }
        if (DatabaseClientFactory.databaseClient.isCooldownActive(reward, player)) {
          continue;
        }
        toClaim.add(reward);
      }

      if (toClaim.isEmpty()) {
        return DailyRewardActionResult.failure(
          DailyRewardActionResult.Status.ON_COOLDOWN,
          "No daily rewards available to claim at this time"
        );
      }

      for (Rewards reward : toClaim) {
        DatabaseClientFactory.databaseClient.updateUserInfo(reward, player);
        if (CobbleDailyRewards.server != null) {
          CobbleDailyRewards.server.execute(() -> reward.getRewards().giveRewards(player));
        } else {
          reward.getRewards().giveRewards(player);
        }
      }

      return DailyRewardActionResult.successBatch("Successfully claimed " + toClaim.size() + " rewards", toClaim.size());
    }, CobbleDailyRewards.EXECUTOR_DAILY_REWARDS);
  }

  /**
   * Resets all daily reward cooldowns for the specified player.
   *
   * @param player the player whose cooldowns should be reset
   * @return future containing the action result
   */
  public CompletableFuture<DailyRewardActionResult> resetCooldowns(ServerPlayerEntity player) {
    if (player == null) {
      return CompletableFuture.completedFuture(
        DailyRewardActionResult.failure(DailyRewardActionResult.Status.ERROR, "Player cannot be null")
      );
    }

    return CompletableFuture.supplyAsync(() -> {
      DatabaseClientFactory.databaseClient.restart(player);
      return DailyRewardActionResult.success("Cooldowns reset successfully for " + player.getGameProfile().getName());
    }, CobbleDailyRewards.EXECUTOR_DAILY_REWARDS);
  }
}
