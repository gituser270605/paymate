package com.example.pay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pay.databinding.FragmentAddPaymentBinding

class AddPaymentFragment : Fragment() {

    private var _binding: FragmentAddPaymentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PaymentViewModel by activityViewModels()
    private var paymentId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paymentId = arguments?.getLong("paymentId") ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = Category.values().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)

        if (paymentId != -1L) {
            val payment = viewModel.getPaymentById(paymentId)
            payment?.let {
                binding.etDescription.setText(it.description)
                binding.etAmount.setText(it.amount.toString())
                binding.actvCategory.setText(it.category.displayName, false)
                binding.btnSave.text = getString(R.string.save)
            }
        } else {
            binding.actvCategory.setText(Category.OTHER.displayName, false)
        }

        binding.btnSave.setOnClickListener {
            val description = binding.etDescription.text.toString()
            val amountStr = binding.etAmount.text.toString()
            val categoryStr = binding.actvCategory.text.toString()
            val category = Category.fromDisplayName(categoryStr) ?: Category.OTHER

            if (description.isBlank() || amountStr.isBlank()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (paymentId == -1L) {
                viewModel.addPayment(description, amount, category)
            } else {
                viewModel.updatePayment(paymentId, description, amount, category)
            }
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
