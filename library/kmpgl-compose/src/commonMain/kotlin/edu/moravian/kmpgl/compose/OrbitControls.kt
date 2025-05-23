/**
 * Orbit controls for a GLView.
 *
 * This requires a modifier and a view model to be passed in.
 *
 * val viewModel = viewModel { GLViewModel() }
 * val orbitControls = rememberOrbitControlsState()
 * GLView(modifier = Modifier.orbitControls(orbitControls, viewModel))
 */

package edu.moravian.kmpgl.compose

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import edu.moravian.kmpgl.math.Euler
import edu.moravian.kmpgl.math.Matrix4
import edu.moravian.kmpgl.math.PI_2
import edu.moravian.kmpgl.math.Quaternion
import edu.moravian.kmpgl.math.Vector3
import edu.moravian.kmpgl.math.Vector3.Companion.ORIGIN
import edu.moravian.kmpgl.math.degToRad
import edu.moravian.kmpgl.math.multiply
import edu.moravian.kmpgl.math.timesAssign
import kotlin.jvm.Transient
import kotlin.math.PI
import kotlin.math.abs

interface ViewControlsState {
    val viewMatrix: Matrix4
    fun update(): Matrix4
}

// This is explicitly not using mutable states since the changes happen extremely rapidly
@Composable
fun rememberOrbitControlsState(
    key: String? = null,
    position: Vector3 = Vector3(),
    rotation: Euler = Euler(),
    scale: Vector3 = Vector3(1f, 1f, 1f)
) = rememberSaveable(saver = OrbitControlsState.Saver, key = key) { OrbitControlsState(position, rotation, scale) }

data class OrbitControlsState(
    val position: Vector3 = Vector3(),
    val rotation: Euler = Euler(),
    val scale: Vector3 = Vector3(1f, 1f, 1f),
): ViewControlsState {
    @Transient
    override val viewMatrix = Matrix4() // only used as a temporary

    @Transient
    private val quat = Quaternion() // only used as a temporary

    override fun update() =
        viewMatrix.compose(position, quat.setFromEuler(rotation), scale).multiply(ISOMETRIC_VIEW)

    companion object {
        private val CAMERA_ISOMETRIC_POS = Vector3(-1f, -1f, 1f)
        private val CAMERA_ISOMETRIC_UP = Vector3(0f, 0f, 1f)
        val ISOMETRIC_VIEW = Matrix4()
            .lookAt(CAMERA_ISOMETRIC_POS, ORIGIN, CAMERA_ISOMETRIC_UP)
            .invert()

        val Saver = Saver<OrbitControlsState, List<List<Float>>>(
            save = {
                listOf(
                    it.position.toArray().toList(),
                    it.rotation.toArray().toList(),
                    it.scale.toArray().toList(),
                )
            },
            restore = {
                OrbitControlsState(
                    Vector3().fromArray(it[0].toFloatArray()),
                    Euler().fromArray(it[1].toFloatArray()),
                    Vector3().fromArray(it[2].toFloatArray()),
                ).apply { update() }
            },
        )
    }
}

fun Modifier.orbitControls(state: OrbitControlsState, viewModel: GLViewModel) = this.pointerInput(Unit) {
    detectTransformGestures(
        onGesture = { _, pan, panMulti, zoom, rotate ->
            println("onGesture: pan: $pan, panMulti: $panMulti, zoom: $zoom, rotate: $rotate")
            state.scale *= zoom
            if (panMulti != Offset.Zero) {
                state.position.x += panMulti.x * 2f / viewModel.width
                state.position.y -= panMulti.y * 2f / viewModel.height
            } else {
                state.rotation.y += pan.x * PI_2 / viewModel.width
                state.rotation.x += pan.y * PI_2 / viewModel.height
            }
            state.rotation.z -= degToRad(rotate)
            state.update()
        }
    )
}

/**
 * This is taken from androidx.compose.foundation.gestures.detectTransformGestures()
 * but distinguishes between multi-finger panning and single-finger panning.
 */
private suspend fun PointerInputScope.detectTransformGestures(
    panZoomLock: Boolean = false,
    onGesture: (centroid: Offset, pan: Offset, panMulti: Offset, zoom: Float, rotation: Float) -> Unit
) {
    awaitEachGesture {
        var rotation = 0f
        var zoom = 1f
        var pan = Offset.Zero
        var panMulti = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop
        var lockedToPanZoom = false

        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.fastAny { it.isConsumed }
            if (!canceled) {
                val zoomChange = event.calculateZoom()
                val rotationChange = event.calculateRotation()
                val panChange = event.calculatePan()
                val multitouch = event.changes.count { change -> change.pressed && change.previousPressed } > 1 // added

                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    rotation += rotationChange
                    if (multitouch) pan += panChange // changed
                    else panMulti += panChange // added

                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize
                    val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                    val panMotion = pan.getDistance()
                    val panMultiMotion = panMulti.getDistance() // added

                    if (zoomMotion > touchSlop ||
                        rotationMotion > touchSlop ||
                        panMotion > touchSlop ||
                        panMultiMotion > touchSlop // added
                    ) {
                        pastTouchSlop = true
                        lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                    }
                }

                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                    if (effectiveRotation != 0f ||
                        zoomChange != 1f ||
                        panChange != Offset.Zero
                    ) {
                        if (multitouch) // added
                            onGesture(centroid, Offset.Zero, panChange, zoomChange, effectiveRotation) // changed
                        else  // added
                            onGesture(centroid, panChange, Offset.Zero, zoomChange, effectiveRotation) // added
                    }
                    event.changes.fastForEach {
                        if (it.positionChanged()) {
                            it.consume()
                        }
                    }
                }
            }
        } while (!canceled && event.changes.fastAny { it.pressed })
    }
}
