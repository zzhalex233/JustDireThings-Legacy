#version 120

varying vec2 texCoord0;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    texCoord0 = gl_MultiTexCoord0.xy;
}
