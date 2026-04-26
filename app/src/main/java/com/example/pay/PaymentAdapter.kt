package com.example.pay

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pay.databinding.ItemPaymentBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class PaymentAdapter(
    private val onEdit: (Payment) -> Unit,
    private val onDelete: (Payment) -> Unit
) : ListAdapter<Payment, PaymentAdapter.PaymentViewHolder>(PaymentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentViewHolder(binding, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PaymentViewHolder(
        private val binding: ItemPaymentBinding,
        private val onEdit: (Payment) -> Unit,
        private val onDelete: (Payment) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        fun bind(payment: Payment) {
            binding.tvDescription.text = payment.description
            binding.tvAmount.text = currencyFormatter.format(payment.amount)
            binding.tvDate.text = dateFormatter.format(payment.date)
            binding.chipCategory.text = payment.category.displayName

            binding.btnEdit.setOnClickListener { onEdit(payment) }
            binding.btnDelete.setOnClickListener { onDelete(payment) }
        }
    }

    class PaymentDiffCallback : DiffUtil.ItemCallback<Payment>() {
        override fun areItemsTheSame(oldItem: Payment, newItem: Payment): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Payment, newItem: Payment): Boolean = oldItem == newItem
    }
}
