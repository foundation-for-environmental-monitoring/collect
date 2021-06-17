package org.odk.collect.android.projects

import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.projects.Project
import org.odk.collect.projects.Project.Saved
import org.odk.collect.projects.ProjectsRepository

class ProjectImporter(
    private val storagePathProvider: StoragePathProvider,
    private val projectsRepository: ProjectsRepository
) {
    fun importNewProject(project: Project.New): Saved {
        val savedProject = projectsRepository.save(project)
        createProjectDirs(savedProject)
        return savedProject
    }

    fun importDemoProject() {
        val project = Saved(DEMO_PROJECT_ID, "Demo project", "D", "#3e9fcc")
        projectsRepository.save(project)
        createProjectDirs(project)
    }

    private fun createProjectDirs(project: Saved) {
        storagePathProvider.getProjectDirPaths(project.uuid).forEach { FileUtils.createDir(it) }
    }

    companion object {
        const val DEMO_PROJECT_ID = "DEMO"
    }
}
