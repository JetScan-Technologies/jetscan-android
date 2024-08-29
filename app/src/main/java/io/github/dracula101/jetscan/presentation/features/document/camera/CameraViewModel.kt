package io.github.dracula101.jetscan.presentation.features.document.camera

import android.content.Context
import android.os.Parcelable
import androidx.camera.core.CameraState
import androidx.camera.core.CameraState.ERROR_CAMERA_DISABLED
import androidx.camera.core.CameraState.ERROR_CAMERA_FATAL_ERROR
import androidx.camera.core.CameraState.ERROR_CAMERA_IN_USE
import androidx.camera.core.CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED
import androidx.camera.core.CameraState.ERROR_MAX_CAMERAS_IN_USE
import androidx.camera.core.CameraState.ERROR_OTHER_RECOVERABLE_ERROR
import androidx.camera.core.CameraState.ERROR_STREAM_CONFIG
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state: MutableStateFlow<AppCameraStatus> =
        MutableStateFlow(AppCameraStatus.Initial)
    val state = _state.asStateFlow()

    private fun onCameraEvent(cameraState: AppCameraStatus) {
        when (cameraState) {
            is AppCameraStatus.Initial -> {
            }

            is AppCameraStatus.CameraInitializationError -> {
                Timber.e("Camera Initialization Error")
            }

            is AppCameraStatus.CameraInitializationSuccess -> {
                Timber.i("Camera Initialization Success")
            }

            AppCameraStatus.PendingCameraInitialization -> {
                Timber.i("Camera Initialization Pending")
            }

            AppCameraStatus.CameraAlreadyInUse -> {
                Timber.e("Camera already in use")
            }

            AppCameraStatus.CameraClosed -> {
                Timber.i("Camera Closed")
            }
        }
        _state.value = cameraState
    }

    fun onCameraStateEvent(state: CameraState) {
        if (state.error != null) {
            return when (state.error!!.code) {
                ERROR_CAMERA_IN_USE, ERROR_MAX_CAMERAS_IN_USE -> {
                    onCameraEvent(AppCameraStatus.CameraAlreadyInUse)
                }

                ERROR_OTHER_RECOVERABLE_ERROR -> {
                    onCameraEvent(
                        AppCameraStatus.CameraInitializationError(
                            "Camera initialization error",
                            state.error!!.cause
                        )
                    )
                }

                ERROR_STREAM_CONFIG -> {
                    onCameraEvent(
                        AppCameraStatus.CameraInitializationError(
                            "Camera stream config error",
                            state.error!!.cause
                        )
                    )
                }

                ERROR_CAMERA_DISABLED -> {
                    onCameraEvent(
                        AppCameraStatus.CameraInitializationError(
                            "Camera disabled",
                            state.error!!.cause
                        )
                    )
                }

                ERROR_CAMERA_FATAL_ERROR -> {
                    onCameraEvent(
                        AppCameraStatus.CameraInitializationError(
                            "Fatal Error while opening camera",
                            state.error!!.cause
                        )
                    )
                }

                ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {
                    onCameraEvent(
                        AppCameraStatus.CameraInitializationError(
                            "Do not disturb mode enabled",
                            state.error!!.cause
                        )
                    )
                }

                else -> {}
            }
        }
        return when (state.type) {
            CameraState.Type.PENDING_OPEN, CameraState.Type.OPENING -> {
                onCameraEvent(AppCameraStatus.PendingCameraInitialization)
            }

            CameraState.Type.OPEN -> {
                onCameraEvent(AppCameraStatus.CameraInitializationSuccess)
            }

            CameraState.Type.CLOSING, CameraState.Type.CLOSED -> {
                onCameraEvent(AppCameraStatus.CameraClosed)
            }
        }
    }

}


sealed class AppCameraStatus {

    @Parcelize
    data object Initial : AppCameraStatus(), Parcelable

    @Parcelize
    data class CameraInitializationError(
        val message: String,
        val cause: Throwable? = null
    ) : AppCameraStatus(), Parcelable

    @Parcelize
    data object PendingCameraInitialization : AppCameraStatus(), Parcelable

    @Parcelize
    data object CameraAlreadyInUse : AppCameraStatus(), Parcelable

    @Parcelize
    data object CameraInitializationSuccess : AppCameraStatus(), Parcelable

    @Parcelize
    data object CameraClosed : AppCameraStatus(), Parcelable

}