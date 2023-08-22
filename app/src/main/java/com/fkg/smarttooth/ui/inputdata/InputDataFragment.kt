package com.fkg.smarttooth.ui.inputdata

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import com.afollestad.vvalidator.util.onTextChanged
import com.google.android.material.R as AndroidR
import com.fkg.smarttooth.R
import com.fkg.smarttooth.base.BaseFragment
import com.fkg.smarttooth.data.model.Jaw
import com.fkg.smarttooth.databinding.FragmentInputDataBinding
import com.fkg.smarttooth.utils.Utils.orZero
import com.fkg.smarttooth.utils.Utils.safeParseDouble
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InputDataFragment(
    private val type: Int,
    private val id: String
) : BaseFragment<FragmentInputDataBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentInputDataBinding =
        FragmentInputDataBinding::inflate

    private var jawData: Jaw? = null
    private val tempData: MutableMap<String, Double> = mutableMapOf()
    private val viewModel: InputDataViewModel by activityViewModels()

    override fun setup() {
        binding.btnEdit.setOnClickListener { setupEditMode() }
        binding.imgDiscrepancy.setImageDrawable(getDrawable(R.drawable.arch_upper, R.drawable.arch_bottom))
        viewModel.getJawData(id, isTop()).observe(this, setJawDataObserver())
        if (!viewModel.verifyLogin()) binding.btnEdit.gone()
    }

    private fun setJawDataObserver() = setObserver<Jaw?>(
        onSuccess = {
            binding.progressBar.gone()
            jawData = it.data
            populateData()
        },
        onError = {
            binding.progressBar.gone()
            showToast(it.message.toString())
        },
        onLoading = { binding.progressBar.visible() }
    )

    private fun populateData() {
        jawData?.let {
            it.toothData.forEach { (key, value) ->
                viewModel.put(key, value)
            }
            tempData[ARCH_LENGTH] = it.archLength
            tempData[LENGTH] = it.length
            tempData[MPV] = it.mpv
            tempData[MMV] = it.mmv
        }

        binding.tableData.removeAllViews()

        Jaw.TOOTH.values().forEach {
            createDataTableRow(it.display, jawData?.toothData?.get(it).orZero())
        }
        createDataTableRow("Panjang Lengkung rahang", jawData?.archLength.orZero())
        createDataTableRow("Panjang Rahang", jawData?.length.orZero())
        createDataTableRow("Lebar 1 (anterior)", jawData?.mpv.orZero())
        createDataTableRow("Lebar 2 (posterior)", jawData?.mmv.orZero())
    }

    private fun setupEditMode() {
        binding.llData.gone()
        binding.tableInput.removeAllViews()
        Jaw.TOOTH.values().forEach { tooth ->
            createInputTableRow(
                tooth.display,
                getDrawable(tooth.imgUpper, tooth.imgBottom),
                jawData?.toothData?.get(tooth)
            ) { viewModel.put(tooth, it.safeParseDouble()) }
        }

        createInputTableRow(
            "Panjang Lengkung Rahang",
            getDrawable(R.drawable.arch_length_upper, R.drawable.arch_length_bottom),
            jawData?.archLength
        ) { tempData[ARCH_LENGTH] = it.safeParseDouble() }

        createInputTableRow(
            "Panjang Rahang",
            resources.getDrawable(R.drawable.length),
            jawData?.length
        ) { tempData[LENGTH] = it.safeParseDouble() }

        createInputTableRow(
            "Lebar 1 (anterior)",
            resources.getDrawable(R.drawable.anterior),
            jawData?.mpv
        ) { tempData[MPV] = it.safeParseDouble() }

        createInputTableRow(
            "Lebar 2 (posterior)",
            resources.getDrawable(R.drawable.posterior),
            jawData?.mmv
        ) { tempData[MMV] = it.safeParseDouble() }

        binding.llInput.visible()
        binding.btnEdit.gone()
        binding.btnBack.visible()
        binding.btnSave.visible()

        binding.btnSave.setOnClickListener { saveJawData() }
        binding.btnBack.setOnClickListener {
            reset()
            setup()
        }
    }

    private fun reset() {
        with(binding) {
            llInput.gone()
            llData.visible()
            btnBack.gone()
            btnSave.gone()
            btnEdit.visible()
            imgDiscrepancy.setImageDrawable(resources.getDrawable(R.drawable.discrepancy))
        }
    }

    private fun saveJawData() {
        viewModel.setJawData(
            id,
            isTop(),
            tempData[ARCH_LENGTH].orZero(),
            tempData[LENGTH].orZero(),
            tempData[MPV].orZero(),
            tempData[MMV].orZero()
        ).observe(this, setSaveObserver())
    }

    private fun setSaveObserver() = setObserver<Void?>(
        onSuccess = {
            binding.progressBar.gone()
            reset()
            setup()
        },
        onError = {
            binding.progressBar.gone()
            showToast(it.message.toString())
        },
        onLoading = { binding.progressBar.visible() }
    )

    private fun getDrawable(@DrawableRes upper: Int, @DrawableRes bottom: Int) =
        if (isTop())  resources.getDrawable(upper) else  resources.getDrawable(bottom)

    private fun createDataTableRow(name: String, value: Double, isHeader: Boolean = false) {
        val tableRow = TableRow(requireContext())
        val tvName = createTextView(14F, Typeface.DEFAULT)
        val tvValue = createTextView()

        tableRow.background =
            if (isHeader) resources.getDrawable(R.color.blue_white)
            else resources.getDrawable(R.color.white)
        tableRow.setPadding(12)

        tvName.text = name
        tvValue.text = value.toString()

        tableRow.addView(tvName)
        tableRow.addView(tvValue)

        binding.tableData.addView(tableRow)
    }

    private fun createInputTableRow(
        title: String,
        image: Drawable,
        value: Double? = null,
        onTextChangeListener: (String) -> Unit = {}
    ) {
        val tableRow = TableRow(requireContext())
        val textView = createTextView()
        val textField = createTextField()
        val editText = createEditText(textField.context)

        textView.text = title
        editText.setText(value.orZero().toString())
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) return@setOnFocusChangeListener

            binding.imgDiscrepancy.setImageDrawable(image)
            if (editText.text.toString().safeParseDouble() == 0.0) {
                editText.setText("")
            }
        }
        editText.onTextChanged {
            onTextChangeListener.invoke(it)
        }

        tableRow.addView(textView)
        textField.addView(editText)
        tableRow.addView(textField)

        binding.tableInput.addView(tableRow)
    }

    private fun createTextView(size: Float = 16F, style: Typeface = Typeface.DEFAULT_BOLD) =
        TextView(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            gravity = Gravity.CENTER_VERTICAL
            textSize = size
            typeface = style
        }

    private fun createTextField() = TextInputLayout(ContextThemeWrapper(requireContext(), AndroidR.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox_Dense)).apply {
        isHintEnabled = false
        layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
            setMargins(0, 12, 0, 12)
        }
        setBoxCornerRadii(24F, 24F, 24F, 24F)
        boxBackgroundColor = resources.getColor(R.color.white)
        boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
    }

    private fun createEditText(ctx: Context) = TextInputEditText(ctx).apply {
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

    private fun isTop() = (type == R.string.tab_atas)

    companion object {
        const val ARCH_LENGTH = "archLength"
        const val LENGTH = "length"
        const val MPV = "mpv"
        const val MMV = "mmv"
    }
}