package edu.moravian.kmpgl.core

/** Constants for OpenGL ES 2.0 and 3.0 */
@Suppress("unused", "ConstPropertyName")
object GL {
    const val GL_TRUE = 1 // if these are named TRUE and FALSE, iOS/Swift fails to build
    const val GL_FALSE = 0
    const val NONE = 0
    @GLES3 const val INVALID_INDEX = -1 // defined as 0xFFFFFFFFu in C

    // Errors
    const val NO_ERROR = 0
    const val INVALID_ENUM = 0x0500
    const val INVALID_VALUE = 0x0501
    const val INVALID_OPERATION = 0x0502
    const val OUT_OF_MEMORY = 0x0505
    const val INVALID_FRAMEBUFFER_OPERATION = 0x0506

    // Capabilities
    const val BLEND = 0x0BE2
    const val DEPTH_TEST = 0x0B71
    const val CULL_FACE = 0x0B44
    const val DITHER = 0x0BD0
    const val POLYGON_OFFSET_FILL = 0x8037
    const val SAMPLE_ALPHA_TO_COVERAGE = 0x809E
    const val SAMPLE_COVERAGE = 0x80A0
    const val SCISSOR_TEST = 0x0C11
    const val STENCIL_TEST = 0x0B90
    @GLES3 const val RASTERIZER_DISCARD = 0x8C89
    @GLES3 const val PRIMITIVE_RESTART_FIXED_INDEX = 0x8D69

    ////////// Parameters //////////
    const val VENDOR = 0x1F00 // string
    const val RENDERER = 0x1F01 // string
    const val VERSION = 0x1F02 // string
    const val EXTENSIONS = 0x1F03 // string
    @GLES3 const val NUM_EXTENSIONS = 0x821D // int
    const val SHADING_LANGUAGE_VERSION = 0x8B8C // string
    @GLES3 const val MAJOR_VERSION = 0x821B // int
    @GLES3 const val MINOR_VERSION = 0x821C // int
    const val SHADER_COMPILER = 0x8DFA // bool

    const val SUBPIXEL_BITS = 0x0D50 // int
    const val RED_BITS = 0x0D52 // int
    const val GREEN_BITS = 0x0D53 // int
    const val BLUE_BITS = 0x0D54 // int
    const val ALPHA_BITS = 0x0D55 // int
    const val DEPTH_BITS = 0x0D56 // int
    const val STENCIL_BITS = 0x0D57 // int

    const val MAX_TEXTURE_SIZE = 0x0D33 // int
    const val MAX_VIEWPORT_DIMS = 0x0D3A // 2 ints
    @GLES3 const val MAX_3D_TEXTURE_SIZE = 0x8073 // int
    @GLES3 const val MAX_ELEMENTS_VERTICES = 0x80E8 // int
    @GLES3 const val MAX_ELEMENTS_INDICES = 0x80E9 // int
    const val ALIASED_POINT_SIZE_RANGE = 0x846D // 2 floats
    const val ALIASED_LINE_WIDTH_RANGE = 0x846E // 2 floats
    const val MAX_RENDERBUFFER_SIZE = 0x84E8 // int
    @GLES3 const val MAX_TEXTURE_LOD_BIAS = 0x84FD // float
    const val MAX_CUBE_MAP_TEXTURE_SIZE = 0x851C // int
    @GLES3 const val MAX_DRAW_BUFFERS = 0x8824 // int
    const val MAX_VERTEX_ATTRIBS = 0x8869 //int
    const val MAX_TEXTURE_IMAGE_UNITS = 0x8872 // int
    @GLES3 const val MAX_ARRAY_TEXTURE_LAYERS = 0x88FF // int
    @GLES3 const val MIN_PROGRAM_TEXEL_OFFSET = 0x8904 // int
    @GLES3 const val MAX_PROGRAM_TEXEL_OFFSET = 0x8905 // int
    @GLES3 const val MAX_VERTEX_UNIFORM_BLOCKS = 0x8A2B // int
    @GLES3 const val MAX_FRAGMENT_UNIFORM_BLOCKS = 0x8A2D // int
    @GLES3 const val MAX_COMBINED_UNIFORM_BLOCKS = 0x8A2E // int
    @GLES3 const val MAX_UNIFORM_BUFFER_BINDINGS = 0x8A2F // int
    @GLES3 const val MAX_UNIFORM_BLOCK_SIZE = 0x8A30 // long
    @GLES3 const val MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS = 0x8A31 // long
    @GLES3 const val MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS = 0x8A33 // long
    @GLES3 const val MAX_FRAGMENT_UNIFORM_COMPONENTS = 0x8B49 // int
    @GLES3 const val MAX_VERTEX_UNIFORM_COMPONENTS = 0x8B4A // int
    @GLES3 const val MAX_VARYING_COMPONENTS = 0x8B4B // int
    const val MAX_VERTEX_TEXTURE_IMAGE_UNITS = 0x8B4C // int
    const val MAX_COMBINED_TEXTURE_IMAGE_UNITS = 0x8B4D // int
    @GLES3 const val MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS = 0x8C80 // int
    @GLES3 const val MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS = 0x8C8A // int
    @GLES3 const val MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS = 0x8C8B // int
    @GLES3 const val MAX_COLOR_ATTACHMENTS = 0x8CDF // int
    @GLES3 const val MAX_SAMPLES = 0x8D57 // int
    @GLES3 const val MAX_ELEMENT_INDEX = 0x8D6B // long
    const val MAX_VERTEX_UNIFORM_VECTORS = 0x8DFB // int
    const val MAX_VARYING_VECTORS = 0x8DFC // int
    const val MAX_FRAGMENT_UNIFORM_VECTORS = 0x8DFD // int
    @GLES3 const val MAX_SERVER_WAIT_TIMEOUT = 0x9111 // long
    @GLES3 const val MAX_VERTEX_OUTPUT_COMPONENTS = 0x9122 // int
    @GLES3 const val MAX_FRAGMENT_INPUT_COMPONENTS = 0x9125 // int

