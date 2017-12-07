package com.surrus.galwaybus.ui

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener
import com.surrus.galwaybus.Constants
import com.surrus.galwaybus.R
import com.surrus.galwaybus.model.BusStop
import com.surrus.galwaybus.model.Location
import com.surrus.galwaybus.ui.viewmodel.BusStopsViewModel
import com.surrus.galwaybus.ui.viewmodel.BusStopsViewModelFactory
import com.surrus.galwaybus.util.ext.observe
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_bus_stop_list.*
import javax.inject.Inject




class BusStopListActivity : AppCompatActivity(), HasSupportFragmentInjector, OnMapReadyCallback {
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var busStopsViewModelFactory: BusStopsViewModelFactory

    private lateinit var busStopsViewModel : BusStopsViewModel

    private var routeId: String = ""
    private var routeName: String = ""
    private var direction: Int = 0

    private var map: GoogleMap? = null
    private lateinit var pagerAdapter: SectionsPagerAdapter


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_stop_list)

        busStopsViewModel = ViewModelProviders.of(this, busStopsViewModelFactory).get(BusStopsViewModel::class.java)

        if (savedInstanceState != null) {
            routeId = savedInstanceState.getString(Constants.ROUTE_ID)
            routeName = savedInstanceState.getString(Constants.ROUTE_NAME)
        } else {
            routeId = intent.extras[Constants.ROUTE_ID] as String
            routeName = intent.extras[Constants.ROUTE_NAME] as String
        }
        setTitle(routeId + " - " + routeName)

//        with(tabLayout) {
//            //addTab(tabLayout.newTab().setText("Direction 1"))
//            //addTab(tabLayout.newTab().setText("Direction 2"))
//            tabGravity = TabLayout.GRAVITY_FILL
//        }


        mapView.onCreate(savedInstanceState)
        mapView.onResume()

        pagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        pager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(pager)

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageSelected(position: Int) {
                direction = position
//                busStopsViewModel.busStops.observe(this@BusStopListActivity) {
//                    updateMap(it!!.get(direction))
//                }
                busStopsViewModel.setDirection(direction)
            }
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        })

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : BasePermissionListener() {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        mapView.getMapAsync(this@BusStopListActivity)
                    }
                }).check()

    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(Constants.ROUTE_ID, routeId)
        outState.putString(Constants.ROUTE_NAME, routeName)
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return StopsFragment.newInstance(routeId, position)
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return "Direction " + (position+1)
        }

    }


    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }



    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.isMyLocationEnabled = true

        busStopsViewModel.busStops.observe(this) {
            updateMap(it!!)
        }
    }


    private fun updateMap(busStopList: List<BusStop>) {

        if (map != null && busStopList.size > 0) {
            map?.clear()

            val builder = LatLngBounds.Builder()
            for (busStop in busStopList) {
                val busStopLocation = LatLng(busStop.latitude, busStop.longitude);
                map?.addMarker(MarkerOptions().position(busStopLocation).title(busStop.longName))
                builder.include(busStopLocation)
            }
            map?.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 64))
        }
    }
}
