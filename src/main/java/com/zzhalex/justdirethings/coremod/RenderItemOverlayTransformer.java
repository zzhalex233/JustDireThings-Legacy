package com.zzhalex.justdirethings.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class RenderItemOverlayTransformer implements IClassTransformer, Opcodes {

    private static final String RENDER_ITEM_CLASS = "net.minecraft.client.renderer.RenderItem";
    private static final String HOOK_OWNER = "com/zzhalex/justdirethings/coremod/hooks/ItemOverlayHooks";
    private static final String HOOK_NAME = "renderItemOverlayIntoGUI";
    private static final String HOOK_DESC = "(Lnet/minecraft/item/ItemStack;II)V";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !RENDER_ITEM_CLASS.equals(transformedName)) {
            return basicClass;
        }

        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(classNode, 0);

        boolean transformed = false;
        for (MethodNode method : classNode.methods) {
            if (isRenderItemOverlayIntoGui(method)) {
                transformed |= injectAfterDurabilityBar(method);
            }
        }

        if (!transformed) {
            return basicClass;
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private static boolean isRenderItemOverlayIntoGui(MethodNode method) {
        return ("renderItemOverlayIntoGUI".equals(method.name) || "func_180453_a".equals(method.name))
                && "(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V".equals(method.desc);
    }

    private static boolean injectAfterDurabilityBar(MethodNode method) {
        boolean sawShowDurabilityBar = false;
        LabelNode durabilityEnd = null;
        boolean atDurabilityEnd = false;
        for (int i = 0; i < method.instructions.size(); i++) {
            AbstractInsnNode node = method.instructions.get(i);

            if (!sawShowDurabilityBar && durabilityEnd == null && node instanceof MethodInsnNode) {
                MethodInsnNode methodCall = (MethodInsnNode) node;
                if ("showDurabilityBar".equals(methodCall.name)) {
                    sawShowDurabilityBar = true;
                }
                continue;
            }

            if (sawShowDurabilityBar && node instanceof JumpInsnNode && node.getOpcode() == IFEQ) {
                durabilityEnd = ((JumpInsnNode) node).label;
                sawShowDurabilityBar = false;
                continue;
            }

            if (durabilityEnd != null && node == durabilityEnd) {
                atDurabilityEnd = true;
                continue;
            }

            if (atDurabilityEnd && node instanceof FrameNode) {
                method.instructions.insert(node, createOverlayHookCall());
                return true;
            }
        }
        return false;
    }

    private static InsnList createOverlayHookCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(ALOAD, 2));
        instructions.add(new VarInsnNode(ILOAD, 3));
        instructions.add(new VarInsnNode(ILOAD, 4));
        instructions.add(new MethodInsnNode(INVOKESTATIC, HOOK_OWNER, HOOK_NAME, HOOK_DESC, false));
        return instructions;
    }
}
