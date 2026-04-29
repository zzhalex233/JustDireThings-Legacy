package com.zzhalex.justdirethings.common.tile.base;

import net.minecraftforge.energy.IEnergyStorage;

public final class EnergyTransferHelper {

    private EnergyTransferHelper() {
    }

    public static int transmitPower(IEnergyStorage sender, IEnergyStorage receiver, int amountToSend) {
        if (sender == null || receiver == null || amountToSend <= 0) {
            return 0;
        }

        int amountFit = receiver.receiveEnergy(amountToSend, true);
        if (amountFit <= 0) {
            return 0;
        }

        int extracted = sender.extractEnergy(amountFit, false);
        if (extracted <= 0) {
            return 0;
        }

        return receiver.receiveEnergy(extracted, false);
    }

    public static int transmitPowerWithLoss(IEnergyStorage sender, IEnergyStorage receiver, int amountToSend, int manhattanDistance, double lossPerBlockPercent) {
        if (sender == null || receiver == null || amountToSend <= 0) {
            return 0;
        }

        int amountFit = receiver.receiveEnergy(amountToSend, true);
        if (amountFit <= 0) {
            return 0;
        }

        int extracted = sender.extractEnergy(amountFit, false);
        if (extracted <= 0) {
            return 0;
        }

        int afterLoss = applyDistanceLoss(extracted, manhattanDistance, lossPerBlockPercent);
        return afterLoss <= 0 ? 0 : receiver.receiveEnergy(afterLoss, false);
    }

    public static int applyDistanceLoss(int amount, int manhattanDistance, double lossPerBlockPercent) {
        if (amount <= 0) {
            return 0;
        }
        double lossRatio = (Math.max(0.0D, lossPerBlockPercent) * Math.max(0, manhattanDistance)) / 100.0D;
        int loss = (int) Math.floor(amount * lossRatio);
        return Math.max(0, amount - loss);
    }
}
