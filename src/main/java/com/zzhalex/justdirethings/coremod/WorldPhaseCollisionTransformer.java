package com.zzhalex.justdirethings.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class WorldPhaseCollisionTransformer implements IClassTransformer, Opcodes {

    private static final String WORLD_CLASS = "net.minecraft.world.World";
    private static final String HOOK_OWNER = "com/zzhalex/justdirethings/coremod/hooks/PhaseHooks";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !WORLD_CLASS.equals(transformedName)) {
            return basicClass;
        }

        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        boolean transformed = false;
        for (MethodNode method : classNode.methods) {
            if (isPublicGetCollisionBoxes(method)) {
                transformed |= injectPhaseBlockCollisionFilter(method);
            }
        }

        if (!transformed) {
            return basicClass;
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private static boolean isPublicGetCollisionBoxes(MethodNode method) {
        return ("getCollisionBoxes".equals(method.name) || "func_184144_a".equals(method.name))
                && "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;".equals(method.desc);
    }

    private static boolean injectPhaseBlockCollisionFilter(MethodNode method) {
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!isPrivateBlockCollisionGatherCall(call)) {
                continue;
            }

            AbstractInsnNode insertionPoint = call.getNext();
            if (insertionPoint == null || insertionPoint.getOpcode() != POP) {
                continue;
            }

            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(ALOAD, 1));
            hook.add(new VarInsnNode(ALOAD, 2));
            hook.add(new VarInsnNode(ALOAD, 3));
            hook.add(new VarInsnNode(ALOAD, 0));
            hook.add(new MethodInsnNode(INVOKESTATIC, HOOK_OWNER, "filterPhaseBlockCollisions",
                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/world/World;)V", false));
            method.instructions.insert(insertionPoint, hook);
            return true;
        }
        return false;
    }

    private static boolean isPrivateBlockCollisionGatherCall(MethodInsnNode call) {
        return call.getOpcode() == INVOKEVIRTUAL
                && WORLD_CLASS.replace('.', '/').equals(call.owner)
                && ("getCollisionBoxes".equals(call.name) || "func_191504_a".equals(call.name))
                && "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;ZLjava/util/List;)Z".equals(call.desc);
    }
}
