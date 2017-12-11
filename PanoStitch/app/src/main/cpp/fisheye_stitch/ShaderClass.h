#ifndef SHADER_H
#define SHADER_H
 
#include <string>
#include <fstream>
#include <iostream>

#include <GLES3/gl3.h>

//#include <glew.h>	// Include glew to get all the required OpenGL headers

class Shader
{
public:
	// The program ID
	GLuint Program;
	// Constructor reads and builds the shader
	Shader();
	Shader(const GLchar *vertexPath, const GLchar *fragmentPath);
	GLint init(const GLchar *vShaderCode, const GLchar *fShaderCode);
	// Use the program
	GLvoid Use();
};

#endif //SHADER_H