    const val NUM_COMPRESSED_TEXTURE_FORMATS = 0x86A2 // int
    const val COMPRESSED_TEXTURE_FORMATS = 0x86A3 // uint array
    @GLES3 const val NUM_PROGRAM_BINARY_FORMATS = 0x87FE // int
    @GLES3 const val PROGRAM_BINARY_FORMATS = 0x87FF // uint array
    const val NUM_SHADER_BINARY_FORMATS = 0x8DF9 // int
    const val SHADER_BINARY_FORMATS = 0x8DF8 // uint array

    const val CURRENT_PROGRAM = 0x8B8D // GLProgram
    const val FRAMEBUFFER_BINDING = 0x8CA6 // GLFramebuffer
    @GLES3 const val DRAW_FRAMEBUFFER_BINDING = FRAMEBUFFER_BINDING // GLFramebuffer
    @GLES3 const val READ_FRAMEBUFFER_BINDING = 0x8CAA // GLFramebuffer
    const val RENDERBUFFER_BINDING = 0x8CA7 // GLRenderbuffer
    @GLES3 const val SAMPLER_BINDING = 0x8919 // GLSampler
    @GLES3 const val TRANSFORM_FEEDBACK_BINDING = 0x8E25 // GLTransformFeedback
    @GLES3 const val VERTEX_ARRAY_BINDING = 0x85B5 // GLVertexArrayObject
    const val ACTIVE_TEXTURE = 0x84E0 // GLTexture
    const val TEXTURE_BINDING_2D = 0x8069 // GLTexture
    const val TEXTURE_BINDING_CUBE_MAP = 0x8514 // GLTexture
    @GLES3 const val TEXTURE_BINDING_3D = 0x806A // GLTexture
    @GLES3 const val TEXTURE_BINDING_2D_ARRAY = 0x8C1D // GLTexture
    const val ARRAY_BUFFER_BINDING = 0x8894 // GLBuffer
    const val ELEMENT_ARRAY_BUFFER_BINDING = 0x8895 // GLBuffer
    @GLES3 const val PIXEL_PACK_BUFFER_BINDING = 0x88ED // GLBuffer
    @GLES3 const val PIXEL_UNPACK_BUFFER_BINDING = 0x88EF // GLBuffer
    @GLES3 const val COPY_READ_BUFFER_BINDING = 0x8F36 /*COPY_READ_BUFFER*/ // GLBuffer
    @GLES3 const val COPY_WRITE_BUFFER_BINDING = 0x8F37 /*COPY_WRITE_BUFFER*/ // GLBuffer

    const val BLEND_EQUATION = 0x8009 // blend equation
    const val BLEND_EQUATION_RGB = BLEND_EQUATION // blend equation
    const val BLEND_EQUATION_ALPHA = 0x883D // blend equation
    const val BLEND_DST_RGB = 0x80C8 // blend function
    const val BLEND_SRC_RGB = 0x80C9 // blend function
    const val BLEND_DST_ALPHA = 0x80CA // blend function
    const val BLEND_SRC_ALPHA = 0x80CB // blend function
    const val BLEND_COLOR = 0x8005 // color (4 floats)
    const val DEPTH_RANGE = 0x0B70 // 2 floats
    const val DEPTH_WRITEMASK = 0x0B72 // bool
    const val DEPTH_CLEAR_VALUE = 0x0B73 // float
    const val DEPTH_FUNC = 0x0B74 // comparison function

    const val STENCIL_CLEAR_VALUE = 0x0B91 // int
    const val STENCIL_FUNC = 0x0B92 // comparison function
    const val STENCIL_FAIL = 0x0B94 // stencil operation
    const val STENCIL_PASS_DEPTH_FAIL = 0x0B95 // stencil operation
    const val STENCIL_PASS_DEPTH_PASS = 0x0B96 // stencil operation
    const val STENCIL_REF = 0x0B97 // int
    const val STENCIL_VALUE_MASK = 0x0B93 // uint
    const val STENCIL_WRITEMASK = 0x0B98 // uint
    const val STENCIL_BACK_FUNC = 0x8800 // comparison function
    const val STENCIL_BACK_FAIL = 0x8801 // stencil operation
    const val STENCIL_BACK_PASS_DEPTH_FAIL = 0x8802 // stencil operation
    const val STENCIL_BACK_PASS_DEPTH_PASS = 0x8803 // stencil operation
    const val STENCIL_BACK_REF = 0x8CA3 // int
    const val STENCIL_BACK_VALUE_MASK = 0x8CA4 // uint
    const val STENCIL_BACK_WRITEMASK = 0x8CA5 // uint

    const val LINE_WIDTH = 0x0B21 // float
    const val CULL_FACE_MODE = 0x0B45 // face (FRONT, BACK, FRONT_AND_BACK)
    const val FRONT_FACE = 0x0B46 // face direction (CW or CCW)
    const val VIEWPORT = 0x0BA2 // 4 ints
    const val SCISSOR_BOX = 0x0C10 // 4 ints
    const val COLOR_CLEAR_VALUE = 0x0C22 // 4 floats
    const val COLOR_WRITEMASK = 0x0C23 // 4 bools
    const val POLYGON_OFFSET_UNITS = 0x2A00 // float
    const val POLYGON_OFFSET_FACTOR = 0x8038 // float

