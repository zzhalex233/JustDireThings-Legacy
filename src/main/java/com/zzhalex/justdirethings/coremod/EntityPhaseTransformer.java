package com.zzhalex.justdirethings.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.InsnNode;

public final class EntityPhaseTransformer implements IClassTransformer, Opcodes {

    private static final String ENTITY_CLASS = "net.minecraft.entity.Entity";
    private static final String HOOK_OWNER = "com/zzhalex/justdirethings/coremod/hooks/PhaseHooks";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !ENTITY_CLASS.equals(transformedName)) {
            return basicClass;
        }

        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        boolean transformed = false;
        for (MethodNode method : classNode.methods) {
            if (isInsideOpaqueBlock(method)) {
                injectPhaseBypass(method);
                transformed = true;
            }
        }

        if (!transformed) {
            return basicClass;
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private static boolean isInsideOpaqueBlock(MethodNode method) {
        return ("isEntityInsideOpaqueBlock".equals(method.name) || "func_70094_T".equals(method.name))
                && "()Z".equals(method.desc);
    }

    private static void injectPhaseBypass(MethodNode method) {
        LabelNode vanillaPath = new LabelNode();
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new MethodInsnNode(INVOKESTATIC, HOOK_OWNER, "canPhase", "(Lnet/minecraft/entity/Entity;)Z", false));
        instructions.add(new JumpInsnNode(IFEQ, vanillaPath));
        instructions.add(new InsnNode(ICONST_0));
        instructions.add(new InsnNode(IRETURN));
        instructions.add(vanillaPath);
        method.instructions.insert(instructions);
    }
}
