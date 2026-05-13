package com.zzhalex.justdirethings.coremod;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

final class ElytraBytecodePatcher implements Opcodes {

    private static final String HOOK_OWNER = "com/zzhalex/justdirethings/coremod/hooks/ElytraHooks";

    private ElytraBytecodePatcher() {
    }

    static boolean redirectElytraChecks(MethodNode method) {
        boolean changed = false;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!isItemElytraUsableCall(call)) {
                continue;
            }

            AbstractInsnNode secondStackLoad = previousOpcode(call, ALOAD);
            AbstractInsnNode itemCompare = previousOpcode(secondStackLoad, IF_ACMPNE);
            AbstractInsnNode firstStackLoad = previousOpcode(itemCompare, ALOAD);
            if (!(secondStackLoad instanceof VarInsnNode)
                    || !(itemCompare instanceof JumpInsnNode)
                    || !(firstStackLoad instanceof VarInsnNode)) {
                continue;
            }

            int stackVar = ((VarInsnNode) secondStackLoad).var;
            InsnList replacement = new InsnList();
            replacement.add(new VarInsnNode(ALOAD, stackVar));
            replacement.add(new VarInsnNode(ALOAD, 0));
            replacement.add(new MethodInsnNode(INVOKESTATIC, HOOK_OWNER, "canElytraFly",
                    "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;)Z", false));
            method.instructions.insertBefore(firstStackLoad, replacement);

            AbstractInsnNode cursor = firstStackLoad;
            AbstractInsnNode stop = call.getNext();
            while (cursor != stop) {
                AbstractInsnNode next = cursor.getNext();
                method.instructions.remove(cursor);
                cursor = next;
            }
            changed = true;
        }
        return changed;
    }

    static boolean redirectElytraDamage(MethodNode method) {
        boolean changed = false;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.getOpcode() != INVOKEVIRTUAL
                    || !"net/minecraft/item/ItemStack".equals(call.owner)
                    || !("damageItem".equals(call.name) || "func_77972_a".equals(call.name))
                    || !"(ILnet/minecraft/entity/EntityLivingBase;)V".equals(call.desc)) {
                continue;
            }

            call.setOpcode(INVOKESTATIC);
            call.owner = HOOK_OWNER;
            call.name = "damageElytra";
            call.desc = "(Lnet/minecraft/item/ItemStack;ILnet/minecraft/entity/EntityLivingBase;)V";
            changed = true;
        }
        return changed;
    }

    private static boolean isItemElytraUsableCall(MethodInsnNode call) {
        return call.getOpcode() == INVOKESTATIC
                && "net/minecraft/item/ItemElytra".equals(call.owner)
                && ("isUsable".equals(call.name) || "func_185069_d".equals(call.name))
                && "(Lnet/minecraft/item/ItemStack;)Z".equals(call.desc);
    }

    private static AbstractInsnNode previousOpcode(AbstractInsnNode instruction, int opcode) {
        for (AbstractInsnNode cursor = instruction == null ? null : instruction.getPrevious(); cursor != null; cursor = cursor.getPrevious()) {
            if (cursor.getOpcode() == opcode) {
                return cursor;
            }
        }
        return null;
    }
}
