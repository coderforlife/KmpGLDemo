#include <jni.h>
#include <string>

#define GL_GLEXT_PROTOTYPES
#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#define PACKAGE Java_edu_moravian_threekt_gl
#define JNI_NAME(PACKAGE, CLASS, FUNC) PACKAGE ## _ ## CLASS ## _ ## FUNC
#define JNI_FUNC(PACKAGE, CLASS, FUNC, RETURN) extern "C" JNIEXPORT RETURN JNICALL JNI_NAME(PACKAGE, CLASS, FUNC)

////////// ANGLE_instanced_arrays_native //////////
PFNGLDRAWARRAYSINSTANCEDANGLEPROC _glDrawArraysInstancedANGLE = nullptr;
JNI_FUNC(PACKAGE, ANGLE_1instanced_1arrays_1native, glDrawArraysInstancedANGLE, void)(JNIEnv *env, jobject _this, jint mode, jint first, jint count, jint instance_count) {
    if (!_glDrawArraysInstancedANGLE) { _glDrawArraysInstancedANGLE = (PFNGLDRAWARRAYSINSTANCEDANGLEPROC)eglGetProcAddress("glDrawArraysInstancedANGLE"); }
    _glDrawArraysInstancedANGLE(mode, first, count, instance_count);
}
PFNGLDRAWELEMENTSINSTANCEDANGLEPROC _glDrawElementsInstancedANGLE = nullptr;
JNI_FUNC(PACKAGE, ANGLE_1instanced_1arrays_1native, glDrawElementsInstancedANGLE, void)(JNIEnv *env, jobject _this, jint mode, jint count, jint type, jint offset, jint primcount) {
    if (!_glDrawElementsInstancedANGLE) { _glDrawElementsInstancedANGLE = (PFNGLDRAWELEMENTSINSTANCEDANGLEPROC)eglGetProcAddress("glDrawElementsInstancedANGLE"); }
    _glDrawElementsInstancedANGLE(mode, count, type, (void*)(uintptr_t)offset, primcount);
}
PFNGLVERTEXATTRIBDIVISORANGLEPROC _glVertexAttribDivisorANGLE = nullptr;
JNI_FUNC(PACKAGE, ANGLE_1instanced_1arrays_1native, glVertexAttribDivisorANGLE, void)(JNIEnv *env, jobject _this, jint index, jint divisor) {
    if (!_glVertexAttribDivisorANGLE) { _glVertexAttribDivisorANGLE = (PFNGLVERTEXATTRIBDIVISORANGLEPROC)eglGetProcAddress("glVertexAttribDivisorANGLE"); }
    _glVertexAttribDivisorANGLE(index, divisor);
}

////////// EXT_instanced_arrays_native //////////
PFNGLDRAWARRAYSINSTANCEDEXTPROC _glDrawArraysInstancedEXT = nullptr;
JNI_FUNC(PACKAGE, EXT_1instanced_1arrays_1native, glDrawArraysInstancedEXT, void)(JNIEnv *env, jobject _this, jint mode, jint first, jint count, jint instance_count) {
    if (!_glDrawArraysInstancedEXT) { _glDrawArraysInstancedEXT = (PFNGLDRAWARRAYSINSTANCEDEXTPROC)eglGetProcAddress("glDrawArraysInstancedEXT"); }
    _glDrawArraysInstancedEXT(mode, first, count, instance_count);
}
PFNGLDRAWELEMENTSINSTANCEDEXTPROC _glDrawElementsInstancedEXT = nullptr;
JNI_FUNC(PACKAGE, EXT_1instanced_1arrays_1native, glDrawElementsInstancedEXT, void)(JNIEnv *env, jobject _this, jint mode, jint count, jint type, jint offset, jint primcount) {
    if (!_glDrawElementsInstancedEXT) { _glDrawElementsInstancedEXT = (PFNGLDRAWELEMENTSINSTANCEDEXTPROC)eglGetProcAddress("glDrawElementsInstancedEXT"); }
    _glDrawElementsInstancedEXT(mode, count, type, (void*)(uintptr_t)offset, primcount);
}
PFNGLVERTEXATTRIBDIVISOREXTPROC _glVertexAttribDivisorEXT = nullptr;
JNI_FUNC(PACKAGE, EXT_1instanced_1arrays_1native, glVertexAttribDivisorEXT, void)(JNIEnv *env, jobject _this, jint index, jint divisor) {
    if (!_glVertexAttribDivisorEXT) { _glVertexAttribDivisorEXT = (PFNGLVERTEXATTRIBDIVISOREXTPROC)eglGetProcAddress("glVertexAttribDivisorEXT"); }
    _glVertexAttribDivisorEXT(index, divisor);
}

