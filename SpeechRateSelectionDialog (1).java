package my.mmu.rssnewsreader.ui.feedsetting;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import my.mmu.rssnewsreader.R;
import my.mmu.rssnewsreader.data.feed.FeedRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SpeechRateSelectionDialog extends AppCompatDialogFragment {

    public static final String TAG = "SpeechRateDialog";

    @Inject
    FeedRepository feedRepository;

    private RadioGroup radioGroup;
    private long feedId;
    private float ttsSpeechRate;
    private FeedSettingDialogListener listener;

    public SpeechRateSelectionDialog(FeedSettingDialogListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            feedId = getArguments().getLong("feedId");
            ttsSpeechRate = getArguments().getFloat("ttsSpeechRate");
        }

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_speechrateselection, null);
        radioGroup = view.findViewById(R.id.speechRateRadioGroup);

        if (ttsSpeechRate == 0) {
            RadioButton radioButton = view.findViewById(R.id.speechRateRadioButton0);
            radioButton.setChecked(true);
        } else if (ttsSpeechRate == 0.25f) {
            RadioButton radioButton = view.findViewById(R.id.speechRateRadioButton1);
            radioButton.setChecked(true);
        } else if (ttsSpeechRate == 0.5f) {
            RadioButton radioButton;
            radioButton = view.findViewById(R.id.speechRateRadioButton2);
            radioButton.setChecked(true);
        } else if (ttsSpeechRate == 1.0f) {
            RadioButton radioButton;
            radioButton = view.findViewById(R.id.speechRateRadioButton3);
            radioButton.setChecked(true);
        } else if (ttsSpeechRate == 1.25f) {
            RadioButton radioButton;
            radioButton = view.findViewById(R.id.speechRateRadioButton4);
            radioButton.setChecked(true);
        } else if (ttsSpeechRate == 1.5f) {
            RadioButton radioButton;
            radioButton = view.findViewById(R.id.speechRateRadioButton5);
            radioButton.setChecked(true);
        } else if (ttsSpeechRate == 1.75f) {
            RadioButton radioButton;
            radioButton = view.findViewById(R.id.speechRateRadioButton6);
            radioButton.setChecked(true);
        } else if (ttsSpeechRate == 2.0f) {
            RadioButton radioButton;
            radioButton = view.findViewById(R.id.speechRateRadioButton7);
            radioButton.setChecked(true);
        } else if (ttsSpeechRate == 3.0f) {
            RadioButton radioButton;
            radioButton = view.findViewById(R.id.speechRateRadioButton8);
            radioButton.setChecked(true);
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(view)
                .setIcon(R.drawable.ic_gauge)
                .setTitle("Select speech rate")
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int radioId = radioGroup.getCheckedRadioButtonId();
                        float speechRate = 0;
                        switch (radioId) {
                            case R.id.speechRateRadioButton1:
                                speechRate = 0.25f;
                                break;
                            case R.id.speechRateRadioButton2:
                                speechRate = 0.5f;
                                break;
                            case R.id.speechRateRadioButton3:
                                speechRate = 1.0f;
                                break;
                            case R.id.speechRateRadioButton4:
                                speechRate = 1.25f;
                                break;
                            case R.id.speechRateRadioButton5:
                                speechRate = 1.5f;
                                break;
                            case R.id.speechRateRadioButton6:
                                speechRate = 1.75f;
                                break;
                            case R.id.speechRateRadioButton7:
                                speechRate = 2.0f;
                                break;
                            case R.id.speechRateRadioButton8:
                                speechRate = 3.0f;
                                break;
                        }
                        feedRepository.updateTtsSpeechRateById(feedId, speechRate);
                        listener.modifySpeechRate(speechRate);
                    }
                });

        return builder.create();
    }
}
