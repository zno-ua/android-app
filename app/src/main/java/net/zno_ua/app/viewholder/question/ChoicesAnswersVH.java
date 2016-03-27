package net.zno_ua.app.viewholder.question;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.model.question.ChoicesAnswers;

import static java.lang.String.format;

/**
 * @author vojkovladimir.
 */
public class ChoicesAnswersVH extends QuestionItemVH<ChoicesAnswers>
        implements CompoundButton.OnCheckedChangeListener {
    private static final String CHARACTER_FORMAT = "%c";
    private static final int KEY_NUMBER = R.id.number;
    private static final int KEY_LETTER = R.id.letter;

    private final int mColorRed;
    private final int mColorGreen;

    private final TableLayout mTableLayout;
    private AppCompatRadioButton[][] mRadioButtons;
    @Nullable
    private final OnAnswerChoiceSelectListener mChoiceSelectListener;

    public ChoicesAnswersVH(LayoutInflater layoutInflater, ViewGroup parent,
                            @Nullable OnAnswerChoiceSelectListener listener) {
        super(layoutInflater.inflate(R.layout.view_choices_answers_item, parent, false));
        mTableLayout = (TableLayout) itemView.findViewById(R.id.table);
        mChoiceSelectListener = listener;
        Context context = itemView.getContext();
        mColorRed = ContextCompat.getColor(context, R.color.red_500);
        mColorGreen = ContextCompat.getColor(context, R.color.green_500);
    }

    public void bind(@NonNull ChoicesAnswers item) {
        generateAnswersTable(item);
        int answer;
        int correct;
        for (int number = 0; number < item.getCorrectAnswer().length(); number++) {
            answer = item.getAnswer().charAt(number) - '0' - 1;
            if (item.isEditable()) {
                setChecked(number, answer);
            } else {
                correct = item.getCorrectAnswer().charAt(number) - '0' - 1;
                setAnswers(number, correct, answer);
            }
        }
    }

    private void generateAnswersTable(@NonNull ChoicesAnswers item) {
        mTableLayout.removeAllViews();
        final Context context = itemView.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);

        TableRow tableRow;
        TextView tcCharacter;
        AppCompatRadioButton radioButton;
        View view;
        char character;
        mRadioButtons = new AppCompatRadioButton[item.getNumbersCount()][];
        for (int i = 0; i < item.getNumbersCount() + 1; i++) {
            tableRow = new TableRow(context);
            if (i > 0) {
                mRadioButtons[i - 1] = new AppCompatRadioButton[item.getLettersCount()];
            }
            for (int j = 0; j < item.getLettersCount() + 1; j++) {
                if (i == 0) {
                    if (j == 0) {
                        view = new Space(context);
                    } else {
                        character = (char) (item.getFirstLetter() + j - 1);
                        tcCharacter = inflateCharacter(layoutInflater, tableRow);
                        tcCharacter.setText(format(CHARACTER_FORMAT, character));
                        view = tcCharacter;
                    }
                } else {
                    if (j == 0) {
                        tcCharacter = inflateCharacter(layoutInflater, tableRow);
                        tcCharacter.setText(String.valueOf(i));
                        view = tcCharacter;
                    } else {
                        radioButton = inflateRadioButton(layoutInflater, tableRow);
                        radioButton.setClickable(item.isEditable());
                        if (item.isEditable()) {
                            radioButton.setOnCheckedChangeListener(this);
                        }
                        radioButton.setTag(KEY_NUMBER, i - 1);
                        radioButton.setTag(KEY_LETTER, j - 1);
                        mRadioButtons[i - 1][j - 1] = radioButton;
                        view = radioButton;
                    }
                }
                tableRow.addView(view);
            }
            mTableLayout.addView(tableRow);
        }
    }

    private static AppCompatRadioButton inflateRadioButton(LayoutInflater inflater,
                                                           ViewGroup parent) {
        return (AppCompatRadioButton) inflater.inflate(R.layout.view_radio_button, parent, false);
    }

    private static TextView inflateCharacter(LayoutInflater inflater, ViewGroup parent) {
        return (TextView) inflater.inflate(R.layout.view_character, parent, false);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int number = (int) buttonView.getTag(KEY_NUMBER);
        int letter = (int) buttonView.getTag(KEY_LETTER);
        if (isChecked) {
            setChecked(number, letter);
            if (mChoiceSelectListener != null) {
                mChoiceSelectListener.onAnswerChoiceSelected(getAdapterPosition(), number, letter);
            }
        }
    }

    private void setChecked(int number, int letter) {
        if (letter != -1) {
            for (int i = 0; i < mRadioButtons.length; i++) {
                mRadioButtons[i][letter].setChecked(i == number);
            }
        }
        for (int i = 0; i < mRadioButtons[number].length; i++) {
            mRadioButtons[number][i].setChecked(i == letter);
        }
    }

    private void setAnswers(int number, int correct, int answer) {
        AppCompatRadioButton radioButton;
        if (answer == -1) {
            for (int i = 0; i < mRadioButtons[number].length; i++) {
                radioButton = mRadioButtons[number][i];
                if (i == correct) {
                    radioButton.setSupportButtonTintList(ColorStateList.valueOf(mColorGreen));
                    radioButton.setChecked(true);
                } else {
                    radioButton.setSupportButtonTintList(ColorStateList.valueOf(mColorRed));
                }
            }
        } else if (answer == correct) {
            radioButton = mRadioButtons[number][correct];
            radioButton.setSupportButtonTintList(ColorStateList.valueOf(mColorGreen));
            radioButton.setChecked(true);
        } else {
            radioButton = mRadioButtons[number][correct];
            radioButton.setSupportButtonTintList(ColorStateList.valueOf(mColorGreen));
            radioButton.setChecked(true);

            radioButton = mRadioButtons[number][answer];
            radioButton.setSupportButtonTintList(ColorStateList.valueOf(mColorRed));
            radioButton.setChecked(true);
        }
    }

    public interface OnAnswerChoiceSelectListener {
        void onAnswerChoiceSelected(int position, int number, int letter);
    }
}
