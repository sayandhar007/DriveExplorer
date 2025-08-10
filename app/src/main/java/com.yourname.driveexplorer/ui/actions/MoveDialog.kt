package com.yourname.driveexplorer.ui.actions

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourname.driveexplorer.R
import com.yourname.driveexplorer.data.model.DriveFile
import com.yourname.driveexplorer.databinding.DialogMoveFileBinding
import com.yourname.driveexplorer.ui.files.FileViewModel
import com.yourname.driveexplorer.util.FolderAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MoveDialog : DialogFragment(), FolderAdapter.FolderClickListener {

    private var _binding: DialogMoveFileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FileViewModel by activityViewModels()
    private lateinit var folderAdapter: FolderAdapter

    private var fileToMoveId: String? = null
    private var currentFolderId: String = "root"

    interface MoveDialogListener {
        fun onMoveConfirmed(fileId: String, targetFolderId: String)
    }

    companion object {
        private const val ARG_FILE_ID = "file_id"
        private const val ARG_CURRENT_FOLDER = "current_folder"

        fun newInstance(fileId: String, currentFolderId: String): MoveDialog {
            return MoveDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_FILE_ID, fileId)
                    putString(ARG_CURRENT_FOLDER, currentFolderId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
        arguments?.let {
            fileToMoveId = it.getString(ARG_FILE_ID)
            currentFolderId = it.getString(ARG_CURRENT_FOLDER) ?: "root"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMoveFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        loadFolders()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        binding.toolbar.title = getString(R.string.move_to_title)
        binding.btnConfirm.setOnClickListener { confirmMove() }

        folderAdapter = FolderAdapter(this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = folderAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    when {
                        state.isLoading -> showLoading()
                        state.error != null -> showError(state.error)
                        else -> showFolders(
                            state.files.filter { it.isFolder && it.id != fileToMoveId }
                        )
                    }
                }
            }
        }
    }

    private fun loadFolders() {
        viewModel.loadFiles(currentFolderId)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun showFolders(folders: List<DriveFile>) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        folderAdapter.submitList(folders)
    }

    private fun showError(error: String) {
        binding.progressBar.visibility = View.GONE
        binding.errorText.apply {
            visibility = View.VISIBLE
            text = error
        }
    }

    private fun confirmMove() {
        fileToMoveId?.let { fileId ->
            (parentFragment as? MoveDialogListener)?.onMoveConfirmed(fileId, currentFolderId)
            dismiss()
        }
    }

    override fun onFolderClick(folder: DriveFile) {
        currentFolderId = folder.id
        loadFolders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setWindowAnimations(R.style.DialogAnimation)
        }
    }
}
