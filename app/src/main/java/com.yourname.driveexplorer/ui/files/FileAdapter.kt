class FileAdapter(
    private val onClick: (DriveFile) -> Unit
) : ListAdapter<DriveFile, FileAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(val binding: ItemFileBinding) : 
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = getItem(position)
        holder.binding.apply {
            fileName.text = file.name
            itemView.setOnClickListener { onClick(file) }
        }
    }
}
