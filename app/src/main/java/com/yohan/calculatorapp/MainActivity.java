package com.yohan.calculatorapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends Activity implements com.yohan.calculatorapp.OnCalculatorClickListener {

    // TextViews
    TextView calculatorText;
    TextView answerText;


    ArrayList<BigDecimal> inputNumbers = new ArrayList<>();
    ArrayList<com.yohan.calculatorapp.Operator> inputOperators = new ArrayList<>();


    HashMap<String, com.yohan.calculatorapp.Operator> operatorHashMap = new HashMap<>();
    HashMap<com.yohan.calculatorapp.Operator, String> reverseOperatorHashMap = new HashMap();

    private BigDecimal answer = null;
    private BigDecimal currentNumber = new BigDecimal(0);
    private int currentNumberIndex = 0;
    private int decimalPoints = 0;
    private boolean answered = false;
    private boolean decimalStatus = false;
    private boolean negativeStatus = false;
    private boolean numberStatus = true;
    private Toast toast = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting up TextViews for calculator display
        calculatorText = findViewById(R.id.calculator_text);
        calculatorText.setText(currentNumber.toString());
        inputNumbers.add(currentNumber);
        answerText = findViewById(R.id.answer_text);

        initOperatorHashMaps();
        initToast();
    }

    private void initOperatorHashMaps() {
        operatorHashMap.put(getString(R.string.plus), com.yohan.calculatorapp.Operator.PLUS);
        operatorHashMap.put(getString(R.string.minus), com.yohan.calculatorapp.Operator.MINUS);
        operatorHashMap.put(getString(R.string.multiply), com.yohan.calculatorapp.Operator.MULTIPLY);
        operatorHashMap.put(getString(R.string.divide), com.yohan.calculatorapp.Operator.DIVIDE);
        operatorHashMap.put(getString(R.string.modulus), com.yohan.calculatorapp.Operator.MODULUS);
        for (String s: operatorHashMap.keySet()) {
            reverseOperatorHashMap.put(operatorHashMap.get(s), s);
        }
    }


    private void initToast() {
        Context context = getApplicationContext();
        CharSequence message = "Invalid Syntax";
        int duration = Toast.LENGTH_SHORT;
        toast = Toast.makeText(context, message, duration);
    }


    @Override
    public void onNumberClick(View view) {
        if (answered) {
            clear();
            answered = false;
        }
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        Button button = findViewById(view.getId());
        BigDecimal update = new BigDecimal(button.getText().toString());
        currentNumber = currentNumber.multiply(new BigDecimal(10));
        currentNumber = currentNumber.add(update);
        if (numberStatus) {
            if (!negativeStatus) {
                inputNumbers.set(currentNumberIndex, currentNumber.scaleByPowerOfTen(-decimalPoints));
            } else {
                inputNumbers.set(currentNumberIndex, currentNumber.scaleByPowerOfTen(-decimalPoints).negate());
            }
        } else {
            numberStatus = true;
            if (!negativeStatus) {
                inputNumbers.add(currentNumberIndex, currentNumber.scaleByPowerOfTen(-decimalPoints));
            } else {
                inputNumbers.add(currentNumberIndex, currentNumber.scaleByPowerOfTen(-decimalPoints).negate());
            }
        }
        if (decimalStatus) decimalPoints++;
        updateText();
    }


    @Override
    public void onOperatorClick(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        switch (view.getId()) {
            case R.id.clear_button:
                clear();
                updateText();
                break;
            case R.id.decimal_button:
                if (answered) answered = false;
                if (!decimalStatus) {
                    decimalStatus = true;
                    decimalPoints += 1;
                }
                break;
            case R.id.equals_button:
                if (inputNumbers.size() > inputOperators.size()) {
                    try {
                        calculate();
                        BigDecimal tempAnswer = answer;
                        updateText();
                        clear();
                        answer = tempAnswer;
                        answered = true;
                        inputNumbers.set(currentNumberIndex, answer);
                    } catch (ArithmeticException e) {
                        invalidSyntax();
                    }
                } else {
                    invalidSyntax();
                }
                break;
            case R.id.plus_minus_button:
                if (answered) answered = false;
                negativeStatus = !negativeStatus;
                inputNumbers.set(currentNumberIndex, inputNumbers.get(currentNumberIndex).negate());
                updateText();
                break;
            default:
                if (answered) answered = false;
                TextView operatorView = findViewById(view.getId());
                String operator = operatorView.getText().toString();
                if (numberStatus) {
                    decimalStatus = false;
                    negativeStatus = false;
                    numberStatus = false;
                    currentNumber = new BigDecimal(0);
                    currentNumberIndex++;
                    decimalPoints = 0;
                    inputOperators.add(operatorHashMap.get(operator));
                } else {
                    inputOperators.set(inputOperators.size() - 1, operatorHashMap.get(operator));
                }
                updateText();
        }
    }


    private void updateText() {
        StringBuilder updatedText = new StringBuilder();
        updatedText.append(inputNumbers.get(0));
        for (int i = 0; i < inputOperators.size(); i++) {
            updatedText.append(reverseOperatorHashMap.get(inputOperators.get(i)));
            if (inputNumbers.size() > i + 1) updatedText.append(inputNumbers.get(i + 1));
        }
        calculatorText.setText(String.valueOf(updatedText));
        if (answered) {
            answerText.setText(answer.toString());
        } else {
            answerText.setText("");
        }
    }


    private void calculate() {
        answer = inputNumbers.get(0);
        for (int i = 0; i < inputOperators.size(); i++) {
            switch (inputOperators.get(i)) {
                case PLUS:
                    answer = answer.add(inputNumbers.get(i + 1));
                    break;
                case MINUS:
                    answer = answer.subtract(inputNumbers.get(i + 1));
                    break;
                case MULTIPLY:
                    answer = answer.multiply(inputNumbers.get(i + 1));
                    break;
                case DIVIDE:
                    answer = answer.divide(inputNumbers.get(i + 1), 100, RoundingMode.HALF_EVEN);
                    break;
                case MODULUS:
                    answer = answer.remainder(inputNumbers.get(i+ 1));
                    break;
            }
        }
        answered = true;
        if (answer.toString().replace(".", "").length() > 10) {
            int scale = 10 - answer.precision() + answer.scale();
            answer = answer.setScale(scale, RoundingMode.HALF_EVEN).stripTrailingZeros();
        }
    }


    private void clear() {
        currentNumber = new BigDecimal(0);
        currentNumberIndex = 0;
        decimalPoints = 0;
        answer = null;

        inputNumbers.clear();
        inputOperators.clear();

        inputNumbers.add(currentNumber);

        answered = false;
        decimalStatus = false;
        negativeStatus = false;
        numberStatus = true;
    }


    private void invalidSyntax() {
        toast.cancel();
        toast.show();
    }
}
