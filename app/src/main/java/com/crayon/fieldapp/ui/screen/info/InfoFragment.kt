package com.crayon.fieldapp.ui.screen.info

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.model.Gender
import com.crayon.fieldapp.databinding.FragmentInfoBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.CityAdapter
import com.crayon.fieldapp.ui.base.adapter.GenderSPAdapter
import com.crayon.fieldapp.ui.base.dialog.DatePickerSpinnerDialog
import com.crayon.fieldapp.ui.base.dialog.getPhoto.GetPhotoDialogFragment
import com.crayon.fieldapp.ui.screen.info.model.UpdateInfoForm
import com.crayon.fieldapp.utils.BitmapUtils
import com.crayon.fieldapp.utils.CityUtils
import com.crayon.fieldapp.utils.FileManager
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.loadImage
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showMessageDialog
import com.crayon.fieldapp.utils.toDate
import com.crayon.fieldapp.utils.toTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InfoFragment : BaseFragment<FragmentInfoBinding, InfoViewModel>(),
    DatePickerSpinnerDialog.DatePickerDialogListener,
    GetPhotoDialogFragment.GetPhotoDialogListener {

    override val layoutId: Int = R.layout.fragment_info

    override val viewModel: InfoViewModel by viewModel()
    private lateinit var cityAdapter: CityAdapter
    private lateinit var currentCityAdapter: CityAdapter
    private lateinit var districtAdapter: CityAdapter
    private lateinit var currentDistrictAdapter: CityAdapter
    private lateinit var genderAdapter: GenderSPAdapter
    private var cityCode: String? = null
    private var current_cityCode: String? = null
    private var districtCode: String? = null
    private var current_districtCode: String? = null
    private var tyeImage: String? = null
    private var idFrontRef: Uri? = null
    private var idBackRef: Uri? = null
    var calendar = Calendar.getInstance()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.apply {
            getUserInfo()
        }

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }
        binding.spCity.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
            }
        }

        binding.spCurrentCity.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
            }
        }

        binding.spDistrict.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
            }
        }

        binding.spCurrentDistrict.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cityAdapter = CityAdapter(requireContext(), arrayListOf())
        currentCityAdapter = CityAdapter(requireContext(), arrayListOf())
        districtAdapter = CityAdapter(requireContext(), arrayListOf())
        currentDistrictAdapter = CityAdapter(requireContext(), arrayListOf())
        genderAdapter = GenderSPAdapter(Gender.values(), this@InfoFragment.requireContext())

        binding.spGender.isEnabled = false
        binding.spGender.isClickable = false
        binding.spGender.adapter = genderAdapter
        // City
        val cities = CityUtils.getAllCity(requireContext())
        cityAdapter.setData(cities)
        binding.spCity.setAdapter(cityAdapter)

        val current_cities = CityUtils.getAllCity(requireContext())
        currentCityAdapter.setData(current_cities)
        binding.spCurrentCity.setAdapter(currentCityAdapter)

        binding.spCity.isEnabled = false
        binding.spCity.isClickable = false
        binding.spCity.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                cityCode = cityAdapter.getItem(position).id
                val districts = CityUtils.getAllDistrictOfCity(requireContext(), cityCode!!)
                districtAdapter.setData(districts)
                binding.spDistrict.setAdapter(districtAdapter)
            }
        }

        binding.spCurrentCity.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                current_cityCode = currentCityAdapter.getItem(position).id
                val current_districts =
                    CityUtils.getAllDistrictOfCity(requireContext(), current_cityCode!!)
                currentDistrictAdapter.setData(current_districts)
                binding.spCurrentDistrict.setAdapter(currentDistrictAdapter)
            }
        }

        binding.spDistrict.isEnabled = false
        binding.spDistrict.isClickable = false
        binding.spDistrict.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                if (districtAdapter.items.size != 0) {
                    districtCode = districtAdapter.getItem(position).id
                }
            }
        }

        binding.spCurrentDistrict.isEnabled = true
        binding.spCurrentDistrict.isClickable = true
        binding.spCurrentDistrict.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                if (currentDistrictAdapter.items.size != 0) {
                    current_districtCode = currentDistrictAdapter.getItem(position).id
                }
            }
        }

        binding.imgIdFront.isEnabled = false
        binding.imgIdFront.isClickable = false
        binding.imgIdFront.setSingleClick {
            tyeImage = "id_front"
            selectFromCamera()
        }

        binding.imgIdBack.isEnabled = false
        binding.imgIdBack.isClickable = false
        binding.imgIdBack.setSingleClick {
            tyeImage = "id_back"
            selectFromCamera()
        }

        binding.txtBirthDay.setSingleClick {
            val datepicker = DatePickerSpinnerDialog()
            datepicker.setListener(this)
            datepicker.show(childFragmentManager, datepicker.getTag())
        }

        binding.imbIcFilter.setSingleClick {
            val firstName = binding.edtFirstName.text.toString().trim()
            val lastName = binding.edtLastName.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val gender = genderAdapter.getItem(binding.spGender.selectedItemPosition).value
            val birthDate = calendar.time.toTimeString("yyyy-MM-dd").toString()
            val phone = binding.edtPhone.text.toString().trim()
            val id = binding.edtId.text.toString().trim()
            val height = binding.edtHeight.text.toString().trim()
            val weight = binding.edtWeight.text.toString().trim()
            val city = cityAdapter.getItem(binding.spCity.selectedItemPosition).id
            val current_city =
                currentCityAdapter.getItem(binding.spCurrentCity.selectedItemPosition).id
            val district = districtAdapter.getItem(binding.spDistrict.selectedItemPosition).id
            val current_district =
                currentDistrictAdapter.getItem(binding.spCurrentDistrict.selectedItemPosition).id
            val address = binding.edtAddress.text.toString()
            val current_address = binding.edtCurrentAddress.text.toString()

            val userForm = UpdateInfoForm(
                firstName = firstName,
                lastName = lastName,
                birthDay = birthDate,
                email = email,
                phone = phone,
                gender = gender,
                heigth = height,
                weigth = weight,
                id = id,
                city = city.toString(),
                district = district.toString(),
                address = address,
                current_address = current_address,
                current_city = current_city.toString(),
                current_district = current_district.toString()
            )

            userForm.validate().also { result ->
                if (result.first) {
                    viewModel.updateInfo(userForm)
                } else {
                    handleErrorMessage(result.second)
                }
            }
        }

        viewModel.apply {
            user.observe(viewLifecycleOwner, Observer { userInfo ->
                val cityCode = userInfo.profile!!.provinceCode ?: "5"
                val currentCityCode = userInfo.profile!!.temporary_province_code ?: "5"
                val currentDistrictCode = userInfo.profile!!.temporary_district_code ?: "136"
                val districtCode = userInfo.profile!!.districtCode ?: "136"
                calendar.time = userInfo.profile!!.birthDate.toString().toDate("yyyy-MM-dd'T'HH:mm")

                var current_city = cityAdapter.getPositionByCode(cityCode.toString())
                if (current_city != -1) {
                    binding.spCity.setSelection(current_city)
                }
                val districts = CityUtils.getAllDistrictOfCity(requireContext(), cityCode!!)
                districtAdapter.setData(districts)
                binding.spDistrict.setAdapter(districtAdapter)
                var current_dist = districtAdapter.getPositionByCode(districtCode.toString())
                if (current_dist != -1) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        delay(1000)
                        binding.spDistrict.setSelection(current_dist)
                    }
                }


                var temp_city = currentCityAdapter.getPositionByCode(currentCityCode.toString())
                if (temp_city != -1) {
                    binding.spCurrentCity.setSelection(temp_city)
                }
                val temp_districts =
                    CityUtils.getAllDistrictOfCity(requireContext(), currentCityCode!!)
                currentDistrictAdapter.setData(temp_districts)
                binding.spCurrentDistrict.setAdapter(currentDistrictAdapter)
                var temp_dist =
                    currentDistrictAdapter.getPositionByCode(currentDistrictCode.toString())
                if (temp_dist != -1) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        delay(1000)
                        binding.spCurrentDistrict.setSelection(temp_dist)
                    }
                }

                binding.imgIdFront.loadImage(
                    imageUrl = userInfo.profile.identificationImageFrontUrl,
                    centerCrop = true,
                    signatureKey = userInfo.updatedAt
                )

                binding.imgIdBack.loadImage(
                    imageUrl = userInfo.profile.identificationImageBackUrl,
                    centerCrop = true,
                    signatureKey = userInfo.updatedAt
                )
            })

            uploadImage.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        showLoading()
                    }

                    Status.SUCCESS -> {
                        hideLoading()
                        if (tyeImage.equals("id_front")) {
                            context?.showMessageDialog("Cập nhật CMND mặt trước thành công")
                        } else {
                            context?.showMessageDialog("Cập nhật CMND mặt sau thành công")
                        }

                    }

                    Status.ERROR -> {
                        hideLoading()
                    }
                }
            })

            updateInfo.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        showLoading()
                    }

                    Status.SUCCESS -> {
                        hideLoading()
                        requireContext().showMessageDialog(title = "Cập nhật thông tin cá nhân thành công")
                    }

                    Status.ERROR -> {
                        hideLoading()
                    }
                }
            })

        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("url")
            ?.observe(viewLifecycleOwner, Observer { result ->
                val file = File(result)
                when (tyeImage) {
                    "id_front" -> {
                        binding.imgIdFront.loadImage(
                            imageUrl = Uri.fromFile(file).path,
                            centerCrop = true
                        )

                        idFrontRef = Uri.fromFile(file)
                        idFrontRef?.let { avatar ->
                            viewModel.updateIdFront(avatar)
                        }
                    }

                    "id_back" -> {
                        binding.imgIdBack.loadImage(
                            imageUrl = Uri.fromFile(file).path,
                            centerCrop = true
                        )
                        idBackRef = Uri.fromFile(file)
                        idBackRef?.let { body ->
                            viewModel.updateIdBack(body)
                        }
                    }
                }
            })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CODE_REQUEST_GALLERY -> {
                    var selectedImageUri = data!!.data
                    val path = FileManager.getPath(requireContext(), selectedImageUri)
                    val file = BitmapUtils.createImageFileToUpload(
                        requireContext(),
                        path,
                        tyeImage.toString()
                    )
                    when (tyeImage) {
                        "id_front" -> {
                            binding.imgIdFront.loadImage(
                                imageUrl = Uri.fromFile(file).path,
                                centerCrop = true
                            )

                            idFrontRef = Uri.fromFile(file)
                            idFrontRef?.let { avatar ->
                                viewModel.updateIdFront(avatar)
                            }
                        }

                        "id_back" -> {
                            binding.imgIdBack.loadImage(
                                imageUrl = Uri.fromFile(file).path,
                                centerCrop = true
                            )
                            idBackRef = Uri.fromFile(file)
                            idBackRef?.let { body ->
                                viewModel.updateIdBack(body)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun selectFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            CODE_REQUEST_GALLERY
        )
    }

    override fun selectFromCamera() {
        val bundle = bundleOf("isTakeImage" to true)
        findNavController().navigate(R.id.action_global_CameraFragment, bundle)
    }

    companion object {
        const val CODE_REQUEST_GALLERY = 123
    }

    override fun getDate(date: Int, month: Int, year: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, date)
        updateBirthDay()
    }

    private fun updateBirthDay() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.txtBirthDay.text = sdf.format(calendar.time).toString()
    }
}