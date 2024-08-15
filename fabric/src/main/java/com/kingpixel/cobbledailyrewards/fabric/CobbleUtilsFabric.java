package com.kingpixel.cobbledailyrewards.fabric;

import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import net.fabricmc.api.ModInitializer;

public class CobbleUtilsFabric implements ModInitializer {
  @Override
  public void onInitialize() {
    CobbleDailyRewards.init();
  }
}
