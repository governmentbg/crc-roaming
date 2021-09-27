package bg.crc.roamingapp.helper

import android.content.Context
import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.app.ApplicationSession.PREF_BLOCK_COUNTRY
import bg.crc.roamingapp.app.ApplicationSession.PREF_BLOCK_TELECOMS
import bg.crc.roamingapp.app.ApplicationSession.PREF_IS_SECOND_TIME_TELECOMES
import bg.crc.roamingapp.app.ApplicationSession.PREF_IS_UPDATE_TELECOMS
import bg.crc.roamingapp.app.ApplicationSession.PREF_LIST_OF_TELECOMS
import bg.crc.roamingapp.app.ApplicationSession.PREF_TELECOMS_VERSION
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.constant.Constants
import bg.crc.roamingapp.constant.ErrorHandler
import bg.crc.roamingapp.debug.MyDebug
import bg.crc.roamingapp.models.telecomslist.Country
import bg.crc.roamingapp.server.ApiClient
import bg.crc.roamingapp.server.ConnectionDetector
import bg.crc.roamingapp.server.RequestInterface
import bg.crc.roamingapp.server.RequestParameters
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Easy access of telecoms list. Below access provided by this class
 * 1) Request For telecoms.
 * 2) Access fetched telecoms
 * 3) Help to add and remove telecoms.
 * */
object TelecomsHelper {
    private val TAG = "RoamingProtect" //javaClass.simpleName

//    val context = RoamingProtect.context
    private var compositeDisposable = CompositeDisposable()

    private var listOfTelecoms: ArrayList<Country?>? = null

    init {
        listOfTelecoms = AppUtils.getTelecomsDataArrayList()
    }

    interface OnTelecomsChangeListener {
        fun onTelecomsChange() {}
        fun onTelecomsFetchError() {}
        fun onTelecomsRequest() {}
        fun onTelecomsFetched() {}
    }

    private var onTelecomsChange: OnTelecomsChangeListener? = null

