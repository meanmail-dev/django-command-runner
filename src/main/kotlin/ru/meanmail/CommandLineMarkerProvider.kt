package ru.meanmail


import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyReferenceExpression


class CommandLineMarkerProvider : RunLineMarkerContributor() {

    private fun isManagementCommand(element: PyClass): Boolean {
        val superclasses = element.superClassExpressions
        if (superclasses.isEmpty()) {
            return false
        }

        val baseCommand = superclasses.filterIsInstance<PyReferenceExpression>()
                .firstOrNull { it.name == "BaseCommand" }
        if (baseCommand === null) {
            return false
        }

        val `package` = baseCommand.asQualifiedName()?.firstComponent

        for (import in (baseCommand.containingFile as PyFile).fromImports) {
            val from = import.importSourceQName.toString()

            for (importElement in import.importElements) {
                if (`package` != importElement.visibleName) {
                    continue
                }
                val fullname = "$from.${baseCommand.text}"
                if (fullname in expectedSuperclasses) {
                    return true
                }
            }
        }

        for (import in (baseCommand.containingFile as PyFile).importTargets) {
            if (import.visibleName in expectedSuperclasses) {
                return true
            }
        }
        return false
    }

    companion object {
        val expectedSuperclasses = listOf(
                "django.core.management.BaseCommand",
                "django.core.management.base.BaseCommand"
        )
    }

    override fun getInfo(element: PsiElement): Info? {
        if (element !is PyClass) {
            return null
        }

        if (!isManagementCommand(element)) {
            return null
        }

        val command = element.containingFile.virtualFile.nameWithoutExtension

        return Info(AllIcons.Actions.Execute, null,
                RunAction(command),
                DebugAction(command),
                RunWithCoverageAction(command),
                ProfileAction(command)
        )
    }

}
