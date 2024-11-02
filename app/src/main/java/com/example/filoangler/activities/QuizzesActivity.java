package com.example.filoangler.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.filoangler.R;
import com.example.filoangler.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizzesActivity extends AppCompatActivity {
    private ConstraintLayout viewIntro, viewQuiz, viewFinished;
    private TextView txtQuestion;
    private RadioButton rbtnA, rbtnB, rbtnC, rbtnD;
    private Button btnPrevious, btnNext, btnProceed, btnExit;
    private ImageButton btnBack;
    private TextView txtFinishedMessage, txtScore;

    // Static proficiency level (as requested)
    private static final String PROFICIENCY_LEVEL = "Novice";

    private List<QuizQuestion> questions;
    private int currentQuestionIndex = 0;
    private List<String> userAnswers;
    private int finalScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quizzes);

        initializeViews();
        loadQuizData();
        setupListeners();

        // Start with intro view
        viewIntro.setVisibility(View.VISIBLE);
        viewQuiz.setVisibility(View.GONE);
        viewFinished.setVisibility(View.GONE);
    }

    private void initializeViews() {
        viewIntro = findViewById(R.id.viewIntro);
        viewQuiz = findViewById(R.id.viewQuiz);
        viewFinished = findViewById(R.id.viewFinished);

        txtQuestion = findViewById(R.id.txtQuestion);
        rbtnA = findViewById(R.id.rbtnA);
        rbtnB = findViewById(R.id.rbtnB);
        rbtnC = findViewById(R.id.rbtnC);
        rbtnD = findViewById(R.id.rbtnD);

        btnPrevious = findViewById(R.id.button2);
        btnNext = findViewById(R.id.button3);
        btnProceed = findViewById(R.id.btnProceed);
        btnExit = findViewById(R.id.btnExit);
        btnBack = findViewById(R.id.imageButton);

        txtFinishedMessage = findViewById(R.id.txtFinishedMessage);
        txtScore = findViewById(R.id.txtScore);

        userAnswers = new ArrayList<>();
    }

    private void loadQuizData() {
        try {
            String jsonString = Utils.loadJSONFromAsset(this,"quiz.json");
            JSONObject json = new JSONObject(jsonString);
            JSONObject quiz = json.getJSONObject("Quiz");
            JSONArray questionsArray = quiz.getJSONArray(PROFICIENCY_LEVEL);

            // Convert JSONArray to List for easier manipulation
            List<JSONObject> questionList = new ArrayList<>();
            for (int i = 0; i < questionsArray.length(); i++) {
                questionList.add(questionsArray.getJSONObject(i));
            }

            // Randomize the questions
            Collections.shuffle(questionList);

            // Convert randomized questions to QuizQuestion objects
            questions = new ArrayList<>();
            for (JSONObject questionObj : questionList) {
                QuizQuestion question = new QuizQuestion();
                question.question = questionObj.getString("question");

                JSONObject options = questionObj.getJSONObject("Options");
                question.options = new String[4];
                if (PROFICIENCY_LEVEL.equals("Novice")) {
                    // For Novice level, get all 4 options
                    question.options[0] = options.getString("a");
                    question.options[1] = options.getString("b");
                    question.options[2] = options.getString("c");
                    question.options[3] = options.getString("d");
                } else {
                    // For Aspiring level, just True/False
                    question.options = new String[]{"True", "False"};
                }

                question.answer = questionObj.getString("answer");
                questions.add(question);
                userAnswers.add("");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        btnProceed.setOnClickListener(v -> transitionToQuiz());

        btnBack.setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> {
            saveCurrentAnswer();
            if (currentQuestionIndex < questions.size() - 1) {
                animateToNextQuestion();
            } else {
                calculateScore();
                transitionToFinished();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            saveCurrentAnswer();
            if (currentQuestionIndex > 0) {
                animateToPreviousQuestion();
            }
        });

        btnExit.setOnClickListener(v -> finish());
    }

    private void saveCurrentAnswer() {
        RadioGroup radioGroup = (RadioGroup) rbtnA.getParent();
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedButton = findViewById(selectedId);
            String answer = "";
            if (selectedButton == rbtnA) answer = "a";
            else if (selectedButton == rbtnB) answer = "b";
            else if (selectedButton == rbtnC) answer = "c";
            else if (selectedButton == rbtnD) answer = "d";
            userAnswers.set(currentQuestionIndex, answer);
        }
    }

    private void displayQuestion(int index) {
        QuizQuestion question = questions.get(index);
        txtQuestion.setText(question.question);

        if (PROFICIENCY_LEVEL.equals("Novice")) {
            rbtnA.setText(question.options[0]);
            rbtnB.setText(question.options[1]);
            rbtnC.setText(question.options[2]);
            rbtnD.setText(question.options[3]);
            rbtnC.setVisibility(View.VISIBLE);
            rbtnD.setVisibility(View.VISIBLE);
        } else {
            rbtnA.setText("True");
            rbtnB.setText("False");
            rbtnC.setVisibility(View.GONE);
            rbtnD.setVisibility(View.GONE);
        }

        // Restore user's previous answer if any
        RadioGroup radioGroup = (RadioGroup) rbtnA.getParent();
        radioGroup.clearCheck();
        String userAnswer = userAnswers.get(index);
        if (!userAnswer.isEmpty()) {
            switch (userAnswer) {
                case "a": rbtnA.setChecked(true); break;
                case "b": rbtnB.setChecked(true); break;
                case "c": rbtnC.setChecked(true); break;
                case "d": rbtnD.setChecked(true); break;
            }
        }

        // Update navigation buttons
        btnPrevious.setEnabled(index > 0);
        btnNext.setText(index == questions.size() - 1 ? "Finish" : "Next");
    }

    private void calculateScore() {
        finalScore = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (userAnswers.get(i).equals(questions.get(i).answer)) {
                finalScore++;
            }
        }
    }

    // Animation methods
    private void transitionToQuiz() {
        viewIntro.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction(() -> {
                    viewIntro.setVisibility(View.GONE);
                    viewQuiz.setVisibility(View.VISIBLE);
                    viewQuiz.setAlpha(0f);
                    viewQuiz.animate()
                            .alpha(1f)
                            .setDuration(500)
                            .start();
                    displayQuestion(currentQuestionIndex);
                })
                .start();
    }

    private void transitionToFinished() {
        viewQuiz.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction(() -> {
                    viewQuiz.setVisibility(View.GONE);
                    viewFinished.setVisibility(View.VISIBLE);
                    viewFinished.setAlpha(0f);

                    // Set final score text
                    int percentage = (finalScore * 100) / questions.size();
                    String message = percentage >= 70 ? "Congratulations!" : "Try Again";
                    txtFinishedMessage.setText(message);
                    txtScore.setText(String.format("You scored %d out of %d (%d%%)",
                            finalScore, questions.size(), percentage));

                    viewFinished.animate()
                            .alpha(1f)
                            .setDuration(500)
                            .start();
                })
                .start();
    }

    private void animateToNextQuestion() {
        AnimatorSet fadeOut = createFadeAnimator(1f, 0f);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentQuestionIndex++;
                displayQuestion(currentQuestionIndex);
                createFadeAnimator(0f, 1f).start();
            }
        });
        fadeOut.start();
    }

    private void animateToPreviousQuestion() {
        AnimatorSet fadeOut = createFadeAnimator(1f, 0f);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentQuestionIndex--;
                displayQuestion(currentQuestionIndex);
                createFadeAnimator(0f, 1f).start();
            }
        });
        fadeOut.start();
    }

    private AnimatorSet createFadeAnimator(float from, float to) {
        List<Animator> animators = new ArrayList<>();

        View[] views = {txtQuestion, rbtnA, rbtnB, rbtnC, rbtnD};
        for (View view : views) {
            ObjectAnimator fade = ObjectAnimator.ofFloat(view, "alpha", from, to);
            fade.setDuration(300);
            animators.add(fade);
        }

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animators);
        return set;
    }

    private static class QuizQuestion {
        String question;
        String[] options;
        String answer;
    }
}