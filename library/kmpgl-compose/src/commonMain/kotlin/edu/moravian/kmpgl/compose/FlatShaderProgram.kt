package edu.moravian.kmpgl.compose

import edu.moravian.kmpgl.core.GL
import edu.moravian.kmpgl.core.GLContext

fun FlatShaderProgram(gl: GLContext, numLights: Int): ShaderProgram {
    val versionStr = gl.getString(GL.VERSION)
    val version = Regex("\\d+\\.\\d+").find(versionStr)?.value?.toFloatOrNull() ?: (gl.version / 10f)
//    println(versionStr)
//    println(gl.getString(GL.SHADING_LANGUAGE_VERSION))
//    println(gl.extensions.available.joinToString(", "))

    // Use the special geometry shader if supported (GL 3.2+ or GL_EXT/OES_geometry_shader)
    // Those extensions also automatically require the GL_EXT/OES_shader_io_blocks extension
    // thus we can use those instead of arrays-of-arrays for passing data to the shader
    return if (version >= 3.2f || "GL_EXT_geometry_shader" in gl.extensions || "GL_OES_geometry_shader" in gl.extensions)
        ShaderProgram(
            FLAT_VERTEX_SHADER_FOR_GEOM.replace("<NUM_LIGHTS>", numLights.toString()),
            FLAT_FRAGMENT_SHADER_FOR_GEOM.replace("<NUM_LIGHTS>", numLights.toString()),
            0x8DD9 /*GEOMETRY_SHADER_EXT*/ to FLAT_GEOMETRY_SHADER.replace("<NUM_LIGHTS>", numLights.toString()),
        )
    else
        ShaderProgram(
            FLAT_VERTEX_SHADER.replace("<NUM_LIGHTS>", numLights.toString()),
            FLAT_FRAGMENT_SHADER.replace("<NUM_LIGHTS>", numLights.toString())
        )
}

private const val COMPUTE_COLOR = """
    // Process lights
    vec3 color = uAmbientLight * uMaterialColor;
    for (int i = 0; i < NUM_LIGHTS; i++) {
        vec3 L = vLightVector[i];

        // Compute lighting
        float diffuse = dot(-L, N);
        float specular = 0.0;
        if (diffuse < 0.0) {
            diffuse = 0.0;
        } else {
            vec3 R = reflect(L, N);
            specular = pow(max(dot(R, E), 0.0), uMaterialShininess) * 0.25;  // 0.25 to look more realistic for plastic
        }
    
        // Compute color
        color += (diffuse + specular) * uLightColor[i] *  uMaterialColor;
    }
"""

val FLAT_VERTEX_SHADER = """
    #version 300 es
    precision highp float;
    
    #define NUM_LIGHTS <NUM_LIGHTS>

    uniform mat4 uModelMatrix;
    uniform mat4 uViewMatrix;
    uniform mat4 uProjectionMatrix;
    uniform vec3 uLightPosition[NUM_LIGHTS];

    in vec4 position;

    out vec3 vPosition;
    out vec3 vEyeVector;
    out vec3 vLightVector[NUM_LIGHTS];

    void main() {
        vec4 PM = uViewMatrix * uModelMatrix * position;
        vec4 P = uProjectionMatrix * PM;

        for (int i = 0; i < NUM_LIGHTS; i++) {
            vLightVector[i] = normalize(P.xyz - uLightPosition[i]);
        }
        vEyeVector = normalize(vec3(0, 0, 2) - P.xyz);
        
        gl_Position = P;
        vPosition = PM.xyz;
    }
    """.trimIndent()

