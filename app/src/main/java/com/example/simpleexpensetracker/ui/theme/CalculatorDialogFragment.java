package com.example.simpleexpensetracker.ui.theme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.simpleexpensetracker.R;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import java.util.Locale;

public class CalculatorDialogFragment extends DialogFragment implements View.OnClickListener {

    private TextView display;
    private CalculatorListener calculatorListener;

    public interface CalculatorListener {
        void onResult(double result);
    }

    public CalculatorDialogFragment() {
    }

    public void setCalculatorListener(CalculatorListener listener) {
        this.calculatorListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_calculator, container, false);
        display = view.findViewById(R.id.calculator_display);
        setupClickListeners(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog() != null ? getDialog().getWindow() : null;
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.btn_clear).setOnClickListener(this);
        view.findViewById(R.id.btn_paren_open).setOnClickListener(this);
        view.findViewById(R.id.btn_paren_close).setOnClickListener(this);
        view.findViewById(R.id.btn_divide).setOnClickListener(this);
        view.findViewById(R.id.btn_7).setOnClickListener(this);
        view.findViewById(R.id.btn_8).setOnClickListener(this);
        view.findViewById(R.id.btn_9).setOnClickListener(this);
        view.findViewById(R.id.btn_multiply).setOnClickListener(this);
        view.findViewById(R.id.btn_4).setOnClickListener(this);
        view.findViewById(R.id.btn_5).setOnClickListener(this);
        view.findViewById(R.id.btn_6).setOnClickListener(this);
        view.findViewById(R.id.btn_subtract).setOnClickListener(this);
        view.findViewById(R.id.btn_1).setOnClickListener(this);
        view.findViewById(R.id.btn_2).setOnClickListener(this);
        view.findViewById(R.id.btn_3).setOnClickListener(this);
        view.findViewById(R.id.btn_add).setOnClickListener(this);
        view.findViewById(R.id.btn_0).setOnClickListener(this);
        view.findViewById(R.id.btn_dot).setOnClickListener(this);
        view.findViewById(R.id.btn_equals).setOnClickListener(this);
        view.findViewById(R.id.btn_backspace).setOnClickListener(this);
        view.findViewById(R.id.done_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String currentText = display.getText().toString();
        if (currentText.equals("Error")) {
            currentText = "0";
        }

        int viewId = view.getId();

        if (viewId == R.id.btn_clear) {
            display.setText("0");
        } else if (viewId == R.id.btn_backspace) {
            if (currentText.length() > 1) {
                display.setText(currentText.substring(0, currentText.length() - 1));
            } else {
                display.setText("0");
            }
        } else if (viewId == R.id.btn_equals) {
            calculateResult(currentText);

        } else if (viewId == R.id.done_button) {
            if (calculatorListener != null) {
                try {
                    double finalResult = Double.parseDouble(display.getText().toString());
                    calculatorListener.onResult(finalResult);
                } catch (NumberFormatException e) {
                }
            }
            dismiss();

        } else {
            String buttonText = ((Button) view).getText().toString();
            String textToAppend = buttonText;

            if (viewId == R.id.btn_divide) textToAppend = "/";
            if (viewId == R.id.btn_multiply) textToAppend = "*";
            if (viewId == R.id.btn_subtract) textToAppend = "-";

            if (isOperator(currentText.substring(currentText.length() -1))) {
                if (isOperator(textToAppend)) {
                    display.setText(currentText.substring(0, currentText.length() - 1) + textToAppend);
                    return;
                }
            }

            if (currentText.equals("0") && !isOperator(textToAppend) && !textToAppend.equals(".")) {
                display.setText(textToAppend);
            } else {
                display.append(textToAppend);
            }
        }
    }

    private boolean isOperator(String text) {
        return text.equals("/") || text.equals("*") || text.equals("-") || text.equals("+");
    }

    private void calculateResult(String currentText) {
        if (currentText.isEmpty() || currentText.matches(".*[+\\-*/.]$")) return;
        try {
            String expressionText = currentText.replace('×', '*').replace('÷', '/').replace('−', '-');
            Expression expression = new ExpressionBuilder(expressionText).build();
            double result = expression.evaluate();
            displayResult(result);

        } catch (Exception e) {
            display.setText("Error");
        }
    }

    private void displayResult(double result) {
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            display.setText("Error");
            return;
        }

        if (result == (long) result) {
            display.setText(String.format(Locale.getDefault(), "%d", (long) result));
        } else {
            display.setText(String.format(Locale.getDefault(), "%.4f", result));
        }
    }
}
