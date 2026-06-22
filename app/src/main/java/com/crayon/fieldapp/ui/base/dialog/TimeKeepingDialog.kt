package com.crayon.fieldapp.ui.base.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.DialogTimekeepingBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class TimeKeepingDialog : DialogFragment(), OnMapReadyCallback {
    private var _binding: DialogTimekeepingBinding? = null
    private val binding get() = _binding!!

    lateinit var mMap: GoogleMap
    lateinit var mCurrent: LatLng
    lateinit var mStore: LatLng
    var mDistant: String = ""
    lateinit var mapFragment: SupportMapFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogTimekeepingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment

        // Get arguments
        arguments?.let {
            mCurrent = LatLng(it.getDouble("current_lat"), it.getDouble("current_long"))
            mStore = LatLng(it.getDouble("store_lat"), it.getDouble("store_long"))
            mDistant = it.getString("distant").toString()
        }

        binding.txtDistant.text =
            String.format(resources.getString(R.string.txt_invalid_distant), mDistant)

        mapFragment.getMapAsync(this)
    }


    override fun onStart() {
        super.onStart()
        dialog?.apply {
            setCanceledOnTouchOutside(true)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.WHITE))
                attributes = attributes.apply { gravity = Gravity.CENTER }
                setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(p0: GoogleMap) {
        p0.apply {
            mMap = this
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID

            val current = MarkerOptions().apply {
                position(mCurrent)
                title("Current Position")
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
            }

            val store = MarkerOptions().apply {
                position(mStore)
                title("Store Position")
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
            }

            mMap.addMarker(current)
            mMap.addMarker(store)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mStore, 11f))
        }
    }
}
