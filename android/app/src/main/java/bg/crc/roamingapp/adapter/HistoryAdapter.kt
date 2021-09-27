package bg.crc.roamingapp.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bg.crc.roamingapp.R
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.constant.Constants
import bg.crc.roamingapp.constant.LocationConverter
import bg.crc.roamingapp.models.history.Z
import kotlinx.android.synthetic.main.history_list_item.view.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


/**
 * Adapter for history list.
 * */
class HistoryAdapter(var context: Context, private var itemModels: java.util.ArrayList<Z>?) :
    RecyclerView.Adapter<HistoryAdapter.Holder>() {
    override fun getItemCount() = itemModels?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        LayoutInflater.from(context).inflate(R.layout.history_list_item, parent, false)
    )

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTimestamp = view.tvTimestamp!!
        var tvGpsCoordinates = view.tvGpsCoordinates!!
//        var tvTelecoms = view.tvTelecoms!!
        var tvMccMncCode = view.tvMccMncCode!!
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = itemModels!![position]

        var countryName: String? = null
        var telecomName: String? = null
        val telecomsData = AppUtils.getTelecomsDataArrayList()
        for (i in 0 until telecomsData!!.size) {
            if (telecomsData[i]?.mcc.equals(item.mcc)) {
                countryName = if (Locale.getDefault().language.equals("bg")) telecomsData[i]?.bgName else telecomsData[i]?.dname
                val tl = telecomsData[i]?.telcos
                for (i1 in tl!!.indices) {
                    if (tl[i1].c.equals(item.mnc)) {
                        telecomName = tl[i1].n
                    }
                }
            }
        }

        holder.tvTimestamp.text = Constants.appDateFormatMultiline.format((item.ts ?: 0) * 1000L)
        holder.tvMccMncCode.text = String.format("%s\nMCC: %s\nMNC: %s", telecomName, item.mcc, item.mnc)
        holder.tvGpsCoordinates.text = Html.fromHtml(String.format(
            "%s<br \\><a href=\"#\">%s<br \\>%s</a>", countryName, LocationConverter.latitudeAsDMS(item.va?.get(0) ?: 0.0, 4),
            LocationConverter.longitudeAsDMS(item.va?.get(1) ?: 0.0, 4))
        , Html.FROM_HTML_MODE_COMPACT)

        holder.tvGpsCoordinates.setOnClickListener {
            val df = DecimalFormat("##.####")
            df.decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH)
            val uri: String = String.format("geo:%s,%s?q=%s,%s", df.format(item.va?.get(0)), df.format(item.va?.get(1)), df.format(item.va?.get(0)), df.format(item.va?.get(1)))
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            context.startActivity(intent)
        }
    }
}