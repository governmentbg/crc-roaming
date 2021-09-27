package bg.crc.roamingapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import bg.crc.roamingapp.R
import bg.crc.roamingapp.activity.SettingsActivity
import bg.crc.roamingapp.databinding.FragmentHomeBinding
import bg.crc.roamingapp.helper.TelecomsHelper
import kotlinx.android.synthetic.main.header.view.*
import kotlinx.android.synthetic.main.internet_connectivity_view.*


class HomeFragment : BaseActivity(), TelecomsHelper.OnTelecomsChangeListener {
    private val binding : FragmentHomeBinding by bindingLazy()
    override val layoutResId = R.layout.fragment_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.headerLayout.title.text = getString(R.string.title_home)

        binding.headerLayout.ivProfile.visibility = View.VISIBLE
        binding.headerLayout.ivProfile.setOnClickListener { v -> showPopup(v) }

        binding.headerLayout.ivSettings.visibility = View.VISIBLE
        binding.headerLayout.ivSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
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
}