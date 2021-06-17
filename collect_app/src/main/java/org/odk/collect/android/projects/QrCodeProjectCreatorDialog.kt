package org.odk.collect.android.projects

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.google.zxing.client.android.BeepManager
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.activities.MainMenuActivity
import org.odk.collect.android.databinding.QrCodeProjectCreatorDialogLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.listeners.PermissionListener
import org.odk.collect.android.permissions.PermissionsProvider
import org.odk.collect.android.utilities.CodeCaptureManagerFactory
import org.odk.collect.android.utilities.CompressionUtils
import org.odk.collect.android.utilities.DialogUtils
import org.odk.collect.android.utilities.ToastUtils
import org.odk.collect.android.views.BarcodeViewDecoder
import org.odk.collect.material.MaterialFullScreenDialogFragment
import org.odk.collect.projects.R
import javax.inject.Inject

class QrCodeProjectCreatorDialog : MaterialFullScreenDialogFragment() {

    @Inject
    lateinit var codeCaptureManagerFactory: CodeCaptureManagerFactory

    @Inject
    lateinit var barcodeViewDecoder: BarcodeViewDecoder

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    @Inject
    lateinit var projectCreator: ProjectCreator

    private var capture: CaptureManager? = null

    private lateinit var beepManager: BeepManager
    lateinit var binding: QrCodeProjectCreatorDialogLayoutBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = QrCodeProjectCreatorDialogLayoutBinding.inflate(inflater)
        setUpToolbar()
        binding.configureManuallyButton.setOnClickListener {
            DialogUtils.showIfNotShowing(ManualProjectCreatorDialog::class.java, requireActivity().supportFragmentManager)
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        beepManager = BeepManager(requireActivity())
        permissionsProvider.requestCameraPermission(
            requireActivity(),
            object : PermissionListener {
                override fun granted() {
                    startScanning(savedInstanceState)
                }

                override fun denied() {
                    dismiss()
                }
            }
        )
        return binding.root
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        capture?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeView.pauseAndWait()
        capture?.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.barcodeView.resume()
        capture?.onResume()
    }

    override fun onDestroy() {
        capture?.onDestroy()
        super.onDestroy()
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    private fun setUpToolbar() {
        toolbar.setTitle(R.string.add_project)
        toolbar.navigationIcon = null
    }

    private fun startScanning(savedInstanceState: Bundle?) {
        capture = codeCaptureManagerFactory.getCaptureManager(requireActivity(), binding.barcodeView, savedInstanceState, listOf(IntentIntegrator.QR_CODE))

        barcodeViewDecoder.waitForBarcode(binding.barcodeView).observe(
            viewLifecycleOwner,
            { barcodeResult: BarcodeResult ->
                try {
                    beepManager.playBeepSoundAndVibrate()
                } catch (e: Exception) {
                    // ignore
                }

                try {
                    handleScanningResult(barcodeResult)
                } catch (e: Exception) {
                    ToastUtils.showShortToast(getString(R.string.invalid_qrcode))
                }
            }
        )
    }

    private fun handleScanningResult(result: BarcodeResult) {
        val projectCreatedSuccessfully = projectCreator.createNewProject(CompressionUtils.decompress(result.text))

        if (projectCreatedSuccessfully) {
            ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
            ToastUtils.showLongToast(getString(R.string.new_project_created))
        } else {
            ToastUtils.showLongToast(getString(R.string.invalid_qrcode))
        }
    }
}