    /**
     * This function request to server for getting list of telecoms.
     * After successfully fetched, list of telecoms data set into the preference .
     **/
    private fun apiCallForTelecomsList(
        context: Context,
        onTelecomsFetched: OnTelecomsChangeListener?
    ) {
        this.onTelecomsChange = onTelecomsFetched
        if (ConnectionDetector.isConnectedToHomeInternet(context)) {
            try {

                val parameter = RequestParameters.getTelecomsListParameter(
                    ApplicationSession.getString(Constants.PREF_SECRET_KEY),
                    ApplicationSession.getInt(Constants.PREF_USER_ID).toString()
                )
                val apiClient = ApiClient.getClient(context).create(RequestInterface::class.java)
                onTelecomsChange?.onTelecomsRequest()
                compositeDisposable.add(
                    apiClient.telecomsList(parameter)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .map {
                            val telecomsData = AppUtils.getUserSelectedTelecomsArrayList(PREF_BLOCK_TELECOMS)
                            val oldCountries = AppUtils.getTelecomsDataArrayList()
                            it.countries?.forEach { country ->
                                AppUtils.setCountryValueUsingCode(country, oldCountries?.lastOrNull { c -> c?.mcc.equals(country.mcc) }, telecomsData)
                            }
                            return@map it
                        }
                        .subscribe({
                            when (it.ec) {
                                ErrorHandler.OK -> {
                                    if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "apiCallForTelecomsList, countries=" + it.countries)
                                    ApplicationSession.putUserData(PREF_TELECOMS_VERSION, it.vn)
                                    ApplicationSession.putUserData(PREF_IS_UPDATE_TELECOMS, false)
                                    AppUtils.saveTelecomsDataArrayList(it.countries as ArrayList<Country>, PREF_LIST_OF_TELECOMS)
                                    initUserTelecomSelection(true)
                                    listOfTelecoms = it.countries.toCollection(ArrayList())
                                    onTelecomsChange?.onTelecomsChange()
                                    onTelecomsChange?.onTelecomsFetched()
                                }
                                else -> {
                                    ErrorHandler.serverHandleError(context, it.ec, null, true)
                                }
                            }
                        }, { error ->
                            onTelecomsChange?.onTelecomsFetchError()
                            if (MyDebug.IS_DEBUG) MyDebug.showLog("error", error.message)
                        })
                )
            } catch (e: Exception) {
                onTelecomsChange?.onTelecomsFetched()
                if (MyDebug.IS_DEBUG) MyDebug.showLog("catch", e.message)
            }
        } else {
            listOfTelecoms =  AppUtils.getTelecomsDataArrayList()
            if(listOfTelecoms == null) {
                AppUtils.showTelecomsNoInternetDialog(context)
            } else {
                onTelecomsChange?.onTelecomsRequest()
                initUserTelecomSelection(false)
                onTelecomsChange?.onTelecomsChange()
                onTelecomsChange?.onTelecomsFetched()
                AppUtils.showTelecomsNoUpdateDialog(context)
            }
        }
    }

    /**
     * On first login initialize user settings
     */
    fun initUserTelecomSelection(fetched: Boolean) {
        val isSecondTime = ApplicationSession.getUserBoolean(PREF_IS_SECOND_TIME_TELECOMES)
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "initUserTelecomSelection, isSecondTime=" + isSecondTime)

        if (fetched || !isSecondTime) {
            ApplicationSession.putUserData(PREF_IS_SECOND_TIME_TELECOMES, true)
            // initial settings - all telecoms selected
            val telecomsData = AppUtils.getTelecomsDataArrayList()
            if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "initUserTelecomSelection, telecomsData != null?" + (telecomsData != null))
            if (telecomsData != null) {
                val blockCountryList: ArrayList<String> = arrayListOf()
                for (i in 0 until telecomsData.size) {
                    if(telecomsData[i]?.telcos?.any { telco -> telco.isCheckTelecoms == true } == true)
                        blockCountryList.add(telecomsData[i]?.phoneCode ?: "")
                }
                AppUtils.saveUserSelectedTelecomsArrayList(blockCountryList, PREF_BLOCK_COUNTRY)

                val blockTelecomsList: ArrayList<String> = arrayListOf()
                for (i in 0 until telecomsData.size) {
                    val tl = telecomsData[i]?.telcos
                    for (i1 in tl!!.indices) {
                        if(tl[i1].isCheckTelecoms == true) {
                            blockTelecomsList.add(
                                String.format(
                                    "%s %s",
                                    telecomsData[i]?.mcc,
                                    tl[i1].c ?: ""
                                )
                            )
                        }
                    }
                }
                AppUtils.saveUserSelectedTelecomsArrayList(blockTelecomsList, PREF_BLOCK_TELECOMS)
            }
            // initial settings - send notifications is ON
            ApplicationSession.setUserSettingNotification(true)
        }
    }

    /**
     * Request to server for fetch list of telecoms.
     * This function request to server, when receive update regarding list of telecoms, list of telecoms null.
    **/
    fun checkOrRequestTelecomsList(context: Context, onTelecomsFetched: OnTelecomsChangeListener?) {
        if (MyDebug.IS_DEBUG) MyDebug.showLog(TAG, "checkOrRequestTelecomsList,"+
                " listOfTelecoms == null? " + (listOfTelecoms == null) +
                " compositeDisposable.size() == 0? " + (compositeDisposable.size() == 0) +
                " PREF_IS_UPDATE_TELECOMS=" + ApplicationSession.getUserBoolean(PREF_IS_UPDATE_TELECOMS)
        )
        if ((listOfTelecoms == null && compositeDisposable.size() == 0) || ApplicationSession.getUserBoolean(PREF_IS_UPDATE_TELECOMS)) {
            apiCallForTelecomsList(context, onTelecomsFetched)
        } else {
            this.onTelecomsChange = onTelecomsFetched
            onTelecomsChange?.onTelecomsChange()
        }
    }
}