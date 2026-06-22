package com.crayon.fieldapp.ui.screen.register

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.request.RequestOptions
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.model.Gender
import com.crayon.fieldapp.data.model.SimpleModel
import com.crayon.fieldapp.data.remote.request.UpdateProfileForm
import com.crayon.fieldapp.databinding.FragmentRegisterBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.CityAdapter
import com.crayon.fieldapp.ui.base.adapter.GenderSPAdapter
import com.crayon.fieldapp.ui.base.adapter.SimpleSPAdapter
import com.crayon.fieldapp.ui.base.dialog.DatePickerSpinnerDialog
import com.crayon.fieldapp.ui.base.dialog.getPhoto.GetPhotoDialogFragment
import com.crayon.fieldapp.utils.CityUtils
import com.crayon.fieldapp.utils.FileManager
import com.crayon.fieldapp.utils.GlideApp
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class RegisterFragment : BaseFragment<FragmentRegisterBinding, RegisterViewModel>(),
    DatePickerSpinnerDialog.DatePickerDialogListener,
    GetPhotoDialogFragment.GetPhotoDialogListener {

    override val layoutId: Int = R.layout.fragment_register

    override val viewModel: RegisterViewModel by viewModel()

    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var selectedImageUri: Uri
    private var tyeImage: String? = null
    private var cityCode: String? = null
    private var currentCityCode: String? = null
    private var districtCode: String? = null
    private var currentDistrictCode: String? = null
    lateinit var bankAdapter: SimpleSPAdapter
    lateinit var genderAdapter: GenderSPAdapter
    lateinit var cityAdapter: CityAdapter
    lateinit var currentCityAdapter: CityAdapter
    lateinit var currentDistrictAdapter: CityAdapter
    lateinit var districtAdapter: CityAdapter
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var avatarRef: String? = null
    private var bodyRef: String? = null
    private var idBackRef: String? = null
    private var idFrontRef: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    var selectedImageUri = result!!.data
                    selectedImageUri?.let {
                        val path = FileManager.getPath(requireContext(), selectedImageUri.data)
                        val file = viewModel.createFile(path, 100)
                        file?.let {
                            when (tyeImage) {
                                "avatar" -> {
                                    avatarRef = it.absolutePath
                                    avatarRef?.let {
                                        showAvatar(binding.imgAvatar, it)
                                        viewModel.setAvatarRef(it)
                                    }
                                }

                                "full_body" -> {
                                    bodyRef = it.absolutePath
                                    bodyRef?.let {
                                        showImage(binding.imgFullBody, it)
                                        viewModel.setBodyRef(it)
                                    }

                                }

                                "id_back" -> {
                                    idBackRef = it.absolutePath
                                    idBackRef?.let {
                                        showImage(binding.imgIdBack, it)
                                        viewModel.setIdBackRef(it)
                                    }
                                }
                                else -> {
                                    idFrontRef = it.absolutePath
                                    idFrontRef?.let {
                                        showImage(binding.imgIdFront, it)
                                        viewModel.setIdFrontRef(it)
                                    }

                                }
                            }
                        }
                    }
                } else {
                    handleErrorMessage("Có lỗi xảy ra")
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.apply {

            firstName.observe(viewLifecycleOwner, { data ->
                binding.edtFirstName.setText(data)
            })

            lastname.observe(viewLifecycleOwner, { data ->
                binding.edtLastName.setText(data)
            })

            birthday.observe(viewLifecycleOwner, { data ->
                binding.txtBirthDay.setText(data)
            })

            email.observe(viewLifecycleOwner, { data ->
                binding.edtEmail.setText(data)
            })

            password.observe(viewLifecycleOwner, { data ->
                binding.edtPassword.setText(data)
            })

            confirmPassword.observe(viewLifecycleOwner, { data ->
                binding.edtConfirmPassword.setText(data)
            })

            phone.observe(viewLifecycleOwner, { data ->
                binding.edtPhone.setText(data)
            })

            tax.observe(viewLifecycleOwner, { data ->
                binding.edtTax.setText(data)
            })

            bankNumber.observe(viewLifecycleOwner, { data ->
                binding.edtBankNumber.setText(data)
            })

            bankName.observe(viewLifecycleOwner, { data ->
                binding.spBankName.setSelection(data)
            })
            bankNumber.observe(viewLifecycleOwner, { data ->
                binding.edtBankNumber.setText(data)
            })
            bankBrand.observe(viewLifecycleOwner, { data ->
                binding.edtBrand.setText(data)
            })
            heigth.observe(viewLifecycleOwner, { data ->
                binding.edtHeight.setText(data)
            })
            weigth.observe(viewLifecycleOwner, { data ->
                binding.edtWeight.setText(data)
            })
            id.observe(viewLifecycleOwner, { data ->
                binding.edtId.setText(data)
            })
            city.observe(viewLifecycleOwner, { data ->
                binding.spCity.setSelection(data)
            })
            temporaryCity.observe(viewLifecycleOwner, { data ->
                binding.spCurrentCity.setSelection(data)
            })
            district.observe(viewLifecycleOwner, { data ->
                binding.spDistrict.setSelection(data)
            })
            temporaryDistrict.observe(viewLifecycleOwner, { data ->
                binding.spCurrentDistrict.setSelection(data)
            })

            avatarRefData.observe(viewLifecycleOwner, Observer {
                Log.d("AAAHAU", "avatarRefData: " + it)
                avatarRef = it
                showAvatar(binding.imgAvatar, it)
            })

            bodyRefData.observe(viewLifecycleOwner, Observer {
                Log.d("AAAHAU", "bodyRefData: " + it)
                bodyRef = it
                showImage(binding.imgFullBody, it)
            })

            idBackRefData.observe(viewLifecycleOwner, Observer {
                Log.d("AAAHAU", "idBackRefData: " + it)
                idBackRef = it
                showImage(binding.imgIdBack, it)
            })

            idFrontRefData.observe(viewLifecycleOwner, Observer {
                Log.d("AAAHAU", "idFrontRefData: " + it)
                idFrontRef = it
                showImage(binding.imgIdFront, it)
            })

            result.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        showLoading()
                    }

                    Status.SUCCESS -> {
                        hideLoading()
                        requireContext().showDialog(
                            title = "Đăng kí thành công, vui lòng tiến hành đăng nhập",
                            textPositive = "Có",
                            positiveListener = {
                                findNavController().navigate(R.id.action_global_loginFragment)
                            }
                        )
                    }

                    Status.ERROR -> {
                        hideLoading()
                    }
                }
            })
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("url")
            ?.observe(viewLifecycleOwner, Observer { result ->
                Log.d("AAA-result", result)
                val file = File(result)
                when (tyeImage) {
                    "avatar" -> {
                        avatarRef = file.absolutePath.toString()
                        avatarRef?.let {
                            showAvatar(binding.imgAvatar, it)
                            viewModel.setAvatarRef(it)
                        }
                    }

                    "full_body" -> {
                        bodyRef = file.absolutePath.toString()
                        bodyRef?.let {
                            showImage(binding.imgFullBody, it)
                            viewModel.setBodyRef(it)
                        }
                    }

                    "id_back" -> {
                        idBackRef = file.absolutePath.toString()
                        idBackRef?.let {
                            showImage(binding.imgIdBack, it)
                            viewModel.setIdBackRef(it)
                        }
                    }

                    "id_front" -> {
                        idFrontRef = file.absolutePath.toString()
                        idFrontRef?.let {
                            showImage(binding.imgIdFront, it)
                            viewModel.setIdFrontRef(it)
                        }
                    }
                }
            })

        cityAdapter = CityAdapter(requireContext(), arrayListOf())
        currentCityAdapter = CityAdapter(requireContext(), arrayListOf())
        districtAdapter = CityAdapter(requireContext(), arrayListOf())
        currentDistrictAdapter = CityAdapter(requireContext(), arrayListOf())

        // Bank name
        var banks = ArrayList<SimpleModel>()
        CityUtils.getAllBank(requireContext()).forEach {
            banks.add(SimpleModel(it.name, it.id))
        }
        bankAdapter = SimpleSPAdapter(requireContext(), banks)
        binding.spBankName.setAdapter(bankAdapter)

        // Gender
        genderAdapter = GenderSPAdapter(Gender.values(), requireContext())
        binding.spGender.adapter = genderAdapter

        // City
        val cities = CityUtils.getAllCity(requireContext())
        cityAdapter.setData(cities)
        currentCityAdapter.setData(cities)
        binding.spCity.setAdapter(cityAdapter)
        binding.spCurrentCity.setAdapter(cityAdapter)

        binding.spCity.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                viewModel.setCity(position)
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
                viewModel.setTemporaryCity(position)
                currentCityCode = currentCityAdapter.getItem(position).id
                val districts = CityUtils.getAllDistrictOfCity(requireContext(), currentCityCode!!)
                currentDistrictAdapter.setData(districts)
                binding.spCurrentDistrict.setAdapter(currentDistrictAdapter)
            }
        }

        binding.spDistrict.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                viewModel.setDistrict(position)
                if (districtAdapter.items.size != 0) {
                    districtCode = districtAdapter.getItem(position).id
                }
            }
        }

        binding.spCurrentDistrict.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                viewModel.setTemporaryCity(position)
                if (currentDistrictAdapter.items.size != 0) {
                    currentDistrictCode = currentDistrictAdapter.getItem(position).id
                }
            }
        }

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.btnTax.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                setData(Uri.parse("http://tracuunnt.gdt.gov.vn/tcnnt/mstcn.jsp"))
            })
        }

        binding.imgAvatar.setSingleClick {
            tyeImage = "avatar"
            openCamera()
        }

        binding.imgFullBody.setSingleClick {
            tyeImage = "full_body"
            openCamera()
        }

        binding.imgIdFront.setSingleClick {
            tyeImage = "id_front"
            openCamera()
        }

        binding.imgIdBack.setSingleClick {
            tyeImage = "id_back"
            openCamera()
        }

        binding.txtBirthDay.setSingleClick {
            val datepicker = DatePickerSpinnerDialog()
            datepicker.setListener(this)
            datepicker.show(childFragmentManager, datepicker.getTag())
        }

        binding.imgRegister.setSingleClick {
            val firstName = binding.edtFirstName.text.toString().trim()
            val lastName = binding.edtLastName.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            val confirmPassword = binding.edtConfirmPassword.text.toString().trim()
            val gender = genderAdapter.getItem(binding.spGender.selectedItemPosition).value
            val birthDate = binding.txtBirthDay.text.toString().trim()
            val phone = binding.edtPhone.text.toString().trim()
            val bankBrand = binding.edtBrand.text.toString().trim()
            val bankNumber = binding.edtBankNumber.text.toString().trim()
            val bankName = bankAdapter.getItem(binding.spBankName.selectedItemPosition).name.trim()
            val id = binding.edtId.text.toString().trim()
            val tax = binding.edtTax.text.toString().trim()
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

            val userForm = UpdateProfileForm(
                firstName = firstName,
                lastName = lastName,
                birthDay = birthDate,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                phone = phone,
                gender = gender,
                tax = tax,
                bank_number = bankNumber,
                bank_name = bankName,
                bank_brand = bankBrand,
                heigth = height,
                weigth = weight,
                id = id,
                city = city.toString(),
                district = district.toString(),
                avatar = avatarRef,
                full_body = bodyRef,
                id_front = idFrontRef,
                id_back = idBackRef,
                address = address,
                temporary_address = current_address,
                temporary_city = current_city.toString(),
                temporary_district = current_district.toString()
            )

            userForm.validate().also { result ->
                if (result.first) {
                    viewModel.register(userForm)
                } else {
                    Toast.makeText(requireContext(), result.second, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun selectFromGallery() {
        openGallery()
    }

    override fun selectFromCamera() {
        openCamera()
    }

    private fun openCamera() {
        val bundle = bundleOf("isTakeImage" to true)
        findNavController().navigate(R.id.action_global_CameraFragment, bundle)
    }


    override fun getDate(date: Int, month: Int, year: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, date)
        updateBirthDay()
    }

    private val dateSetListener =
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }

    private fun updateBirthDay() {
        val myFormat = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.txtBirthDay.text = sdf.format(calendar.time).toString()
        viewModel.setBirthDay(sdf.format(calendar.time).toString())
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        val chooserIntent = Intent.createChooser(intent, "Select Picture")
        galleryLauncher.launch(chooserIntent)
    }

    fun showAvatar(holder: ImageView, data: String) {
        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .circleCrop()
            .placeholder(R.mipmap.ic_launcher_round)
            .error(R.mipmap.ic_launcher_round)
        GlideApp.with(requireContext()).load(data).apply(options)
            .into(holder)
    }

    fun showImage(holder: ImageView, path: String) {
        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .fitCenter()
            .placeholder(R.mipmap.ic_launcher_round)
            .error(R.mipmap.ic_launcher_round)
        GlideApp.with(requireContext()).load(path).apply(options)
            .into(holder)
    }

    companion object {
        const val CODE_REQUEST_GALLERY = 1
    }
}