package com.hoang.survey.listsurveys

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.hoang.survey.R
import com.hoang.survey.api.SurveyItemResponse

class SurveyPagerAdapter(private val activity: MainActivity, private val surveys: ArrayList<SurveyItemResponse>) : PagerAdapter() {

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

}