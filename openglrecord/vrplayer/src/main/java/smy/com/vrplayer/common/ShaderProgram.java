/**
 * Copyright 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * Licensed under the Creative Commons CC BY-NC 4.0 Attribution-NonCommercial
 * License (the "License"). You may obtain a copy of the License at
 * https://creativecommons.org/licenses/by-nc/4.0/.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package smy.com.vrplayer.common;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ShaderProgram {
    public static final String TAG = ShaderProgram.class.getSimpleName();
    private static final HashMap<Integer, String> sRawGLSL = new HashMap<Integer, String>();
    public static Context sContext;

    private int shaderProgramHandle;

    public ShaderProgram(String vertexShader, String fragmentShader) {
        shaderProgramHandle = createProgram(vertexShader, fragmentShader);
    }

    public ShaderProgram(int vertShaderResId, int fragShaderResId){
        shaderProgramHandle = createProgram(vertShaderResId,fragShaderResId);
    }

    public int getShaderHandle() {
        return shaderProgramHandle;
    }

    public void release() {
        GLES20.glDeleteProgram(shaderProgramHandle);
        shaderProgramHandle = -1;
    }

    private static void checkLocation(int location, String name) {
        if (location >= 0) {
            return;
        }
        throw new RuntimeException("Could not find location for " + name);
    }

    public int getAttribute(String name) {
        int loc = GLES20.glGetAttribLocation(shaderProgramHandle, name);
        checkLocation(loc, name);
        return loc;
    }

    public int getUniform(String name) {
        int loc = GLES20.glGetUniformLocation(shaderProgramHandle, name);
        checkLocation(loc, name);
        return loc;
    }

    private static int createProgram(int vertShaderResId, int fragShaderResId){
        String vertSource = fetchShader(vertShaderResId);
        String fragSource = fetchShader(fragShaderResId);
        return createProgram(vertSource,fragSource);
    }

    private static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        int program = GLES20.glCreateProgram();
        GLHelpers.checkGlError("glCreateProgram");
        if (program == 0) {
            Log.e(TAG, "Could not create program");
            return 0;
        }
        GLES20.glAttachShader(program, vertexShader);
        GLHelpers.checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        GLHelpers.checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        GLHelpers.checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ":");
            Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    public static final String fetchShader(final int resID){
        if(sRawGLSL.containsKey(resID)){
            return sRawGLSL.get(resID);
        }
        final StringBuilder sb = new StringBuilder();
        InputStreamReader isr = null;
        BufferedReader br = null;
        try{
            final Resources res = sContext.getResources();
            isr = new InputStreamReader(res.openRawResource(resID));
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            sRawGLSL.put(resID, sb.toString());
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if(isr != null){
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
