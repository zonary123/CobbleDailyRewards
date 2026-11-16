package com.kingpixel.cobbledailyrewards.api.model;

import com.kingpixel.cobbleutils.Model.ItemModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Headless view model representing a single daily reward's state for a player.
 */
public class DailyRewardEntryView {

  private final String id;
  private final short slot;
  private final String displayName;
  private final List<String> lore;
  private final boolean hasPermission;
  private final boolean onCooldown;
  private final long cooldownRemainingMillis;
  private final String formattedCooldown;
  private final boolean claimable;
  private final ItemModel displayItem;

  public DailyRewardEntryView(String id,
                              short slot,
                              String displayName,
                              List<String> lore,
                              boolean hasPermission,
                              boolean onCooldown,
                              long cooldownRemainingMillis,
                              String formattedCooldown,
                              boolean claimable,
                              ItemModel displayItem) {
    this.id = id != null ? id : "";
    this.slot = slot;
    this.displayName = displayName != null ? displayName : "";
    this.lore = lore != null ? Collections.unmodifiableList(new ArrayList<>(lore)) : Collections.emptyList();
    this.hasPermission = hasPermission;
    this.onCooldown = onCooldown;
    this.cooldownRemainingMillis = Math.max(0L, cooldownRemainingMillis);
    this.formattedCooldown = formattedCooldown != null ? formattedCooldown : "";
    this.claimable = claimable;
    this.displayItem = displayItem;
  }

  public String getId() {
    return id;
  }

  public short getSlot() {
    return slot;
  }

  public String getDisplayName() {
    return displayName;
  }

  public List<String> getLore() {
    return lore;
  }

  public boolean hasPermission() {
    return hasPermission;
  }

  public boolean isOnCooldown() {
    return onCooldown;
  }

  public long getCooldownRemainingMillis() {
    return cooldownRemainingMillis;
  }

  public String getFormattedCooldown() {
    return formattedCooldown;
  }

  public boolean isClaimable() {
    return claimable;
  }

  public ItemModel getDisplayItem() {
    return displayItem;
  }

  @Override
  public String toString() {
    return "DailyRewardEntryView{" +
      "id='" + id + '\'' +
      ", slot=" + slot +
      ", displayName='" + displayName + '\'' +
      ", hasPermission=" + hasPermission +
      ", onCooldown=" + onCooldown +
      ", cooldownRemainingMillis=" + cooldownRemainingMillis +
      ", formattedCooldown='" + formattedCooldown + '\'' +
      ", claimable=" + claimable +
      '}';
  }
}