    const val SAMPLE_BUFFERS = 0x80A8 // int
    const val SAMPLES = 0x80A9 // int
    const val SAMPLE_COVERAGE_VALUE = 0x80AA // float
    const val SAMPLE_COVERAGE_INVERT = 0x80AB // bool

    const val IMPLEMENTATION_COLOR_READ_TYPE = 0x8B9A // read pixel type
    const val IMPLEMENTATION_COLOR_READ_FORMAT = 0x8B9B // read pixel format

    @GLES3 const val UNIFORM_BUFFER_OFFSET_ALIGNMENT = 0x8A34 // int
    @GLES3 const val TRANSFORM_FEEDBACK_PAUSED = 0x8E23 // bool
    @GLES3 const val TRANSFORM_FEEDBACK_ACTIVE = 0x8E24 // bool

    @GLES3 const val READ_BUFFER = 0x0C02 // gl.BACK, gl.NONE or gl.COLOR_ATTACHMENT{0-15}
    @GLES3 const val DRAW_BUFFER0 = 0x8825
    @GLES3 const val DRAW_BUFFER1 = 0x8826
    @GLES3 const val DRAW_BUFFER2 = 0x8827
    @GLES3 const val DRAW_BUFFER3 = 0x8828
    @GLES3 const val DRAW_BUFFER4 = 0x8829
    @GLES3 const val DRAW_BUFFER5 = 0x882A
    @GLES3 const val DRAW_BUFFER6 = 0x882B
    @GLES3 const val DRAW_BUFFER7 = 0x882C
    @GLES3 const val DRAW_BUFFER8 = 0x882D
    @GLES3 const val DRAW_BUFFER9 = 0x882E
    @GLES3 const val DRAW_BUFFER10 = 0x882F
    @GLES3 const val DRAW_BUFFER11 = 0x8830
    @GLES3 const val DRAW_BUFFER12 = 0x8831
    @GLES3 const val DRAW_BUFFER13 = 0x8832
    @GLES3 const val DRAW_BUFFER14 = 0x8833
    @GLES3 const val DRAW_BUFFER15 = 0x8834

    // also pixelStore values
    const val UNPACK_ALIGNMENT = 0x0CF5 // int
    const val PACK_ALIGNMENT = 0x0D05 // int
    @GLES3 const val UNPACK_ROW_LENGTH = 0x0CF2 // int
    @GLES3 const val UNPACK_SKIP_ROWS = 0x0CF3 // int
    @GLES3 const val UNPACK_SKIP_PIXELS = 0x0CF4 // int
    @GLES3 const val PACK_ROW_LENGTH = 0x0D02 // int
    @GLES3 const val PACK_SKIP_ROWS = 0x0D03 // int
    @GLES3 const val PACK_SKIP_PIXELS = 0x0D04 // int
    @GLES3 const val UNPACK_SKIP_IMAGES = 0x806D // int
    @GLES3 const val UNPACK_IMAGE_HEIGHT = 0x806E // int

    // also hints
    const val GENERATE_MIPMAP_HINT = 0x8192 // hint mode
    @GLES3 const val FRAGMENT_SHADER_DERIVATIVE_HINT = 0x8B8B // hint mode

    // indexable parameters
    @GLES3 const val UNIFORM_BUFFER_BINDING = 0x8A28 // GLBuffer
    @GLES3 const val UNIFORM_BUFFER_START = 0x8A29 // int
    @GLES3 const val UNIFORM_BUFFER_SIZE = 0x8A2A // int
    @GLES3 const val TRANSFORM_FEEDBACK_BUFFER_START = 0x8C84 // int
    @GLES3 const val TRANSFORM_FEEDBACK_BUFFER_SIZE = 0x8C85 // int
    @GLES3 const val TRANSFORM_FEEDBACK_BUFFER_BINDING = 0x8C8F // GLBuffer


    ////////// Parameter Values //////////

    // Blend function
    const val ZERO = 0
    const val ONE = 1
    const val SRC_COLOR = 0x0300
    const val ONE_MINUS_SRC_COLOR = 0x0301
    const val SRC_ALPHA = 0x0302
    const val ONE_MINUS_SRC_ALPHA = 0x0303
    const val DST_ALPHA = 0x0304
    const val ONE_MINUS_DST_ALPHA = 0x0305
    const val DST_COLOR = 0x0306
    const val ONE_MINUS_DST_COLOR = 0x0307
    const val SRC_ALPHA_SATURATE = 0x0308
    const val CONSTANT_COLOR = 0x8001
    const val ONE_MINUS_CONSTANT_COLOR = 0x8002
    const val CONSTANT_ALPHA = 0x8003
    const val ONE_MINUS_CONSTANT_ALPHA = 0x8004

    // Blend equation
    const val FUNC_ADD = 0x8006
    const val FUNC_SUBTRACT = 0x800A
    const val FUNC_REVERSE_SUBTRACT = 0x800B
    @GLES3 const val MIN = 0x8007
    @GLES3 const val MAX = 0x8008

    // Comparison function
    const val NEVER = 0x0200
    const val LESS = 0x0201
    const val EQUAL = 0x0202
    const val LEQUAL = 0x0203
    const val GREATER = 0x0204
    const val NOTEQUAL = 0x0205
    const val GEQUAL = 0x0206
    const val ALWAYS = 0x0207

    // Comparison mode
    //const val NONE = 0
    @GLES3 const val COMPARE_REF_TO_TEXTURE = 0x884E

    // Stencil operation
    // const val ZERO = 0
    const val KEEP = 0x1E00
    const val REPLACE = 0x1E01
    const val INCR = 0x1E02
    const val DECR = 0x1E03
    const val INVERT = 0x150A
    const val INCR_WRAP = 0x8507
    const val DECR_WRAP = 0x8508

