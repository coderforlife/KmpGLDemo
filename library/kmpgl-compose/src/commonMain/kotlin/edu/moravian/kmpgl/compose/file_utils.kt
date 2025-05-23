package edu.moravian.kmpgl.compose

import androidx.compose.runtime.Composable

/**
 * This function is used to get the context of the current composable to be
 * used by the readAsset function. On Android, it returns the current context,
 * while on iOS, it returns null.
 */
@Composable
expect fun getContext(): Any?

/**
 * This function is used to read an asset file from the app's assets folder.
 * It returns null if the file is not found or if there is an error reading the
 * file.
 *
 * To have a file added to the assets folder, see the following:
 *      https://medium.com/@kennedy998/bundle-and-read-assets-as-resources-in-kotlin-multiplatform-android-ios-and-desktop-3d48d3e3cd5b
 * TLDR:
 *      Create the folder "<composeApp>/src/commonMain/resources/assets"
 *      Make sure commonMain dependencies includes `implementation(compose.components.resources)`
 *      Make sure sourceSets["main"].apply { } includes:
 *          assets.srcDirs("src/commonMain/resources/assets")
 *      If there is a resources.srcDirs("src/commonMain/resources"), comment it out
 *
 * This should really be a suspend function, but it is just used for testing purposes
 * so it is not a suspend function for now.
 */
expect fun readAsset(path: String, context: Any?): ByteArray?

/**
 * This function is a convenience function that calls readAsset with the
 * current context.
 */
@Composable
fun readAsset(path: String): ByteArray? = readAsset(path, getContext())
