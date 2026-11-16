package com.kingpixel.cobbledailyrewards.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Headless view model encapsulating the complete daily rewards context for a player.
 */
public class PlayerDailyRewardsContext {

  private final UUID playerUuid;
  private final String playerName;
  private final boolean systemActive;
  private final int totalRewards;
  private final int claimableCount;
  private final int onCooldownCount;
  private final int noPermissionCount;
  private final List<DailyRewardEntryView> rewards;

  public PlayerDailyRewardsContext(UUID playerUuid,
                                  String playerName,
                                  boolean systemActive,
                                  List<DailyRewardEntryView> rewards) {
    this.playerUuid = playerUuid;
    this.playerName = playerName != null ? playerName : "";
    this.systemActive = systemActive;
    this.rewards = rewards != null ? Collections.unmodifiableList(new ArrayList<>(rewards)) : Collections.emptyList();
    this.totalRewards = this.rewards.size();

    int claimable = 0;
    int onCooldown = 0;
    int noPerm = 0;

    for (DailyRewardEntryView reward : this.rewards) {
      if (reward.isClaimable()) {
        claimable++;
      } else if (reward.isOnCooldown()) {
        onCooldown++;
      } else if (!reward.hasPermission()) {
        noPerm++;
      }
    }

    this.claimableCount = claimable;
    this.onCooldownCount = onCooldown;
    this.noPermissionCount = noPerm;
  }

  public UUID getPlayerUuid() {
    return playerUuid;
  }

  public String getPlayerName() {
    return playerName;
  }

  public boolean isSystemActive() {
    return systemActive;
  }

  public int getTotalRewards() {
    return totalRewards;
  }

  public int getClaimableCount() {
    return claimableCount;
  }

  public int getOnCooldownCount() {
    return onCooldownCount;
  }

  public int getNoPermissionCount() {
    return noPermissionCount;
  }

  public List<DailyRewardEntryView> getRewards() {
    return rewards;
  }

  public boolean hasAnyClaimable() {
    return claimableCount > 0;
  }

  public Optional<DailyRewardEntryView> getReward(String rewardId) {
    if (rewardId == null) return Optional.empty();
    return rewards.stream()
      .filter(r -> r.getId().equalsIgnoreCase(rewardId))
      .findFirst();
  }

  @Override
  public String toString() {
    return "PlayerDailyRewardsContext{" +
      "playerUuid=" + playerUuid +
      ", playerName='" + playerName + '\'' +
      ", systemActive=" + systemActive +
      ", totalRewards=" + totalRewards +
      ", claimableCount=" + claimableCount +
      ", onCooldownCount=" + onCooldownCount +
      ", noPermissionCount=" + noPermissionCount +
      '}';
  }
}