    // Face
    const val FRONT = 0x0404
    const val BACK = 0x0405
    const val FRONT_AND_BACK =0x0408

    // Face direction
    const val CW = 0x0900
    const val CCW = 0x0901

    // Hint mode
    const val DONT_CARE = 0x1100
    const val FASTEST = 0x1101
    const val NICEST = 0x1102

    ////////// Program and shader parameters //////////
    const val DELETE_STATUS = 0x8B80 // bool
    const val COMPILE_STATUS = 0x8B81 // bool
    const val LINK_STATUS = 0x8B82 // bool
    const val VALIDATE_STATUS = 0x8B83 // bool
    const val INFO_LOG_LENGTH = 0x8B84
    const val ATTACHED_SHADERS = 0x8B85
    const val ACTIVE_UNIFORMS = 0x8B86
    const val ACTIVE_UNIFORM_MAX_LENGTH = 0x8B87
    const val ACTIVE_ATTRIBUTES = 0x8B89
    const val ACTIVE_ATTRIBUTE_MAX_LENGTH = 0x8B8A
    const val SHADER_TYPE = 0x8B4F // shader type
    const val SHADER_SOURCE_LENGTH = 0x8B88
    @GLES3 const val TRANSFORM_FEEDBACK_BUFFER_MODE = 0x8C7F // transform feedback mode
    @GLES3 const val TRANSFORM_FEEDBACK_VARYINGS = 0x8C83
    @GLES3 const val ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH = 0x8A35
    @GLES3 const val ACTIVE_UNIFORM_BLOCKS = 0x8A36
    @GLES3 const val TRANSFORM_FEEDBACK_VARYING_MAX_LENGTH = 0x8C76
    @GLES3 const val PROGRAM_BINARY_RETRIEVABLE_HINT = 0x8257 // bool
    @GLES3 const val PROGRAM_BINARY_LENGTH = 0x8741

    // Shader type
    const val FRAGMENT_SHADER = 0x8B30
    const val VERTEX_SHADER = 0x8B31

    // Precision type
    const val LOW_FLOAT = 0x8DF0
    const val MEDIUM_FLOAT = 0x8DF1
    const val HIGH_FLOAT = 0x8DF2
    const val LOW_INT = 0x8DF3
    const val MEDIUM_INT = 0x8DF4
    const val HIGH_INT = 0x8DF5

    // Transform feedback mode
    @GLES3 const val INTERLEAVED_ATTRIBS = 0x8C8C
    @GLES3 const val SEPARATE_ATTRIBS = 0x8C8D

    // Buffer usage
    const val STATIC_DRAW = 0x88E4
    const val STREAM_DRAW = 0x88E0
    const val DYNAMIC_DRAW = 0x88E8
    @GLES3 const val STREAM_READ = 0x88E1
    @GLES3 const val STREAM_COPY = 0x88E2
    @GLES3 const val STATIC_READ = 0x88E5
    @GLES3 const val STATIC_COPY = 0x88E6
    @GLES3 const val DYNAMIC_READ = 0x88E9
    @GLES3 const val DYNAMIC_COPY = 0x88EA

    // Buffer target
    const val ARRAY_BUFFER = 0x8892
    const val ELEMENT_ARRAY_BUFFER = 0x8893
    @GLES3 const val PIXEL_PACK_BUFFER = 0x88EB
    @GLES3 const val PIXEL_UNPACK_BUFFER = 0x88EC
    @GLES3 const val COPY_READ_BUFFER = 0x8F36
    @GLES3 const val COPY_WRITE_BUFFER = 0x8F37
    @GLES3 const val UNIFORM_BUFFER = 0x8A11
    @GLES3 const val TRANSFORM_FEEDBACK_BUFFER = 0x8C8E

    // Buffer param
    const val BUFFER_SIZE = 0x8764 // long or int
    const val BUFFER_USAGE = 0x8765 // buffer usage
    @GLES3 const val BUFFER_ACCESS_FLAGS = 0x911F // access flags
    @GLES3 const val BUFFER_MAPPED = 0x88BC // bool
    @GLES3 const val BUFFER_MAP_LENGTH = 0x9120 // long
    @GLES3 const val BUFFER_MAP_OFFSET = 0x9121 // long

    // Get buffer pointer
    @GLES3 const val BUFFER_MAP_POINTER = 0x88BD

    // Map access flags
    @GLES3 const val MAP_READ_BIT = 0x0001
    @GLES3 const val MAP_WRITE_BIT = 0x0002
    @GLES3 const val MAP_INVALIDATE_RANGE_BIT = 0x0004
    @GLES3 const val MAP_INVALIDATE_BUFFER_BIT = 0x0008
    @GLES3 const val MAP_FLUSH_EXPLICIT_BIT = 0x0010
    @GLES3 const val MAP_UNSYNCHRONIZED_BIT = 0x0020

    // Clear buffer
    @GLES3 const val COLOR = 0x1800
    @GLES3 const val DEPTH = 0x1801
    @GLES3 const val STENCIL = 0x1802

    // Uniform param
    @GLES3 const val UNIFORM_TYPE = 0x8A37 // uniform data type
    @GLES3 const val UNIFORM_SIZE = 0x8A38
    @GLES3 const val UNIFORM_NAME_LENGTH = 0x8A39
    @GLES3 const val UNIFORM_BLOCK_INDEX = 0x8A3A
    @GLES3 const val UNIFORM_OFFSET = 0x8A3B
    @GLES3 const val UNIFORM_ARRAY_STRIDE = 0x8A3C
    @GLES3 const val UNIFORM_MATRIX_STRIDE = 0x8A3D
    @GLES3 const val UNIFORM_IS_ROW_MAJOR = 0x8A3E // bool

