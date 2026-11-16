package com.kingpixel.cobbledailyrewards.api.ui;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Thread-safe registry for custom daily reward menu providers.
 */
public final class DailyRewardMenuRegistry {

  private static final DailyRewardMenuProvider DEFAULT_PROVIDER = new DefaultGooeyDailyRewardMenuProvider();
  private static volatile DailyRewardMenuProvider currentProvider = DEFAULT_PROVIDER;

  private DailyRewardMenuRegistry() {}

  /**
   * Registers a custom menu provider.
   *
   * @param provider the custom provider, or null to revert to default
   */
  public static synchronized void register(DailyRewardMenuProvider provider) {
    currentProvider = (provider != null) ? provider : DEFAULT_PROVIDER;
  }

  /**
   * Unregisters the custom provider, reverting to the default GooeyLibs implementation.
   */
  public static synchronized void unregister() {
    currentProvider = DEFAULT_PROVIDER;
  }

  /**
   * Returns whether a custom menu provider is currently registered.
   *
   * @return true if a non-default provider is active
   */
  public static boolean hasCustomProvider() {
    return currentProvider != DEFAULT_PROVIDER;
  }

  /**
   * Retrieves the currently active menu provider.
   *
   * @return the active menu provider
   */
  public static DailyRewardMenuProvider getProvider() {
    return currentProvider;
  }

  /**
   * Opens the main daily rewards menu for the given player using the active provider.
   *
   * @param player the player opening the menu
   */
  public static void openMainMenu(ServerPlayerEntity player) {
    currentProvider.openMainMenu(player);
  }

  /**
   * Opens the daily rewards menu for a viewer inspecting a target player using the active provider.
   *
   * @param viewer the viewing player
   * @param target the target player
   */
  public static void openMainMenu(ServerPlayerEntity viewer, ServerPlayerEntity target) {
    currentProvider.openMainMenu(viewer, target);
  }

  /**
   * Opens the reward preview menu for a reward using the active provider.
   *
   * @param player   the player opening the preview
   * @param rewardId the reward ID
   */
  public static void openRewardPreviewMenu(ServerPlayerEntity player, String rewardId) {
    currentProvider.openRewardPreviewMenu(player, rewardId);
  }
}
