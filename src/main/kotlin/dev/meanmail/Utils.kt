package dev.meanmail

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import java.io.File

/**
 * A simple class to hold Django project configuration information
 * that was previously obtained from DjangoFacet
 */
class DjangoProjectInfo(
    val module: Module?,
    val manageFilePath: String,
    val projectRootPath: String?
)

/**
 * Find Django project information without relying on DjangoFacet
 */
fun findDjangoFacet(element: PsiElement): DjangoProjectInfo? {
    val module = ModuleUtil.findModuleForPsiElement(element) ?: return null
    val project = element.project

    // Try to find manage.py in the module's content roots
    val sourceRoots = module.rootManager.sourceRoots
    var manageFile: VirtualFile? = null
    var projectRoot: VirtualFile? = null

    // Look for manage.py in source roots
    for (root in sourceRoots) {
        val managePy = root.findChild("manage.py")
        if (managePy != null && managePy.exists()) {
            manageFile = managePy
            projectRoot = root
            break
        }
    }

    // If not found in source roots, try to find it in the project base directory
    if (manageFile == null) {
        val basePath = project.basePath
        if (basePath != null) {
            val baseDir = File(basePath)
            val managePyFile = File(baseDir, "manage.py")
            if (managePyFile.exists()) {
                return DjangoProjectInfo(
                    module,
                    managePyFile.absolutePath,
                    basePath
                )
            }
        }
        return null
    }

    return DjangoProjectInfo(
        module,
        manageFile.path,
        projectRoot?.path
    )
}
