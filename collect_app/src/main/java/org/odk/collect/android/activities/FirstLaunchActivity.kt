package org.odk.collect.android.activities

import android.os.Bundle
import org.odk.collect.android.R
import org.odk.collect.android.databinding.FirstLaunchLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.projects.ManualProjectCreatorDialog
import org.odk.collect.android.projects.ProjectImporter
import org.odk.collect.android.projects.QrCodeProjectCreatorDialog
import org.odk.collect.android.utilities.DialogUtils
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.projects.Project
import javax.inject.Inject

class FirstLaunchActivity : CollectAbstractActivity() {

    @Inject
    lateinit var projectImporter: ProjectImporter

    @Inject
    lateinit var versionInformation: VersionInformation

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    private lateinit var binding: FirstLaunchLayoutBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Collect_Light)

        binding = FirstLaunchLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        DaggerUtils.getComponent(this).inject(this)

        binding.configureViaQrButton.setOnClickListener {
            DialogUtils.showIfNotShowing(QrCodeProjectCreatorDialog::class.java, supportFragmentManager)
        }

        binding.configureManuallyButton.setOnClickListener {
            DialogUtils.showIfNotShowing(ManualProjectCreatorDialog::class.java, supportFragmentManager)
        }

        binding.appName.text = String.format(
            "%s %s",
            getString(R.string.app_name),
            versionInformation.versionToDisplay
        )

        binding.configureLater.setOnClickListener {
            projectImporter.importDemoProject()
            currentProjectProvider.setCurrentProject(Project.DEMO_PROJECT_ID)

            ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
        }
    }
}
