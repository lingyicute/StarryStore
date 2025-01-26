package com.looker.starrystore.ui.favourites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.looker.starrystore.databinding.ProductItemBinding
import com.looker.starrystore.model.Product
import com.looker.starrystore.model.Repository
import com.looker.starrystore.utility.common.extension.authentication
import com.looker.starrystore.utility.common.extension.corneredBackground
import com.looker.starrystore.utility.common.extension.dp
import com.looker.starrystore.utility.common.extension.getColorFromAttr
import com.looker.starrystore.utility.common.nullIfEmpty
import com.google.android.material.R as MaterialR

class FavouriteFragmentAdapter(
    private val onProductClick: (String) -> Unit
) : RecyclerView.Adapter<FavouriteFragmentAdapter.ViewHolder>() {

    inner class ViewHolder(binding: ProductItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val icon = binding.icon
        val name = binding.name
        val summary = binding.summary
        val version = binding.status
    }

    var apps: List<List<Product>> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var repositories: Map<Long, Repository> = emptyMap()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ProductItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ).apply {
            itemView.setOnClickListener {
                if (apps.isNotEmpty() && apps[absoluteAdapterPosition].firstOrNull() != null) {
                    onProductClick(apps[absoluteAdapterPosition].first().packageName)
                }
            }
        }

    override fun getItemCount(): Int = apps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = apps[position].first().item()
        val repository: Repository? = repositories[item.repositoryId]
        holder.name.text = item.name
        holder.summary.isVisible = item.summary.isNotEmpty()
        holder.summary.text = item.summary
        if (repository != null) {
            val iconUrl = item.icon(holder.icon, repository)
            holder.icon.load(iconUrl) {
                authentication(repository.authentication)
            }
        }
        holder.version.apply {
            text = item.installedVersion.nullIfEmpty() ?: item.version
            val isInstalled = item.installedVersion.nullIfEmpty() != null
            when {
                item.canUpdate -> {
                    backgroundTintList =
                        context.getColorFromAttr(MaterialR.attr.colorSecondaryContainer)
                    setTextColor(context.getColorFromAttr(MaterialR.attr.colorOnSecondaryContainer))
                }

                isInstalled -> {
                    backgroundTintList =
                        context.getColorFromAttr(MaterialR.attr.colorPrimaryContainer)
                    setTextColor(context.getColorFromAttr(MaterialR.attr.colorOnPrimaryContainer))
                }

                else -> {
                    setPadding(0, 0, 0, 0)
                    setTextColor(context.getColorFromAttr(MaterialR.attr.colorOnBackground))
                    background = null
                    return@apply
                }
            }
            background = context.corneredBackground
            6.dp.let { setPadding(it, it, it, it) }
        }
    }
}
