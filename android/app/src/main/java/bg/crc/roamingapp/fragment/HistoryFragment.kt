package bg.crc.roamingapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import bg.crc.roamingapp.R
import bg.crc.roamingapp.activity.SettingsActivity
import bg.crc.roamingapp.adapter.HistoryAdapter
import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.constant.Constants
import bg.crc.roamingapp.constant.ErrorHandler
import bg.crc.roamingapp.databinding.FragmentHistoryBinding
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.helper.TelecomsHelper
import bg.crc.roamingapp.models.history.Z
import bg.crc.roamingapp.server.ApiClient
import bg.crc.roamingapp.server.ConnectionDetector
import bg.crc.roamingapp.server.RequestInterface
import bg.crc.roamingapp.server.RequestParameters
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.header.view.*
import kotlinx.android.synthetic.main.internet_connectivity_view.*
import java.util.*

class HistoryFragment : BaseActivity(), TelecomsHelper.OnTelecomsChangeListener {
    private var compositeDisposable = CompositeDisposable()

    private val binding : FragmentHistoryBinding by bindingLazy()
    override val layoutResId = R.layout.fragment_history

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.headerLayout.title.text = getString(R.string.title_history)
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
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvHistory.layoutManager = layoutManager
        apiCallForHistoryList()
    }

    /**
     * This function request to server for "History"
     * After success set data into the list.
     * */
    private fun apiCallForHistoryList() {
        if (ConnectionDetector.isConnectedToHomeInternet(requireContext())) {
            try {
                binding.pbHistory.visibility = View.VISIBLE
                val parameter = RequestParameters.getHistoryParameter(
                    ApplicationSession.getString(Constants.PREF_SECRET_KEY),
                    ApplicationSession.getInt(Constants.PREF_USER_ID).toString()
                )
                val apiClient = ApiClient.getClient(requireContext()).create(RequestInterface::class.java)
                compositeDisposable.add(
                    apiClient.historyList(parameter)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            binding.pbHistory.visibility = View.GONE
                            when (it.ec) {
                                ErrorHandler.OK -> {
                                    if (it.z?.size == 0) {
                                        binding.tvNoData.visibility = View.VISIBLE
                                    } else {
                                        val adapter = HistoryAdapter(requireContext(), it.z as ArrayList<Z>?)
                                        binding.rvHistory.adapter = adapter
                                    }
                                }
                                else -> {
                                    ErrorHandler.serverHandleError(requireContext(), it.ec, null, true)
                                }
                            }
                        }, { error ->
                            binding.pbHistory.visibility = View.GONE
                            if (MyDebug.IS_DEBUG) MyDebug.showLog("error", error.message)
                        })
                )
            } catch (e: Exception) {
                binding.pbHistory.visibility = View.GONE
                if (MyDebug.IS_DEBUG) MyDebug.showLog("catch", e.message)
            }
        } else {
            AppUtils.showHistoryNoInternetDialog(requireContext())
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