package com.kingpixel.cobbledailyrewards.forge;

import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import net.minecraftforge.fml.common.Mod;

@Mod(CobbleDailyRewards.MOD_ID)
public class CobbleDailyRewardsForge {
  public CobbleDailyRewardsForge() {
    CobbleDailyRewards.init();
  }
}