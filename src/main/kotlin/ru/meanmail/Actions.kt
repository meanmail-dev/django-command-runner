package ru.meanmail

import com.intellij.coverage.CoverageExecutor
import com.intellij.execution.Executor
import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.text.TextWithMnemonic.fromPlainText
import com.jetbrains.python.profiler.ProfilerExecutor
import com.jetbrains.python.run.PythonConfigurationType
import com.jetbrains.python.run.PythonRunConfiguration
import javax.swing.Icon


@Suppress("MissingRecentApi")
abstract class Action(private val command: String,
                      title: String, description: String,
                      icon: Icon) : AnAction(title, description, icon) {

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val djangoFacet = findDjangoFacet(file)

        val runManager = RunManager.getInstance(file.project)
        var config = runManager.findConfigurationByName(command)

        if (config == null) {
            config = runManager.createConfiguration(command,
                    PythonConfigurationType::class.java)
        }

        val configuration = config.configuration as PythonRunConfiguration
        configuration.scriptName = djangoFacet?.configuration?.manageFilePath ?: "manage.py"
        val module = djangoFacet?.module ?: ModuleUtil.findModuleForFile(file)
        configuration.configurationModule.module = module
        val workingDirectory = djangoFacet?.configuration?.projectRootPath
                ?: module?.moduleFile?.parent?.canonicalPath
                ?: file.project.basePath
        configuration.baseParams.workingDirectory = workingDirectory
        if (command !in configuration.scriptParameters) {
            configuration.scriptParameters += command
        }

        runManager.addConfiguration(config)
        runManager.selectedConfiguration = config

        ProgramRunnerUtil.executeConfiguration(config, getExecutor())
    }

    open fun getExecutor(): Executor {
        throw NotImplementedError()
    }
}

class RunAction(command: String) :
        Action(command,
                "Run '${fromPlainText(command)}'",
                "Run '${fromPlainText(command)}'",
                AllIcons.Actions.Execute) {

    override fun getExecutor(): Executor {
        return DefaultRunExecutor.getRunExecutorInstance()
    }
}


class DebugAction(command: String) :
        Action(command,
                "Debug '${fromPlainText(command)}'",
                "Debug '${fromPlainText(command)}'",
                AllIcons.Actions.StartDebugger) {

    override fun getExecutor(): Executor {
        return DefaultDebugExecutor.getDebugExecutorInstance()
    }
}

class RunWithCoverageAction(command: String) :
        Action(command,
                "Run '${fromPlainText(command)}' with Coverage",
                "Run '${fromPlainText(command)}' with Coverage",
                AllIcons.General.RunWithCoverage) {

    override fun getExecutor(): Executor {
        val executor = ExecutorRegistry.getInstance()
                .getExecutorById(CoverageExecutor.EXECUTOR_ID)

        return executor ?: DefaultRunExecutor.getRunExecutorInstance()
    }
}

class ProfileAction(command: String) :
        Action(command,
                "Profile '${fromPlainText(command)}'",
                "Profile '${fromPlainText(command)}'",
                AllIcons.Actions.Profile) {

    override fun getExecutor(): Executor {
        val executor = ExecutorRegistry.getInstance()
                .getExecutorById(ProfilerExecutor.PROFILE_EXECUTOR_ID)

        return executor ?: DefaultRunExecutor.getRunExecutorInstance()
    }
}
