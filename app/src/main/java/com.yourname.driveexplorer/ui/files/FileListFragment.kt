package com.yourname.driveexplorer.ui.files

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourname.driveexplorer.R
import com.yourname.driveexplorer.data.model.DriveFile
import com.yourname.driveexplorer.databinding.FragmentFileListBinding
import com.yourname.driveexplorer.ui.actions.FileActionsBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FileListFragment : Fragment(), FileAdapter.FileClickListener {

    private var _binding: FragmentFileListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FileListViewModel by viewModels()
    private val args: FileListFragmentArgs by navArgs()
    private lateinit var fileAdapter: FileAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileListBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        viewModel.loadFiles(args.folderId ?: "root")
    }

    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = fileAdapter
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
                        else -> showFiles(state.files)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.errorText.visibility = View.GONE
    }

    private fun showFiles(files: List<DriveFile>) {
        binding.progressBar.visibility = View.GONE
        binding.errorText.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        fileAdapter.submitList(files)
    }

    private fun showError(error: Throwable) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.errorText.visibility = View.VISIBLE
        binding.errorText.text = error.localizedMessage
    }

    override fun onFileClick(file: DriveFile) {
        if (file.isFolder) {
            navigateToFolder(file.id)
        } else {
            showFileActions(file)
        }
    }

    private fun navigateToFolder(folderId: String) {
        val action = FileListFragmentDirections.actionFileListSelf(folderId)
        findNavController().navigate(action)
    }

    private fun showFileActions(file: DriveFile) {
        FileActionsBottomSheet.newInstance(file).apply {
            setActionListener(object : FileActionsBottomSheet.ActionListener {
                override fun onCopyRequested(fileId: String) {
                    viewModel.copyFile(fileId)
                }
                override fun onMoveRequested(fileId: String) {
                    viewModel.moveFile(fileId)
                }
            })
        }.show(parentFragmentManager, "file_actions")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_file_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(folderId: String? = null) = FileListFragment().apply {
            arguments = Bundle().apply {
                putString("folderId", folderId)
            }
        }
    }
}
