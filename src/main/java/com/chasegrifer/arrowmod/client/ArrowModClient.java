package com.chasegrifer.arrowmod.client;

import net.fabricmc.api.ClientModInitializer;
import com.chasegrifer.arrowmod.ArrowShooter;

public class ArrowModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArrowShooter.register();
    }
}