package com.yourname.driveexplorer.ui.actions

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yourname.driveexplorer.R
import com.yourname.driveexplorer.databinding.DialogShareFileBinding
import com.yourname.driveexplorer.ui.files.FileViewModel
import com.yourname.driveexplorer.util.ShareRole
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareDialog : DialogFragment() {

    private var _binding: DialogShareFileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FileViewModel by activityViewModels()

    private var fileId: String? = null
    private var fileName: String? = null

    interface ShareDialogListener {
        fun onShareConfirmed(fileId: String, email: String, role: ShareRole, message: String)
    }

    companion object {
        private const val ARG_FILE_ID = "file_id"
        private const val ARG_FILE_NAME = "file_name"

        fun newInstance(fileId: String, fileName: String): ShareDialog {
            return ShareDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_FILE_ID, fileId)
                    putString(ARG_FILE_NAME, fileName)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            fileId = it.getString(ARG_FILE_ID)
            fileName = it.getString(ARG_FILE_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogShareFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding.toolbar.title = getString(R.string.share_dialog_title, fileName)
        binding.toolbar.setNavigationOnClickListener { dismiss() }

        binding.roleSelector.setOnCheckedChangeListener { _, checkedId ->
            binding.messageInput.visibility = when (checkedId) {
                R.id.role_editor -> View.VISIBLE
                else -> View.GONE
            }
        }

        binding.btnShare.setOnClickListener {
            validateAndShare()
        }
    }

    private fun validateAndShare() {
        val email = binding.emailInput.text.toString().trim()
        val message = binding.messageInput.text.toString().trim()
        val role = when (binding.roleSelector.checkedRadioButtonId) {
            R.id.role_viewer -> ShareRole.VIEWER
            R.id.role_commenter -> ShareRole.COMMENTER
            else -> ShareRole.EDITOR
        }

        when {
            email.isEmpty() -> showError(getString(R.string.error_email_required))
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                showError(getString(R.string.error_invalid_email))
            role == ShareRole.EDITOR && message.isEmpty() -> 
                showError(getString(R.string.error_message_required_for_editor))
            else -> confirmShare(email, role, message)
        }
    }

    private fun confirmShare(email: String, role: ShareRole, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm_share_title))
            .setMessage(getString(R.string.confirm_share_message, fileName, email, role.displayName))
            .setPositiveButton(R.string.share) { _, _ ->
                fileId?.let { id ->
                    (parentFragment as? ShareDialogListener)?.onShareConfirmed(
                        id, email, role, message
                    )
                    dismiss()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showError(message: String) {
        binding.emailInput.error = message
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setWindowAnimations(R.style.DialogAnimation)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
