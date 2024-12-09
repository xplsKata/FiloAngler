package com.example.filoangler.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

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

    private LoginManager loginManager;
    private AuthManager authManager;

    private String PROFICIENCY_LEVEL;
    private boolean isProficientUser = false;

    private List<QuizQuestion> questions;
    private int currentQuestionIndex = 0;
    private List<String> userAnswers;
    private int finalScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quizzes);

        loginManager = new LoginManager(this);
        authManager = new AuthManager();

        initializeViews();
        getUserProficiency();
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
            String jsonString = Utils.loadJSONFromAsset(this, "quizzes.json");
            if (jsonString == null) {
                throw new IllegalStateException("Could not load quiz.json file");
            }

            Gson gson = new Gson();
            QuizData quizData = gson.fromJson(jsonString, QuizData.class);

            // Get questions based on proficiency level
            if (PROFICIENCY_LEVEL.equals("Aspiring")) {
                questions = new ArrayList<>(quizData.Quiz.Aspiring);
            } else {
                questions = new ArrayList<>(quizData.Quiz.Novice);
            }

            userAnswers = new ArrayList<>(Collections.nCopies(questions.size(), ""));
            Collections.shuffle(questions);

            runOnUiThread(() -> {
                btnProceed.setEnabled(true);

                // Show warning dialog for Proficient users
                if (isProficientUser) {
                    new AlertDialog.Builder(this)
                            .setTitle("Notice")
                            .setMessage("As a Proficient angler, taking this quiz will not increase your proficiency status.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();
                }
            });

        } catch (Exception e) {
            Log.e("Quizzes", "Error loading quiz data", e);
            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("Error Loading Quiz")
                        .setMessage("Could not load quiz questions. Please try again.")
                        .setPositiveButton("OK", (dialog, id) -> finish())
                        .show();
                btnProceed.setEnabled(false);
            });
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
            rbtnA.setText(question.Options.a);
            rbtnB.setText(question.Options.b);
            rbtnC.setText(question.Options.c);
            rbtnD.setText(question.Options.d);
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

        // Check if user passed and update proficiency if needed
        int percentage = (finalScore * 100) / questions.size();
        if (percentage >= 70 && !isProficientUser) {
            updateProficiency();
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

    //Firebase
    private void getUserProficiency() {
        authManager.GetDb().getReference().child("Users")
                .child(loginManager.GetCurrentUser().getUid())
                .child("Account Details")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PROFICIENCY_LEVEL = snapshot.child("AnglerStatus").getValue(String.class);
                        isProficientUser = "Proficient".equals(PROFICIENCY_LEVEL);
                        loadQuizData();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void updateProficiency() {
        String nextLevel;

        // Determine next level based on current PROFICIENCY_LEVEL
        if (PROFICIENCY_LEVEL.equals("Aspiring")) {
            nextLevel = "Novice";
        } else if (PROFICIENCY_LEVEL.equals("Novice")) {
            nextLevel = "Proficient";
        } else {
            return; // No update needed for Proficient users
        }

        // Update Firebase database
        authManager.GetDb().getReference().child("Users")
                .child(loginManager.GetCurrentUser().getUid())
                .child("Account Details")
                .child("AnglerStatus")
                .setValue(nextLevel)
                .addOnSuccessListener(aVoid -> {
                    // Show success message to user
                    new AlertDialog.Builder(QuizzesActivity.this)
                            .setTitle("Congratulations!")
                            .setMessage("Your angler status has been upgraded to " + nextLevel)
                            .setPositiveButton("OK", null)
                            .show();

                    // Update local variable
                    PROFICIENCY_LEVEL = nextLevel;
                    isProficientUser = "Proficient".equals(nextLevel);
                })
                .addOnFailureListener(e -> {
                    // Show error message if update fails
                    new AlertDialog.Builder(QuizzesActivity.this)
                            .setTitle("Error")
                            .setMessage("Failed to update angler status. Please try again.")
                            .setPositiveButton("OK", null)
                            .show();
                });
    }

    //For json
    private static class QuizQuestion {
        String question;
        Options Options;
        String answer;
    }

    private static class QuizData {
        Quiz Quiz;
    }

    private static class Quiz {
        String title;
        List<QuizQuestion> Novice;
        List<QuizQuestion> Aspiring;
    }

    private static class Options {
        String a;
        String b;
        String c;
        String d;
    }
}