////////// NV_instanced_arrays_native //////////
PFNGLDRAWARRAYSINSTANCEDNVPROC _glDrawArraysInstancedNV = nullptr;
JNI_FUNC(PACKAGE, NV_1instanced_1arrays_1native, glDrawArraysInstancedNV, void)(JNIEnv *env, jobject _this, jint mode, jint first, jint count, jint instance_count) {
    if (!_glDrawArraysInstancedNV) { _glDrawArraysInstancedNV = (PFNGLDRAWARRAYSINSTANCEDNVPROC)eglGetProcAddress("glDrawArraysInstancedNV"); }
    _glDrawArraysInstancedNV(mode, first, count, instance_count);
}
PFNGLDRAWELEMENTSINSTANCEDNVPROC _glDrawElementsInstancedNV = nullptr;
JNI_FUNC(PACKAGE, NV_1instanced_1arrays_1native, glDrawElementsInstancedNV, void)(JNIEnv *env, jobject _this, jint mode, jint count, jint type, jint offset, jint primcount) {
    if (!_glDrawElementsInstancedNV) { _glDrawElementsInstancedNV = (PFNGLDRAWELEMENTSINSTANCEDNVPROC)eglGetProcAddress("glDrawElementsInstancedNV"); }
    _glDrawElementsInstancedNV(mode, count, type, (void*)(uintptr_t)offset, primcount);
}
PFNGLVERTEXATTRIBDIVISORNVPROC _glVertexAttribDivisorNV = nullptr;
JNI_FUNC(PACKAGE, NV_1instanced_1arrays_1native, glVertexAttribDivisorNV, void)(JNIEnv *env, jobject _this, jint index, jint divisor) {
    if (!_glVertexAttribDivisorNV) { _glVertexAttribDivisorNV = (PFNGLVERTEXATTRIBDIVISORNVPROC)eglGetProcAddress("glVertexAttribDivisorNV"); }
    _glVertexAttribDivisorNV(index, divisor);
}


////////// OES_vertex_array_object_native //////////
// This one is apparently always available
JNI_FUNC(PACKAGE, OES_1vertex_1array_1object_1native, glGenVertexArraysOES, void)(JNIEnv *env, jobject _this, jint n, jintArray out) {
    glGenVertexArraysOES(n, (GLuint*)env->GetIntArrayElements(out, nullptr));
}
JNI_FUNC(PACKAGE, OES_1vertex_1array_1object_1native, glDeleteVertexArraysOES, void)(JNIEnv *env, jobject _this, jint n, jintArray arrays) {
    glDeleteVertexArraysOES(n, (GLuint*)env->GetIntArrayElements(arrays, nullptr));
}
JNI_FUNC(PACKAGE, OES_1vertex_1array_1object_1native, glIsVertexArrayOES, jint)(JNIEnv *env, jobject _this, jint array) {
    return glIsVertexArrayOES(array);
}
JNI_FUNC(PACKAGE, OES_1vertex_1array_1object_1native, glBindVertexArrayOES, void)(JNIEnv *env, jobject _this, jint array) {
    glBindVertexArrayOES(array);
}

////////// EXT_draw_buffers_native //////////
PFNGLDRAWBUFFERSEXTPROC _glDrawBuffersEXT = nullptr;
JNI_FUNC(PACKAGE, EXT_1draw_1buffers_1native, glDrawBuffersEXT, void)(JNIEnv *env, jobject _this, jintArray bufs) {
    if (!_glDrawBuffersEXT) { _glDrawBuffersEXT = (PFNGLDRAWBUFFERSEXTPROC)eglGetProcAddress("glDrawBuffersEXT"); }
    _glDrawBuffersEXT(env->GetArrayLength(bufs), (GLenum*)env->GetIntArrayElements(bufs, nullptr));
}

////////// NV_draw_buffers_native //////////
PFNGLDRAWBUFFERSNVPROC _glDrawBuffersNV = nullptr;
JNI_FUNC(PACKAGE, NV_1draw_1buffers_1native, glDrawBuffersNV, void)(JNIEnv *env, jobject _this, jintArray bufs) {
    if (!_glDrawBuffersNV) { _glDrawBuffersNV = (PFNGLDRAWBUFFERSNVPROC)eglGetProcAddress("glDrawBuffersNV"); }
    _glDrawBuffersNV(env->GetArrayLength(bufs), (GLenum*)env->GetIntArrayElements(bufs, nullptr));
}

////////// ANGLE_framebuffer_multisample_native //////////
PFNGLRENDERBUFFERSTORAGEMULTISAMPLEANGLEPROC _glRenderbufferStorageMultisampleANGLE = nullptr;
JNI_FUNC(PACKAGE, ANGLE_1framebuffer_1multisample_1native, glRenderbufferStorageMultisampleANGLE, void)(JNIEnv *env, jobject _this, jint target, jint samples, jint internalformat, jint width, jint height) {
    if (!_glRenderbufferStorageMultisampleANGLE) { _glRenderbufferStorageMultisampleANGLE = (PFNGLRENDERBUFFERSTORAGEMULTISAMPLEANGLEPROC)eglGetProcAddress("glRenderbufferStorageMultisampleANGLE"); }
    _glRenderbufferStorageMultisampleANGLE(target, samples, internalformat, width, height);
}

