package com.startupsurveys.ui.survey

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.startupsurveys.util.PaymentHelper.Companion.completePayment
import com.startupsurveys.util.PaymentHelper.Companion.getExplorerUrl
import com.startupsurveys.util.SurveyHelper
import kotlinx.coroutines.*


class SurveyFragment : Fragment() {

    private lateinit var surveyView: SurveyView
    private lateinit var surveyReward: String
    private lateinit var scope: CoroutineScope

    private val userAddress = "rKi7x3BMPLiWaMK8fYE1V2m6no6KQUxpNm"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_survey, container, false)
        surveyView = view.findViewById(R.id.survey_view)
        setupQuestions()

        return view
    }

    private fun setupQuestions() {
        surveyReward = arguments?.getString("reward") ?: "0.1"
        val appName = arguments?.getString("appName") ?: "UNKNOWN"
        val steps: ArrayList<Step> = arrayListOf()
//        https://github.com/QuickBirdEng/SurveyKit#create-survey-steps
        steps.add(
            InstructionStep(
                title = "Review the $appName app",
                text = "Complete a series of questions on $appName to earn a $surveyReward XRP reward",
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

                // TODO: make user definable.
                // matching secret for below address: sEdS4BT6SzzQxnvwMdAjJotnTjnwE4S
                scope = CoroutineScope(Job() + Dispatchers.IO)

                scope.launch {
                    completePayment(userAddress, surveyReward) { result, error ->
                        run {
                            scope.launch(Dispatchers.Main) {
                                val alertDialog: AlertDialog? = activity?.let {
                                    val title: String
                                    val message: String
                                    if (error != null) {
                                        title = "Error completing survey"
                                        message = error.message ?: "Unknown error"
                                    } else {
                                        title = "Survey complete!"
                                        message = "Result: ${result?.engineResultMessage() ?: "Close to return to the app"}"
                                    }

                                    val builder = AlertDialog.Builder(it)
                                        .setMessage(message)
                                        .setTitle(title)

                                    builder.apply {
                                        setPositiveButton(
                                            R.string.done
                                        ) { dialog, id ->
                                            // User clicked OK button
                                            dialog.dismiss()
                                        }
//                                        setNeutralButton(
//                                            R.string.view
//                                        ) { dialog, id ->
//                                            // User cancelled the dialog
//                                            val url = getExplorerUrl(userAddress)
//                                            openWebPage(url)
//                                            dialog.dismiss()
//                                        }

                                    }
                                    // Set other dialog properties

                                    // Create the AlertDialog
                                    builder.create()
                                }

                                alertDialog?.show()
                                // TODO: show transaction
                                (activity as MainActivity).navigateTo(HomeFragment())
                            }
                        }
                    }
                }
            }
        }

        activity?.actionBar?.title = "Survey: $appName"
    }

    fun openWebPage(url: String?) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (context?.packageManager?.let { intent.resolveActivity(it) } != null) {
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // kill scope
        scope.cancel()
    }

}