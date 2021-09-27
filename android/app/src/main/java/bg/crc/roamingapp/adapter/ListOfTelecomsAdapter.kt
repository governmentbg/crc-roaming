package bg.crc.roamingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bg.crc.roamingapp.R
import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.app.ApplicationSession.PREF_BLOCK_COUNTRY
import bg.crc.roamingapp.app.ApplicationSession.PREF_BLOCK_TELECOMS
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.models.telecomslist.Country
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.android.synthetic.main.telecom_country_list_item.view.*
import kotlinx.android.synthetic.main.telecom_list_item.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Adapter for list of telecoms.
 * */
class ListOfTelecomsAdapter(
    var context: Context,
    private var itemModels: java.util.ArrayList<Country?>?
) :
    RecyclerView.Adapter<ListOfTelecomsAdapter.Holder>() {

    private var layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount() = itemModels?.size ?: 0

    var telecomsData = AppUtils.getUserSelectedTelecomsArrayList(PREF_BLOCK_TELECOMS)
    var countryData = AppUtils.getUserSelectedTelecomsArrayList(PREF_BLOCK_COUNTRY)

    init {
        if (telecomsData == null) {
            telecomsData = ArrayList()
        }
        if (countryData == null) {
            countryData = ArrayList()
        }
        itemModels?.forEach { country ->
            country?.telcos?.forEach { telecom ->
                telecom.isCheckTelecoms =
                    telecomsData?.contains("${country.mcc} ${telecom.c}") ?: false
            }
            country?.isCheckCountry = countryData?.contains(country?.phoneCode)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        LayoutInflater.from(context).inflate(R.layout.telecom_country_list_item, parent, false)
    )

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        var tvCountryName = view.tvCountryName!!
        var tvCountryCode = view.tvCountryCode!!
        var ivCountryLogo = view.ivCountryLogo!!
        var ivEuFlag = view.ivEuFlag!!
        var llTelecomsContainer = view.llTelecomsContainer!!
        var chbCountry = view.chbCountry!!
    }

    /**
     * This function set data and add view dynamically according to telecoms data into the [llTelecomsContainer].
     * Store selected telecoms for check Roaming.
     * [blockTelecomsList] -> for selected telecoms
     * [blockCountryList]. -> for selected country
     **/
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = itemModels!![position]

        holder.ivEuFlag.visibility = if (item?.isEU == 0) View.GONE else View.VISIBLE
        Glide.with(context)
            .load(item?.countryFlag)
            .placeholder(R.drawable.ic_launcher)
            .into(holder.ivCountryLogo)

        if(item?.bgName != null && item.dname != null)
            holder.tvCountryName.text = if (Locale.getDefault().language.equals("bg")) item.bgName else item.dname
        holder.tvCountryCode.text = item?.phoneCode

        item?.isCheckCountry = item?.telcos?.stream()?.allMatch { it.isCheckTelecoms ?: false }
        holder.chbCountry.isChecked = item?.isCheckCountry ?: false
        holder.chbCountry.setOnClickListener {
//            if(it.tvCountryCode != null && it.tvCountryCode.equals(item?.countryCode)) {
                if (holder.chbCountry.isChecked) {
                    countryData?.add(item?.phoneCode ?: "")

                    item?.telcos?.forEach { telecome ->
                        telecomsData?.add("${item.mcc} ${telecome.c}")
                        telecome.isCheckTelecoms = true
                    }

                } else {
                    countryData?.removeAll { it.equals(item?.phoneCode ?: "") }

                    item?.telcos?.forEach { telecome ->
                        telecomsData?.removeAll { it.equals("${item.mcc} ${telecome.c}") }
                        telecome.isCheckTelecoms = false
                    }
                }
                item?.isCheckCountry = holder.chbCountry.isChecked
                storeCountryList()
                storeTelecomsList()
                notifyDataSetChanged()
//            }
        }

        holder.llTelecomsContainer.removeAllViews()
        item?.telcos?.forEach { telecome ->
            val detailView = layoutInflater.inflate(
                R.layout.telecom_list_item,
                holder.llTelecomsContainer,
                false
            )
            detailView.findViewById<TextView>(R.id.telecomeName).text = telecome.n ?: ""
            detailView.findViewById<TextView>(R.id.tvMccMncCode).text =
                String.format("%s %s", item.mcc, telecome.c ?: "")
            holder.llTelecomsContainer.addView(detailView)

            detailView.chbTelecom.isChecked = telecome.isCheckTelecoms ?: false
            detailView.chbTelecom.setOnClickListener {
                telecome.isCheckTelecoms = detailView.chbTelecom.isChecked
                item.isCheckCountry = item.telcos.stream().allMatch { it.isCheckTelecoms ?: false }
                holder.chbCountry.isChecked = item.isCheckCountry ?: false

                if (detailView.chbTelecom.isChecked) {
                    telecomsData?.add("${item.mcc} ${telecome.c}")
                } else {
                    telecomsData?.removeAll { it.equals("${item.mcc} ${telecome.c}") }
                }
                storeTelecomsList()
            }
        }
    }

    /**
    * Store selected telecoms list into the preference.
    **/
    private fun storeTelecomsList() {
        ApplicationSession.putUserData(PREF_BLOCK_TELECOMS, Gson().toJson(telecomsData))
    }

    /**
     * Store selected country list into the preference.
     **/
    private fun storeCountryList() {
        ApplicationSession.putUserData(PREF_BLOCK_COUNTRY, Gson().toJson(countryData))
    }

}