package com.hoang.survey.listsurveys

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.hoang.survey.R
import com.hoang.survey.api.SurveyItemResponse
import com.hoang.survey.base.loadImage

class SurveyPagerAdapter : RecyclerView.Adapter<SurveyViewHolder>() {
    var data: List<SurveyItemResponse> = listOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyViewHolder {
        return SurveyViewHolder(SurveyView(parent.context as MainActivity, parent))
    }

    override fun onBindViewHolder(holder: SurveyViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun submitData(newData: List<SurveyItemResponse>) {
        this.data = newData
    }
}

class SurveyViewHolder constructor(private val surveyView: SurveyView) : RecyclerView.ViewHolder(surveyView.view) {
    fun bind(survey: SurveyItemResponse) {
        surveyView.bind(survey)
    }
}

class SurveyView(val activity: MainActivity, container: ViewGroup) {
    val view = LayoutInflater.from(activity).inflate(R.layout.layout_survey, container, false)
    private val tvTitle: TextView
    private val tvDescription: TextView
    private val ivSurvey: ImageView

    init {
        ivSurvey = view.findViewById<ImageView>(R.id.img_survey)
        tvTitle = view.findViewById<TextView>(R.id.tv_survey_title)
        tvDescription = view.findViewById<TextView>(R.id.tv_survey_desc)
    }

    fun bind(survey: SurveyItemResponse) {
        tvTitle.text = survey.tille
        tvDescription.text = survey.description
        ivSurvey.loadImage(survey.coverImageUrlBig)
    }
}

class SurveyPagerAdapter2(private val activity: MainActivity, private val surveys: ArrayList<SurveyItemResponse>) :
    PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val view = LayoutInflater.from(activity).inflate(R.layout.layout_survey, container, false)
        val ivSurvey = view.findViewById<ImageView>(R.id.img_survey)
        val tvTitle = view.findViewById<TextView>(R.id.tv_survey_title)
        val tvDescription = view.findViewById<TextView>(R.id.tv_survey_desc)

        val survey = surveys[position]
        Glide.with(activity).load(survey.coverImageUrlBig).into(ivSurvey)
        tvTitle.text = survey.tille
        tvDescription.text = survey.description
        container.addView(view)
        return view
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return surveys.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    /***
     * Notify the viewpager about new data: compare and only
     */
    fun submitData(newData: List<SurveyItemResponse>) {

    }

}