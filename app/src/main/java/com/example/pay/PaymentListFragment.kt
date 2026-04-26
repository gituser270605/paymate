package com.example.pay

import android.os.Bundle
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.pay.databinding.FragmentPaymentListBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PaymentListFragment : Fragment() {

    private var _binding: FragmentPaymentListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PaymentViewModel by activityViewModels()
    
    private val adapter = PaymentAdapter(
        onEdit = { payment ->
            val bundle = Bundle().apply {
                putLong("paymentId", payment.id)
            }
            findNavController().navigate(R.id.action_paymentListFragment_to_addPaymentFragment, bundle)
        },
        onDelete = { payment ->
            viewModel.deletePayment(payment.id)
        }
    )
    
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.adapter = adapter
        setupCategoryChips()

        binding.etSearch.addTextChangedListener { text ->
            viewModel.updateSearchQuery(text?.toString() ?: "")
        }

        binding.fabAddPayment.setOnClickListener {
            findNavController().navigate(R.id.action_paymentListFragment_to_addPaymentFragment)
        }

        binding.btnSetBudget.setOnClickListener {
            showSetBudgetDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.payments.collect { payments ->
                        adapter.submitList(payments)
                        binding.tvEmptyState.visibility = if (payments.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                
                launch {
                    combine(viewModel.totalSpend, viewModel.budget) { total, budget ->
                        total to budget
                    }.collect { (total, budget) ->
                        updateSummary(total, budget)
                    }
                }
            }
        }
    }

    private fun setupCategoryChips() {
        Category.values().forEach { category ->
            val chip = Chip(ContextThemeWrapper(requireContext(), com.google.android.material.R.style.Widget_Material3_Chip_Filter)).apply {
                text = category.displayName
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        viewModel.setSelectedCategory(category)
                    } else if (binding.chipGroupCategories.checkedChipId == View.NO_ID) {
                        viewModel.setSelectedCategory(null)
                    }
                }
            }
            binding.chipGroupCategories.addView(chip)
        }

        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setSelectedCategory(null)
        }
    }

    private fun updateSummary(total: Double, budget: Double) {
        binding.tvTotalSpend.text = currencyFormatter.format(total)
        
        if (budget > 0) {
            binding.tvBudgetSummary.text = getString(
                R.string.spent_of_format,
                currencyFormatter.format(total),
                currencyFormatter.format(budget)
            )
            binding.budgetProgress.visibility = View.VISIBLE
            val progress = ((total / budget) * 100).toInt().coerceIn(0, 100)
            binding.budgetProgress.setProgress(progress, true)
            
            if (total > budget) {
                binding.budgetProgress.setIndicatorColor(requireContext().getColor(R.color.error))
            } else {
                binding.budgetProgress.setIndicatorColor(requireContext().getColor(R.color.primary))
            }
        } else {
            binding.tvBudgetSummary.text = getString(R.string.budget_not_set)
            binding.budgetProgress.visibility = View.GONE
        }
    }

    private fun showSetBudgetDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.set_budget)

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setText(viewModel.budget.value.toString())
        builder.setView(input)

        builder.setPositiveButton(R.string.save) { _, _ ->
            val amount = input.text.toString().toDoubleOrNull() ?: 0.0
            viewModel.setBudget(amount)
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
