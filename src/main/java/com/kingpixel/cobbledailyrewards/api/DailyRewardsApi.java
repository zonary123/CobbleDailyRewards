package com.kingpixel.cobbledailyrewards.api;

import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbledailyrewards.api.action.DailyRewardActionResult;
import com.kingpixel.cobbledailyrewards.api.model.DailyRewardEntryView;
import com.kingpixel.cobbledailyrewards.api.model.PlayerDailyRewardsContext;
import com.kingpixel.cobbledailyrewards.api.ui.DailyRewardMenuProvider;
import com.kingpixel.cobbledailyrewards.api.ui.DailyRewardMenuRegistry;
import com.kingpixel.cobbledailyrewards.application.DailyRewardActionService;
import com.kingpixel.cobbledailyrewards.application.DailyRewardContextService;
import com.kingpixel.cobbledailyrewards.database.DatabaseClient;
import com.kingpixel.cobbledailyrewards.database.DatabaseClientFactory;
import com.kingpixel.cobbledailyrewards.models.Rewards;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Unified Public API for CobbleDailyRewards.
 * Serves as the primary entry point for companion mods (Rotom Phone, custom GUIs, Web panels)
 * to interact with the daily rewards system headlessly or via pluggable UI providers.
 */
public final class DailyRewardsApi {

  private static final DailyRewardActionService ACTION_SERVICE = new DailyRewardActionService();
  private static final DailyRewardContextService CONTEXT_SERVICE = new DailyRewardContextService();

  private DailyRewardsApi() {}

  // ================= UI / Menu Provider Methods =================

  /**
   * Opens the main daily rewards menu for a player using the registered menu provider.
   *
   * @param player the player opening the menu
   */
  public static void openMainMenu(ServerPlayerEntity player) {
    DailyRewardMenuRegistry.openMainMenu(player);
  }

  /**
   * Opens the daily rewards menu for a viewer inspecting a target player using the registered menu provider.
   *
   * @param viewer the viewing player
   * @param target the target player
   */
  public static void openMainMenu(ServerPlayerEntity viewer, ServerPlayerEntity target) {
    DailyRewardMenuRegistry.openMainMenu(viewer, target);
  }

  /**
   * Opens the reward preview menu for a reward using the registered menu provider.
   *
   * @param player   the player opening the preview
   * @param rewardId the ID of the reward
   */
  public static void openRewardPreviewMenu(ServerPlayerEntity player, String rewardId) {
    DailyRewardMenuRegistry.openRewardPreviewMenu(player, rewardId);
  }

  /**
   * Registers a custom menu provider.
   *
   * @param provider the custom provider to register
   */
  public static void registerMenuProvider(DailyRewardMenuProvider provider) {
    DailyRewardMenuRegistry.register(provider);
  }

  /**
   * Unregisters the custom menu provider, reverting back to the default GooeyLibs implementation.
   */
  public static void unregisterMenuProvider() {
    DailyRewardMenuRegistry.unregister();
  }

  /**
   * Checks if a custom menu provider is currently registered.
   *
   * @return true if a custom provider is active
   */
  public static boolean hasCustomMenuProvider() {
    return DailyRewardMenuRegistry.hasCustomProvider();
  }

  /**
   * Retrieves the currently active menu provider.
   *
   * @return the active menu provider
   */
  public static DailyRewardMenuProvider getMenuProvider() {
    return DailyRewardMenuRegistry.getProvider();
  }

  // ================= Headless Action Methods =================

  /**
   * Claims a specific reward for a player without opening any GUI.
   *
   * @param player   the player claiming the reward
   * @param rewardId the reward ID
   * @return future containing the action result
   */
  public static CompletableFuture<DailyRewardActionResult> claimReward(ServerPlayerEntity player, String rewardId) {
    return ACTION_SERVICE.claimReward(player, rewardId);
  }

  /**
   * Claims all available, claimable rewards for a player without opening any GUI.
   *
   * @param player the player claiming rewards
   * @return future containing aggregate action result
   */
  public static CompletableFuture<DailyRewardActionResult> claimAllAvailable(ServerPlayerEntity player) {
    return ACTION_SERVICE.claimAllAvailable(player);
  }

  /**
   * Resets all daily reward cooldowns for a player.
   *
   * @param player the target player
   * @return future containing the action result
   */
  public static CompletableFuture<DailyRewardActionResult> resetCooldowns(ServerPlayerEntity player) {
    return ACTION_SERVICE.resetCooldowns(player);
  }

  // ================= Query / View Context Methods =================

  /**
   * Retrieves the complete headless view model context for a player.
   *
   * @param player the player
   * @return future containing PlayerDailyRewardsContext
   */
  public static CompletableFuture<PlayerDailyRewardsContext> getPlayerContext(ServerPlayerEntity player) {
    return CONTEXT_SERVICE.getPlayerContext(player);
  }

  /**
   * Retrieves a single daily reward view model for a player.
   *
   * @param player   the player
   * @param rewardId the reward identifier
   * @return future containing DailyRewardEntryView, or null if not found
   */
  public static CompletableFuture<DailyRewardEntryView> getRewardView(ServerPlayerEntity player, String rewardId) {
    return CONTEXT_SERVICE.getRewardView(player, rewardId);
  }

  // ================= Services & State =================

  /**
   * Gets the singleton action service.
   *
   * @return the action service
   */
  public static DailyRewardActionService getActionService() {
    return ACTION_SERVICE;
  }

  /**
   * Gets the singleton context service.
   *
   * @return the context service
   */
  public static DailyRewardContextService getContextService() {
    return CONTEXT_SERVICE;
  }

  /**
   * Checks whether the daily rewards system is globally enabled.
   *
   * @return true if enabled
   */
  public static boolean isActive() {
    return CobbleDailyRewards.config != null && CobbleDailyRewards.config.isActive();
  }

  /**
   * Gets the list of configured rewards.
   *
   * @return list of configured rewards
   */
  public static List<Rewards> getRewards() {
    return CobbleDailyRewards.rewardsConfig != null ? CobbleDailyRewards.rewardsConfig.getRewards() : List.of();
  }

  /**
   * Gets the current database client.
   *
   * @return the database client instance
   */
  public static DatabaseClient getDatabaseClient() {
    return DatabaseClientFactory.databaseClient;
  }
}
