package bg.crc.roamingapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import bg.crc.roamingapp.R
import bg.crc.roamingapp.activity.SettingsActivity
import bg.crc.roamingapp.adapter.ListOfTelecomsAdapter
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.databinding.FragmentTelecomsBinding
import bg.crc.roamingapp.helper.TelecomsHelper
import kotlinx.android.synthetic.main.header.view.*
import kotlinx.android.synthetic.main.internet_connectivity_view.*

/**
 * This screen display List of Telecoms UI.
 * User can able to select telecoms
 */
class TelecomsFragment : BaseActivity(), TelecomsHelper.OnTelecomsChangeListener {
    private val binding : FragmentTelecomsBinding by bindingLazy()
    override val layoutResId = R.layout.fragment_telecoms

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.headerLayout.title.text = getString(R.string.title_telecoms)
        init()

        binding.headerLayout.ivProfile.visibility = View.VISIBLE
        binding.headerLayout.ivProfile.setOnClickListener { v -> showPopup(v) }

        binding.headerLayout.ivSettings.visibility = View.VISIBLE
        binding.headerLayout.ivSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
    }

    /**
     * Recyclerview's layout manager.
     * Request for fetch telecoms list.
     **/
    private fun init() {
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvTelecomCountry.layoutManager = layoutManager
        TelecomsHelper.checkOrRequestTelecomsList(requireContext(), this)
    }

    /**
    * This function set data into the list
    **/
    private fun setTelecomsData() {
        val adapter = ListOfTelecomsAdapter(requireContext(), AppUtils.getTelecomsDataArrayList())
        binding.rvTelecomCountry.adapter = adapter
    }

    /**
     *This override function notify to user about internet connectivity.
     * */
    override fun onInternetConnectivityChange() {
        tvInternetConnection?.visibility = if (isInternetAvailable) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    override fun onTelecomsChange() {
        setTelecomsData()
    }

    /**
     * Handle what happen after fetch telecoms list.
     **/
    override fun onTelecomsFetched() {
        binding.pbTelecomsList.visibility = View.GONE
        setTelecomsData()
    }

    /**
     * Handle telecoms list fetch error.
     **/
    override fun onTelecomsFetchError() {
        binding.pbTelecomsList.visibility = View.GONE
    }

    /**
    * Handle telecoms list fetch request.
    **/
    override fun onTelecomsRequest() {
        binding.pbTelecomsList.visibility = View.VISIBLE
    }
}