package com.startupsurveys.ui.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.quickbirdstudios.surveykit.FinishReason
import com.quickbirdstudios.surveykit.OrderedTask
import com.quickbirdstudios.surveykit.SurveyTheme
import com.quickbirdstudios.surveykit.result.TaskResult
import com.quickbirdstudios.surveykit.steps.CompletionStep
import com.quickbirdstudios.surveykit.steps.InstructionStep
import com.quickbirdstudios.surveykit.steps.Step
import com.quickbirdstudios.surveykit.survey.SurveyView
import com.startupsurveys.MainActivity
import com.startupsurveys.R
import com.startupsurveys.ui.home.HomeFragment
import com.startupsurveys.util.SurveyHelper


class SurveyFragment : Fragment() {

    private lateinit var surveyView: SurveyView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view  = inflater.inflate(R.layout.fragment_survey, container, false)
        surveyView = view.findViewById(R.id.survey_view)

        setupQuestions()

        return view
    }

    private fun setupQuestions() {
        val appName = arguments?.getString("appName") ?: "UNKNOWN"
        val steps: ArrayList<Step> = arrayListOf()
//        https://github.com/QuickBirdEng/SurveyKit#create-survey-steps
        steps.add(
            InstructionStep(
                title = "Review the $appName app",
                text = "Complete a series of questions on $appName to earn a Crypto reward",
                buttonText = "Start"
            )
        )

        val numQuestions = arguments?.getInt("numQuestions") ?: 5
        steps.addAll(SurveyHelper.generateSurveyQuestionSteps(appName, numQuestions))

        steps.add(
            CompletionStep(
                title = "Done",
                text = "Thanks for completing the survey",
                buttonText = "Get reward"
            )
        )

        val task = OrderedTask(steps = steps)
        val configuration = SurveyTheme(
            themeColorDark = ContextCompat.getColor(
                requireContext(),
                androidx.appcompat.R.color.material_grey_300
            ),
            themeColor = ContextCompat.getColor(
                requireContext(),
                androidx.appcompat.R.color.material_blue_grey_800
            ),
            textColor = ContextCompat.getColor(
                requireContext(),
                androidx.appcompat.R.color.material_grey_600
            ),
        )
        surveyView.start(task, configuration)

        surveyView.onSurveyFinish = { taskResult: TaskResult, reason: FinishReason ->
            if (reason == FinishReason.Discarded) {
                // Route home
                (activity as MainActivity).navigateTo(HomeFragment())
            } else if (reason == FinishReason.Completed) {
                // TODO: add success logic to remit payment to user's saved address.

                val text = "Survey Completed!"
                val duration = Toast.LENGTH_LONG
                val toast = Toast.makeText(context, text, duration)
                toast.show()

                // TODO: show transaction
                (activity as MainActivity).navigateTo(HomeFragment())
            }
        }

        activity?.actionBar?.title = "Survey: $appName"
    }

}