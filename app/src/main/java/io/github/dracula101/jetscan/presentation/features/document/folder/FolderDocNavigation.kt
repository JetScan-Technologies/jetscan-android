package io.github.dracula101.jetscan.presentation.features.document.folder

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.doc.DocumentFolder
import io.github.dracula101.jetscan.presentation.features.auth.authGraph
import io.github.dracula101.jetscan.presentation.platform.base.util.composableWithPushTransitions
import io.github.dracula101.jetscan.presentation.platform.composition.LockOrientation
import timber.log.Timber


const val FOLDER_DOCUMENT_ROUTE = "folder_document"
const val FOLDER_DOCUMENT_ID_ARGUMENT = "uid"
const val FOLDER_DOCUMENT_PATH_ARGUMENT = "path"

fun NavGraphBuilder.createFolderDocumentDestinationRoute(
    onNavigateBack: () -> Unit,
    navigateToDoc: (Document) -> Unit,
    navigateToFolder: (DocumentFolder) -> Unit,
    navigateToFolderPath: (folderName:String, folderDocId: String, path: String) -> Unit
) {
    composableWithPushTransitions(
        route = "$FOLDER_DOCUMENT_ROUTE/{$FOLDER_DOCUMENT_ID_ARGUMENT}/{$FOLDER_DOCUMENT_PATH_ARGUMENT}",
        arguments = listOf(
            navArgument(FOLDER_DOCUMENT_ID_ARGUMENT) {
                type = NavType.StringType
            },
            navArgument(FOLDER_DOCUMENT_PATH_ARGUMENT) {
                type = NavType.StringType
            }
        ),
    ){
        val documentID = it.arguments?.getString(FOLDER_DOCUMENT_ID_ARGUMENT) ?: ""
        val path = Uri.decode(it.arguments?.getString(FOLDER_DOCUMENT_PATH_ARGUMENT) ?: DocumentFolder.ROOT_FOLDER)
        LockOrientation (isBoth = true){
            FolderDocumentScreen(
                documentId = documentID,
                path = path,
                onBack = onNavigateBack,
                onNavigateToDocument = { document->
                    navigateToDoc(document)
                },
                onNavigateToFolder ={ folder->
                    navigateToFolder(folder)
                },
                onNavigateToFolderPath = { folderName, path->
                    navigateToFolderPath(folderName, documentID, path)
                }
            )
        }
    }
}
/**
 * Navigate to the home screen. Note this will only work if home destination was added
 * via [authGraph].
 */
fun NavController.navigateToFolder(
    folderId: String,
    path: String,
    navOptions: NavOptions? = null,
) {
    val encodedPath = Uri.encode(path)
    navigate("$FOLDER_DOCUMENT_ROUTE/$folderId/$encodedPath", navOptions)
}

fun NavController.navigateToFolderDest(
    folderName: String,
    folderId: String,
    destPath: String,
){
    val currentDocumentPath = Uri.decode(currentBackStackEntry?.arguments?.getString(FOLDER_DOCUMENT_PATH_ARGUMENT) ?: "") + "/" + folderName
    if(currentDocumentPath == destPath) return
    val pathParts = currentDocumentPath.split("/")
    val destPathParts = destPath.split("/")
    val commonPath = pathParts.zip(destPathParts).takeWhile { it.first == it.second }.map { it.first }
    val pathToNavigateBack = pathParts.drop(commonPath.size)
    repeat(pathToNavigateBack.size){
        popBackStack()
    }
}
