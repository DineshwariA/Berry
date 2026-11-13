//In android emulator
package com.berry.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String BASE_URL = "http://10.0.2.2:8080";

    private final int cream = Color.rgb(255, 247, 242);
    private final int red = Color.rgb(215, 25, 63);
    private final int dark = Color.rgb(48, 37, 33);
    private final int muted = Color.rgb(136, 118, 111);

    private JSONArray cachedRecords = new JSONArray();
    private String currentFilter = "ALL";
    private LinearLayout recordsContainer;
    private TextView recordsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDashboard();
    }

    private void showDashboard() {
        setContentView(R.layout.activity_main);

        findViewById(R.id.addItemButton)
                .setOnClickListener(v -> showAddItemForm());

        findViewById(R.id.viewItemsButton)
                .setOnClickListener(v -> showRecords());

        loadDashboard();
    }

    private void loadDashboard() {
        new Thread(() -> {
            try {
                JSONObject data = new JSONObject(
                        getFromBackend("/api/dashboard"));

                runOnUiThread(() -> {
                    setNumber(R.id.totalItems,
                            data.optInt("totalItems"));
                    setNumber(R.id.lentItems,
                            data.optInt("lentItems"));
                    setNumber(R.id.borrowedItems,
                            data.optInt("borrowedItems"));
                    setNumber(R.id.overdueItems,
                            data.optInt("overdueItems"));
                });

            } catch (Exception e) {
                e.printStackTrace();

                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Couldn't load dashboard: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void setNumber(int id, int number) {
        TextView view = findViewById(id);

        if (view != null) {
            view.setText(String.valueOf(number));
        }
    }

    private void showRecords() {
        currentFilter = "ALL";

        LinearLayout page = createPage();

        addTitle(page, "Your little collection");
        addText(page, "Everything you've lent and borrowed.",
                muted, 14);

        recordsCount = addText(page, "Loading records...", red, 13);

        RadioGroup filters = new RadioGroup(this);
        filters.setOrientation(RadioGroup.HORIZONTAL);
        filters.setGravity(Gravity.CENTER);
        filters.setPadding(0, dp(12), 0, dp(12));

        RadioButton all = makeRadio("All", "ALL");
        RadioButton lent = makeRadio("Lent", "LENT");
        RadioButton borrowed = makeRadio("Borrowed", "BORROWED");

        filters.addView(all);
        filters.addView(lent);
        filters.addView(borrowed);
        page.addView(filters);

        all.setChecked(true);

        recordsContainer = new LinearLayout(this);
        recordsContainer.setOrientation(LinearLayout.VERTICAL);
        page.addView(recordsContainer);

        Button back = createButton("← Back to dashboard", false);

        LinearLayout.LayoutParams backParams =
                new LinearLayout.LayoutParams(-1, dp(52));
        backParams.topMargin = dp(18);
        page.addView(back, backParams);

        back.setOnClickListener(v -> showDashboard());

        filters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == all.getId()) {
                currentFilter = "ALL";
            } else if (checkedId == lent.getId()) {
                currentFilter = "LENT";
            } else if (checkedId == borrowed.getId()) {
                currentFilter = "BORROWED";
            }

            renderRecords();
        });

        loadRecords();
    }

    private RadioButton makeRadio(String label, String tag) {
        RadioButton button = new RadioButton(this);

        button.setId(View.generateViewId());
        button.setText(label);
        button.setTag(tag);
        button.setTextColor(red);
        button.setTextSize(13);
        button.setPadding(dp(5), dp(5), dp(5), dp(5));

        return button;
    }

    private void loadRecords() {
        new Thread(() -> {
            try {
                JSONArray records =
                        new JSONArray(getFromBackend("/api/records"));

                runOnUiThread(() -> {
                    cachedRecords = records;
                    renderRecords();
                });

            } catch (Exception e) {
                e.printStackTrace();

                runOnUiThread(() -> {
                    if (recordsContainer != null) {
                        recordsContainer.removeAllViews();

                        addText(recordsContainer,
                                "Couldn't load records: " + e.getMessage(),
                                red, 14);
                    }
                });
            }
        }).start();
    }

    private void renderRecords() {
        if (recordsContainer == null) {
            return;
        }

        recordsContainer.removeAllViews();

        int shown = 0;

        for (int i = 0; i < cachedRecords.length(); i++) {
            JSONObject record = cachedRecords.optJSONObject(i);

            if (record == null) {
                continue;
            }

            String direction = record.optString("direction", "BORROWED")
                    .toUpperCase(Locale.ROOT);

            if (!currentFilter.equals("ALL")
                    && !currentFilter.equals(direction)) {
                continue;
            }

            addRecordCard(recordsContainer, record);
            shown++;
        }

        if (recordsCount != null) {
            recordsCount.setText(shown + (shown == 1
                    ? " record shown"
                    : " records shown"));
        }

        if (shown == 0) {
            TextView empty = addText(recordsContainer,
                    currentFilter.equals("ALL")
                            ? "No records yet. Add your first little item! 🍓"
                            : "No " + currentFilter.toLowerCase(Locale.ROOT)
                            + " items here yet.",
                    muted, 15);

            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(10), dp(30), dp(10), dp(30));
        }
    }

    private void addRecordCard(LinearLayout parent, JSONObject record) {
        int id = record.optInt("id");
        String item = record.optString("itemName", "Unnamed item");
        String person = record.optString("personName", "Unknown");
        String borrowDate = record.optString("borrowDate", "");
        String dueDate = record.optString("dueDate", "");
        String status = record.optString("status", "Borrowed");

        String direction = record.optString("direction", "BORROWED")
                .toUpperCase(Locale.ROOT);

        boolean lent = direction.equals("LENT");
        boolean outstanding = status.equalsIgnoreCase("Borrowed");

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(17), dp(15), dp(17), dp(15));
        card.setBackground(makeCardBackground(Color.WHITE));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(-1, -2);
        cardParams.bottomMargin = dp(12);
        parent.addView(card, cardParams);

        TextView directionLabel = addText(card,
                lent ? "↑  LENT" : "↓  BORROWED",
                red, 11);
        directionLabel.setTypeface(null, Typeface.BOLD);

        TextView itemTitle = addText(card, item, dark, 20);
        itemTitle.setTypeface(null, Typeface.BOLD);
        itemTitle.setPadding(0, dp(5), 0, dp(7));

        addText(card,
                (lent ? "Given to: " : "Borrowed from: ") + person,
                muted, 14);

        if (!borrowDate.isEmpty()) {
            addText(card, "Borrow date: " + borrowDate, muted, 12);
        }

        if (!dueDate.isEmpty()) {
            addText(card, "Due date: " + dueDate, muted, 12);
        }

        TextView statusText = addText(card,
                outstanding ? "● Awaiting return" : "✓ Returned",
                outstanding ? red : Color.rgb(65, 120, 76), 12);
        statusText.setPadding(0, dp(8), 0, dp(8));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        card.addView(actions);

        if (outstanding) {
            Button returnedButton = createButton("Mark returned", true);

            LinearLayout.LayoutParams actionParams =
                    new LinearLayout.LayoutParams(0, dp(44), 1);
            actionParams.setMarginEnd(dp(5));
            actions.addView(returnedButton, actionParams);

            returnedButton.setOnClickListener(v ->
                    confirmReturn(id, item));
        }

        Button deleteButton = createButton("Delete", false);

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(
                        outstanding ? 0 : -1, dp(44),
                        outstanding ? 1 : 0);

        if (outstanding) {
            deleteParams.setMarginStart(dp(5));
        }

        actions.addView(deleteButton, deleteParams);

        deleteButton.setOnClickListener(v ->
                confirmDelete(id, item));
    }

    private void confirmDelete(int id, String item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete this record?")
                .setMessage("Remove \"" + item
                        + "\" from Berry? This cannot be undone.")
                .setNegativeButton("Keep it",
                        (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete",
                        (dialog, which) -> deleteRecord(id))
                .show();
    }

    private void deleteRecord(int id) {
        new Thread(() -> {
            try {
                postToBackend("/api/delete-item?id=" + id, "");

                runOnUiThread(() -> {
                    Toast.makeText(this, "Record deleted 🍓",
                            Toast.LENGTH_SHORT).show();
                    loadRecords();
                });

            } catch (Exception e) {
                e.printStackTrace();

                runOnUiThread(() -> Toast.makeText(this,
                        "Delete error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void confirmReturn(int id, String item) {
        new AlertDialog.Builder(this)
                .setTitle("Mark as returned?")
                .setMessage("Has \"" + item + "\" been returned?")
                .setNegativeButton("Not yet",
                        (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes, returned",
                        (dialog, which) -> markReturned(id))
                .show();
    }

    private void markReturned(int id) {
        new Thread(() -> {
            try {
                postToBackend("/api/return-item?id=" + id, "");

                runOnUiThread(() -> {
                    Toast.makeText(this, "Updated successfully!",
                            Toast.LENGTH_SHORT).show();
                    loadRecords();
                });

            } catch (Exception e) {
                e.printStackTrace();

                runOnUiThread(() -> Toast.makeText(this,
                        "Update error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void showAddItemForm() {
        LinearLayout page = createPage();

        addTitle(page, "Add a little something");
        addText(page, "Keep track of what you lend and borrow.",
                muted, 14);

        addText(page, "What happened?", dark, 15);

        RadioGroup directionGroup = new RadioGroup(this);
        directionGroup.setOrientation(RadioGroup.VERTICAL);
        directionGroup.setPadding(0, dp(5), 0, dp(10));

        RadioButton borrowedOption = makeRadio(
                "I borrowed it — someone gave it to me",
                "BORROWED");

        RadioButton lentOption = makeRadio(
                "I lent it — I gave it to someone",
                "LENT");

        directionGroup.addView(borrowedOption);
        directionGroup.addView(lentOption);
        borrowedOption.setChecked(true);
        page.addView(directionGroup);

        EditText itemInput = createInput(page, "Item name");
        EditText personInput = createInput(page, "Person's name");
        EditText borrowDateInput =
                createInput(page, "Borrow date (YYYY-MM-DD)");
        EditText dueDateInput =
                createInput(page, "Due date (YYYY-MM-DD)");

        borrowDateInput.setText(new SimpleDateFormat(
                "yyyy-MM-dd", Locale.US).format(new Date()));

        Button saveButton = createButton("Save to Berry 🍓", true);

        LinearLayout.LayoutParams saveParams =
                new LinearLayout.LayoutParams(-1, dp(54));
        saveParams.topMargin = dp(18);
        page.addView(saveButton, saveParams);

        Button backButton = createButton("← Back", false);

        LinearLayout.LayoutParams backParams =
                new LinearLayout.LayoutParams(-1, dp(50));
        backParams.topMargin = dp(8);
        page.addView(backButton, backParams);

        backButton.setOnClickListener(v -> showDashboard());

        saveButton.setOnClickListener(v -> {
            String item = itemInput.getText().toString().trim();
            String person = personInput.getText().toString().trim();
            String borrowDate =
                    borrowDateInput.getText().toString().trim();
            String dueDate =
                    dueDateInput.getText().toString().trim();

            int checkedId = directionGroup.getCheckedRadioButtonId();

            RadioButton selected = checkedId == -1
                    ? null : directionGroup.findViewById(checkedId);

            String direction = selected == null
                    ? "BORROWED" : selected.getTag().toString();

            if (item.isEmpty() || person.isEmpty()
                    || borrowDate.isEmpty() || dueDate.isEmpty()) {
                Toast.makeText(this, "Please fill in every field.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidDate(borrowDate) || !isValidDate(dueDate)) {
                Toast.makeText(this,
                        "Use dates in YYYY-MM-DD format.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (dueDate.compareTo(borrowDate) < 0) {
                Toast.makeText(this,
                        "The due date cannot be before the borrow date.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            saveButton.setEnabled(false);

            new Thread(() -> {
                try {
                    JSONObject body = new JSONObject();
                    body.put("itemName", item);
                    body.put("personName", person);
                    body.put("borrowDate", borrowDate);
                    body.put("dueDate", dueDate);
                    body.put("direction", direction);

                    postToBackend("/api/add-item", body.toString());

                    runOnUiThread(() -> {
                        saveButton.setEnabled(true);
                        Toast.makeText(this,
                                "Added to Berry! 🍓",
                                Toast.LENGTH_SHORT).show();

                        showDashboard();
                    });

                } catch (Exception e) {
                    e.printStackTrace();

                    runOnUiThread(() -> {
                        saveButton.setEnabled(true);
                        Toast.makeText(this,
                                "Save error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });
    }

    private LinearLayout createPage() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(cream);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(24), dp(22), dp(24));

        scroll.addView(page);
        setContentView(scroll);

        return page;
    }

    private void addTitle(LinearLayout parent, String title) {
        TextView text = new TextView(this);
        text.setText(title);
        text.setTextColor(dark);
        text.setTextSize(29);
        text.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(8);
        parent.addView(text, params);
    }

    private TextView addText(LinearLayout parent, String value,
                             int color, int size) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextColor(color);
        text.setTextSize(size);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(7);
        parent.addView(text, params);

        return text;
    }

    private Button createButton(String label, boolean primary) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(13);
        button.setTextColor(primary ? Color.WHITE : red);

        button.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        primary ? red : Color.rgb(240, 223, 215)));

        return button;
    }

    private EditText createInput(LinearLayout parent, String hint) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(15);
        input.setTextColor(dark);
        input.setHint(hint);
        input.setHintTextColor(muted);
        input.setPadding(dp(14), dp(8), dp(14), dp(8));
        input.setBackground(makeCardBackground(Color.WHITE));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, dp(54));
        params.topMargin = dp(8);
        parent.addView(input, params);

        return input;
    }

    private GradientDrawable makeCardBackground(int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(16));
        shape.setStroke(dp(1), Color.rgb(240, 229, 222));
        return shape;
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private boolean isValidDate(String value) {
        SimpleDateFormat format =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);

        try {
            Date date = format.parse(value);
            return date != null && format.format(date).equals(value);
        } catch (Exception e) {
            return false;
        }
    }

    private String getFromBackend(String path) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            return readStream(connection);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String postToBackend(String path, String body)
            throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + path);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);
            connection.setRequestProperty(
                    "Content-Type", "application/json; charset=UTF-8");

            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

            try (OutputStream output = connection.getOutputStream()) {
                output.write(bytes);
            }

            return readStream(connection);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readStream(HttpURLConnection connection)
            throws Exception {
        int code = connection.getResponseCode();

        InputStream stream = code >= 200 && code < 400
                ? connection.getInputStream()
                : connection.getErrorStream();

        if (stream == null) {
            throw new Exception("Server returned HTTP " + code);
        }

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        if (code < 200 || code >= 400) {
            throw new Exception("HTTP " + code + ": " + result);
        }

        return result.toString();
    }
}