    // Uniform block param
    @GLES3 const val UNIFORM_BLOCK_BINDING = 0x8A3F
    @GLES3 const val UNIFORM_BLOCK_DATA_SIZE = 0x8A40
    @GLES3 const val UNIFORM_BLOCK_NAME_LENGTH = 0x8A41
    @GLES3 const val UNIFORM_BLOCK_ACTIVE_UNIFORMS = 0x8A42
    @GLES3 const val UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES = 0x8A43 // uint array
    @GLES3 const val UNIFORM_BLOCK_REFERENCED_BY_VERTEX_SHADER = 0x8A44 // bool
    @GLES3 const val UNIFORM_BLOCK_REFERENCED_BY_FRAGMENT_SHADER = 0x8A46 // bool

    // Vertex attrib param
    const val VERTEX_ATTRIB_ARRAY_ENABLED = 0x8622 // bool
    const val VERTEX_ATTRIB_ARRAY_SIZE = 0x8623
    const val VERTEX_ATTRIB_ARRAY_STRIDE = 0x8624
    const val VERTEX_ATTRIB_ARRAY_TYPE = 0x8625 // data type
    const val VERTEX_ATTRIB_ARRAY_NORMALIZED = 0x886A // bool
    const val VERTEX_ATTRIB_ARRAY_POINTER = 0x8645
    const val VERTEX_ATTRIB_ARRAY_BUFFER_BINDING = 0x889F // GLBuffer
    const val CURRENT_VERTEX_ATTRIB = 0x8626 // 4 elements, float, ints, or uints
    @GLES3 const val VERTEX_ATTRIB_ARRAY_INTEGER = 0x88FD // bool
    @GLES3 const val VERTEX_ATTRIB_ARRAY_DIVISOR = 0x88FE

    // Framebuffer target
    const val FRAMEBUFFER = 0x8D40
    @GLES3 const val READ_FRAMEBUFFER = 0x8CA8
    @GLES3 const val DRAW_FRAMEBUFFER = 0x8CA9
    @GLES3 const val FRAMEBUFFER_DEFAULT = 0x8218

    // Framebuffer status
    const val FRAMEBUFFER_COMPLETE = 0x8CD5
    const val FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 0x8CD6
    const val FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 0x8CD7
    const val FRAMEBUFFER_INCOMPLETE_DIMENSIONS = 0x8CD9
    const val FRAMEBUFFER_UNSUPPORTED = 0x8CDD
    @GLES3 const val FRAMEBUFFER_UNDEFINED = 0x8219
    @GLES3 const val FRAMEBUFFER_INCOMPLETE_MULTISAMPLE = 0x8D56

    // Framebuffer attachment param
    const val FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE = 0x8CD0 // attachment object type
    const val FRAMEBUFFER_ATTACHMENT_OBJECT_NAME = 0x8CD1 // GLRenderbuffer or GLTexture
    const val FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL = 0x8CD2
    const val FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE = 0x8CD3 // texture cube face
    @GLES3 const val FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING = 0x8210 // color encoding
    @GLES3 const val FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE = 0x8211 // component type
    @GLES3 const val FRAMEBUFFER_ATTACHMENT_RED_SIZE = 0x8212
    @GLES3 const val FRAMEBUFFER_ATTACHMENT_GREEN_SIZE = 0x8213
    @GLES3 const val FRAMEBUFFER_ATTACHMENT_BLUE_SIZE = 0x8214
    @GLES3 const val FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE = 0x8215
    @GLES3 const val FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE = 0x8216
    @GLES3 const val FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE = 0x8217
    @GLES3 const val FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER = 0x8CD4

    // Framebuffer attachment object type
    //const val NONE = 0
    const val TEXTURE = 0x1702
    //const val RENDERBUFFER = 0x8D41
    //@GLES3 const val FRAMEBUFFER_DEFAULT = 0x8218

    // Framebuffer attachment component type
    //@GLES3 const val FLOAT = 0x1406
    //@GLES3 const val INT = 0x1404
    //@GLES3 const val UNSIGNED_INT = 0x1405
    @GLES3 const val UNSIGNED_NORMALIZED = 0x8C17
    @GLES3 const val SIGNED_NORMALIZED = 0x8F9C

    // Renderbuffer target
    const val RENDERBUFFER = 0x8D41

    // Renderbuffer param
    const val RENDERBUFFER_WIDTH = 0x8D42
    const val RENDERBUFFER_HEIGHT = 0x8D43
    const val RENDERBUFFER_INTERNAL_FORMAT = 0x8D44 // internal format
    const val RENDERBUFFER_RED_SIZE = 0x8D50
    const val RENDERBUFFER_GREEN_SIZE = 0x8D51
    const val RENDERBUFFER_BLUE_SIZE = 0x8D52
    const val RENDERBUFFER_ALPHA_SIZE = 0x8D53
    const val RENDERBUFFER_DEPTH_SIZE = 0x8D54
    const val RENDERBUFFER_STENCIL_SIZE = 0x8D55
    @GLES3 const val RENDERBUFFER_SAMPLES = 0x8CAB

    // Renderbuffer internal format param
    @GLES3 const val NUM_SAMPLE_COUNTS = 0x9380 // int
    //const val SAMPLES = 0x80A9 // int array

    // Texture targets
    const val TEXTURE_2D = 0x0DE1
    const val TEXTURE_CUBE_MAP = 0x8513
    @GLES3 const val TEXTURE_3D = 0x806F
    @GLES3 const val TEXTURE_2D_ARRAY = 0x8C1A

