package my.mmu.rssnewsreader.ui.feedsetting;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.lifecycle.ViewModelProvider;

import my.mmu.rssnewsreader.R;
import my.mmu.rssnewsreader.ui.allentries.AllEntriesViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FeedSettingDialog extends AppCompatDialogFragment implements FeedSettingDialogListener {

    public static String TAG = "SettingFeedDialog";

    private FeedSettingViewModel feedSettingViewModel;

    private long feedId;
    private String title;
    private String description;
    private String link;
    private String language;
    private String languageDisplayName;
    private float speechRateDisplay;
    private TextInputEditText titleEditText;
    private TextInputEditText descriptionEditText;
    private MaterialAutoCompleteTextView languageTextView;
    private MaterialAutoCompleteTextView speechRateTextView;
    private LanguageSelectionDialog dialog;
    private CircularProgressIndicator loading;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_feedsetting, null);
        feedSettingViewModel = new ViewModelProvider(this).get(FeedSettingViewModel.class);

        if (getArguments() != null) {
            feedId = getArguments().getLong("feedId");
            title = getArguments().getString("title");
            link = getArguments().getString("link");
            description = getArguments().getString("description");
            language = getArguments().getString("language");
            speechRateDisplay = getArguments().getFloat("ttsSpeechRate");
            if (language != null) {
                try {
                    Locale locale = new Locale(language);
                    languageDisplayName = locale.getDisplayName();
                } catch (Exception e) {
                    Log.d(TAG, "Failed to create Locale " + e.getMessage());
                    language = null;
                    languageDisplayName = LanguageSelectionDialog.languageIdentifierTAG;
                }
            } else {
                languageDisplayName = LanguageSelectionDialog.languageIdentifierTAG;
            }
        }

        loading = view.findViewById(R.id.loadingTts);
        titleEditText = view.findViewById(R.id.feedTitleEditText);
        descriptionEditText = view.findViewById(R.id.feedDescriptionEditText);
        TextInputEditText urlEditText = view.findViewById(R.id.feedUrlEditText);
        languageTextView = view.findViewById(R.id.languageTextView);
        speechRateTextView = view.findViewById(R.id.speechRateTextView);

        urlEditText.setText(link);
        titleEditText.setText(title);
        descriptionEditText.setText(description);

        String speechRateDisplayName;
        if (speechRateDisplay == 0) {
            speechRateDisplayName = "Use system's settings";
        } else{
            speechRateDisplayName = Float.toString(speechRateDisplay) + "x";
        }
        speechRateTextView.setText(speechRateDisplayName);
        speechRateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SpeechRateSelectionDialog speechRateSelectionDialog = new SpeechRateSelectionDialog(FeedSettingDialog.this);
                Bundle args = new Bundle();
                args.putLong("feedId", feedId);
                args.putFloat("ttsSpeechRate", speechRateDisplay);
                speechRateSelectionDialog.setArguments(args);
                speechRateSelectionDialog.show(getChildFragmentManager(), SpeechRateSelectionDialog.TAG);
            }
        });

        languageTextView.setText(languageDisplayName);
        languageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.setVisibility(View.VISIBLE);
                dialog = new LanguageSelectionDialog(FeedSettingDialog.this, requireContext());
                Bundle args = new Bundle();
                args.putString("language", language);
                dialog.setArguments(args);
            }
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(view)
                .setIcon(R.drawable.ic_setting)
                .setTitle("Feed settings")
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String updatedTitle = Objects.requireNonNull(titleEditText.getText()).toString();
                        String updatedDesc = Objects.requireNonNull(descriptionEditText.getText()).toString();
                        feedSettingViewModel.updateTitleDescLanguage(updatedTitle, updatedDesc, language, link);
                        Toast.makeText(requireContext(), "Update on speech rate will be reflected in the next article", Toast.LENGTH_SHORT).show();
                    }
                });

        return builder.create();
    }

    @Override
    public void modifyLanguage(String newLanguage) {
        if (newLanguage.equals(LanguageSelectionDialog.languageIdentifierTAG)) {
            language = null;
        } else {
            language = newLanguage;
        }
        if (language == null) {
            languageTextView.setText(newLanguage);
        } else {
            Locale locale = new Locale(newLanguage);
            languageTextView.setText(locale.getDisplayName());
        }
    }

    @Override
    public void modifySpeechRate(float ttsSpeechRate) {
        String speechRate;
        if (ttsSpeechRate == 0) {
            speechRate = "Use system's setting";
        } else {
            speechRate = ttsSpeechRate + "x";
        }
        speechRateTextView.setText(speechRate);
        speechRateDisplay = ttsSpeechRate;
    }

    @Override
    public void showDialog() {
        loading.setVisibility(View.INVISIBLE);
        dialog.show(getChildFragmentManager(), LanguageSelectionDialog.TAG);
    }
}
