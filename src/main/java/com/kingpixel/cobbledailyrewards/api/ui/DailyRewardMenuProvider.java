package com.kingpixel.cobbledailyrewards.api.ui;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Service Provider Interface (SPI) for Daily Reward menu rendering.
 * Allows external companion mods (Rotom Phone, Custom GUI plugins, Web integrations)
 * to intercept menu opening and provide custom interfaces.
 */
public interface DailyRewardMenuProvider {

  /**
   * Opens the main daily rewards menu for the specified player.
   *
   * @param player the player opening the menu
   */
  void openMainMenu(ServerPlayerEntity player);

  /**
   * Opens the daily rewards menu for a viewer targeting another player (e.g., admin inspecting).
   *
   * @param viewer the viewing player
   * @param target the target player whose rewards are viewed
   */
  void openMainMenu(ServerPlayerEntity viewer, ServerPlayerEntity target);

  /**
   * Opens the reward preview menu displaying the contents of a specific daily reward.
   *
   * @param player   the player opening the preview
   * @param rewardId the identifier of the reward
   */
  void openRewardPreviewMenu(ServerPlayerEntity player, String rewardId);
}
