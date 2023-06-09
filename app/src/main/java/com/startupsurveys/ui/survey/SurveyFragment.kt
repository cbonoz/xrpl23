package com.startupsurveys.ui.survey

import android.content.Context
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
import com.startupsurveys.util.PrefManager
import com.startupsurveys.util.SurveyHelper
import com.startupsurveys.util.SurveyHelper.Companion.convertAppNameToDomain
import kotlinx.coroutines.*


class SurveyFragment : Fragment() {

    private lateinit var surveyView: SurveyView
    private lateinit var surveyReward: String
    private lateinit var scope: CoroutineScope

    private lateinit var userAddress: String
    private lateinit var prefManager: PrefManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_survey, container, false)
        surveyView = view.findViewById(R.id.survey_view)
        prefManager = PrefManager(context as Context)
        userAddress =
            prefManager.getString("USER_ADDRESS", "rQheBm9b6m8YXvEWBqZg58S7fAtcU8efcP").toString()
        scope = CoroutineScope(Job() + Dispatchers.IO)
        setupQuestions()

        return view
    }

    private fun setupQuestions() {
        surveyReward = arguments?.getString("reward") ?: "0.1"
        val appName = arguments?.getString("appName") ?: "UNKNOWN"
        val domainName = convertAppNameToDomain(appName)
        val steps: ArrayList<Step> = arrayListOf()
//        https://github.com/QuickBirdEng/SurveyKit#create-survey-steps
        steps.add(
            InstructionStep(
                title = "Review the $appName app",
                text = "$domainName\n\nComplete a series of questions on $appName to earn a $surveyReward XRP reward",
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

                scope.launch {
                    completePayment(userAddress, surveyReward) { result, error ->
                        run {
                            scope.launch(Dispatchers.Main) {
                                val alertDialog: AlertDialog? = activity?.let {
                                    val title: String
                                    val message: String
                                    if (error != null) {
                                        title = "Error completing survey"
                                        message = if (error.message?.indexOf("validate") != -1) {
                                            "Checksum was not able to validate. Please check your address and try again."
                                        } else {
                                            error.message ?: "Unknown error"
                                        }
                                    } else {
                                        title = "Survey complete!"
                                        message =
                                            "Result: ${result?.engineResultMessage() ?: "Close to return to the app"}"
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