#version 130

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
in vec4 vt;
out vec4 vVaryingColor;

void main() {
vVaryingColor=gl_Color;
gl_Position=gl_ProjectionMatrix*gl_ModelViewMatrix*vt;
}