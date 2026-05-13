package com.zzhalex.justdirethings.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class EntityRendererNightVisionTransformer implements IClassTransformer, Opcodes {

    private static final String ENTITY_RENDERER_CLASS = "net.minecraft.client.renderer.EntityRenderer";
    private static final String HOOK_OWNER = "com/zzhalex/justdirethings/coremod/hooks/NightVisionHooks";
    private static final String HOOK_NAME = "hasNightVisionAbility";
    private static final String HOOK_DESC = "(Lnet/minecraft/entity/EntityLivingBase;)Z";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !ENTITY_RENDERER_CLASS.equals(transformedName)) {
            return basicClass;
        }

        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        boolean transformed = false;
        for (MethodNode method : classNode.methods) {
            if (isGetNightVisionBrightness(method)) {
                injectNightVisionAbilityReturn(method);
                transformed = true;
            } else if (isUpdateLightmap(method)) {
                transformed |= injectNightVisionEffectGate(method);
            }
        }

        if (!transformed) {
            return basicClass;
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private static boolean isGetNightVisionBrightness(MethodNode method) {
        return ("getNightVisionBrightness".equals(method.name) || "func_180438_a".equals(method.name))
                && "(Lnet/minecraft/entity/EntityLivingBase;F)F".equals(method.desc);
    }

    private static boolean isUpdateLightmap(MethodNode method) {
        return ("updateLightmap".equals(method.name) || "func_78472_g".equals(method.name))
                && "(F)V".equals(method.desc);
    }

    private static void injectNightVisionAbilityReturn(MethodNode method) {
        LabelNode vanillaPath = new LabelNode();
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(ALOAD, 1));
        instructions.add(new MethodInsnNode(INVOKESTATIC, HOOK_OWNER, HOOK_NAME, HOOK_DESC, false));
        instructions.add(new JumpInsnNode(IFEQ, vanillaPath));
        instructions.add(new InsnNode(FCONST_1));
        instructions.add(new InsnNode(FRETURN));
        instructions.add(vanillaPath);
        method.instructions.insert(instructions);
    }

    private static boolean injectNightVisionEffectGate(MethodNode method) {
        boolean changed = false;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!isPotionActiveCall(call) || !isCheckingNightVision(call)) {
                continue;
            }

            InsnList before = new InsnList();
            before.add(new InsnNode(SWAP));
            before.add(new InsnNode(DUP_X1));
            before.add(new InsnNode(SWAP));
            method.instructions.insertBefore(call, before);

            InsnList after = new InsnList();
            after.add(new InsnNode(SWAP));
            after.add(new MethodInsnNode(INVOKESTATIC, HOOK_OWNER, HOOK_NAME, HOOK_DESC, false));
            after.add(new InsnNode(IOR));
            method.instructions.insert(call, after);
            changed = true;
        }
        return changed;
    }

    private static boolean isPotionActiveCall(MethodInsnNode call) {
        return call.getOpcode() == INVOKEVIRTUAL
                && ("isPotionActive".equals(call.name) || "func_70644_a".equals(call.name))
                && "(Lnet/minecraft/potion/Potion;)Z".equals(call.desc);
    }

    private static boolean isCheckingNightVision(MethodInsnNode call) {
        for (AbstractInsnNode cursor = call.getPrevious(); cursor != null; cursor = cursor.getPrevious()) {
            if (cursor instanceof MethodInsnNode) {
                return false;
            }
            if (!(cursor instanceof FieldInsnNode)) {
                continue;
            }
            FieldInsnNode field = (FieldInsnNode) cursor;
            return "net/minecraft/init/MobEffects".equals(field.owner)
                    && ("NIGHT_VISION".equals(field.name) || "field_76439_r".equals(field.name))
                    && "Lnet/minecraft/potion/Potion;".equals(field.desc);
        }
        return false;
    }
}
