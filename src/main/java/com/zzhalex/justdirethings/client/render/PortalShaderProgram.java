package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.JustDireThingsLegacy;
import com.zzhalex.justdirethings.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

final class PortalShaderProgram {

    private static final ResourceLocation VERTEX_SHADER = new ResourceLocation(Reference.MOD_ID, "shaders/portal_entity_legacy.vsh");
    private static final ResourceLocation FRAGMENT_SHADER = new ResourceLocation(Reference.MOD_ID, "shaders/portal_entity_legacy.fsh");

    private static boolean initialized;
    private static boolean failed;
    private static int program;
    private static int samplerUniform;
    private static int gameTimeUniform;
    private static int layersUniform;

    private PortalShaderProgram() {
    }

    static boolean bind(float gameTime, int layers) {
        if (!OpenGlHelper.shadersSupported || failed) {
            return false;
        }
        ensureInitialized();
        if (program <= 0) {
            return false;
        }
        GL20.glUseProgram(program);
        GL20.glUniform1i(samplerUniform, 0);
        GL20.glUniform1f(gameTimeUniform, gameTime);
        GL20.glUniform1i(layersUniform, layers);
        return true;
    }

    static void unbind() {
        if (program > 0) {
            GL20.glUseProgram(0);
        }
    }

    private static void ensureInitialized() {
        if (initialized || failed) {
            return;
        }
        initialized = true;
        int vertex = 0;
        int fragment = 0;
        try {
            vertex = compileShader(VERTEX_SHADER, GL20.GL_VERTEX_SHADER);
            fragment = compileShader(FRAGMENT_SHADER, GL20.GL_FRAGMENT_SHADER);
            program = GL20.glCreateProgram();
            GL20.glAttachShader(program, vertex);
            GL20.glAttachShader(program, fragment);
            GL20.glLinkProgram(program);
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == 0) {
                throw new IOException(GL20.glGetProgramInfoLog(program, 4096));
            }
            samplerUniform = GL20.glGetUniformLocation(program, "Sampler0");
            gameTimeUniform = GL20.glGetUniformLocation(program, "GameTime");
            layersUniform = GL20.glGetUniformLocation(program, "Layers");
        } catch (IOException | RuntimeException ex) {
            failed = true;
            if (program > 0) {
                GL20.glDeleteProgram(program);
                program = 0;
            }
            JustDireThingsLegacy.LOGGER.warn("Unable to initialize portal shader renderer; falling back to textured portal quads", ex);
        } finally {
            if (vertex > 0) {
                GL20.glDeleteShader(vertex);
            }
            if (fragment > 0) {
                GL20.glDeleteShader(fragment);
            }
        }
    }

    private static int compileShader(ResourceLocation location, int type) throws IOException {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, readResource(location));
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            String log = GL20.glGetShaderInfoLog(shader, 4096);
            GL20.glDeleteShader(shader);
            throw new IOException("Failed to compile " + location + ": " + log);
        }
        return shader;
    }

    private static String readResource(ResourceLocation location) throws IOException {
        IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(location);
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        }
        return builder.toString();
    }
}
