package com.zzhalex.justdirethings.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class EntityLivingBaseElytraTransformer implements IClassTransformer, Opcodes {

    private static final String ENTITY_LIVING_BASE_CLASS = "net.minecraft.entity.EntityLivingBase";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !ENTITY_LIVING_BASE_CLASS.equals(transformedName)) {
            return basicClass;
        }

        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        boolean transformed = false;
        for (MethodNode method : classNode.methods) {
            if (isUpdateElytra(method)) {
                transformed |= ElytraBytecodePatcher.redirectElytraChecks(method);
                transformed |= ElytraBytecodePatcher.redirectElytraDamage(method);
            }
        }

        if (!transformed) {
            return basicClass;
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private static boolean isUpdateElytra(MethodNode method) {
        return ("updateElytra".equals(method.name) || "func_184616_r".equals(method.name))
                && "()V".equals(method.desc);
    }
}