////////// NV_framebuffer_multisample_native //////////
PFNGLRENDERBUFFERSTORAGEMULTISAMPLENVPROC _glRenderbufferStorageMultisampleNV = nullptr;
JNI_FUNC(PACKAGE, NV_1framebuffer_1multisample_1native, glRenderbufferStorageMultisampleNV, void)(JNIEnv *env, jobject _this, jint target, jint samples, jint internalformat, jint width, jint height) {
    if (!_glRenderbufferStorageMultisampleNV) { _glRenderbufferStorageMultisampleNV = (PFNGLRENDERBUFFERSTORAGEMULTISAMPLENVPROC)eglGetProcAddress("glRenderbufferStorageMultisampleNV"); }
    _glRenderbufferStorageMultisampleNV(target, samples, internalformat, width, height);
}

////////// APPLE_framebuffer_multisample_native //////////
PFNGLRENDERBUFFERSTORAGEMULTISAMPLEAPPLEPROC _glRenderbufferStorageMultisampleAPPLE = nullptr;
JNI_FUNC(PACKAGE, APPLE_1framebuffer_1multisample_1native, glRenderbufferStorageMultisampleAPPLE, void)(JNIEnv *env, jobject _this, jint target, jint samples, jint internalformat, jint width, jint height) {
    if (!_glRenderbufferStorageMultisampleAPPLE) { _glRenderbufferStorageMultisampleAPPLE = (PFNGLRENDERBUFFERSTORAGEMULTISAMPLEAPPLEPROC)eglGetProcAddress("glRenderbufferStorageMultisampleAPPLE"); }
    _glRenderbufferStorageMultisampleAPPLE(target, samples, internalformat, width, height);
}
PFNGLRESOLVEMULTISAMPLEFRAMEBUFFERAPPLEPROC _glResolveMultisampleFramebufferAPPLE = nullptr;
JNI_FUNC(PACKAGE, APPLE_1framebuffer_1multisample_1native, glResolveMultisampleFramebufferAPPLE, void)(JNIEnv *env, jobject _this) {
    if (!_glResolveMultisampleFramebufferAPPLE) { _glResolveMultisampleFramebufferAPPLE = (PFNGLRESOLVEMULTISAMPLEFRAMEBUFFERAPPLEPROC)eglGetProcAddress("glResolveMultisampleFramebufferAPPLE"); }
    _glResolveMultisampleFramebufferAPPLE();
}

////////// EXT_multisampled_render_to_texture_native //////////
PFNGLRENDERBUFFERSTORAGEMULTISAMPLEEXTPROC _glRenderbufferStorageMultisampleEXT = nullptr;
JNI_FUNC(PACKAGE, EXT_1multisampled_1render_1to_1texture_1native, glRenderbufferStorageMultisampleEXT, void)(JNIEnv *env, jobject _this, jint target, jint samples, jint internalformat, jint width, jint height) {
    if (!_glRenderbufferStorageMultisampleEXT) { _glRenderbufferStorageMultisampleEXT = (PFNGLRENDERBUFFERSTORAGEMULTISAMPLEEXTPROC)eglGetProcAddress("glRenderbufferStorageMultisampleEXT"); }
    _glRenderbufferStorageMultisampleEXT(target, samples, internalformat, width, height);
}
PFNGLFRAMEBUFFERTEXTURE2DMULTISAMPLEEXTPROC _glFramebufferTexture2DMultisampleEXT = nullptr;
JNI_FUNC(PACKAGE, EXT_1multisampled_1render_1to_1texture_1native, glFramebufferTexture2DMultisampleEXT, void)(JNIEnv *env, jobject _this, jint target, jint attachment, jint textarget, jint texture, jint level, jint samples) {
    if (!_glFramebufferTexture2DMultisampleEXT) { _glFramebufferTexture2DMultisampleEXT = (PFNGLFRAMEBUFFERTEXTURE2DMULTISAMPLEEXTPROC)eglGetProcAddress("glFramebufferTexture2DMultisampleEXT"); }
    _glFramebufferTexture2DMultisampleEXT(target, attachment, textarget, texture, level, samples);
}

////////// IMG_multisampled_render_to_texture_native //////////
// This one is apparently always available
JNI_FUNC(PACKAGE, IMG_1multisampled_1render_1to_1texture_1native, glRenderbufferStorageMultisampleIMG, void)(JNIEnv *env, jobject _this, jint target, jint samples, jint internalformat, jint width, jint height) {
    glRenderbufferStorageMultisampleIMG(target, samples, internalformat, width, height);
}
JNI_FUNC(PACKAGE, IMG_1multisampled_1render_1to_1texture_1native, glFramebufferTexture2DMultisampleIMG, void)(JNIEnv *env, jobject _this, jint target, jint attachment, jint textarget, jint texture, jint level, jint samples) {
    glFramebufferTexture2DMultisampleIMG(target, attachment, textarget, texture, level, samples);
}