    // Texture cube map face
    const val TEXTURE_CUBE_MAP_POSITIVE_X = 0x8515
    const val TEXTURE_CUBE_MAP_NEGATIVE_X = 0x8516
    const val TEXTURE_CUBE_MAP_POSITIVE_Y = 0x8517
    const val TEXTURE_CUBE_MAP_NEGATIVE_Y = 0x8518
    const val TEXTURE_CUBE_MAP_POSITIVE_Z = 0x8519
    const val TEXTURE_CUBE_MAP_NEGATIVE_Z = 0x851A

    // Texture param
    const val TEXTURE_MAG_FILTER = 0x2800 // mag filter
    const val TEXTURE_MIN_FILTER = 0x2801 // min filter
    const val TEXTURE_WRAP_S = 0x2802 // wrap
    const val TEXTURE_WRAP_T = 0x2803 // wrap
    @GLES3 const val TEXTURE_WRAP_R = 0x8072 // wrap
    @GLES3 const val TEXTURE_COMPARE_MODE = 0x884C // compare mode
    @GLES3 const val TEXTURE_COMPARE_FUNC = 0x884D // compare function
    @GLES3 const val TEXTURE_MIN_LOD = 0x813A // float
    @GLES3 const val TEXTURE_MAX_LOD = 0x813B // float
    @GLES3 const val TEXTURE_BASE_LEVEL = 0x813C // int
    @GLES3 const val TEXTURE_MAX_LEVEL = 0x813D // int
    @GLES3 const val TEXTURE_IMMUTABLE_FORMAT = 0x912F // bool
    @GLES3 const val TEXTURE_IMMUTABLE_LEVELS = 0x82DF // int
    @GLES3 const val TEXTURE_SWIZZLE_R = 0x8E42 // color swizzle
    @GLES3 const val TEXTURE_SWIZZLE_G = 0x8E43 // color swizzle
    @GLES3 const val TEXTURE_SWIZZLE_B = 0x8E44 // color swizzle
    @GLES3 const val TEXTURE_SWIZZLE_A = 0x8E45 // color swizzle

    // Mag filter
    const val NEAREST = 0x2600
    const val LINEAR = 0x2601

    // Min filter
    //const val NEAREST = 0x2600
    //const val LINEAR = 0x2601
    const val NEAREST_MIPMAP_NEAREST = 0x2700
    const val LINEAR_MIPMAP_NEAREST = 0x2701
    const val NEAREST_MIPMAP_LINEAR = 0x2702
    const val LINEAR_MIPMAP_LINEAR = 0x2703

    // Wrap
    const val REPEAT = 0x2901
    const val CLAMP_TO_EDGE = 0x812F
    const val MIRRORED_REPEAT = 0x8370

    // Color swizzle
    //@GLES3 const val RED = 0x1903
    @GLES3 const val GREEN = 0x1904
    @GLES3 const val BLUE = 0x1905
    //const val ALPHA = 0x1906
    //const val ZERO = 0
    //const val ONE = 1

    // Query lookup
    @GLES3 const val CURRENT_QUERY = 0x8865

    // Query target
    @GLES3 const val ANY_SAMPLES_PASSED = 0x8C2F
    @GLES3 const val ANY_SAMPLES_PASSED_CONSERVATIVE = 0x8D6A
    @GLES3 const val TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN = 0x8C88

    // Query param
    @GLES3 const val QUERY_RESULT = 0x8866
    @GLES3 const val QUERY_RESULT_AVAILABLE = 0x8867

    // Sync wait flags
    @GLES3 const val SYNC_FLUSH_COMMANDS_BIT = 0x00000001

    // Sync wait status
    @GLES3 const val ALREADY_SIGNALED = 0x911A
    @GLES3 const val TIMEOUT_EXPIRED = 0x911B
    @GLES3 const val CONDITION_SATISFIED = 0x911C
    @GLES3 const val WAIT_FAILED = 0x911D

    // Sync wait timeout
    @GLES3 const val TIMEOUT_IGNORED: Long = -1 // defined as 0xFFFFFFFFFFFFFFFFull in C

    // Sync param
    @GLES3 const val OBJECT_TYPE = 0x9112 // always SYNC_FENCE
    @GLES3 const val SYNC_CONDITION = 0x9113 // always SYNC_GPU_COMMANDS_COMPLETE
    @GLES3 const val SYNC_STATUS = 0x9114 // sync status
    @GLES3 const val SYNC_FLAGS = 0x9115 // always 0

    // Sync object type and condition
    @GLES3 const val SYNC_FENCE = 0x9116
    @GLES3 const val SYNC_GPU_COMMANDS_COMPLETE = 0x9117

    // Sync status
    @GLES3 const val UNSIGNALED = 0x9118
    @GLES3 const val SIGNALED = 0x9119

    // Transform feedback target
    @GLES3 const val TRANSFORM_FEEDBACK = 0x8E22

    // Draw mode
    const val POINTS = 0x0000
    const val LINES = 0x0001
    const val LINE_LOOP = 0x0002
    const val LINE_STRIP = 0x0003
    const val TRIANGLES = 0x0004
    const val TRIANGLE_STRIP = 0x0005
    const val TRIANGLE_FAN = 0x0006

    // Bitmask for clear
    const val DEPTH_BUFFER_BIT = 0x00000100u
    const val STENCIL_BUFFER_BIT = 0x00000400u
    const val COLOR_BUFFER_BIT = 0x00004000u