val FLAT_FRAGMENT_SHADER = """
    #version 300 es
    #extension GL_OES_standard_derivatives : enable // not needed but doesn't hurt
    precision mediump float;

    #define NUM_LIGHTS <NUM_LIGHTS>

    uniform vec3 uAmbientLight;
    uniform vec3 uLightColor[NUM_LIGHTS];
    
    uniform vec3 uMaterialColor;
    uniform float uMaterialShininess;

    in vec3 vRealPosition;
    in vec3 vPosition;
    in vec3 vEyeVector;
    in vec3 vLightVector[NUM_LIGHTS];
    
    out vec4 fragColor;

    void main() {
        // Compute normal and eye vectors
        vec3 X = dFdx(vPosition);
        vec3 Y = dFdy(vPosition);
        // Older systems may need:
        //vec3 X = vec3(dFdx(vPosition.x), dFdx(vPosition.y), dFdx(vPosition.z));
        //vec3 Y = vec3(dFdy(vPosition.x), dFdy(vPosition.y), dFdy(vPosition.z));
        vec3 N = normalize(cross(X, Y));
        vec3 E = vEyeVector;
        
        $COMPUTE_COLOR
    
        // Set final color
        fragColor.rgb = color;
        fragColor.a = 1.0;
    }""".trimIndent()

val FLAT_VERTEX_SHADER_FOR_GEOM = """
    #version 320 es
    precision highp float;
    
    #define NUM_LIGHTS <NUM_LIGHTS>

    uniform mat4 uModelMatrix;
    uniform mat4 uViewMatrix;
    uniform mat4 uProjectionMatrix;
    uniform vec3 uLightPosition[NUM_LIGHTS];

    in vec4 position;

    out Vertex {
        vec3 Position;
        vec3 EyeVector;
        vec3 LightVector[NUM_LIGHTS];
    } gOut;

    void main() {
        vec4 PM = uViewMatrix * uModelMatrix * position;
        vec4 P = uProjectionMatrix * PM;

        for (int i = 0; i < NUM_LIGHTS; i++) {
            gOut.LightVector[i] = normalize(P.xyz - uLightPosition[i]);
        }
        gOut.EyeVector = normalize(vec3(0, 0, 2) - P.xyz);
        
        gl_Position = P;
        gOut.Position = PM.xyz;
    }""".trimIndent()

val FLAT_GEOMETRY_SHADER = """
    #version 320 es
    #extension GL_EXT_geometry_shader : enable

    #define NUM_LIGHTS <NUM_LIGHTS>

    layout(triangles) in;
    layout(triangle_strip, max_vertices=3) out;
    
    // NOTE: Pixel 6 fails to compile if using arrays-of-arrays for gLightVector
    // So instead we have to use interface blocks
    in Vertex {
        vec3 Position;
        vec3 EyeVector;
        vec3 LightVector[NUM_LIGHTS];
    } gIn[3];
    
    out vec3 vNormal;
    out vec3 vEyeVector;
    out vec3 vLightVector[NUM_LIGHTS];
    
    void main() {
        vec3 n = normalize(cross(
            normalize(gIn[1].Position-gIn[0].Position),
            normalize(gIn[2].Position-gIn[0].Position)
        ));
        for (int i = 0; i < gl_in.length(); i++) {
            gl_Position = gl_in[i].gl_Position;
            for (int j = 0; j < NUM_LIGHTS; j++) { vLightVector[j] = gIn[i].LightVector[j]; }
            vEyeVector = gIn[i].EyeVector;
            vNormal = n;
            EmitVertex();
        }
    }""".trimIndent()

val FLAT_FRAGMENT_SHADER_FOR_GEOM = """
    #version 320 es
    //#extension GL_OES_standard_derivatives : enable
    precision mediump float;

    #define NUM_LIGHTS <NUM_LIGHTS>

    uniform vec3 uAmbientLight;
    uniform vec3 uLightColor[NUM_LIGHTS];
    
    uniform vec3 uMaterialColor;
    uniform float uMaterialShininess;

    in vec3 vNormal;
    in vec3 vEyeVector;
    in vec3 vLightVector[NUM_LIGHTS];
    
    out vec4 fragColor;

    void main() {
        // Compute normal and eye vectors
        vec3 N = vNormal;
        vec3 E = vEyeVector;
        
        $COMPUTE_COLOR
    
        // Set final color
        fragColor.rgb = color;
        fragColor.a = 1.0;
    }""".trimIndent()