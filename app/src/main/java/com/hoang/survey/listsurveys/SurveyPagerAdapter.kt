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
        notifyDataSetChanged()
    }

    fun getItem(position: Int): SurveyItemResponse {
        return data[position]
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
        ivSurvey.loadImage(survey.coverImageUrlBig, R.drawable.placeholder)
    }
}