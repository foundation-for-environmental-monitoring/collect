package org.odk.collect.android.preferences.screens

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import org.odk.collect.android.R
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.utilities.DialogUtils
import org.odk.collect.android.utilities.MultiClickGuard
import org.odk.collect.androidshared.ColorPickerDialog
import org.odk.collect.androidshared.ColorPickerViewModel
import org.odk.collect.androidshared.ui.OneSignTextWatcher
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import javax.inject.Inject

class ProjectDisplayPreferencesFragment :
    BaseAdminPreferencesFragment(), Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
        val colorPickerViewModel = ViewModelProvider(requireActivity()).get(
            ColorPickerViewModel::class.java
        )
        colorPickerViewModel.pickedColor.observe(
            this,
            { color: String ->
                val (uuid, name, icon) = currentProjectProvider.getCurrentProject()
                projectsRepository.save(Project.Saved(uuid, name, icon, color))
                findPreference<Preference>(PROJECT_COLOR_KEY)!!.summaryProvider =
                    ProjectDetailsSummaryProvider(
                        PROJECT_COLOR_KEY, currentProjectProvider
                    )
            }
        )
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.project_display_preferences, rootKey)
        findPreference<Preference>(PROJECT_COLOR_KEY)!!.onPreferenceClickListener = this

        findPreference<Preference>(PROJECT_NAME_KEY)!!.summaryProvider =
            ProjectDetailsSummaryProvider(
                PROJECT_NAME_KEY, currentProjectProvider
            )
        findPreference<Preference>(PROJECT_ICON_KEY)!!.summaryProvider =
            ProjectDetailsSummaryProvider(
                PROJECT_ICON_KEY, currentProjectProvider
            )
        findPreference<Preference>(PROJECT_COLOR_KEY)!!.summaryProvider =
            ProjectDetailsSummaryProvider(
                PROJECT_COLOR_KEY, currentProjectProvider
            )
        findPreference<Preference>(PROJECT_NAME_KEY)!!.onPreferenceChangeListener = this
        findPreference<Preference>(PROJECT_ICON_KEY)!!.onPreferenceChangeListener = this
        (findPreference<Preference>(PROJECT_NAME_KEY) as EditTextPreference).text =
            currentProjectProvider.getCurrentProject().name
        (findPreference<Preference>(PROJECT_ICON_KEY) as EditTextPreference).text =
            currentProjectProvider.getCurrentProject().icon
        (findPreference<Preference>(PROJECT_ICON_KEY) as EditTextPreference).setOnBindEditTextListener { editText: EditText ->
            editText.addTextChangedListener(
                OneSignTextWatcher(editText)
            )
        }
    }

    private class ProjectDetailsSummaryProvider(
        private val key: String,
        private val currentProjectProvider: CurrentProjectProvider
    ) : Preference.SummaryProvider<Preference> {
        override fun provideSummary(preference: Preference): CharSequence {
            return when (key) {
                PROJECT_NAME_KEY -> currentProjectProvider.getCurrentProject().name
                PROJECT_ICON_KEY -> currentProjectProvider.getCurrentProject().icon
                PROJECT_COLOR_KEY -> {
                    val summary: Spannable = SpannableString("■")
                    summary.setSpan(
                        ForegroundColorSpan(
                            Color.parseColor(
                                currentProjectProvider.getCurrentProject().color
                            )
                        ),
                        0, summary.length, 0
                    )
                    summary
                }
                else -> ""
            }
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (MultiClickGuard.allowClick(javaClass.name)) {
            when (preference.key) {
                PROJECT_COLOR_KEY -> {
                    val (_, _, icon, color) = currentProjectProvider.getCurrentProject()
                    val bundle = Bundle()
                    bundle.putString(ColorPickerDialog.CURRENT_COLOR, color)
                    bundle.putString(ColorPickerDialog.CURRENT_ICON, icon)
                    DialogUtils.showIfNotShowing(
                        ColorPickerDialog::class.java,
                        bundle,
                        requireActivity().supportFragmentManager
                    )
                }
            }
            return true
        }
        return false
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val (uuid, name, icon, color) = currentProjectProvider.getCurrentProject()
        when (preference.key) {
            PROJECT_NAME_KEY -> projectsRepository.save(
                Project.Saved(
                    uuid, newValue.toString(), icon, color
                )
            )
            PROJECT_ICON_KEY -> projectsRepository.save(
                Project.Saved(
                    uuid, name, newValue.toString(), color
                )
            )
        }
        return true
    }

    companion object {
        const val PROJECT_NAME_KEY = "project_name"
        const val PROJECT_ICON_KEY = "project_icon"
        const val PROJECT_COLOR_KEY = "project_color"
    }
}
