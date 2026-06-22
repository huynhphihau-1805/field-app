package com.crayon.fieldapp.ui.screen.monitor.store.listStore.detail

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentDetailStoreBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailStoreFragment : BaseFragment<FragmentDetailStoreBinding, DetailStoreViewModel>(),
    OnMapReadyCallback {

    override val layoutId: Int = R.layout.fragment_detail_store

    override val viewModel: DetailStoreViewModel by viewModel()

    lateinit var agencyId: String
    lateinit var storeId: String
    private var gmap: GoogleMap? = null
    private var location: LatLng? = null
    private var name: String? = null
    lateinit var mapFragment: SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
        storeId = requireArguments().get("storeId").toString()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        viewModel.apply {
            fetchDetailStore(agencyId, storeId)
        }

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.apply {
            store.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.ERROR,
                    Status.SUCCESS -> {
                        it.data?.let { store ->
                            location = LatLng(store.lat!!, store.lng!!)
                            name = store.name
                            mapFragment.getMapAsync(this@DetailStoreFragment)
                        }
                    }

                    Status.LOADING -> {

                    }
                }

            })
        }

    }

    override fun onMapReady(p0: GoogleMap) {
        p0.let { map ->
            gmap = p0
            gmap?.let { map ->
                location?.let {
                    map.addMarker(MarkerOptions().position(it).title(name.toString()))
                    map.setMinZoomPreference(15.0f);
                    map.setMaxZoomPreference(20.0f);
                    map.moveCamera(CameraUpdateFactory.newLatLng(it))
                }
            }
        }
    }
}