    // Texture units
    const val TEXTURE0 = 0x84C0
    const val TEXTURE1 = 0x84C1
    const val TEXTURE2 = 0x84C2
    const val TEXTURE3 = 0x84C3
    const val TEXTURE4 = 0x84C4
    const val TEXTURE5 = 0x84C5
    const val TEXTURE6 = 0x84C6
    const val TEXTURE7 = 0x84C7
    const val TEXTURE8 = 0x84C8
    const val TEXTURE9 = 0x84C9
    const val TEXTURE10 = 0x84CA
    const val TEXTURE11 = 0x84CB
    const val TEXTURE12 = 0x84CC
    const val TEXTURE13 = 0x84CD
    const val TEXTURE14 = 0x84CE
    const val TEXTURE15 = 0x84CF
    const val TEXTURE16 = 0x84D0
    const val TEXTURE17 = 0x84D1
    const val TEXTURE18 = 0x84D2
    const val TEXTURE19 = 0x84D3
    const val TEXTURE20 = 0x84D4
    const val TEXTURE21 = 0x84D5
    const val TEXTURE22 = 0x84D6
    const val TEXTURE23 = 0x84D7
    const val TEXTURE24 = 0x84D8
    const val TEXTURE25 = 0x84D9
    const val TEXTURE26 = 0x84DA
    const val TEXTURE27 = 0x84DB
    const val TEXTURE28 = 0x84DC
    const val TEXTURE29 = 0x84DD
    const val TEXTURE30 = 0x84DE
    const val TEXTURE31 = 0x84DF

    // Framebuffer attachments
    const val COLOR_ATTACHMENT0 = 0x8CE0
    @GLES3 const val COLOR_ATTACHMENT1 = 0x8CE1
    @GLES3 const val COLOR_ATTACHMENT2 = 0x8CE2
    @GLES3 const val COLOR_ATTACHMENT3 = 0x8CE3
    @GLES3 const val COLOR_ATTACHMENT4 = 0x8CE4
    @GLES3 const val COLOR_ATTACHMENT5 = 0x8CE5
    @GLES3 const val COLOR_ATTACHMENT6 = 0x8CE6
    @GLES3 const val COLOR_ATTACHMENT7 = 0x8CE7
    @GLES3 const val COLOR_ATTACHMENT8 = 0x8CE8
    @GLES3 const val COLOR_ATTACHMENT9 = 0x8CE9
    @GLES3 const val COLOR_ATTACHMENT10 = 0x8CEA
    @GLES3 const val COLOR_ATTACHMENT11 = 0x8CEB
    @GLES3 const val COLOR_ATTACHMENT12 = 0x8CEC
    @GLES3 const val COLOR_ATTACHMENT13 = 0x8CED
    @GLES3 const val COLOR_ATTACHMENT14 = 0x8CEE
    @GLES3 const val COLOR_ATTACHMENT15 = 0x8CEF
    const val DEPTH_ATTACHMENT = 0x8D00
    @GLES3 const val DEPTH_STENCIL_ATTACHMENT = 0x821A
    const val STENCIL_ATTACHMENT = 0x8D20

    // Uniform Types
    //const val FLOAT = 0x1406
    const val FLOAT_VEC2 = 0x8B50
    const val FLOAT_VEC3 = 0x8B51
    const val FLOAT_VEC4 = 0x8B52
    //const val INT = 0x1404
    const val INT_VEC2 = 0x8B53
    const val INT_VEC3 = 0x8B54
    const val INT_VEC4 = 0x8B55
    //const val UNSIGNED_INT = 0x1405
    @GLES3 const val UNSIGNED_INT_VEC2 = 0x8DC6
    @GLES3 const val UNSIGNED_INT_VEC3 = 0x8DC7
    @GLES3 const val UNSIGNED_INT_VEC4 = 0x8DC8
    const val BOOL = 0x8B56
    const val BOOL_VEC2 = 0x8B57
    const val BOOL_VEC3 = 0x8B58
    const val BOOL_VEC4 = 0x8B59
    const val FLOAT_MAT2 = 0x8B5A
    const val FLOAT_MAT3 = 0x8B5B
    const val FLOAT_MAT4 = 0x8B5C
    @GLES3 const val FLOAT_MAT2x3 = 0x8B65
    @GLES3 const val FLOAT_MAT2x4 = 0x8B66
    @GLES3 const val FLOAT_MAT3x2 = 0x8B67
    @GLES3 const val FLOAT_MAT3x4 = 0x8B68
    @GLES3 const val FLOAT_MAT4x2 = 0x8B69
    @GLES3 const val FLOAT_MAT4x3 = 0x8B6A
    const val SAMPLER_2D = 0x8B5E
    @GLES3 const val SAMPLER_3D = 0x8B5F
    const val SAMPLER_CUBE = 0x8B60
    @GLES3 const val SAMPLER_2D_SHADOW = 0x8B62
    @GLES3 const val SAMPLER_2D_ARRAY = 0x8DC1
    @GLES3 const val SAMPLER_2D_ARRAY_SHADOW = 0x8DC4
    @GLES3 const val SAMPLER_CUBE_SHADOW = 0x8DC5
    @GLES3 const val INT_SAMPLER_2D = 0x8DCA
    @GLES3 const val INT_SAMPLER_3D = 0x8DCB
    @GLES3 const val INT_SAMPLER_CUBE = 0x8DCC
    @GLES3 const val INT_SAMPLER_2D_ARRAY = 0x8DCF
    @GLES3 const val UNSIGNED_INT_SAMPLER_2D = 0x8DD2
    @GLES3 const val UNSIGNED_INT_SAMPLER_3D = 0x8DD3
    @GLES3 const val UNSIGNED_INT_SAMPLER_CUBE = 0x8DD4
    @GLES3 const val UNSIGNED_INT_SAMPLER_2D_ARRAY = 0x8DD7


    @GLES3 const val DEPTH_COMPONENT = 0x1902
    const val DEPTH_COMPONENT16 = 0x81A5
    @GLES3 const val DEPTH_COMPONENT24 = 0x81A6
    @GLES3 const val DEPTH_COMPONENT32F = 0x8CAC

