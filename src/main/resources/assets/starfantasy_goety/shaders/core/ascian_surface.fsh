#version 150
uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main(){vec4 c=texture(Sampler0,texCoord)*vertexColor*ColorModulator;if(c.a<0.01)discard;fragColor=c;}
