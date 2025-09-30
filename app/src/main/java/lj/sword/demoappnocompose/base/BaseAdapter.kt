package lj.sword.demoappnocompose.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * RecyclerView 通用适配器基类
 * 支持 ViewBinding、DiffUtil、点击事件
 * 
 * @param T 数据类型
 * @param VB ViewBinding 类型
 * 
 * @author Sword
 * @since 1.0.0
 */
abstract class BaseAdapter<T, VB : ViewBinding> : RecyclerView.Adapter<BaseAdapter.BaseViewHolder<VB>>() {

    /**
     * 数据列表
     */
    protected val items = mutableListOf<T>()

    /**
     * Item 点击监听器
     */
    private var onItemClickListener: ((T, Int) -> Unit)? = null

    /**
     * Item 长按监听器
     */
    private var onItemLongClickListener: ((T, Int) -> Boolean)? = null

    /**
     * 创建 ViewBinding（子类实现）
     */
    protected abstract fun createBinding(parent: ViewGroup): VB

    /**
     * 绑定数据（子类实现）
     */
    protected abstract fun bind(binding: VB, item: T, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val binding = createBinding(parent)
        val holder = BaseViewHolder(binding)

        // 设置点击事件
        binding.root.setOnClickListener {
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemClickListener?.invoke(items[position], position)
            }
        }

        // 设置长按事件
        binding.root.setOnLongClickListener {
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemLongClickListener?.invoke(items[position], position) ?: false
            } else {
                false
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        bind(holder.binding, items[position], position)
    }

    override fun getItemCount(): Int = items.size

    /**
     * 设置数据列表（使用 DiffUtil）
     */
    fun setItems(newItems: List<T>, areItemsTheSame: (T, T) -> Boolean) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = items.size
            override fun getNewListSize(): Int = newItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return areItemsTheSame(items[oldItemPosition], newItems[newItemPosition])
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition] == newItems[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items.clear()
        items.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * 设置数据列表（不使用 DiffUtil）
     */
    fun setItems(newItems: List<T>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    /**
     * 添加数据
     */
    fun addItem(item: T) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    /**
     * 添加数据列表
     */
    fun addItems(newItems: List<T>) {
        val oldSize = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(oldSize, newItems.size)
    }

    /**
     * 移除数据
     */
    fun removeItem(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * 清空数据
     */
    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }

    /**
     * 获取指定位置的数据
     */
    fun getItem(position: Int): T? {
        return if (position in items.indices) items[position] else null
    }

    /**
     * 设置 Item 点击监听器
     */
    fun setOnItemClickListener(listener: (T, Int) -> Unit) {
        onItemClickListener = listener
    }

    /**
     * 设置 Item 长按监听器
     */
    fun setOnItemLongClickListener(listener: (T, Int) -> Boolean) {
        onItemLongClickListener = listener
    }

    /**
     * ViewHolder 基类
     */
    class BaseViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)
}
