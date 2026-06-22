package studio.phillip.yolo.presentation.manage.timekeeping.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.model.SelectItem
import com.crayon.fieldapp.databinding.DialogSelectProjectBinding
import com.crayon.fieldapp.ui.screen.monitor.attendance.adapter.SelectProjectRVAdapter
import com.crayon.fieldapp.utils.setSingleClick

class SelectProjectDialog(val itemClickListener: (SelectItem) -> Unit = {}) : DialogFragment() {
    lateinit var adapter: SelectProjectRVAdapter
    lateinit var mLayoutManager: RecyclerView.LayoutManager
    private var _binding: DialogSelectProjectBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ánh xạ binding
        _binding = DialogSelectProjectBinding.inflate(inflater, container, false)
        return binding.root.apply {
            binding.btnChoose.setSingleClick {
                Log.d("AAA", "btn_choose")
                val agency = adapter.getSelectedItem()
                agency?.let {
                    itemClickListener(it)
                }
                this@SelectProjectDialog.dismiss()
            }

            this@SelectProjectDialog.arguments?.let {
                val items = it.getSerializable("projects") as ArrayList<SelectItem>
                this@SelectProjectDialog.activity?.let { activity ->
                    adapter = SelectProjectRVAdapter(items, activity, {
                        binding.btnChoose.setBackgroundColor(
                            resources.getColor(R.color.colorAccent, null)
                        )
                        binding.btnChoose.isEnabled = true
                    })
                    mLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
                    binding.rvProject.layoutManager = mLayoutManager
                    binding.rvProject.adapter = adapter
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(false)
            val window = dialog.window
            val wlp = window!!.attributes
            wlp.gravity = Gravity.CENTER
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            window.attributes = wlp
            window.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}