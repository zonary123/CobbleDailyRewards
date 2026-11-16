import com.kingpixel.cobbledailyrewards.api.action.DailyRewardActionResult;
import com.kingpixel.cobbledailyrewards.api.model.DailyRewardEntryView;
import com.kingpixel.cobbledailyrewards.api.model.PlayerDailyRewardsContext;
import com.kingpixel.cobbledailyrewards.api.ui.DailyRewardMenuProvider;
import com.kingpixel.cobbledailyrewards.api.ui.DailyRewardMenuRegistry;
import com.kingpixel.cobbledailyrewards.application.DailyRewardActionService;
import net.minecraft.server.network.ServerPlayerEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestDailyRewardsApi {

  @AfterEach
  void tearDown() {
    DailyRewardMenuRegistry.unregister();
  }

  @Test
  void testActionResultSuccess() {
    DailyRewardActionResult result = DailyRewardActionResult.successClaim("Claimed", "day_1");
    assertTrue(result.isSuccess());
    assertEquals(DailyRewardActionResult.Status.SUCCESS, result.getStatus());
    assertEquals("Claimed", result.getMessage());
    assertEquals("day_1", result.getRewardId());
    assertEquals(0L, result.getCooldownRemainingMillis());
    assertEquals(1, result.getClaimedCount());
  }

  @Test
  void testActionResultCooldown() {
    DailyRewardActionResult result = DailyRewardActionResult.cooldown("Wait", "vip_reward", 60000L);
    assertFalse(result.isSuccess());
    assertEquals(DailyRewardActionResult.Status.ON_COOLDOWN, result.getStatus());
    assertEquals("vip_reward", result.getRewardId());
    assertEquals(60000L, result.getCooldownRemainingMillis());
    assertEquals(0, result.getClaimedCount());
  }

  @Test
  void testActionResultFailures() {
    DailyRewardActionResult notFound = DailyRewardActionResult.failure(
      DailyRewardActionResult.Status.REWARD_NOT_FOUND,
      "Not found",
      "missing_id"
    );
    assertFalse(notFound.isSuccess());
    assertEquals(DailyRewardActionResult.Status.REWARD_NOT_FOUND, notFound.getStatus());
    assertEquals("missing_id", notFound.getRewardId());

    DailyRewardActionResult denied = DailyRewardActionResult.failure(
      DailyRewardActionResult.Status.PERMISSION_DENIED,
      "No perm"
    );
    assertFalse(denied.isSuccess());
    assertEquals(DailyRewardActionResult.Status.PERMISSION_DENIED, denied.getStatus());
  }

  @Test
  void testMenuRegistryRegistrationAndFallback() {
    assertFalse(DailyRewardMenuRegistry.hasCustomProvider());

    DailyRewardMenuProvider custom = new DailyRewardMenuProvider() {
      @Override
      public void openMainMenu(ServerPlayerEntity player) {}

      @Override
      public void openMainMenu(ServerPlayerEntity viewer, ServerPlayerEntity target) {}

      @Override
      public void openRewardPreviewMenu(ServerPlayerEntity player, String rewardId) {}
    };

    DailyRewardMenuRegistry.register(custom);
    assertTrue(DailyRewardMenuRegistry.hasCustomProvider());
    assertEquals(custom, DailyRewardMenuRegistry.getProvider());

    // Registering null reverts to default
    DailyRewardMenuRegistry.register(null);
    assertFalse(DailyRewardMenuRegistry.hasCustomProvider());

    // Re-register and unregister
    DailyRewardMenuRegistry.register(custom);
    assertTrue(DailyRewardMenuRegistry.hasCustomProvider());
    DailyRewardMenuRegistry.unregister();
    assertFalse(DailyRewardMenuRegistry.hasCustomProvider());
  }

  @Test
  void testDailyRewardEntryView() {
    List<String> lore = List.of("Line 1", "Line 2");
    DailyRewardEntryView view = new DailyRewardEntryView(
      "reward_1",
      (short) 4,
      "Daily Diamond",
      lore,
      true,
      false,
      0L,
      "Ready",
      true,
      null
    );

    assertEquals("reward_1", view.getId());
    assertEquals((short) 4, view.getSlot());
    assertEquals("Daily Diamond", view.getDisplayName());
    assertEquals(2, view.getLore().size());
    assertTrue(view.hasPermission());
    assertFalse(view.isOnCooldown());
    assertEquals(0L, view.getCooldownRemainingMillis());
    assertEquals("Ready", view.getFormattedCooldown());
    assertTrue(view.isClaimable());
  }

  @Test
  void testPlayerDailyRewardsContext() {
    UUID uuid = UUID.randomUUID();

    DailyRewardEntryView claimable = new DailyRewardEntryView(
      "day_1", (short) 0, "Day 1", List.of(), true, false, 0L, "Ready", true, null
    );
    DailyRewardEntryView onCooldown = new DailyRewardEntryView(
      "day_2", (short) 1, "Day 2", List.of(), true, true, 5000L, "5s", false, null
    );
    DailyRewardEntryView noPerm = new DailyRewardEntryView(
      "vip_1", (short) 2, "VIP 1", List.of(), false, false, 0L, "Ready", false, null
    );

    PlayerDailyRewardsContext ctx = new PlayerDailyRewardsContext(
      uuid,
      "PlayerTest",
      true,
      List.of(claimable, onCooldown, noPerm)
    );

    assertEquals(uuid, ctx.getPlayerUuid());
    assertEquals("PlayerTest", ctx.getPlayerName());
    assertTrue(ctx.isSystemActive());
    assertEquals(3, ctx.getTotalRewards());
    assertEquals(1, ctx.getClaimableCount());
    assertEquals(1, ctx.getOnCooldownCount());
    assertEquals(1, ctx.getNoPermissionCount());
    assertTrue(ctx.hasAnyClaimable());

    Optional<DailyRewardEntryView> found = ctx.getReward("day_2");
    assertTrue(found.isPresent());
    assertEquals("day_2", found.get().getId());
    assertTrue(found.get().isOnCooldown());

    Optional<DailyRewardEntryView> missing = ctx.getReward("non_existing");
    assertFalse(missing.isPresent());
  }

  @Test
  void testActionServiceNullSafety() throws ExecutionException, InterruptedException {
    DailyRewardActionService service = new DailyRewardActionService();

    DailyRewardActionResult r1 = service.claimReward(null, "id").get();
    assertNotNull(r1);
    assertEquals(DailyRewardActionResult.Status.ERROR, r1.getStatus());

    DailyRewardActionResult r2 = service.claimReward(null, null).get();
    assertNotNull(r2);
    assertEquals(DailyRewardActionResult.Status.ERROR, r2.getStatus());

    DailyRewardActionResult r3 = service.claimAllAvailable(null).get();
    assertNotNull(r3);
    assertEquals(DailyRewardActionResult.Status.ERROR, r3.getStatus());

    DailyRewardActionResult r4 = service.resetCooldowns(null).get();
    assertNotNull(r4);
    assertEquals(DailyRewardActionResult.Status.ERROR, r4.getStatus());
  }
}