    const val STENCIL_INDEX8 = 0x8D48

    @GLES3 const val DEPTH_STENCIL = 0x84F9
    @GLES3 const val DEPTH24_STENCIL8 = 0x88F0
    @GLES3 const val DEPTH32F_STENCIL8 = 0x8CAD


    const val BYTE = 0x1400
    const val UNSIGNED_BYTE = 0x1401
    const val SHORT = 0x1402
    const val UNSIGNED_SHORT = 0x1403
    const val INT = 0x1404
    const val UNSIGNED_INT = 0x1405
    const val FLOAT = 0x1406
    @GLES3 const val HALF_FLOAT = 0x140B
    const val FIXED = 0x140C

    const val ALPHA = 0x1906
    const val RGB = 0x1907
    const val RGBA = 0x1908
    const val LUMINANCE = 0x1909
    const val LUMINANCE_ALPHA = 0x190A

    const val UNSIGNED_SHORT_4_4_4_4 = 0x8033
    const val UNSIGNED_SHORT_5_5_5_1 = 0x8034
    const val UNSIGNED_SHORT_5_6_5 = 0x8363

    const val RGBA4 = 0x8056
    const val RGB5_A1 = 0x8057
    const val RGB565 = 0x8D62

    @GLES3 const val RED = 0x1903
    @GLES3 const val RED_INTEGER = 0x8D94
    @GLES3 const val RG = 0x8227
    @GLES3 const val RG_INTEGER = 0x8228
    @GLES3 const val RGB_INTEGER = 0x8D98
    @GLES3 const val RGBA_INTEGER = 0x8D99
    @GLES3 const val RGB8 = 0x8051
    @GLES3 const val RGBA8 = 0x8058
    @GLES3 const val RGB10_A2 = 0x8059

    @GLES3 const val UNSIGNED_INT_2_10_10_10_REV = 0x8368
    @GLES3 const val UNSIGNED_INT_10F_11F_11F_REV = 0x8C3B
    @GLES3 const val UNSIGNED_INT_5_9_9_9_REV = 0x8C3E
    @GLES3 const val FLOAT_32_UNSIGNED_INT_24_8_REV = 0x8DAD
    @GLES3 const val UNSIGNED_INT_24_8 = 0x84FA
    @GLES3 const val INT_2_10_10_10_REV = 0x8D9F

    @GLES3 const val SRGB = 0x8C40
    @GLES3 const val SRGB8 = 0x8C41
    @GLES3 const val SRGB8_ALPHA8 = 0x8C43
    @GLES3 const val RGBA32F = 0x8814
    @GLES3 const val RGB32F = 0x8815
    @GLES3 const val RGBA16F = 0x881A
    @GLES3 const val RGB16F = 0x881B
    @GLES3 const val R11F_G11F_B10F = 0x8C3A
    @GLES3 const val RGB9_E5 = 0x8C3D
    @GLES3 const val RGBA32UI = 0x8D70
    @GLES3 const val RGB32UI = 0x8D71
    @GLES3 const val RGBA16UI = 0x8D76
    @GLES3 const val RGB16UI = 0x8D77
    @GLES3 const val RGBA8UI = 0x8D7C
    @GLES3 const val RGB8UI = 0x8D7D
    @GLES3 const val RGBA32I = 0x8D82
    @GLES3 const val RGB32I = 0x8D83
    @GLES3 const val RGBA16I = 0x8D88
    @GLES3 const val RGB16I = 0x8D89
    @GLES3 const val RGBA8I = 0x8D8E
    @GLES3 const val RGB8I = 0x8D8F
    @GLES3 const val R8 = 0x8229
    @GLES3 const val RG8 = 0x822B
    @GLES3 const val R16F = 0x822D
    @GLES3 const val R32F = 0x822E
    @GLES3 const val RG16F = 0x822F
    @GLES3 const val RG32F = 0x8230
    @GLES3 const val R8I = 0x8231
    @GLES3 const val R8UI = 0x8232
    @GLES3 const val R16I = 0x8233
    @GLES3 const val R16UI = 0x8234
    @GLES3 const val R32I = 0x8235
    @GLES3 const val R32UI = 0x8236
    @GLES3 const val RG8I = 0x8237
    @GLES3 const val RG8UI = 0x8238
    @GLES3 const val RG16I = 0x8239
    @GLES3 const val RG16UI = 0x823A
    @GLES3 const val RG32I = 0x823B
    @GLES3 const val RG32UI = 0x823C
    @GLES3 const val R8_SNORM = 0x8F94
    @GLES3 const val RG8_SNORM = 0x8F95
    @GLES3 const val RGB8_SNORM = 0x8F96
    @GLES3 const val RGBA8_SNORM = 0x8F97
    @GLES3 const val RGB10_A2UI = 0x906F

    @GLES3 const val COMPRESSED_R11_EAC = 0x9270
    @GLES3 const val COMPRESSED_SIGNED_R11_EAC = 0x9271
    @GLES3 const val COMPRESSED_RG11_EAC = 0x9272
    @GLES3 const val COMPRESSED_SIGNED_RG11_EAC = 0x9273
    @GLES3 const val COMPRESSED_RGB8_ETC2 = 0x9274
    @GLES3 const val COMPRESSED_SRGB8_ETC2 = 0x9275
    @GLES3 const val COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2 = 0x9276
    @GLES3 const val COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2 = 0x9277
    @GLES3 const val COMPRESSED_RGBA8_ETC2_EAC = 0x9278
    @GLES3 const val COMPRESSED_SRGB8_ALPHA8_ETC2_EAC = 0x9279
}
